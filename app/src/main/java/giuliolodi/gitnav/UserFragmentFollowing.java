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
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.List;

import butterknife.BindString;
import giuliolodi.gitnav.Adapters.UserAdapter;

public class UserFragmentFollowing {

    private String user;
    private Context context;
    private View v;
    private List<User> followers;
    private UserAdapter userAdapter;
    private RecyclerView rv;
    private FragmentManager fm;

    @BindString(R.string.network_error) String network_error;

    public void populate(String user, Context context, View v, FragmentManager fm) {
        this.user = user;
        this.context = context;
        this.v = v;
        this.fm = fm;
        if (Constants.isNetworkAvailable(context)) {
            new getFollowing().execute();
        }
        else {
            Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
        }
    }

    class getFollowing extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(Constants.getToken(context));
            try {
                followers = userService.getFollowing(user);
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /*
                Set adapter. Pass FragmentManager as parameter because
                the adapter needs it to open a UserFragment when a profile icon is clicked.
             */
            userAdapter = new UserAdapter(followers, fm);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
            rv = (RecyclerView) v.findViewById(R.id.user_fragment_following_rv);
            rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
            rv.setLayoutManager(mLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
        }
    }

}
