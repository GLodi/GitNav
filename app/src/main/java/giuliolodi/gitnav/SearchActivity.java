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


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseDrawerActivity {

    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.search_viewpager) ViewPager searchViewPager;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.repositories) String repositories;
    @BindString(R.string.users) String users;
    @BindString(R.string.code) String code;

    private SearchRepos searchRepos;
    private SearchUsers searchUsers;
    private SearchCode searchCode;

    private List<Integer> views;
    private SearchView searchView;

    private boolean PREVENT_MULTIPLE_SEPARATOR_LINE = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.search_activity, frameLayout);

        ButterKnife.bind(this);

        views = new ArrayList<>();
        views.add(R.layout.search_repos);
        views.add(R.layout.search_users);
        views.add(R.layout.search_code);

        searchViewPager.setOffscreenPageLimit(3);
        searchViewPager.setAdapter(new MyAdapter(getApplicationContext()));

        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(searchViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

    }

    public class MyAdapter extends PagerAdapter {

        Context context;

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 3;
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
                    return repositories;
                case 1:
                    return users;
                case 2:
                    return code;
            }
            return super.getPageTitle(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(2).setChecked(true);

        /*
            This will create new objects for the 3 types of Search that can be done. It is needed
            in order to avoid any problem with data that is set to be displayed on a non-existing
            view
         */
        searchRepos = new SearchRepos();
        searchUsers = new SearchUsers();
        searchCode = new SearchCode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsubAll();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    private void unsubAll() {
        /*
            By unsubbing from the Observables that handle the requests, we avoid any error caused
            by binding data to non-existing views
         */
        searchRepos.unsubSearchRepos();
        searchUsers.unsubSearchUsers();
        searchCode.unsubSearchCode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        /*
            When the user presses Enter, the queue is passed to the objects that will handle each request
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (Constants.isNetworkAvailable(getApplicationContext())) {
                    unsubAll();
                    searchRepos.populate(query, SearchActivity.this, findViewById(R.id.search_repos_rl), PREVENT_MULTIPLE_SEPARATOR_LINE);
                    searchUsers.populate(query, SearchActivity.this, findViewById(R.id.search_users_rl), PREVENT_MULTIPLE_SEPARATOR_LINE);
                    searchCode.populate(query, SearchActivity.this, findViewById(R.id.search_code_rl), PREVENT_MULTIPLE_SEPARATOR_LINE);
                    PREVENT_MULTIPLE_SEPARATOR_LINE = false;
                }
                else {
                    Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        return super.onOptionsItemSelected(item);
    }

}
