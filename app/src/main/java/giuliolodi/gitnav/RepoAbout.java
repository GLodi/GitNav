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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.*;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.Adapters.RepoAboutAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepoAbout {

    @BindView(R.id.repo_about_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_about_gridview) RecyclerView gridView;
    @BindView(R.id.repo_about_rl2) RelativeLayout relativeLayout;
    @BindView(R.id.repo_about_image) CircleImageView imageView;
    @BindView(R.id.repo_about_reponame) TextView repoName;
    @BindView(R.id.repo_about_username) TextView username;
    @BindView(R.id.repo_about_description) TextView description;

    @BindString(R.string.stargazers) String stargazers;
    @BindString(R.string.forks) String forks;
    @BindString(R.string.issues) String issues;
    @BindString(R.string.contributors) String contributors;

    private Context context;
    private Repository repo;
    private List<String> nameList, numberList;
    private List<Drawable> imageList;
    private PrettyTime p = new PrettyTime();

    private Observable<List<Contributor>> observable;
    private Observer<List<Contributor>> observer;
    private Subscription subscription;

    public void populate (final Context context, View v, final Repository repo, final int stargazerNumber) {
        this.context = context;
        this.repo = repo;

        ButterKnife.bind(this, v);

        progressBar.setVisibility(View.VISIBLE);

        Picasso.with(context).load(repo.getOwner().getAvatarUrl()).resize(75, 75).centerCrop().into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", repo.getOwner().getLogin()));
                ((Activity)context).overridePendingTransition(0, 0);
            }
        });
        repoName.setTypeface(EasyFonts.robotoRegular(context));
        username.setTypeface(EasyFonts.robotoRegular(context));
        description.setTypeface(EasyFonts.robotoRegular(context));

        repoName.setText(repo.getName());
        username.setText(repo.getOwner().getLogin());
        if (repo.getDescription() != null && !repo.getDescription().isEmpty())
            description.setText(repo.getDescription());
        else
            description.setVisibility(View.GONE);

        observable = Observable.create(new Observable.OnSubscribe<List<Contributor>>() {
            @Override
            public void call(Subscriber<? super List<Contributor>> subscriber) {
                RepositoryService repositoryService = new RepositoryService();
                repositoryService.getClient().setOAuth2Token(Constants.getToken(context));
                try {
                    subscriber.onNext(repositoryService.getContributors(new RepositoryId(repo.getOwner().getLogin(), repo.getName()), true));
                } catch (IOException e) {e.printStackTrace();}
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<Contributor>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("rx", e.getMessage());
            }

            @Override
            public void onNext(List<Contributor> contributorList) {
                progressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);

                nameList = new ArrayList<>();
                nameList.add(stargazers);
                nameList.add(forks);
                nameList.add(issues);
                nameList.add(contributors);

                numberList = new ArrayList<>();
                numberList.add(String.valueOf(stargazerNumber));
                numberList.add(String.valueOf(repo.getForks()));
                numberList.add(String.valueOf(repo.getOpenIssues()));
                numberList.add(String.valueOf(contributorList.size()));

                imageList = new ArrayList<>();
                imageList.add(context.getResources().getDrawable(R.drawable.octicons_430_heart_256_0_000000_none));
                imageList.add(context.getResources().getDrawable(R.drawable.octicons_430_repoforked_256_0_000000_none));
                imageList.add(context.getResources().getDrawable(R.drawable.octicons_430_issueopened_256_0_000000_none));
                imageList.add(context.getResources().getDrawable(R.drawable.octicons_430_flame_256_0_000000_none));

                gridView.setLayoutManager(new GridLayoutManager(context, 3));
                gridView.setHasFixedSize(true);
                gridView.setNestedScrollingEnabled(false);
                gridView.setAdapter(new RepoAboutAdapter(context, nameList, numberList, imageList, repo.getName(), repo.getOwner().getLogin()));
            }
        };

        subscription = observable.subscribe(observer);

    }

    public void unsubRepoAbout() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
