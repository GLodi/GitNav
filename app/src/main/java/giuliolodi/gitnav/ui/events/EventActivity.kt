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
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.support.v7.widget.RecyclerView
import giuliolodi.gitnav.utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_base_drawer.*
import android.os.Looper.getMainLooper



/**
 * Created by giulio on 15/05/2017.
 */

class EventActivity : BaseDrawerActivity(), EventContract.View {

    @Inject lateinit var mPresenter: EventContract.Presenter<EventContract.View>

    private var DOWNLOAD_PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.event_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (NetworkUtils.isNetworkAvailable(applicationContext)) {
            showLoading()
            mPresenter.subscribe(DOWNLOAD_PAGE_N, ITEMS_PER_PAGE)
        }
        else {
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            hideLoading()
        }
    }

    private fun initLayout() {
        supportActionBar?.title = getString(R.string.events)

        val llm = LinearLayoutManager(applicationContext)
        llm.orientation = LinearLayoutManager.VERTICAL

        event_activity_rv.layoutManager = llm
        event_activity_rv.addItemDecoration(DividerItemDecoration(event_activity_rv.context, llm.orientation))
        event_activity_rv.itemAnimator = DefaultItemAnimator()
        event_activity_rv.adapter = EventAdapter()

        setupOnScrollListener()

        event_activity_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        event_activity_swipe.setOnRefreshListener {
            if (NetworkUtils.isNetworkAvailable(applicationContext)) {
                DOWNLOAD_PAGE_N = 1
                (event_activity_rv.adapter as EventAdapter).clear()
                mPresenter.subscribe(DOWNLOAD_PAGE_N, ITEMS_PER_PAGE)
            }
            else {
                Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }
    }

    override fun addEvents(eventList: List<Event>) {
        LOADING = false
        (event_activity_rv.adapter as EventAdapter).addEvents(eventList)
    }

    override fun showLoading() {
        event_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (event_activity_progress_bar.isShown)
            event_activity_progress_bar.visibility = View.GONE
        if (event_activity_swipe.isRefreshing)
            event_activity_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    private fun setupOnScrollListener() {

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING)
                    return
                val visibleItemCount = (event_activity_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (event_activity_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (event_activity_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (NetworkUtils.isNetworkAvailable(applicationContext)) {
                        LOADING = true
                        DOWNLOAD_PAGE_N += 1
                        (event_activity_rv.adapter as EventAdapter).addLoading()
                        mPresenter.subscribe(DOWNLOAD_PAGE_N, ITEMS_PER_PAGE)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                        hideLoading()
                    }
                }
            }
        }

        event_activity_rv.setOnScrollListener(mScrollListener)

    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(0).isChecked = true
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, EventActivity::class.java)
        }
    }

}
