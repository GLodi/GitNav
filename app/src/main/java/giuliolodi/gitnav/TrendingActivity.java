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


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private String BASE_URL = "https://github.com/trending";
    private String DAILY_URL = "?since=daily";
    private String WEEKLY_URL = "?since=weekly";
    private String MONTHLY_URL = "?since=monthly";
    private String URL;

    private RepositoryService repositoryService;

    private Observable<Repository> observable;
    private Observer<Repository> observer;
    private Subscription s;

    private List<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.trending_activity, frameLayout);

        ButterKnife.bind(this);

        getSupportActionBar().setTitle(trending);

        URL = BASE_URL + DAILY_URL;

        observable = Observable.create(new Observable.OnSubscribe<Repository>() {
            @Override
            public void call(Subscriber<? super Repository> subscriber) {
                try {
                    Document document = Jsoup.connect(URL).get();
                    Elements repoList = document.getElementsByTag("ol").get(0).getElementsByTag("li");

                    if (repoList != null && !repoList.isEmpty()) {
                        Element string;
                        String ss;
                        list = new ArrayList<String>();

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

                        for (int i = 0; i < list.size()/2; i++) {
                            subscriber.onNext(repositoryService.getRepository(list.get(i), list.get(i+1)));
                            i++;
                        }
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

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Repository repository) {
                if (repository != null) {
                    progressBar.setVisibility(View.GONE);
                    Repository repo = repository;
                }
            }
        };

        s = observable.subscribe(observer);
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
