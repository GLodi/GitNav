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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CommitActivity  extends BaseDrawerActivity {

    @BindView(R.id.commit_activity_progressbar) ProgressBar progressBar;
    @BindString(R.string.network_error) String network_error;

    private Intent intent;
    private RepositoryCommit commit;
    private CommitService commitService;
    private String owner, repo, sha, commit_url, commit_title;

    private Observable<RepositoryCommit> observable;
    private Observer<RepositoryCommit> observer;
    private Subscription subscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.commit_activity, frameLayout);

        ButterKnife.bind(this);

        intent = getIntent();
        owner = intent.getStringExtra("owner");
        repo = intent.getStringExtra("repo");
        sha = intent.getStringExtra("sha");
        commit_url = intent.getStringExtra("commit_url");
        commit_title = intent.getStringExtra("commit_title");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getSupportActionBar().setTitle(commit_title);
        getSupportActionBar().setSubtitle(owner + "/" + repo);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<RepositoryCommit>() {
            @Override
            public void call(Subscriber<? super RepositoryCommit> subscriber) {
                commitService = new CommitService();
                commitService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));
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
                progressBar.setVisibility(View.GONE);
            }
        };

        subscription = observable.subscribe(observer);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.commit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            switch (item.getItemId()) {
                case R.id.open_in_browser:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(commit_url));
                    startActivity(browserIntent);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }
}
