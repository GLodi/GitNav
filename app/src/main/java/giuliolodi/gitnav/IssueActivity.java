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

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

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

public class IssueActivity extends BaseDrawerActivity {

    @BindView(R.id.issue_activity_progressbar) ProgressBar progressBar;
    @BindString(R.string.issue) String issueString;

    private Intent intent;
    private Issue issue;
    private IssueService issueService;
    private String owner, repo, issueNumber, issueUrl;

    private Observable<Issue> observable;
    private Observer<Issue> observer;
    private Subscription subscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.issue_activity, frameLayout);

        ButterKnife.bind(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        intent = getIntent();
        owner = intent.getStringExtra("owner");
        repo = intent.getStringExtra("repo");
        issueNumber = intent.getStringExtra("issueNumber");

        getSupportActionBar().setTitle(issueString + " #" + issueNumber);
        getSupportActionBar().setSubtitle(owner + "/" + repo);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<Issue>() {
            @Override
            public void call(Subscriber<? super Issue> subscriber) {
                issueService = new IssueService();
                issueService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));
                try {
                    issue = issueService.getIssue(owner, repo, issueNumber);
                    subscriber.onNext(issue);
                } catch (IOException e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<Issue>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Issue issue) {
                progressBar.setVisibility(View.GONE);
            }
        };

        subscription = observable.subscribe(observer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.issue_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Constants.isNetworkAvailable(getApplicationContext()) && item.getItemId() == R.id.open_in_browser) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(issue.getHtmlUrl()));
            startActivity(browserIntent);
        }
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}
