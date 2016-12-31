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
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import giuliolodi.gitnav.Adapters.UserAdapter;

public class UserFollowers {

    private String user;
    private Context context;
    private View v;
    private List<User> followers;
    private List<User> followersTemp;
    private List<User> t;
    private UserAdapter userAdapter;
    private RecyclerView rv;
    private LinearLayoutManager mLayoutManager;
    private UserService userService;
    private TextView noUsers;

    // Number of page that we have currently downloaded. Starts at 1
    private int DOWNLOAD_PAGE_N = 1;

    // Number of items downloaded per page
    private int ITEMS_DOWNLOADED_PER_PAGE = 10;

    // Flag that prevents multiple pages from being downloaded at the same time
    private boolean LOADING = false;

    private boolean NO_MORE = true;

    @BindString(R.string.network_error) String network_error;

    public void populate(String user, Context context, View v) {
        this.user = user;
        this.context = context;
        this.v = v;
        if (Constants.isNetworkAvailable(context)) {
            new getFollowers().execute();
        }
        else {
            Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
        }
    }

    private class getFollowers extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            userService = new UserService();
            userService.getClient().setOAuth2Token(Constants.getToken(context));

            followers = new ArrayList<>();
            followersTemp = new ArrayList<>(userService.pageFollowers(user, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

            try {
                for (int i = 0; i < followersTemp.size(); i++) {
                    followers.add(userService.getUser(followersTemp.get(i).getLogin()));
                }
            } catch (IOException e) {e.printStackTrace();}


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /*
                Set adapter. Pass FragmentManager as parameter because
                the adapter needs it to open a UserActivity when a profile icon is clicked.
             */

            noUsers = (TextView) v.findViewById(R.id.user_followers_tv);
            noUsers.setTypeface(EasyFonts.robotoRegular(context));

            if(followers.isEmpty())
                noUsers.setVisibility(View.VISIBLE);

            userAdapter = new UserAdapter(followers, context);
            mLayoutManager = new LinearLayoutManager(context);
            rv = (RecyclerView) v.findViewById(R.id.user_followers_rv);
            rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
            rv.setLayoutManager(mLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());

            setupOnScrollListener();

            rv.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
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
                    if (NO_MORE)
                        new getMoreUsers().execute();
                }
            }
        };

        rv.setOnScrollListener(mScrollListener);

    }

    private class getMoreUsers extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            followersTemp.clear();
            followers.add(null);
            userAdapter.notifyItemInserted(followers.size() - 1);
        }

        @Override
        protected String doInBackground(String... params) {
            t = new ArrayList<>(userService.pageFollowers(user, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());
            if (t.isEmpty()) {
                NO_MORE = false;
            }
            try {
                for (int i = 0; i < t.size(); i++) {
                    followersTemp.add(userService.getUser(t.get(i).getLogin()));
                }
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            followers.remove(followers.lastIndexOf(null));
            for (int i = 0; i < followersTemp.size(); i++) {
                followers.add(followersTemp.get(i));
            }
            if (NO_MORE)
                userAdapter.notifyItemChanged(followers.size() - 1);
            else
                userAdapter.notifyDataSetChanged();
            LOADING = false;
        }
    }

}
