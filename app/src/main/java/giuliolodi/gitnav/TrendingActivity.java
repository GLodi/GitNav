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


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.StarredAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TrendingActivity extends  BaseDrawerActivity{

    @BindView(R.id.trending_refresh) SwipeRefreshLayout  swipeRefreshLayout;
    @BindView(R.id.trending_progress_bar) ProgressBar progressBar;
    @BindView(R.id.trending_no_repo) TextView no_repo;
    @BindView(R.id.trending_recycler_view) RecyclerView recyclerView;
    @BindString(R.string.trending) String trending;
    @BindString(R.string.network_error) String network_error;

    private String BASE_URL = "https://github.com/trending";
    private String DAILY_URL = "?since=daily";
    private String WEEKLY_URL = "?since=weekly";
    private String MONTHLY_URL = "?since=monthly";
    private String URL;

    private RepositoryService repositoryService;
    private LinearLayoutManager linearLayoutManager;
    private StarredAdapter starredAdapter;

    private Observable<Repository> observable;
    private Observer<Repository> observer;
    private Subscription s;

    private List<String> list = new ArrayList<>();
    private List<Repository> repositoryList = new ArrayList<>();

    private boolean PREVENT_LINES = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.trending_activity, frameLayout);

        ButterKnife.bind(this);

        getSupportActionBar().setTitle(trending);

        URL = BASE_URL + DAILY_URL;

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#448AFF"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Constants.isNetworkAvailable(getApplicationContext())) {
                    if (!s.isUnsubscribed())
                        swipeRefreshLayout.setRefreshing(false);
                    else {
                        s.unsubscribe();
                        repositoryList = new ArrayList<>();
                        starredAdapter.notifyDataSetChanged();
                        s = observable.subscribe(observer);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
                }
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<Repository>() {
            @Override
            public void call(Subscriber<? super Repository> subscriber) {
                try {
                    Document document = Jsoup.connect(URL).get();
                    Elements repoList = document.getElementsByTag("ol").get(0).getElementsByTag("li");

                    if (repoList != null && !repoList.isEmpty()) {
                        Element string;
                        String ss;
                        list = new ArrayList<>();

                        for (int i = 0; i < repoList.size(); i++) {
                            string = repoList.get(i).getElementsByTag("div").get(0).getElementsByTag("h3").get(0).getElementsByTag("a").get(0);
                            ss = string.children().get(0).ownText() + string.ownText();
                            String[] t = ss.split("/");
                            String a = t[0].replace(" ", "");
                            String b = t[1];
                            list.add(a);
                            list.add(b);
                        }

                        repositoryService = new RepositoryService();
                        repositoryService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

                        for (int i = 0; i < list.size(); i++) {
                            subscriber.onNext(repositoryService.getRepository(list.get(i), list.get(i+1)));
                            i++;
                        }

                        subscriber.onCompleted();
                    }
                    else {
                        subscriber.onError(new IOException("repoList is empty"));
                    }
                } catch (IOException e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<Repository>() {
            @Override
            public void onCompleted() {
                if (repositoryList == null || repositoryList.isEmpty())
                    no_repo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("rx", e.getMessage());
                progressBar.setVisibility(View.GONE);
                no_repo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(Repository repository) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (repository != null) {
                    progressBar.setVisibility(View.GONE);
                    if (repositoryList == null || repositoryList.isEmpty()) {
                        repositoryList.add(repository);
                        starredAdapter = new StarredAdapter(repositoryList, TrendingActivity.this);
                        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        if (getPrevent())
                            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(starredAdapter);
                        starredAdapter.notifyDataSetChanged();
                        PREVENT_LINES = false;
                    }
                    else {
                        repositoryList.add(repository);
                        starredAdapter.notifyItemChanged(repositoryList.size() - 1);
                    }
                }
            }
        };

        if (Constants.isNetworkAvailable(getApplicationContext()))
            s = observable.subscribe(observer);
        else {
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
        }
    }

    private boolean getPrevent() {
        return PREVENT_LINES;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(3).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (s != null && !s.isUnsubscribed())
            s.unsubscribe();
    }
}
