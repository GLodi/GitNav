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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.CommitFileAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CommitActivityFilelist {

    @BindView(R.id.commit_activity_filelist_progressbar) ProgressBar progressBar;
    @BindView(R.id.commit_activity_filelist_rv) RecyclerView recyclerView;

    private Context context;
    private RepositoryCommit commit;
    private CommitFileAdapter commitFileAdapter;
    private LinearLayoutManager linearLayoutManager;
    private CommitService commitService;

    private Observable<RepositoryCommit> observable;
    private Observer<RepositoryCommit> observer;
    private Subscription subscription;

    public void populate(final Context context, View view, final String owner, final String repo, final String sha) {
        this.context = context;

        ButterKnife.bind(this, view);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<RepositoryCommit>() {
            @Override
            public void call(Subscriber<? super RepositoryCommit> subscriber) {
                commitService = new CommitService();
                commitService.getClient().setOAuth2Token(Constants.getToken(context));
                try {
                    subscriber.onNext(commitService.getCommit(new RepositoryId(owner, repo), sha));
                } catch (IOException e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<RepositoryCommit>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(RepositoryCommit repositoryCommit) {
                commit = repositoryCommit;
                List<CommitFile> commitFiles = repositoryCommit.getFiles();
                progressBar.setVisibility(View.GONE);

                commitFileAdapter = new CommitFileAdapter(commitFiles, context);
                linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(commitFileAdapter);
                commitFileAdapter.notifyDataSetChanged();
            }
        };

        subscription = observable.subscribe(observer);
    }

    public void unsubCommitActivityFilelist() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

}
