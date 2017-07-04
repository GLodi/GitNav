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

package giuliolodi.gitnav.ui.starred

import android.content.Context
import android.content.Intent
import android.os.Bundle
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.base_activity_drawer.*

/**
 * Created by giulio on 19/05/2017.
 */
class StarredActivity : BaseDrawerActivity() {

    private val STARRED_FRAGMENT_TAG = "STARRED_FRAGMENT_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.starred_activity, content_frame)

        var starredFragment: StarredFragment? = supportFragmentManager.findFragmentByTag(STARRED_FRAGMENT_TAG) as StarredFragment?
        if (starredFragment == null) {
            starredFragment = StarredFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.starred_activity_frame, starredFragment, STARRED_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(2).isChecked = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, StarredActivity::class.java)
        }
    }

}