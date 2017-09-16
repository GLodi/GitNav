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

package giuliolodi.gitnav.ui.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity

/**
 * Created by giulio on 10/07/2017.
 */
class RepoActivity : BaseActivity() {

    private val REPO_FRAGMENT_TAG = "REPO_FRAGMENT_TAG"

    private var mRepoFragment: RepoFragment? = null

    private var mBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.repo_activity)

        mRepoFragment = supportFragmentManager.findFragmentByTag(REPO_FRAGMENT_TAG) as RepoFragment?
        if (mRepoFragment == null) {
            mRepoFragment = RepoFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.repo_activity_frame, mRepoFragment, REPO_FRAGMENT_TAG)
                    .commit()
        }
    }

    fun needToBack(back: Boolean) {
        mBack = back
    }

    override fun onBackPressed() {
        if (mBack)
            super.onBackPressed()
        else
            mRepoFragment?.onActivityBackPress()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, RepoActivity::class.java)
        }
    }

}