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

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.GistFileAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GistActivity extends BaseDrawerActivity {

    @BindView(R.id.gist_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.gist_activity_filelist_rv) RecyclerView recyclerView;
    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.gist_starred) String gist_starred;
    @BindString(R.string.gist_unstarred) String gist_unstarred;

    private Menu menu;
    private Intent intent;
    private String gistId;
    private Gist gist;
    private GistService gistService;
    private List<GistFile> gistFiles;
    private GistFileAdapter gistFileAdapter;
    private LinearLayoutManager linearLayoutManager;

    private boolean IS_GIST_STARRED;

    private Observable<Gist> observable;
    private Observer<Gist> observer;
    private Subscription subscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.gist_activity, frameLayout);

        ButterKnife.bind(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Get the id of the required Gist
        intent = getIntent();
        gistId = intent.getStringExtra("GistId");

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<Gist>() {
            @Override
            public void call(Subscriber<? super Gist> subscriber) {
                gistService = new GistService();
                gistService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

                try {
                    IS_GIST_STARRED = gistService.isStarred(gistId);
                } catch (IOException e) {e.printStackTrace();}

                try {
                    gist = gistService.getGist(gistId);
                    subscriber.onNext(gist);
                } catch (IOException e) {e.printStackTrace();}

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<Gist>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Gist gist) {
                getSupportActionBar().setTitle(gist.getDescription());
                progressBar.setVisibility(View.GONE);

                createOptionMenu();

                gistFiles = new ArrayList<>(gist.getFiles().values());
                gistFileAdapter = new GistFileAdapter(gistFiles);
                linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(gistFileAdapter);
                gistFileAdapter.notifyDataSetChanged();

            }
        };

        if (Constants.isNetworkAvailable(getApplicationContext()))
            subscription = observable.subscribe(observer);
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    private void createOptionMenu() {
        getMenuInflater().inflate(R.menu.gist_activity_menu, menu);
        super.onCreateOptionsMenu(menu);

        /*
            Check whether the Gist is starred bye the user and
            show corresponding icon in Toolbar
        */
        if (IS_GIST_STARRED)
            menu.findItem(R.id.follow_icon).setVisible(true);
        else
            menu.findItem(R.id.unfollow_icon).setVisible(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            switch (item.getItemId()) {
                case R.id.follow_icon:
                    new unstarGist().execute();
                    return true;
                case R.id.unfollow_icon:
                    new starGist().execute();
                    return true;
                case R.id.open_in_browser:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gist.getHtmlUrl()));
                    startActivity(browserIntent);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }


    private class starGist extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                gistService.starGist(gistId);
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            menu.findItem(R.id.follow_icon).setVisible(true);
            menu.findItem(R.id.unfollow_icon).setVisible(false);
            Toasty.success(getApplicationContext(), gist_starred, Toast.LENGTH_LONG).show();
        }
    }

    private class unstarGist extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                gistService.unstarGist(gistId);
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            menu.findItem(R.id.unfollow_icon).setVisible(true);
            menu.findItem(R.id.follow_icon).setVisible(false);
            Toasty.success(getApplicationContext(), gist_unstarred, Toast.LENGTH_LONG).show();
        }
    }
}
