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
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

public class GistListActivity extends BaseDrawerActivity {

    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.gists_viewpager) ViewPager gistsViewpager;

    @BindString(R.string.mine) String mine;
    @BindString(R.string.starred) String starred;
    @BindString(R.string.network_error) String network_error;

    private GistListMine gistListMine;
    private GistListStarred gistListStarred;
    private List<Integer> views;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.gist_list_activity, frameLayout);

        ButterKnife.bind(this);

        views = new ArrayList<>();
        views.add(R.layout.gist_list_mine);
        views.add(R.layout.gist_list_starred);

        gistsViewpager.setOffscreenPageLimit(2);
        gistsViewpager.setAdapter(new MyAdapter(getApplicationContext()));

        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        tabLayout.setupWithViewPager(gistsViewpager);

    }

    public class MyAdapter extends PagerAdapter {

        Context context;

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 2;
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
                    return mine;
                case 1:
                    return starred;
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
        navigationView.getMenu().getItem(4).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            gistListMine = new GistListMine();
            gistListMine.populate(GistListActivity.this, findViewById(R.id.gists_mine_rl));
            gistListStarred = new GistListStarred();
            gistListStarred.populate(GistListActivity.this, findViewById(R.id.gists_starred_rl));
        } else
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_options:
                startActivity(new Intent(getApplicationContext(), OptionActivity.class));
                overridePendingTransition(0,0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
