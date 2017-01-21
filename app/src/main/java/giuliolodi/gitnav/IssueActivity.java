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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.Adapters.CommentAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueActivity extends BaseDrawerActivity {

    @BindView(R.id.issue_activity_progressbar) ProgressBar progressBar;
    @BindView(R.id.issue_activity_rv) RecyclerView recyclerView;
    @BindView(R.id.issue_activity_username) TextView username;
    @BindView(R.id.issue_activity_title) TextView title;
    @BindView(R.id.issue_activity_description) TextView description;
    @BindView(R.id.issue_activity_image) CircleImageView imageView;
    @BindView(R.id.issue_activity_nested) NestedScrollView nestedScrollView;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.issue) String issueString;

    private Intent intent;
    private Issue issue;
    private IssueService issueService;
    private String owner, repo, issueNumber;
    private List<Comment> issueComments;
    private CommentAdapter commentAdapter;
    private LinearLayoutManager linearLayoutManager;

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

        username.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
        title.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
        description.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<Issue>() {
            @Override
            public void call(Subscriber<? super Issue> subscriber) {
                issueService = new IssueService();
                issueService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));
                try {
                    issue = issueService.getIssue(owner, repo, issueNumber);
                    issueComments = issueService.getComments(owner, repo, issueNumber);
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
                nestedScrollView.setVisibility(View.VISIBLE);

                username.setText(issue.getUser().getLogin());
                title.setText(issue.getTitle());
                description.setText(issue.getBody());
                Picasso.with(getApplicationContext()).load(issue.getUser().getAvatarUrl()).resize(75, 75).centerCrop().into(imageView);

                commentAdapter = new CommentAdapter(issueComments, getApplicationContext());
                linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setNestedScrollingEnabled(false);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(commentAdapter);
                commentAdapter.notifyDataSetChanged();
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
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            switch (item.getItemId()) {
                case R.id.open_in_browser:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(issue.getHtmlUrl()));
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
