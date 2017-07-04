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

package giuliolodi.gitnav.ui.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.base_activity_drawer.*

/**
 * Created by giulio on 15/05/2017.
 */
class EventActivity : BaseDrawerActivity() {

    private val EVENT_FRAGMENT_TAG = "EVENT_FRAGMENT_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.event_activity, content_frame)

        var eventFragment: EventFragment? = supportFragmentManager.findFragmentByTag(EVENT_FRAGMENT_TAG) as EventFragment?
        if (eventFragment == null) {
            eventFragment = EventFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.event_activity_frame, eventFragment, EVENT_FRAGMENT_TAG)
                    .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(0).isChecked = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, EventActivity::class.java)
        }
    }

}
