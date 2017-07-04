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

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.event_fragment.*
import org.eclipse.egit.github.core.event.Event
import javax.inject.Inject

/**
 * Created by giulio on 24/06/2017.
 */
class EventFragment : BaseFragment(), EventContract.View {

    @Inject lateinit var mPresenter: EventContract.Presenter<EventContract.View>

    private var mEventList: MutableList<Event> = mutableListOf()
    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false
    private var LOADING_MAIN = false
    private var NO_SHOWING: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.event_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.events)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL

        event_fragment_rv.layoutManager = llm
        event_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        event_fragment_rv.itemAnimator = DefaultItemAnimator()
        event_fragment_rv.adapter = EventAdapter()

        (event_fragment_rv.adapter as EventAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0,0)
                }

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING)
                    return
                val visibleItemCount = (event_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (event_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (event_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING = true
                        PAGE_N += 1
                        (event_fragment_rv.adapter as EventAdapter).addLoading()
                        mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                        hideLoading()
                    }
                }
            }
        }
        event_fragment_rv.setOnScrollListener(mScrollListener)

        event_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        event_fragment_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                hideNoEvents()
                PAGE_N = 1
                (event_fragment_rv.adapter as EventAdapter).clear()
                mEventList.clear()
                LOADING_MAIN = true
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }

        if (!mEventList.isEmpty()) (event_fragment_rv.adapter as EventAdapter).addEvents(mEventList)
        else if (NO_SHOWING) showNoEvents()
        else if (LOADING_MAIN) showLoading()
        else {
            if (isNetworkAvailable()) {
                showLoading()
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }
    }

    override fun showEvents(eventList: List<Event>) {
        mEventList.addAll(eventList)
        (event_fragment_rv.adapter as EventAdapter).addEvents(eventList)
        if (PAGE_N == 1 && eventList.isEmpty()) showNoEvents()
        LOADING = false
    }

    override fun showLoading() {
        event_fragment_progress_bar.visibility = View.VISIBLE
        LOADING_MAIN = true
    }

    override fun hideLoading() {
        if (event_fragment_progress_bar.isShown)
            event_fragment_progress_bar.visibility = View.GONE
        if (event_fragment_swipe.isRefreshing)
            event_fragment_swipe.isRefreshing = false
        LOADING_MAIN = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    private fun showNoEvents() {
        event_fragment_no_events.visibility = View.VISIBLE
        NO_SHOWING = true
    }

    private fun hideNoEvents() {
        if (event_fragment_no_events.visibility == View.VISIBLE)
            event_fragment_no_events.visibility = View.VISIBLE
        NO_SHOWING = false
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        mPresenter.onDetachView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
    }

}