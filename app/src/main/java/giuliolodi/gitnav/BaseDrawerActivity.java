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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BaseDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FrameLayout frameLayout;
    NavigationView navigationView;

    @BindString(R.string.logout) String logout;
    @BindString(R.string.confirm_logout) String confirmLogout;
    @BindString(R.string.yes) String yes;
    @BindString(R.string.no) String no;

    private long DRAWER_DELAY = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set username, email and profile picture in nav drawer
        View hView = navigationView.getHeaderView(0);
        TextView nav_user = (TextView) hView.findViewById(R.id.nav_user);
        nav_user.setText(Constants.getUsername(getApplicationContext()));
        TextView nav_email = (TextView) hView.findViewById(R.id.nav_email);
        nav_email.setText(Constants.getEmail(getApplicationContext()));
        TextView nav_full_name = (TextView) hView.findViewById(R.id.nav_full_name);
        nav_full_name.setText(Constants.getFullName(getApplicationContext()));
        CircleImageView image_view = (CircleImageView) hView.findViewById(R.id.imageView);
        Bitmap thumbnail = new ImageSaver(getApplicationContext())
                .setFileName("thumbnail.png")
                .setDirectoryName("images")
                .load();
        image_view.setImageBitmap(thumbnail);

        // Set nav drawer profile's onClickListener
        RelativeLayout nav_click = (RelativeLayout) hView.findViewById(R.id.nav_click);
        nav_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.START);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), UserActivity.class).putExtra("userS", Constants.getUsername(getApplicationContext())));
                        overridePendingTransition(0,0);
                    }
                }, DRAWER_DELAY);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        //to prevent current item select over and over
        if (item.isChecked()){
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        if (id == R.id.nav_repos) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), RepoListActivity.class));
                    overridePendingTransition(0,0);
                }
            }, DRAWER_DELAY);
        } else if (id == R.id.nav_starred) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), StarredActivity.class));
                    overridePendingTransition(0,0);
                }
            }, DRAWER_DELAY);
        } else if (id == R.id.nav_search) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0,0);
                }
            }, DRAWER_DELAY);
        } else if (id == R.id.nav_gists) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), GistListActivity.class));
                    overridePendingTransition(0,0);
                }
            }, DRAWER_DELAY);
        } else if (id == R.id.nav_trending) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), TrendingActivity.class));
                    overridePendingTransition(0,0);
                }
            }, DRAWER_DELAY);
        } else if (id == R.id.nav_manage) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), OptionActivity.class));
                    overridePendingTransition(0,0);
                }
            }, DRAWER_DELAY);
        } else if (id == R.id.nav_logout) {
            // This will delete every info put inside SharedPreferences and Intent to LoginActivity
            ButterKnife.bind(this);
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final SharedPreferences.Editor editor = sp.edit();
            new AlertDialog.Builder(this)
                    .setTitle(logout)
                    .setMessage(confirmLogout)
                    .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Delete all sp info stored
                            editor.putString(Constants.getTokenKey(getApplicationContext()), "");
                            editor.putString(Constants.getUserKey(getApplicationContext()), "");
                            editor.putBoolean(Constants.getAuthdKey(getApplicationContext()), false);
                            editor.putString(Constants.getEmailKey(getApplicationContext()), "");
                            editor.putString(Constants.getFullNameKey(getApplicationContext()), "");
                            editor.commit();

                            // Intent to LoginActivity
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                    })
                    .setNegativeButton(no, null)
                    .show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}