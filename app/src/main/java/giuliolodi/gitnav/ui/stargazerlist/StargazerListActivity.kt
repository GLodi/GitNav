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

package giuliolodi.gitnav.ui.stargazerlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import kotlinx.android.synthetic.main.base_activity.*

/**
 * Created by giulio on 25/08/2017.
 */
class StargazerListActivity : BaseActivity() {

    private val USER_LIST_FRAGMENT_TAG = "USER_LIST_FRAGMENT_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.stargazer_list_activity, content_frame)

        var stargazerListFragment: StargazerListFragment? = supportFragmentManager.findFragmentByTag(USER_LIST_FRAGMENT_TAG) as StargazerListFragment?
        if (stargazerListFragment == null) {
            stargazerListFragment = StargazerListFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.stargazer_list_activity_frame, stargazerListFragment, USER_LIST_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, StargazerListActivity::class.java)
        }
    }
    
}