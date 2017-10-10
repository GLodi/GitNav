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

package giuliolodi.gitnav.ui.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.base_activity_drawer.*

/**
 * Created by giulio on 07/10/2017.
 */
class OptionActivity : BaseDrawerActivity() {

    private val OPTION_FRAGMENT_TAG = "OPTION_FRAGMENT_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.option_activity, content_frame)

        var optionFragment: OptionFragment? = supportFragmentManager.findFragmentByTag(OPTION_FRAGMENT_TAG) as OptionFragment?
        if (optionFragment == null) {
            optionFragment = OptionFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.option_activity_frame, optionFragment, OPTION_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(6).subMenu.getItem(0).isChecked = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, OptionActivity::class.java)
        }
    }

}