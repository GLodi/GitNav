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

import org.eclipse.egit.github.core.SearchUser;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.UserAdapter;

public class SearchUsers {

    @BindView(R.id.search_users_progress_bar) ProgressBar progressBar;
    @BindView(R.id.search_users_rv) RecyclerView recyclerView;
    @BindView(R.id.no_users) TextView noUsers;

    private UserAdapter userAdapter;
    private LinearLayoutManager linearLayoutManager;

    private String query;
    private List<User> userList;
    private List<SearchUser> userSearchList;
    private Context context;

    private boolean LOADING = false;
    private boolean PREVENT_MULTIPLE_SEPARATOR_LINE;

    public void populate(String query, Context context, View v, boolean PREVENT_MULTIPLE_SEPARATOR_LINE) {
        this.query = query;
        this.context = context;
        this.PREVENT_MULTIPLE_SEPARATOR_LINE = PREVENT_MULTIPLE_SEPARATOR_LINE;
        ButterKnife.bind(this, v);
        LOADING = true;
        new getUsers().execute();

    }

    public boolean isLOADING() {
        return LOADING;
    }

    private class getUsers extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            noUsers.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(Constants.getToken(context));
            userList = new ArrayList<>();

            try {
                userSearchList = userService.searchUsers(query);
                for (int i = 0; i < userSearchList.size(); i++) {
                    userList.add(userService.getUser(userSearchList.get(i).getLogin()));
                }
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (userSearchList == null || userSearchList.isEmpty())
                noUsers.setVisibility(View.VISIBLE);

            userAdapter = new UserAdapter(userList, context);
            linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            if (PREVENT_MULTIPLE_SEPARATOR_LINE)
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(userAdapter);
            progressBar.setVisibility(View.GONE);
            userAdapter.notifyDataSetChanged();

            LOADING = false;

        }
    }

}
