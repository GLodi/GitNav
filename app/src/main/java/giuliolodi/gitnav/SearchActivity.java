/*
 * MIT License
 *
 * Copyright (c) 2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package giuliolodi.gitnav;


import android.app.SearchManager;
import android.content.Context;
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        final SearchRepos searchRepos = new SearchRepos();
        final SearchUsers searchUsers = new SearchUsers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (Constants.isNetworkAvailable(getApplicationContext())) {

                    if (!searchRepos.isLOADING())
                        searchRepos.populate(query, SearchActivity.this, findViewById(R.id.search_repos_rl), PREVENT_MULTIPLE_SEPARATOR_LINE);

                    if (!searchUsers.isLOADING())
                        searchUsers.populate(query, SearchActivity.this, findViewById(R.id.search_users_rl), PREVENT_MULTIPLE_SEPARATOR_LINE);
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
        switch (item.getItemId()) {
            case R.id.action_options:

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
