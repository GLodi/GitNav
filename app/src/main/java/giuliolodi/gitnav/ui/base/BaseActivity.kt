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

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import giuliolodi.gitnav.App
import giuliolodi.gitnav.R
import giuliolodi.gitnav.di.component.ActivityComponent
import giuliolodi.gitnav.di.component.DaggerActivityComponent
import giuliolodi.gitnav.di.module.ActivityModule
import giuliolodi.gitnav.utils.Constants
import giuliolodi.gitnav.utils.NetworkUtils

/**
 * Created by giulio on 12/05/2017.
 */
open class BaseActivity : AppCompatActivity(), BaseContract.View, BaseFragment.Callback {

    private lateinit var mActivityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        when(applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).getString("PREF_KEY_THEME", null)) {
            "light" -> setTheme(R.style.AppTheme_NoActionBar)
            "dark" -> setTheme(R.style.AppTheme_NoActionBar_Dark)
            null -> applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).edit().putString("PREF_KEY_THEME", "light").apply()
        }
        super.onCreate(savedInstanceState)

        val app: App = application as App

        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build()
    }

    // Not used here
    override fun initDrawer(username: String, fullName: String?, email: String?, profilePic: Bitmap?) {}

    fun getActivityComponent(): ActivityComponent {
        return mActivityComponent
    }

    override fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(applicationContext)
    }

    /*
     * This is supposed to update the activity on its fragment's status.
     * I'm not currently taking advantage of it, as the dispose of Flowables are handled by fragments
      * themselves.
     */
    override fun onFragmentAttached() {
    }

    override fun onFragmentDetached(tag: String) {
    }

}

