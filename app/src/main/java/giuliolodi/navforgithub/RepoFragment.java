/*
 * Copyright (c)  2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package giuliolodi.navforgithub;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepoFragment extends Fragment {

    List<Repository> repositoryList;
    RepoAdapter repoAdapter;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.repo_fragment, container, false);



        // Get reference to the RecyclerView and get the data
        recyclerView = (RecyclerView) v.findViewById(R.id.repo_recycler_view);
        new getRepositories().execute();

        return v;
    }

    class getRepositories extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String... strings) {
            // Authenticate
            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(getContext()));
            try {
                // Get the RepositoryList and sort it based on creation date
                Map sort = new HashMap();
                sort.put("sort", "created");
                repositoryList = repositoryService.getRepositories(sort);
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // Bind list to adapter
            repoAdapter = new RepoAdapter(repositoryList);

            // Set adapter on RecyclerView and notify it
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(repoAdapter);
            repoAdapter.notifyDataSetChanged();
        }
    }

}
