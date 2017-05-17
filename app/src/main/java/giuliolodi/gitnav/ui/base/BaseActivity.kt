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

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import giuliolodi.gitnav.App
import giuliolodi.gitnav.di.component.ActivityComponent
import giuliolodi.gitnav.di.component.DaggerActivityComponent
import giuliolodi.gitnav.di.module.ActivityModule
import giuliolodi.gitnav.utils.NetworkUtils


/**
 * Created by giulio on 12/05/2017.
 */

open class BaseActivity : AppCompatActivity(), BaseContract.View {

    private lateinit var mActivityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app: App = application as App

        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build()
    }

    // Not used here
    override fun initDrawer(username: String, fullName: String?, email: String?, profilePic: Bitmap) {}

    fun getActivityComponent(): ActivityComponent {
        return mActivityComponent
    }

    override fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

