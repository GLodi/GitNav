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

package giuliolodi.gitnav.ui.gistlist

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.adapters.GistListAdapter
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.gist.GistActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gist_list_fragment.*
import org.eclipse.egit.github.core.Gist
import javax.inject.Inject

/**
 * Created by giulio on 27/06/2017.
 */
class GistListFragment : BaseFragment(), GistListContract.View {

    @Inject lateinit var mPresenter: GistListContract.Presenter<GistListContract.View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.gist_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.gists)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        gist_list_fragment_rv.layoutManager = llm
        gist_list_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        gist_list_fragment_rv.itemAnimator = DefaultItemAnimator()
        gist_list_fragment_rv.adapter = GistListAdapter()
        (gist_list_fragment_rv.adapter as GistListAdapter).getGistClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { gistId -> mPresenter.onGistClick(gistId) }

        val mScrollListenerStarred = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (gist_list_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (gist_list_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (gist_list_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mPresenter.onLastItemVisible(isNetworkAvailable(), dy)
                }
            }
        }
        gist_list_fragment_rv.setOnScrollListener(mScrollListenerStarred)

        gist_list_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        gist_list_fragment_swipe.setOnRefreshListener { mPresenter.onSwipeToRefresh(isNetworkAvailable()) }

        gist_list_fragment_bottomview.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.gist_list_fragment_bottom_menu_mine -> { mPresenter.onBottomViewMineGistClick(isNetworkAvailable()) }
                R.id.gist_list_fragment_bottom_menu_starred -> { mPresenter.onBottomViewStarredGistClick(isNetworkAvailable()) }
            }
            true
        }

        mPresenter.subscribe(isNetworkAvailable())
    }

    override fun showGists(gistList: List<Gist>) {
        (gist_list_fragment_rv.adapter as GistListAdapter).addGists(gistList)
    }

    override fun showLoading() {
        gist_list_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        gist_list_fragment_progress_bar.visibility = View.GONE
        gist_list_fragment_swipe.isRefreshing = false
    }

    override fun showListLoading() {
        (gist_list_fragment_rv.adapter as GistListAdapter).showLoading()
    }

    override fun hideListLoading() {
        (gist_list_fragment_rv.adapter as GistListAdapter).hideLoading()
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun setAdapterAndClickListener() {
        gist_list_fragment_rv.adapter = GistListAdapter()
        (gist_list_fragment_rv.adapter as GistListAdapter).getGistClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { gistId -> mPresenter.onGistClick(gistId) }
    }

    override fun clearAdapter() {
        (gist_list_fragment_rv.adapter as GistListAdapter).clear()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun showNoGists(mineOrStarred: String) {
        gist_list_fragment_no.visibility = View.VISIBLE
        when(mineOrStarred) {
            "mine" -> gist_list_fragment_no.text = getString(R.string.no_gists_mine)
            "starred" -> gist_list_fragment_no.text = getString(R.string.no_gists_starred)
        }
    }

    override fun hideNoGists() {
        gist_list_fragment_no.visibility = View.GONE
    }

    override fun intentToGistActivitiy(gistId: String) {
        startActivity(GistActivity.getIntent(context).putExtra("gistId", gistId))
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