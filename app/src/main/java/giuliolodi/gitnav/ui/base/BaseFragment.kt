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
import android.support.v4.app.Fragment
import android.view.View
import giuliolodi.gitnav.di.component.ActivityComponent

/**
 * Created by giulio on 28/05/2017.
 */

abstract class BaseFragment : Fragment(), BaseContract.View {

    private var mBaseActivity: BaseActivity? = null
    private var mBaseDrawerActivity: BaseDrawerActivity? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayout(view, savedInstanceState)
    }

    protected abstract fun initLayout(view: View?, savedInstanceState: Bundle?)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is BaseActivity) {
            mBaseActivity = context
            mBaseActivity?.onFragmentAttached()
        }
        else if (context is BaseDrawerActivity) {
            mBaseDrawerActivity = context
            mBaseDrawerActivity?.onFragmentAttached()
        }
    }

    override fun onDetach() {
        mBaseActivity = null
        mBaseDrawerActivity = null
        super.onDetach()
    }

    fun getBaseActivity(): BaseActivity? {
        return mBaseActivity
    }

    fun getBaseDrawerActivity(): BaseDrawerActivity? {
        return mBaseDrawerActivity
    }

    fun getActivityComponent(): ActivityComponent? {
        if (mBaseActivity != null)
            return mBaseActivity?.getActivityComponent()
        else if (mBaseDrawerActivity != null)
            return mBaseDrawerActivity?.getActivityComponent()
        return null
    }

    // Not used here
    override fun initDrawer(username: String, fullName: String?, email: String?, profilePic: Bitmap?) {
    }

    override fun isNetworkAvailable(): Boolean {
        if (mBaseActivity != null)
            return mBaseActivity?.isNetworkAvailable()!!
        else
            return mBaseDrawerActivity?.isNetworkAvailable()!!
    }

    interface Callback {

        fun onFragmentAttached()

        fun onFragmentDetached(tag: String)

    }

}