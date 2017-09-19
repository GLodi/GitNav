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

package giuliolodi.gitnav.ui.forklist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity

/**
 * Created by giulio on 19/09/2017.
 */
class ForkListActivity : BaseActivity() {
    
    private val FORK_LIST_FRAGMENT_TAG = "FORK_LIST_FRAGMENT_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fork_list_activity)

        var forkListFragment: ForkListFragment? = supportFragmentManager.findFragmentByTag(FORK_LIST_FRAGMENT_TAG) as ForkListFragment?
        if (forkListFragment == null) {
            forkListFragment = ForkListFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fork_list_activity_frame, forkListFragment, FORK_LIST_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ForkListActivity::class.java)
        }
    }
    
}