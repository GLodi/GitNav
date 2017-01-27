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
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.StarService;

import java.io.IOException;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.CommentAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GistComments {

    @BindView(R.id.gist_comments_progressbar) ProgressBar progressBar;
    @BindView(R.id.gist_comments_rv) RecyclerView recyclerView;
    @BindView(R.id.gist_comments_nocomments) TextView noComments;

    @BindString(R.string.no_comments) String noCommentsString;
    @BindString(R.string.network_error) String network_error;

    private Context context;
    private GistService gistService;
    private CommentAdapter commentAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Observable<List<Comment>> observable;
    private Observer<List<Comment>> observer;
    private Subscription subscription;

    public void populate(final Context context, View v, final String gistId) {
        this.context = context;

        ButterKnife.bind(this, v);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<Comment>>() {
            @Override
            public void call(Subscriber<? super List<Comment>> subscriber) {
                gistService = new GistService();
                gistService.getClient().setOAuth2Token(Constants.getToken(context));
                try {
                    subscriber.onNext(gistService.getComments(gistId));
                } catch (IOException e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<Comment>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Comment> gistComments) {
                progressBar.setVisibility(View.GONE);
                if (gistComments.isEmpty()) {
                    noComments.setTypeface(EasyFonts.robotoRegular(context));
                    noComments.setText(noCommentsString);
                } else {
                    commentAdapter = new CommentAdapter(gistComments, context);
                    linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(commentAdapter);
                    commentAdapter.notifyDataSetChanged();
                }
            }
        };

        if (Constants.isNetworkAvailable(context))
            subscription = observable.subscribe(observer);
        else
            Toasty.warning(context, network_error, Toast.LENGTH_LONG).show();

    }

}
