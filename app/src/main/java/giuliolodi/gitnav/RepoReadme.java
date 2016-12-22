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
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.mukesh.MarkdownView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.ContentsService;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepoReadme {

    @BindView(R.id.repo_readme_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_readme_markedview) MarkdownView markedView;

    private ContentsService contentsService;

    private String markdown;

    private Context context;

    private Observable<String> observable;
    private Observer<String> observer;
    private Subscription subscription;

    public void populate(final Context context, View v, final Repository repo) {
        this.context = context;

        ButterKnife.bind(this, v);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                contentsService = new ContentsService();
                contentsService.getClient().setOAuth2Token(Constants.getToken(context));
                try {
                    subscriber.onNext(contentsService.getReadme(new RepositoryId(repo.getOwner().getLogin(), repo.getName())).getContent());
                } catch (Exception e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("rx", e.getMessage());
            }

            @Override
            public void onNext(String markdown64) {
                if (markdown64 != null && !markdown64.isEmpty()) {
                    try {
                        markdown = new String(Base64.decode(markdown64, Base64.DEFAULT), "UTF-8");
                    } catch (UnsupportedEncodingException e) {e.printStackTrace();}
                    markedView.setMarkDownText(markdown);
                    markedView.setOpenUrlInBrowser(true);
                }

                progressBar.setVisibility(View.GONE);
            }
        };

        subscription = observable.subscribe(observer);

    }

    public void unsubRepoReadme() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
