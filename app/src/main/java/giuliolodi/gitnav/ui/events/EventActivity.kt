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
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.event_activity.*
import org.eclipse.egit.github.core.event.Event
import javax.inject.Inject
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import giuliolodi.gitnav.ui.user.UserActivity2
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.base_activity_drawer.*

/**
 * Created by giulio on 15/05/2017.
 */

class EventActivity : BaseDrawerActivity(), EventContract.View {

    @Inject lateinit var mPresenter: EventContract.Presenter<EventContract.View>

    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.event_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (isNetworkAvailable()) {
            showLoading()
            mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE)
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
        event_activity_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        event_activity_rv.itemAnimator = DefaultItemAnimator()
        event_activity_rv.adapter = EventAdapter()

        (event_activity_rv.adapter as EventAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity2.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        setupOnScrollListener()

        event_activity_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        event_activity_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                PAGE_N = 1
                (event_activity_rv.adapter as EventAdapter).clear()
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE)
            }
            else {
                Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }
    }

    override fun showEvents(eventList: List<Event>) {
        (event_activity_rv.adapter as EventAdapter).addEvents(eventList)
        if (PAGE_N == 1 && eventList.isEmpty()) event_activity_no_events.visibility = View.VISIBLE
        LOADING = false
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
                    if (isNetworkAvailable()) {
                        LOADING = true
                        PAGE_N += 1
                        (event_activity_rv.adapter as EventAdapter).addLoading()
                        mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(0).isChecked = true
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
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
