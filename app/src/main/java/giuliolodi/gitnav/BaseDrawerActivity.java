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

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class BaseDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FrameLayout frameLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);;

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
                startActivity(new Intent(getApplicationContext(), UserActivity.class).putExtra("userS", Constants.getUsername(getApplicationContext())));
                overridePendingTransition(0,0);
                drawerLayout.closeDrawer(GravityCompat.START);
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
            startActivity(new Intent(getApplicationContext(), RepoListActivity.class));
            overridePendingTransition(0,0);
        } else if (id == R.id.nav_starred) {
            startActivity(new Intent(getApplicationContext(), StarredActivity.class));
            overridePendingTransition(0,0);
        } else if (id == R.id.nav_search) {
            startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            overridePendingTransition(0,0);
        } else if (id == R.id.nav_gists) {
            startActivity(new Intent(getApplicationContext(), GistsActivity.class));
            overridePendingTransition(0,0);
        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_send) {
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}