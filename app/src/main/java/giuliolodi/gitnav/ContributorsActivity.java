/*
 * Copyright 2017 GLodi
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

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.ContributorAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContributorsActivity extends BaseDrawerActivity {

    @BindView(R.id.contributors_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.contributors_activity_rv) RecyclerView recyclerView;
    @BindView(R.id.contributors_activity_no_contributors) TextView noContributors;
    @BindString(R.string.collaborators) String collaborators;
    @BindString(R.string.contributors) String contributors;
    @BindString(R.string.network_error) String network_error;

    private Intent intent;
    private String repoName, ownerName;
    private ContributorAdapter contributorAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Observable<List<Contributor>> observable;
    private Observer<List<Contributor>> observer;
    private Subscription subscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.contributors_activity, frameLayout);

        ButterKnife.bind(this);

        intent = getIntent();
        repoName = intent.getStringExtra("repoName");
        ownerName = intent.getStringExtra("ownerName");

        getSupportActionBar().setTitle(contributors);
        getSupportActionBar().setSubtitle(ownerName + "/" + repoName);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<Contributor>>() {
            @Override
            public void call(Subscriber<? super List<Contributor>> subscriber) {
                RepositoryService repositoryService = new RepositoryService();
                repositoryService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));
                try {
                    subscriber.onNext(repositoryService.getContributors(new RepositoryId(ownerName, repoName), true));
                } catch (Exception e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<Contributor>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("rx", e.getMessage());
            }

            @Override
            public void onNext(List<Contributor> users) {
                if (users != null) {
                    progressBar.setVisibility(View.GONE);
                    contributorAdapter = new ContributorAdapter(users, ContributorsActivity.this);
                    linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(contributorAdapter);
                    contributorAdapter.notifyDataSetChanged();
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    noContributors.setVisibility(View.VISIBLE);
                }
            }
        };

        if (Constants.isNetworkAvailable(getApplicationContext()))
            subscription = observable.subscribe(observer);
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}
