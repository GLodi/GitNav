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
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.StarService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class RepoActivity extends BaseDrawerActivity {

    @BindView(R.id.repo_viewpager) ViewPager repoViewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.repo_progressbar) ProgressBar progressBar;

    @BindString(R.string.repository) String repository;
    @BindString(R.string.about) String about;
    @BindString(R.string.readme) String readme;
    @BindString(R.string.files) String files;
    @BindString(R.string.commits) String commits;
    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.repo_starred) String repo_starred;
    @BindString(R.string.repo_unstarred) String repo_unstarred;

    private Repository repo;
    private RepositoryService repositoryService;
    private StarService starService;
    private Intent intent;
    private String owner;
    private String name;
    private int stargazerNumber;

    private RepoAbout repoAbout;
    private RepoReadme repoReadme;
    private RepoCommits repoCommits;
    private RepoContent repoContent;

    private List<Integer> views;
    private Menu menu;

    private boolean IS_REPO_STARRED = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.repo_activity, frameLayout);

        ButterKnife.bind(this);

        getSupportActionBar().setTitle(repository);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepoActivity.super.onBackPressed();
                overridePendingTransition(0,0);
            }
        });

        intent = getIntent();
        owner = intent.getStringExtra("owner");
        name = intent.getStringExtra("name");

        views = new ArrayList<>();
        views.add(R.layout.repo_about);
        views.add(R.layout.repo_readme);
        views.add(R.layout.repo_content);
        views.add(R.layout.repo_commits);

        repoViewPager.setOffscreenPageLimit(4);
        repoViewPager.setAdapter(new MyAdapter(getApplicationContext()));
        repoViewPager.setCurrentItem(1);

        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(repoViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

        progressBar.setVisibility(View.VISIBLE);

        if (Constants.isNetworkAvailable(getApplicationContext()))
            new getRepo().execute();
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
    }

    private class MyAdapter extends PagerAdapter {

        Context context;

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(views.get(position), container, false);
            container.addView(layout);
            return layout;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return about;
                case 1:
                    return readme;
                case 2:
                    return files;
                case 3:
                    return commits;
            }
            return super.getPageTitle(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    @Override
    public void onBackPressed() {
        if (repoViewPager.getCurrentItem() == 2 && repoContent.treeDepth != 0) {
            repoContent.handleOnBackPressed();
        } else {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repoReadme.unsubRepoReadme();
        repoCommits.unsubRepoCommits();
        repoContent.unsubRepoContent();
        repoAbout.unsubRepoAbout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    private void createOptionMenu() {
        getMenuInflater().inflate(R.menu.repo_activity_menu, menu);
        super.onCreateOptionsMenu(menu);

        if (IS_REPO_STARRED)
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
                    new unstarRepo().execute();
                    return true;
                case R.id.unfollow_icon:
                    new starRepo().execute();
                    return true;
                case R.id.open_in_browser:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo.getHtmlUrl()));
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

    private class getRepo extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

            starService = new StarService();
            starService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

            try {
                repo = repositoryService.getRepository(owner, name);
            } catch (IOException e) {e.printStackTrace();}

            try {
                IS_REPO_STARRED = starService.isStarring(new RepositoryId(repo.getOwner().getLogin(), repo.getName()));
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getSupportActionBar().setTitle(repo.getName());
            getSupportActionBar().setSubtitle(repo.getOwner().getLogin());

            progressBar.setVisibility(View.GONE);

            /*
                Create the option menu, now that IS_REPO_STARRED is set, it can check
                whether the repo is starred or not
            */
            createOptionMenu();

            stargazerNumber = repo.getWatchers();

            repoReadme = new RepoReadme();
            repoReadme.populate(RepoActivity.this, findViewById(R.id.repo_readme_ll), repo);

            repoAbout = new RepoAbout();
            repoAbout.populate(RepoActivity.this, findViewById(R.id.repo_about_rl), repo, stargazerNumber);

            repoContent = new RepoContent();
            repoContent.populate(RepoActivity.this, findViewById(R.id.repo_content_ll), repo);

            repoCommits = new RepoCommits();
            repoCommits.populate(RepoActivity.this, findViewById(R.id.repo_commits_ll), repo);
        }
    }

    private class starRepo extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                starService.star(new RepositoryId(repo.getOwner().getLogin(), repo.getName()));
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            menu.findItem(R.id.follow_icon).setVisible(true);
            menu.findItem(R.id.unfollow_icon).setVisible(false);
            Toasty.success(getApplicationContext(), repo_starred, Toast.LENGTH_LONG).show();
        }
    }

    private class unstarRepo extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                starService.unstar(new RepositoryId(repo.getOwner().getLogin(), repo.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            menu.findItem(R.id.unfollow_icon).setVisible(true);
            menu.findItem(R.id.follow_icon).setVisible(false);
            Toasty.success(getApplicationContext(), repo_unstarred, Toast.LENGTH_LONG).show();
        }
    }

}
