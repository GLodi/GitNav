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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.StargazerService;

import java.io.IOException;
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

public class UserListActivity extends BaseDrawerActivity {

    @BindView(R.id.user_list_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.user_list_activity_rv) RecyclerView recyclerView;
    @BindView(R.id.user_list_no_users) TextView no_user;
    @BindString(R.string.stargazers) String stargazers;

    private Intent intent;
    private String repoName;
    private String ownerName;
    private UserAdapter userAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<User> userList;

    private Observable observable;
    private Observer observer;
    private Subscription s;

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
            public void call(Subscriber<? super List<User>> subscriber) {
                StargazerService stargazerService = new StargazerService();
                stargazerService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

                try {
                    userList = stargazerService.getStargazers(new RepositoryId(ownerName, repoName));
                } catch (IOException e) {e.printStackTrace();}

                if (userList != null && !userList.isEmpty())
                    subscriber.onNext(userList);
                else
                    subscriber.onNext(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<User>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<User> list) {
                if (list != null) {
                    userAdapter = new UserAdapter(list, UserListActivity.this);
                    linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(userAdapter);
                    userAdapter.notifyDataSetChanged();
                }
                else
                    no_user.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        };

        s = observable.subscribe(observer);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (s != null && !s.isUnsubscribed()){
            s.unsubscribe();
            progressBar.setVisibility(View.GONE);
        }    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

}
