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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import giuliolodi.gitnav.Adapters.UserAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class UserFollowing {

    private List<User> following = new ArrayList<>();
    private List<User> followingTemp;
    private UserAdapter userAdapter;
    private RecyclerView rv;
    private LinearLayoutManager mLayoutManager;
    private UserService userService;
    private TextView noUsers;
    private RecyclerView.OnScrollListener mScrollListener;

    private Observable<User> observable;
    private Observer<User> observer;
    private Subscription subscription;

    // Number of page that we have currently downloaded. Starts at 1
    private int DOWNLOAD_PAGE_N = 1;

    // Number of items downloaded per page
    private int ITEMS_DOWNLOADED_PER_PAGE = 10;

    // Flag that prevents multiple pages from being downloaded at the same time
    private boolean LOADING = false;

    @BindString(R.string.network_error) String network_error;

    public void populate(final String user, final Context context, final View v) {

        observable = Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(final Subscriber<? super User> subscriber) {
                userService = new UserService();
                userService.getClient().setOAuth2Token(Constants.getToken(context));

                followingTemp = new ArrayList<>(userService.pageFollowing(user, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

                try {
                    for (int i = 0; i < followingTemp.size(); i++) {
                        User u = userService.getUser(followingTemp.get(i).getLogin());
                        following.add(u);
                        subscriber.onNext(u);
                    }
                } catch (IOException e) {e.printStackTrace();}

                mScrollListener = new RecyclerView.OnScrollListener() {
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

                            followingTemp = new ArrayList<>(userService.pageFollowing(user, DOWNLOAD_PAGE_N, ITEMS_DOWNLOADED_PER_PAGE).next());

                            try {
                                for (int i = 0; i < followingTemp.size(); i++) {
                                    User u = userService.getUser(followingTemp.get(i).getLogin());
                                    following.add(u);
                                    subscriber.onNext(u);
                                }
                            } catch (IOException e) {e.printStackTrace();}
                        }
                    }
                };

                rv.setOnScrollListener(mScrollListener);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("rx", e.getMessage());
            }

            @Override
            public void onNext(User user) {
                if (following.isEmpty()) {
                    noUsers.setVisibility(View.VISIBLE);
                }
                if (user != null && following.size() == 1) {
                    noUsers = (TextView) v.findViewById(R.id.user_following_tv);
                    noUsers.setTypeface(EasyFonts.robotoRegular(context));
                    userAdapter = new UserAdapter(following, context);
                    mLayoutManager = new LinearLayoutManager(context);
                    rv = (RecyclerView) v.findViewById(R.id.user_following_rv);
                    rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
                    rv.setLayoutManager(mLayoutManager);
                    rv.setItemAnimator(new DefaultItemAnimator());
                    rv.setAdapter(userAdapter);
                    userAdapter.notifyDataSetChanged();
                }
                else if (user != null && !following.isEmpty()){
                    LOADING = false;
                    userAdapter.notifyItemChanged(following.size() - 1);
                }
                else {
                    LOADING = false;
                    subscription.unsubscribe();
                }
            }
        };

        if (Constants.isNetworkAvailable(context)) {
            subscription = observable.subscribe(observer);
        }
        else {
            Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
        }
    }

}
