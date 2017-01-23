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

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.CommitCommentsAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CommitActivityComments {

    @BindView(R.id.commit_activity_comments_progressbar) ProgressBar progressBar;
    @BindView(R.id.commit_activity_comments_rv) RecyclerView recyclerView;
    @BindView(R.id.commit_activity_comments_nocomments) TextView noComments;

    private Context context;
    private CommitService commitService;
    private CommitCommentsAdapter commitCommentsAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Observable<List<CommitComment>> observable;
    private Observer<List<CommitComment>> observer;
    private Subscription subscription;

    public void populate(final Context context, View view, final String owner, final String repo, final String sha) {
        this.context = context;

        ButterKnife.bind(this, view);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<CommitComment>>() {
            @Override
            public void call(Subscriber<? super List<CommitComment>> subscriber) {
                commitService = new CommitService();
                commitService.getClient().setOAuth2Token(Constants.getToken(context));
                try {
                    subscriber.onNext(commitService.getComments(new RepositoryId(owner, repo), sha));
                } catch (IOException e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<CommitComment>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<CommitComment> commitComment) {
                progressBar.setVisibility(View.GONE);
                if (commitComment.isEmpty()) {
                    noComments.setTypeface(EasyFonts.robotoRegular(context));
                    noComments.setVisibility(View.VISIBLE);
                } else {
                    commitCommentsAdapter = new CommitCommentsAdapter(commitComment, context);
                    linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(commitCommentsAdapter);
                    commitCommentsAdapter.notifyDataSetChanged();
                }
            }
        };

        subscription = observable.subscribe(observer);
    }

    public void unsubCommitActivityComments() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

}
