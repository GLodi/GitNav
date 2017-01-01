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

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.ContentsService;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepoContent {

    private Observable<List<RepositoryContents>> observable;
    private Observer<List<RepositoryContents>> observer;
    private Subscription subscription;

    public void populate(final Context context, final Repository repo) {

        observable = observable.create(new Observable.OnSubscribe<List<RepositoryContents>>() {
            @Override
            public void call(Subscriber<? super List<RepositoryContents>> subscriber) {
                ContentsService contentsService = new ContentsService();
                contentsService.getClient().setOAuth2Token(Constants.getToken(context));

                try {
                    subscriber.onNext(contentsService.getContents(new RepositoryId(repo.getOwner().getLogin(), repo.getName()), "app/src"));
                } catch (IOException e) {e.printStackTrace();}

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<RepositoryContents>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<RepositoryContents> repositoryContents) {

            }
        };

        subscription = observable.subscribe(observer);

    }

    public void unsubRepoContent() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
