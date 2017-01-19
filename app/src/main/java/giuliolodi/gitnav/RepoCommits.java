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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.CommitAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepoCommits {

    @BindView(R.id.repo_commits_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_commits_rv) RecyclerView recyclerView;

    private Context context;
    private Repository repo;
    private CommitService commitService;
    private List<RepositoryCommit> repositoryCommitList;
    private CommitAdapter commitAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Observable observable;
    private Observer observer;
    private Subscription s;

    public void populate(final Context context, View v, Repository repo) {
        this.context = context;
        this.repo = repo;

        ButterKnife.bind(this, v);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<RepositoryCommit>>() {
            @Override
            public void call(Subscriber<? super List<RepositoryCommit>> subscriber) {
                commitService = new CommitService();
                commitService.getClient().setOAuth2Token(Constants.getToken(context));

                try {
                    repositoryCommitList = commitService.getCommits(new RepositoryId(getRepo().getOwner().getLogin(), getRepo().getName()));
                } catch (IOException e) {e.printStackTrace();}

                if (repositoryCommitList != null)
                    subscriber.onNext(repositoryCommitList);
                else
                    subscriber.onNext(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<RepositoryCommit>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<RepositoryCommit> list) {
                repositoryCommitList.addAll(list);
                commitAdapter = new CommitAdapter(list, context);
                linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(commitAdapter);
                commitAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
            }
        };

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                context.startActivity(new Intent(context, CommitActivity.class)
                    .putExtra("owner", getRepo().getOwner().getLogin())
                    .putExtra("repo", getRepo().getName())
                    .putExtra("sha", repositoryCommitList.get(position).getSha())
                    .putExtra("commit_url", getRepo().getHtmlUrl())
                    .putExtra("commit_title", repositoryCommitList.get(position).getCommit().getMessage()));
                ((Activity)context).overridePendingTransition(0,0);
            }
        });

        s = observable.subscribe(observer);
    }

    private Repository getRepo() {
        return repo;
    }

    public void unsubRepoCommits() {
        if (s != null && !s.isUnsubscribed()){
            s.unsubscribe();
            progressBar.setVisibility(View.GONE);
        }
    }

}
