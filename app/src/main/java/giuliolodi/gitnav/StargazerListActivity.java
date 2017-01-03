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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.StargazerService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.UserAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StargazerListActivity extends BaseDrawerActivity {

    @BindView(R.id.user_list_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.user_list_activity_rv) RecyclerView recyclerView;
    @BindView(R.id.user_list_no_users) TextView no_user;
    @BindString(R.string.stargazers) String stargazers;
    @BindString(R.string.network_error) String network_error;

    private Intent intent;
    private String repoName;
    private String ownerName;
    private UserAdapter userAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<User> userList = new ArrayList<>();
    private List<User> tempUserList = new ArrayList<>();
    private RecyclerView.OnScrollListener mScrollListener;

    private Observable observable;
    private Observer observer;
    private Subscription s;

    private int DOWNLOAD_PAGE_N = 1;
    private int ITEMS_PER_PAGE = 20;
    private boolean LOADING = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.user_list_activity, frameLayout);

        ButterKnife.bind(this);

        intent = getIntent();
        repoName = intent.getStringExtra("repoName");
        ownerName = intent.getStringExtra("ownerName");

        getSupportActionBar().setTitle(stargazers);
        getSupportActionBar().setSubtitle(ownerName + "/" + repoName);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<User>>() {
            @Override
            public void call(final Subscriber<? super List<User>> subscriber) {
                final StargazerService stargazerService = new StargazerService();
                stargazerService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

                subscriber.onNext(tempUserList = new ArrayList<>(stargazerService.pageStargazers(new RepositoryId(ownerName, repoName), DOWNLOAD_PAGE_N, ITEMS_PER_PAGE).next()));

                mScrollListener = new RecyclerView.OnScrollListener() {
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
                            subscriber.onNext(tempUserList = new ArrayList<>(stargazerService.pageStargazers(new RepositoryId(ownerName, repoName), DOWNLOAD_PAGE_N, ITEMS_PER_PAGE).next()));
                        }
                    }
                };

                recyclerView.setOnScrollListener(mScrollListener);

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<User>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("rx", e.getMessage());
            }

            @Override
            public void onNext(List<User> users) {
                if (users != null) {
                    if (userList == null || userList.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        userList.addAll(users);
                        userAdapter = new UserAdapter(userList, StargazerListActivity.this);
                        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(userAdapter);
                        userAdapter.notifyDataSetChanged();
                    }
                    else if (!users.isEmpty()) {
                        userList.addAll(users);
                        userAdapter.notifyItemChanged(userList.size() - 1);
                        LOADING = false;
                    }
                    else {
                        s.unsubscribe();
                        LOADING = false;
                    }
                }
            }
        };

        if (Constants.isNetworkAvailable(getApplicationContext()))
            s = observable.subscribe(observer);
        else
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (s != null && !s.isUnsubscribed()) {
            s.unsubscribe();
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

}
