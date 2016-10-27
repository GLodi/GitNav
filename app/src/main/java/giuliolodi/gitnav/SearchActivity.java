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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseDrawerActivity{

    @BindView(R.id.tab_layout) TabLayout tabLayout;

    @BindString(R.string.repositories) String repositories;
    @BindString(R.string.users) String users;
    @BindString(R.string.code) String code;

    private List<Integer> views;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.search_activity, frameLayout);

        ButterKnife.bind(this);

        initInstancesDrawer();

        views = new ArrayList<>();
        views.add(R.layout.search_fragment_repos);
        views.add(R.layout.search_fragment_users);
        views.add(R.layout.search_fragment_code);

        // Setup lists for tabs
        final List<String> mTitleDataList = new ArrayList<>();
        mTitleDataList.add("REPOSITORIES");
        mTitleDataList.add("USERS");
        mTitleDataList.add("CODE");

    }

    private void initInstancesDrawer() {

        tabLayout.setVisibility(View.VISIBLE);

        tabLayout.addTab(tabLayout.newTab().setText(repositories));
        tabLayout.addTab(tabLayout.newTab().setText(users));
        tabLayout.addTab(tabLayout.newTab().setText(code));
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tabLayout.getTabAt(2).select();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

}
