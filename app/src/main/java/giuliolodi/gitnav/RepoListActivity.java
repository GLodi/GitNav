/*
 * Copyright 2016 GLodi
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
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.RepoAdapter;

public class RepoListActivity extends BaseDrawerActivity {

    private List<Repository> repositoryList;
    private List<Repository> t;
    private RepoAdapter repoAdapter;
    private LinearLayoutManager mLayoutManager;
    private RepositoryService repositoryService;

    @BindView (R.id.repo_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.repo_list_progress_bar) ProgressBar progressBar;
    @BindView(R.id.repo_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.repo_no_repo) TextView noRepos;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.repositories) String repositories;

    private Map FILTER_OPTION;

    /*
        In order to prevent a bug that shows multiple line dividers on top of each other, I inserted
        a variable that allows the creation of only one line.
    */
    public boolean PREVENT_MULTIPLE_SEPARATION_LINES = true;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.repo_list_activity, frameLayout);

        ButterKnife.bind(this);

        // Created filter and set to "created"
        FILTER_OPTION = new HashMap();
        FILTER_OPTION.put("sort", "created");

        if (Constants.isNetworkAvailable(getApplicationContext()))
            new getRepositories().execute();
        else
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

        // Set swipe color and listener. For some reason access through R.color doesn't work
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#448AFF"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Constants.isNetworkAvailable(getApplicationContext())) {
                    HIDE_PROGRESS_BAR = false;
                    PREVENT_MULTIPLE_SEPARATION_LINES = false;
                    new getRepositories().execute();
                }
                else
                    Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repo_list_sort_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            switch (item.getItemId()) {
                case R.id.repo_sort_created:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "created");
                    PREVENT_MULTIPLE_SEPARATION_LINES = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getRepositories().execute();
                    return true;
                case R.id.repo_sort_updated:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "updated");
                    PREVENT_MULTIPLE_SEPARATION_LINES = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getRepositories().execute();
                    return true;
                case R.id.repo_sort_pushed:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "pushed");
                    PREVENT_MULTIPLE_SEPARATION_LINES = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getRepositories().execute();
                    return true;
                case R.id.repo_sort_alphabetical:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "full_name");
                    PREVENT_MULTIPLE_SEPARATION_LINES = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getRepositories().execute();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
        }
    }

    class getRepositories extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set ProgressBar visible
            if (HIDE_PROGRESS_BAR)
                progressBar.setVisibility(View.VISIBLE);

            // Set recycler view invisible
            recyclerView.setVisibility(View.INVISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            // Authenticate
            repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

            // Get the RepositoryList and sort it based on creation date
            repositoryList = new ArrayList<>(repositoryService.pageRepositories(Constants.getUsername(getApplicationContext()), FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            HIDE_PROGRESS_BAR = true;

            // Check if there are no repos
            noRepos.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
            if (repositoryList.isEmpty())
                noRepos.setVisibility(View.VISIBLE);

            // Set ProgressBar invisible
            progressBar.setVisibility(View.GONE);

            // Set recycler view visible
            recyclerView.setVisibility(View.VISIBLE);

            // Stop refresh circle
            swipeRefreshLayout.setRefreshing(false);

            // Bind list to adapter
            repoAdapter = new RepoAdapter(repositoryList, RepoListActivity.this);

            // Set adapter on RecyclerView and notify it
             mLayoutManager = new LinearLayoutManager(getApplicationContext());
            if (PREVENT_MULTIPLE_SEPARATION_LINES) {
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));
            }
            PREVENT_MULTIPLE_SEPARATION_LINES = true;
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            setupOnScrollListener();

            recyclerView.setAdapter(repoAdapter);
            repoAdapter.notifyDataSetChanged();
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
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    DOWNLOAD_PAGE_N += 1;
                    LOADING = true;
                    new getMoreRepos().execute();
                }
            }
        };

        recyclerView.setOnScrollListener(mScrollListener);

    }

    private class getMoreRepos extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            t = new ArrayList<>(repositoryService.pageRepositories(Constants.getUsername(getApplicationContext()), FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
            for (int i = 0; i < t.size(); i++) {
                repositoryList.add(t.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LOADING = false;

            // This is used instead of .notiftDataSetChanged for performance reasons
            repoAdapter.notifyItemChanged(repositoryList.size() - 1);
        }
    }

}
