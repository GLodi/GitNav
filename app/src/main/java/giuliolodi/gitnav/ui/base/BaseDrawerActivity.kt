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

package giuliolodi.gitnav.ui.base

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import giuliolodi.gitnav.App
import giuliolodi.gitnav.di.component.ActivityComponent
import giuliolodi.gitnav.di.component.DaggerActivityComponent
import giuliolodi.gitnav.di.module.ActivityModule
import giuliolodi.gitnav.utils.NetworkUtils
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import giuliolodi.gitnav.R
import kotlinx.android.synthetic.main.activity_base_drawer.*
import kotlinx.android.synthetic.main.app_bar_home.*

/**
 * Created by giulio on 15/05/2017.
 */

open class BaseDrawerActivity : AppCompatActivity(), BaseContract.View, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mActivityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base_drawer);

        val app: App = application as App

        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build()

        initLayout()
    }

    private fun initLayout() {
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.setDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    fun getActivityComponent(): ActivityComponent {
        return mActivityComponent
    }

    override fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        //to prevent current item select over and over
        if (item.isChecked) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return false
        }
        when (item.itemId) {

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
