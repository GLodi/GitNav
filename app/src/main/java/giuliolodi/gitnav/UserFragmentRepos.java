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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import giuliolodi.gitnav.Adapters.RepoAdapter;

public class UserFragmentRepos {

    private List<Repository> repositoryList;
    private Context context;
    private String user;
    private RepoAdapter repoAdapter;
    private View v;
    private RecyclerView rv;
    private Map FILTER_OPTION;

    @BindString(R.string.network_error) String network_error;

    /*
        Populate() is called when a UserFragment is created. This is
        the first of three classes that populate the fragments below UserFragment:
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
            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(context));
            try {
                repositoryList = repositoryService.getRepositories(user, FILTER_OPTION);
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            repoAdapter = new RepoAdapter(repositoryList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
            rv = (RecyclerView) v.findViewById(R.id.user_fragment_repos_rv);
            rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
            rv.setLayoutManager(mLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setAdapter(repoAdapter);
            repoAdapter.notifyDataSetChanged();

        }
    }

}
