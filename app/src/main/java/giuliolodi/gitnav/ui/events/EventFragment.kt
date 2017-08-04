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
import giuliolodi.gitnav.utils.EndlessRecyclerViewScrollListener
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
                .subscribe { username -> mPresenter.onImageClick(username) }

        val mScrollListener = object : EndlessRecyclerViewScrollListener(llm) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                mPresenter.loadEvents(isNetworkAvailable())
            }
        }
        event_fragment_rv.setOnScrollListener(mScrollListener)

        event_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        event_fragment_swipe.setOnRefreshListener { mPresenter.onSwipeToRefresh(isNetworkAvailable()) }

       mPresenter.subscribe(isNetworkAvailable())
    }

    override fun showEvents(eventList: List<Event>) {
        (event_fragment_rv.adapter as EventAdapter).addEvents(eventList)
    }

    override fun showLoading() {
        event_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        event_fragment_progress_bar.visibility = View.GONE
        event_fragment_swipe.isRefreshing = false
    }

    override fun showListLoading() {
        (event_fragment_rv.adapter as EventAdapter).addLoading()
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoEvents() {
        event_fragment_no_events.visibility = View.VISIBLE
    }

    override fun hideNoEvents() {
        event_fragment_no_events.visibility = View.VISIBLE
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun clearAdapter() {
        (event_fragment_rv.adapter as EventAdapter).clear()
    }

    override fun intentToUserActivity(username: String) {
        startActivity(UserActivity.getIntent(context).putExtra("username", username))
        activity.overridePendingTransition(0,0)
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