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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.RepoAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ForkListActivity extends BaseDrawerActivity {

    @BindView(R.id.fork_list_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.fork_list_activity_rv) RecyclerView recyclerView;
    @BindView(R.id.fork_list_activity_no_forks) TextView noForks;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.forks) String forks;

    private List<Repository> t, repositoryList = new ArrayList<>();
    private RepositoryService repositoryService = new RepositoryService();
    private Intent intent;
    private String owner, repo;
    private RepoAdapter repoAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Observable<List<Repository>> observable;
    private Observer<List<Repository>> observer;
    private Subscription subscription;

    private int ITEMS_PER_PAGE = 10;
    private int DOWNLOAD_PAGE_N = 1;
    private boolean LOADING = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fork_list_activity, frameLayout);

        ButterKnife.bind(this);

        intent = getIntent();
        owner = intent.getStringExtra("ownerName");
        repo = intent.getStringExtra("repoName");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getSupportActionBar().setTitle(forks);
        getSupportActionBar().setSubtitle(owner + "/" + repo);

        repoAdapter = new RepoAdapter(repositoryList, ForkListActivity.this);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(repoAdapter);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<Repository>>() {
            @Override
            public void call(Subscriber<? super List<Repository>> subscriber) {
                repositoryService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));
                t = new ArrayList<>(repositoryService.pageForks(new RepositoryId(owner, repo), DOWNLOAD_PAGE_N, ITEMS_PER_PAGE).next());
                subscriber.onNext(t);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<Repository>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Repository> repositories) {
                progressBar.setVisibility(View.GONE);
                if (repositoryList.isEmpty() && repositories.isEmpty()){
                    noForks.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
                    noForks.setVisibility(View.VISIBLE);
                    subscription.unsubscribe();
                }
                if (repositoryList.isEmpty() && repositories != null && !repositories.isEmpty()) {
                    repositoryList.addAll(repositories);
                    repoAdapter.notifyDataSetChanged();
                    LOADING = false;
                } else if (repositories != null && !repositories.isEmpty()) {
                    repositoryList.remove(repositoryList.lastIndexOf(null));
                    repositoryList.addAll(repositories);
                    repoAdapter.notifyItemChanged(repositoryList.size() - 1);
                    LOADING = false;
                } else {
                    repositoryList.remove(repositoryList.lastIndexOf(null));
                    repoAdapter.notifyDataSetChanged();
                    subscription.unsubscribe();
                }
            }
        };

        setupOnScrollListener();

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (Constants.isNetworkAvailable(getApplicationContext())) {
                    getApplicationContext().startActivity(new Intent(getApplicationContext(), RepoActivity.class)
                            .putExtra("owner", owner)
                            .putExtra("repo", repo));
                    ForkListActivity.this.overridePendingTransition(0, 0);
                } else
                    Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
            }
        });

        if (Constants.isNetworkAvailable(getApplicationContext()))
            subscription = observable.subscribe(observer);
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

    }

    private void setupOnScrollListener() {

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (LOADING)
                    return;
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    LOADING = true;
                    DOWNLOAD_PAGE_N += 1;
                    repositoryList.add(null);
                    repoAdapter.notifyItemInserted(repositoryList.size() - 1);
                    subscription = observable.subscribe(observer);
                }
            }
        };

        recyclerView.setOnScrollListener(mScrollListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}
