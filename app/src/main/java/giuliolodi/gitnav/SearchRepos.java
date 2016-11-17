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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.RepoAdapter;

public class SearchRepos {

    @BindView(R.id.search_repos_progress_bar) ProgressBar progressBar;
    @BindView(R.id.search_repos_rv) RecyclerView recyclerView;
    @BindView(R.id.no_repositories) TextView noRepositories;

    private RepoAdapter repoAdapter;
    private LinearLayoutManager linearLayoutManager;

    private String query;
    private List<Repository> repositoryList;
    private Context context;

    private boolean PREVENT_MULTIPLE_SEPARATOR_LINE;
    private boolean LOADING = false;

    public void populate(String query, Context context, View v, boolean PREVENT_MULTIPLE_SEPARATOR_LINE) {
        this.query = query;
        this.context = context;
        this.PREVENT_MULTIPLE_SEPARATOR_LINE = PREVENT_MULTIPLE_SEPARATOR_LINE;
        ButterKnife.bind(this, v);
        LOADING = true;
        new getRepos().execute();

    }

    public boolean isLOADING() {
        return LOADING;
    }

    private class getRepos extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            noRepositories.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(context));

            try {
                repositoryList = repositoryService.searchRepositories(query);
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (repositoryList == null || repositoryList.isEmpty())
                noRepositories.setVisibility(View.VISIBLE);

            repoAdapter = new RepoAdapter(repositoryList, context);
            linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            if (PREVENT_MULTIPLE_SEPARATOR_LINE)
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(repoAdapter);
            progressBar.setVisibility(View.GONE);
            repoAdapter.notifyDataSetChanged();

            LOADING = false;

        }
    }
}
