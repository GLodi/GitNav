/*
 * MIT License
 *
 * Copyright (c) 2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package giuliolodi.gitnav;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.StarService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.StarredAdapter;


public class StarredFragment extends Fragment {

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

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.starred) String starred;

    public Map FILTER_OPTION;

    /*
        In order to prevent a bug that shows multiple line dividers on top of each other, I inserted
        a variable that allows the creation of only one line.
    */
    public static boolean PREVENT_MULTPLE_SEPARATION_LINE = true;

    /*
        Having decided to use a SwipeRefreshLayout, using both that and the ProgressBar would be redundant.
        For this reason, whenever the info is refreshed with a Swipe, the ProgressBar is hidden. This is
        handled with HIDE_PROGRESS_BAR.
    */
    public boolean HIDE_PROGRESS_BAR = true;

    /*
        In order to implement a successful fragment lifecyle, I decided to overwrite
        onBackPressed() in MainActivity and attach a Callback interface to it.
        USER_FRAGMENT_HAS_BEEN_ADEED handles the change of TitleBar between UserFragment
        and StarredFragment when the back botton is pressed.
    */
    public static boolean USER_FRAGMENT_HAS_BEEN_ADEED = false;

    // Number of page that we have currently downloaded. Starts at 1
    private int DOWNLOAD_PAGE_N = 1;

    // Number of items downloaded per page
    private int ITEMS_DOWNLOADED_PER_PAGE = 10;

    // Flag that prevents multiple pages from being downloaded at the same time
    private boolean LOADING = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.starred_fragment, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, v);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(starred);

        // Create filter
        FILTER_OPTION = new HashMap();

        /*
            Check if connection is available and gets the data,
            otherwise user is notified
         */
        if (Constants.isNetworkAvailable(getContext()))
            new getStarred().execute();
        else
            Toast.makeText(getContext(), network_error, Toast.LENGTH_LONG).show();

        // Set swipe color and listener. For some reason access through R.color doesn't work
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#448AFF"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                HIDE_PROGRESS_BAR = false;
                if (Constants.isNetworkAvailable(getContext())) {
                    PREVENT_MULTPLE_SEPARATION_LINE = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getStarred().execute();
                }
                else
                    Toast.makeText(getContext(), network_error, Toast.LENGTH_LONG).show();
            }
        });

        // If user goes back to StarredFragment from another fragment, the tile is changed accordingly
        getFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        if (((AppCompatActivity) getActivity()) != null)
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(starred);
                    }
                });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.starred_sort_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
        Each option is given by GitHub API. They will trigger a new getStarred
        with different FILTER_OPTION.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Constants.isNetworkAvailable(getContext())) {
            switch (item.getItemId()) {
                case R.id.starred_sort_starred:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "created");
                    PREVENT_MULTPLE_SEPARATION_LINE = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getStarred().execute();
                    return true;
                case R.id.starred_sort_updated:
                    item.setChecked(true);
                    FILTER_OPTION.put("sort", "updated");
                    PREVENT_MULTPLE_SEPARATION_LINE = false;
                    DOWNLOAD_PAGE_N = 1;
                    new getStarred().execute();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else
            Toast.makeText(getContext(), network_error, Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
    }

    private class getStarred extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set progress bar visible
            if (HIDE_PROGRESS_BAR)
                progressBar.setVisibility(View.VISIBLE);

            // Set recycler vies invisible
            recyclerView.setVisibility(View.INVISIBLE);
        }
        @Override
        protected String doInBackground(String... strings) {
            // Setup StarService
            starService = new StarService();
            starService.getClient().setOAuth2Token(Constants.getToken(getContext()));

            // Store list of starred repos
            starredRepoList = new ArrayList<>(starService.pageStarred(Constants.getUsername(getContext()), FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            HIDE_PROGRESS_BAR = true;

            // Set progress bar invisible
            progressBar.setVisibility(View.GONE);

            // Set recycler view visible
            recyclerView.setVisibility(View.VISIBLE);

            // Stop refresh circle
            swipeRefreshLayout.setRefreshing(false);

            /*
                Set adapter. Pass FragmentManager as parameter because
                the adapter needs it to open a UserFragment when a profile icon is clicked.
             */
            starredAdapter = new StarredAdapter(starredRepoList, getFragmentManager());

            // Set adapter on RecyclerView and notify it
            layoutManager = new PreCachingLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setExtraLayoutSpace(getContext().getResources().getDisplayMetrics().heightPixels);
            if (PREVENT_MULTPLE_SEPARATION_LINE) {
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            }
            PREVENT_MULTPLE_SEPARATION_LINE = true;
            recyclerView.getItemAnimator().isRunning();
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
                    new getMoreStarred().execute();
                }
            }
        };

        recyclerView.setOnScrollListener(mScrollListener);

    }


    private class getMoreStarred extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            t = new ArrayList<>(starService.pageStarred(Constants.getUsername(getContext()), FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
            for (int i = 0; i < t.size(); i++) {
                starredRepoList.add(t.get(i));
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LOADING = false;

            // This is used instead of .notiftDataSetChanged for performance reasons
            starredAdapter.notifyItemChanged(starredRepoList.size() - 1);
        }
    }

}

