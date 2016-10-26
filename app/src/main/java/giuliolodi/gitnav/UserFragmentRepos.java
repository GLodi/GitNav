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
import android.os.AsyncTask;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import giuliolodi.gitnav.Adapters.RepoAdapter;

public class UserFragmentRepos {

    private List<Repository> repositoryList;
    private List<Repository> t;
    private Context context;
    private String user;
    private RepoAdapter repoAdapter;
    private View v;
    private RecyclerView rv;
    private Map FILTER_OPTION;
    private RepositoryService repositoryService;
    private LinearLayoutManager mLayoutManager;

    // Number of page that we have currently downloaded. Starts at 1
    private int DOWNLOAD_PAGE_N = 1;

    // Number of items downloaded per page
    private int ITEMS_DOWNLOADED_PER_PAGE = 10;

    // Flag that prevents multiple pages from being downloaded at the same time
    private boolean LOADING = false;

    @BindString(R.string.network_error) String network_error;

    /*
        Populate() is called when a UserActivity is created. This is
        the first of three classes that populate the fragments below UserActivity:
        Repos, Followers and Following.
     */

    public void populate(String user, Context context, View v) {
        this.user = user;
        this.context = context;
        this.v = v;
        FILTER_OPTION = new HashMap();
        FILTER_OPTION.put("sort", "created");
        if (Constants.isNetworkAvailable(context))
            new getRepos().execute();
        else
            Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
    }

    private class getRepos extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(context));

            repositoryList = new ArrayList<>(repositoryService.pageRepositories(user, FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            repoAdapter = new RepoAdapter(repositoryList);
            mLayoutManager = new LinearLayoutManager(context);
            rv = (RecyclerView) v.findViewById(R.id.user_fragment_repos_rv);
            rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
            rv.setLayoutManager(mLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());

            setupOnScrollListener();

            rv.setAdapter(repoAdapter);
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

        rv.setOnScrollListener(mScrollListener);

    }

    private class getMoreRepos extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            t = new ArrayList<>(repositoryService.pageRepositories(user, FILTER_OPTION, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
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
