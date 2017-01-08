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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.ContentsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.FileAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepoContent {

    @BindView(R.id.repo_content_rl) RelativeLayout relativeLayout;
    @BindView(R.id.repo_content_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_content_rv) RecyclerView recyclerView;
    @BindView(R.id.repo_content_tree) TextView treeView;
    @BindString(R.string.network_error) String network_error;

    private Context context;
    private FileAdapter fileAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Observable<List<RepositoryContents>> observable;
    private Observer<List<RepositoryContents>> observer;
    private Subscription subscription;
    private final ContentsService contentsService = new ContentsService();
    private List<RepositoryContents> contentsList = new ArrayList<>();
    private List<String> pathTree = new ArrayList<>();
    private String path, treeText = "";
    private boolean IS_LOADING = false;

    public int treeDepth = 0;

    public void populate(final Context context, View v, final Repository repo) {
        this.context = context;

        ButterKnife.bind(this, v);

        progressBar.setVisibility(View.VISIBLE);
        contentsService.getClient().setOAuth2Token(Constants.getToken(context));

        treeView.setTypeface(EasyFonts.robotoRegular(context));
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        pathTree.add("");

        observable = observable.create(new Observable.OnSubscribe<List<RepositoryContents>>() {
            @Override
            public void call(final Subscriber<? super List<RepositoryContents>> subscriber) {
                try {
                    subscriber.onNext(contentsService.getContents(new RepositoryId(repo.getOwner().getLogin(), repo.getName()), path));
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
                Collections.sort(repositoryContents, new Comparator<RepositoryContents>() {
                    @Override
                    public int compare(RepositoryContents repositoryContents, RepositoryContents t1) {
                        return repositoryContents.getType().compareTo(t1.getType());
                    }
                });
                contentsList.addAll(repositoryContents);
                fileAdapter = new FileAdapter(repositoryContents);
                recyclerView.setAdapter(fileAdapter);
                fileAdapter.notifyDataSetChanged();

                setTree();

                IS_LOADING = false;

                progressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
        };

        subscription = observable.subscribe(observer);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (Constants.isNetworkAvailable(context)) {
                    if (!IS_LOADING && contentsList.get(position).getType().equals("dir")) {
                        IS_LOADING = true;
                        path = contentsList.get(position).getPath();
                        pathTree.add(contentsList.get(position).getPath());
                        treeDepth += 1;
                        contentsList.clear();
                        subscription = observable.subscribe(observer);
                    }
                } else
                    Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void setTree() {
        treeView.setText("");
        treeText = "/";
        treeText += pathTree.get(pathTree.size() - 1);
        treeView.setText(treeText);

    }

    public void handleOnBackPressed() {
        if (Constants.isNetworkAvailable(context)) {
            if (!IS_LOADING) {
                IS_LOADING = true;
                path = pathTree.get(pathTree.size() - 2);
                pathTree.remove(pathTree.size() - 1);
                treeDepth -= 1;
                contentsList.clear();
                subscription = observable.subscribe(observer);
            }
        } else
            Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();

    }

    public void unsubRepoContent() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
