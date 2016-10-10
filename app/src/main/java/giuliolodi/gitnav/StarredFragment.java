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

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.StarService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StarredFragment extends Fragment {

    List<Repository> starredRepoList;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    StarredAdapter starredAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    public Map FILTER_OPTION;
    public boolean PREVENT_MULTPLE_SEPARATION_LINE = true;
    public boolean HIDE_PROGRESS_BAR = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.starred_fragment, container, false);
        setHasOptionsMenu(true);

        // Get references
        recyclerView = (RecyclerView) v.findViewById(R.id.starred_recycler_view);
        progressBar = (ProgressBar) v.findViewById(R.id.starred_progress_bar);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.starred_refresh);

        // Create filter
        FILTER_OPTION = new HashMap();

        new getStarred().execute();

        // Set swipe color and listener. For some reason access through R.color doesn't work
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#448AFF"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                HIDE_PROGRESS_BAR = false;
                new getStarred().execute();
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.starred_sort_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.starred_sort_starred:
                item.setChecked(true);
                FILTER_OPTION.put("sort", "created");
                new getStarred().execute();
                return true;
            case R.id.starred_sort_updated:
                item.setChecked(true);
                FILTER_OPTION.put("sort", "updated");
                new getStarred().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class getStarred extends AsyncTask<String, String, String> {
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
            StarService starService = new StarService();
            starService.getClient().setOAuth2Token(Constants.getToken(getContext()));

            // Store list of starred repos
            try {
                starredRepoList = starService.getStarred(Constants.getUsername(getContext()), FILTER_OPTION);
            } catch (IOException e) {e.printStackTrace();}

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

            // Set adapter
            starredAdapter = new StarredAdapter(starredRepoList);

            // Set adapter on RecyclerView and notify it
            PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setExtraLayoutSpace(getContext().getResources().getDisplayMetrics().heightPixels);
            if (PREVENT_MULTPLE_SEPARATION_LINE) {
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
                PREVENT_MULTPLE_SEPARATION_LINE = false;
            }
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(starredAdapter);
            starredAdapter.notifyDataSetChanged();
        }
    }

}

