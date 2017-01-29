/*
 * Copyright 2017 GLodi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package giuliolodi.gitnav;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.StarService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.StarredAdapter;


public class StarredActivity extends BaseDrawerActivity {

    // List of repos that is passed to the adapter
    private List<Repository> starredRepoList;

    // Temporary list for repos loaded while user is scrolling
    private List<Repository> t;

    private StarredAdapter starredAdapter;
    private PreCachingLayoutManager layoutManager;
    private StarService starService;

    @BindView(R.id.starred_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.starred_progress_bar) ProgressBar progressBar;
    @BindView(R.id.starred_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.starred_no_repo) TextView noRepos;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.starred) String starred;

    public Map FILTER_OPTION;

    /*
        In order to prevent a bug that shows multiple line dividers on top of each other, I inserted
        a variable that allows the creation of only one line.
    */
    private boolean PREVENT_MULTIPLE_SEPARATOR_LINES = true;

    /*
        Having decided to use a SwipeRefreshLayout, using both that and the ProgressBar would be redundant.
        For this reason, whenever the info is refreshed with a Swipe, the ProgressBar is hidden. This is
        handled with HIDE_PROGRESS_BAR.
    */
    public boolean HIDE_PROGRESS_BAR = true;

    // Number of page that we have currently downloaded. Starts at 1
    private int DOWNLOAD_PAGE_N = 1;

    // Number of items downloaded per page
    private int ITEMS_DOWNLOADED_PER_PAGE = 10;

    // Flag that prevents multiple pages from being downloaded at the same time
    private boolean LOADING = false;

    // Prevent infinite loading
    private boolean NO_MORE = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.starred_activity, frameLayout);

        ButterKnife.bind(this);

        // Create filter
        FILTER_OPTION = new HashMap();

        /*
            Check if connection is available and gets the data,
            otherwise user is notified
         */
        if (Constants.isNetworkAvailable(getApplicationContext()))
            new getStarred().execute();
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

        // Set swipe color and listener. For some reason access through R.color doesn't work
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#448AFF"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Constants.isNetworkAvailable(getApplicationContext())) {
                    PREVENT_MULTIPLE_SEPARATOR_LINES = false;
                    HIDE_PROGRESS_BAR = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getStarred().execute();
                }
                else
                    Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(2).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.starred_sort_menu, menu);
        return true;
    }

    /*
        Each option is given by GitHub API. They will trigger a new getStarred
        with different FILTER_OPTION.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            switch (item.getItemId()) {
                case R.id.starred_sort_starred:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "created");
                    DOWNLOAD_PAGE_N = 1;
                    PREVENT_MULTIPLE_SEPARATOR_LINES = false;
                    new getStarred().execute();
                    return true;
                case R.id.starred_sort_updated:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "updated");
                    DOWNLOAD_PAGE_N = 1;
                    PREVENT_MULTIPLE_SEPARATOR_LINES = false;
                    new getStarred().execute();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
    }

    private class getStarred extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set progress bar visible
            if (HIDE_PROGRESS_BAR)
                progressBar.setVisibility(View.VISIBLE);

            // Set recycler view invisible
            recyclerView.setVisibility(View.INVISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            // Setup StarService
            starService = new StarService();
            starService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

            // Store list of starred repos
            starredRepoList = new ArrayList<>(starService.pageStarred(Constants.getUsername(getApplicationContext()), FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            noRepos.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
            if (starredRepoList.isEmpty())
                noRepos.setVisibility(View.VISIBLE);

            HIDE_PROGRESS_BAR = true;

            // Set progress bar invisible
            progressBar.setVisibility(View.GONE);

            // Set recycler view visible
            recyclerView.setVisibility(View.VISIBLE);

            // Stop refresh circle
            swipeRefreshLayout.setRefreshing(false);

            /*
                Set adapter. Pass FragmentManager as parameter because
                the adapter needs it to open a UserActivity when a profile icon is clicked.
             */
            starredAdapter = new StarredAdapter(starredRepoList, StarredActivity.this);

            // Set adapter on RecyclerView and notify it
            layoutManager = new PreCachingLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            if (PREVENT_MULTIPLE_SEPARATOR_LINES) {
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation()));
            }
            PREVENT_MULTIPLE_SEPARATOR_LINES = true;
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            setupOnScrollListener();

            recyclerView.setAdapter(starredAdapter);
            starredAdapter.notifyDataSetChanged();
        }
    }

    /*
        This will allow the recyclerview to load more content as the user scrolls down
     */
    private void setupOnScrollListener() {

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (LOADING)
                    return;
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    DOWNLOAD_PAGE_N += 1;
                    LOADING = true;
                    if (NO_MORE)
                        new getMoreStarred().execute();
                }
            }
        };

        recyclerView.setOnScrollListener(mScrollListener);

    }


    private class getMoreStarred extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            starredRepoList.add(null);
            starredAdapter.notifyItemChanged(starredRepoList.size() - 1);
        }

        @Override
        protected String doInBackground(String... params) {
            t = new ArrayList<>(starService.pageStarred(Constants.getUsername(getApplicationContext()), FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
            if (t.isEmpty())
                NO_MORE = false;
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            starredRepoList.remove(starredRepoList.lastIndexOf(null));
            for (int i = 0; i < t.size(); i++) {
                starredRepoList.add(t.get(i));
            }
            if (NO_MORE)
                starredAdapter.notifyItemChanged(starredRepoList.size() - 1);
            else
                starredAdapter.notifyDataSetChanged();
            LOADING = false;
        }
    }

}

