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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.CodeSearchResult;
import org.eclipse.egit.github.core.TextMatch;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.CodeAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchCode  {

    @BindView(R.id.search_code_progress_bar) ProgressBar progressBar;
    @BindView(R.id.search_code_rv) RecyclerView recyclerView;
    @BindView(R.id.no_code) TextView noCode;

    private CodeAdapter codeAdapter;
    private LinearLayoutManager linearLayoutManager;

    private String query;
    private List<CodeSearchResult> searchResultList;
    private Context context;

    private Observable observable;
    private Observer observer;
    private Subscription s;

    private boolean LOADING = false;
    private boolean PREVENT_MULTIPLE_SEPARATOR_LINE;

    public void populate(String query, Context context, View v, boolean PREVENT_MULTIPLE_SEPARATOR_LINE) {
        this.query = query;
        this.context = context;
        this.PREVENT_MULTIPLE_SEPARATOR_LINE = PREVENT_MULTIPLE_SEPARATOR_LINE;
        ButterKnife.bind(this, v);
        LOADING = true;

        progressBar.setVisibility(View.VISIBLE);
        noCode.setVisibility(View.INVISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<CodeSearchResult>>() {
            @Override
            public void call(Subscriber<? super List<CodeSearchResult>> subscriber) {
                RepositoryService repositoryService = new RepositoryService();
                repositoryService.getClient().setOAuth2Token(Constants.getToken(getContext()));

                try {
                    searchResultList = repositoryService.searchCode(getQuery());
                } catch (IOException e) {e.printStackTrace();}

                if (searchResultList != null)
                    subscriber.onNext(searchResultList);
                else
                    subscriber.onNext(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<CodeSearchResult>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("RX", e.getMessage());
            }

            @Override
            public void onNext(List<CodeSearchResult> searchResultList) {
                if (searchResultList == null ||searchResultList.isEmpty())
                    noCode.setVisibility(View.VISIBLE);

                codeAdapter = new CodeAdapter(searchResultList, getContext());
                linearLayoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(linearLayoutManager);
                if (getPrevent())
                    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(codeAdapter);
                progressBar.setVisibility(View.GONE);
                codeAdapter.notifyDataSetChanged();

                LOADING = false;
            }
        };

        s = observable.subscribe(observer);
    }

    private boolean getPrevent() {
        return PREVENT_MULTIPLE_SEPARATOR_LINE;
    }

    private Context getContext() {
        return context;
    }

    private String getQuery() {
        return query;
    }

    public boolean isLOADING() {
        return LOADING;
    }

    public void unsubSearchCode() {
        s.unsubscribe();
    }

}
