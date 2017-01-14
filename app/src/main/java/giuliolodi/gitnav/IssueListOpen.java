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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.service.IssueService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueListOpen {

    @BindView(R.id.issuelist_open_progressbar) ProgressBar progressBar;
    @BindView(R.id.issuelist_open_rv) RecyclerView rv;

    private Context context;
    private String owner, repo;
    private IssueService issueService;
    private Observable<List<Issue>> observable;
    private Observer<List<Issue>> observer;
    private Subscription subscription;
    private List<Issue> repositoryIssues;

    private int DOWNLOAD_PAGE_N = 1;
    private int ITEMS_PER_PAGE = 10;

    public void populate(final Context context, View view, final String owner, final String repo) {
        this.context = context;
        this.owner = owner;
        this.repo = repo;

        ButterKnife.bind(this, view);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<Issue>>() {
            @Override
            public void call(Subscriber<? super List<Issue>> subscriber) {
                issueService = new IssueService();
                issueService.getClient().setOAuth2Token(Constants.getToken(context));
                repositoryIssues = new ArrayList<>(issueService.pageIssues(owner, repo, null, DOWNLOAD_PAGE_N, ITEMS_PER_PAGE).next());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void unsubIssuelistOpen() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
            progressBar.setVisibility(View.GONE);
        }
    }

}
