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


import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.GistAdapter;

public class GistListMine {

    @BindView(R.id.gists_mine_progress_bar) ProgressBar progressBar;
    @BindView(R.id.gists_mine_rv) RecyclerView recyclerView;
    @BindView(R.id.gists_mine_no) TextView noMineGists;

    private List<Gist> gistsList;
    private List<Gist> t;
    private Context context;
    private GistAdapter gistAdapter;
    private LinearLayoutManager linearLayoutManager;
    private GistService gistService;

    // Number of page that we have currently downloaded. Starts at 1
    private int DOWNLOAD_PAGE_N = 1;

    // Number of items downloaded per page
    private int ITEMS_DOWNLOADED_PER_PAGE = 10;

    // Flag that prevents multiple pages from being downloaded at the same time
    private boolean LOADING = false;

    public void populate(Context context, View v) {
        this.context = context;
        ButterKnife.bind(this, v);
        new getMineGists().execute();
    }

    private class getMineGists extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            noMineGists.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            gistService = new GistService();
            gistService.getClient().setOAuth2Token(Constants.getToken(context));

            gistsList = new ArrayList<>(gistService.pageGists(Constants.getUsername(context), DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.GONE);

            if (gistsList.isEmpty())
                noMineGists.setVisibility(View.VISIBLE);

            gistAdapter = new GistAdapter(gistsList);
            linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(gistAdapter);

            setupOnScrollListener();

            gistAdapter.notifyDataSetChanged();

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
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    DOWNLOAD_PAGE_N += 1;
                    LOADING = true;
                    new getMoreMineGists().execute();
                }
            }
        };

        recyclerView.setOnScrollListener(mScrollListener);

    }

    private class getMoreMineGists extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            t = new ArrayList<>(gistService.pageGists(Constants.getUsername(context), DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
            for (int i = 0; i < t.size(); i++) {
                gistsList.add(t.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LOADING = false;

            // This is used instead of .notiftDataSetChanged for performance reasons
            gistAdapter.notifyItemChanged(gistsList.size() - 1);
        }
    }

}
