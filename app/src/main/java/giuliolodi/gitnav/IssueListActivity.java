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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueListActivity extends BaseDrawerActivity {

    @BindView(R.id.issuelist_activity_viewpager) ViewPager issueListViewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;

    @BindString(R.string.issues) String issuesString;
    @BindString(R.string.open) String openString;
    @BindString(R.string.closed) String closedString;

    private Intent intent;
    private String owner, repo;
    private List<Integer> views;
    private IssueListOpen issueListOpen;
    private IssueListClosed issueListClosed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.issuelist_activity, frameLayout);

        ButterKnife.bind(this);

        intent = getIntent();
        owner = intent.getExtras().getString("owner");
        repo = intent.getExtras().getString("repo");

        getSupportActionBar().setTitle(issuesString);
        getSupportActionBar().setSubtitle(owner + "/" + repo);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(0,0);
            }
        });

        views = new ArrayList<>();
        views.add(R.layout.issuelist_open);
        views.add(R.layout.issuelist_closed);

        issueListViewPager.setOffscreenPageLimit(2);
        issueListViewPager.setAdapter(new MyAdapter(getApplicationContext()));

        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(issueListViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

    }

    private class MyAdapter extends PagerAdapter {

        Context context;

        private MyAdapter(Context context) {
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
                    return openString;
                case 1:
                    return closedString;
            }
            return super.getPageTitle(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        issueListOpen = new IssueListOpen();
        issueListOpen.populate(IssueListActivity.this, findViewById(R.id.issuelist_open_rl), owner, repo);

        issueListClosed = new IssueListClosed();
        issueListClosed.populate(IssueListActivity.this, findViewById(R.id.issuelist_closed_rl), owner, repo);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        issueListOpen.unsubIssueListOpen();
        issueListClosed.unsubIssueListClosed();
    }
}
