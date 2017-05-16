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
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.event_activity.*
import org.eclipse.egit.github.core.event.Event
import javax.inject.Inject

/**
 * Created by giulio on 15/05/2017.
 */

class EventActivity : BaseDrawerActivity(), EventContract.View {

    @Inject lateinit var mPresenter: EventContract.Presenter<EventContract.View>

    private val DOWNLOAD_PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private val LOADING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.event_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        showLoading()
        mPresenter.subscribe(DOWNLOAD_PAGE_N, ITEMS_PER_PAGE)
    }

    private fun initLayout() {
        supportActionBar?.title = getString(R.string.events)

        val llm = LinearLayoutManager(applicationContext)
        llm.orientation = LinearLayoutManager.VERTICAL

        event_activity_rv.layoutManager = llm
        event_activity_rv.addItemDecoration(DividerItemDecoration(event_activity_rv.context, llm.orientation))
        event_activity_rv.itemAnimator = DefaultItemAnimator()
        event_activity_rv.adapter = EventAdapter()
    }

    override fun addEvents(eventList: List<Event>) {
        (event_activity_rv.adapter as EventAdapter).addEvents(eventList)
    }

    override fun showLoading() {
        event_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (event_activity_progress_bar.isShown)
            event_activity_progress_bar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, EventActivity::class.java)
        }
    }

}
