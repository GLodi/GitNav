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

    private var mGistList: MutableList<Gist> = mutableListOf()
    private var PAGE_N = 1
    private var ITEMS_PER_PAGE = 20
    private var LOADING: Boolean = false
    private var LOADING_MAIN: Boolean = false
    private var MINE_STARRED: String = "mine"
    private var NO_SHOWING: Boolean = false

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

        (gist_list_fragment_rv.adapter as GistListAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { gistId ->
                    startActivity(GistActivity.getIntent(context).putExtra("gistId", gistId))
                    activity.overridePendingTransition(0,0)
                }

        val mScrollListenerStarred = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING)
                    return
                val visibleItemCount = (gist_list_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (gist_list_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (gist_list_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING = true
                        PAGE_N += 1
                        (gist_list_fragment_rv.adapter as GistListAdapter).addLoading()
                        when (MINE_STARRED) {
                            "mine" -> mPresenter.getMineGists(PAGE_N, ITEMS_PER_PAGE)
                            "starred" -> mPresenter.getStarredGists(PAGE_N, ITEMS_PER_PAGE)
                        }
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                        hideLoading()
                    }
                }
            }
        }
        gist_list_fragment_rv.setOnScrollListener(mScrollListenerStarred)

        gist_list_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        gist_list_fragment_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                hideNoGists()
                PAGE_N = 1
                (gist_list_fragment_rv.adapter as GistListAdapter).clear()
                mGistList.clear()
                LOADING_MAIN = true
                when (MINE_STARRED) {
                    "mine" -> mPresenter.getMineGists(PAGE_N, ITEMS_PER_PAGE)
                    "starred" -> mPresenter.getStarredGists(PAGE_N, ITEMS_PER_PAGE)
                }
            } else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }

        gist_list_fragment_bottomview.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.gist_list_fragment_bottom_menu_mine -> {
                    showLoading()
                    hideNoGists()
                    PAGE_N = 1
                    gist_list_fragment_rv.adapter = GistListAdapter()
                    (gist_list_fragment_rv.adapter as GistListAdapter).clear()
                    mGistList.clear()
                    MINE_STARRED = "mine"
                    mPresenter.getMineGists(PAGE_N, ITEMS_PER_PAGE)
                }
                R.id.gist_list_fragment_bottom_menu_starred -> {
                    showLoading()
                    hideNoGists()
                    PAGE_N = 1
                    gist_list_fragment_rv.adapter = GistListAdapter()
                    (gist_list_fragment_rv.adapter as GistListAdapter).clear()
                    mGistList.clear()
                    MINE_STARRED = "starred"
                    mPresenter.getStarredGists(PAGE_N, ITEMS_PER_PAGE)
                }
            }
            true
        }

        if (!mGistList.isEmpty()) (gist_list_fragment_rv.adapter as GistListAdapter).addGists(mGistList)
        else if (LOADING_MAIN) showLoading()
        else if (NO_SHOWING) showNoGists()
        else {
            if (isNetworkAvailable()) {
                showLoading()
                when (MINE_STARRED) {
                    "mine" -> mPresenter.getMineGists(PAGE_N, ITEMS_PER_PAGE)
                    "starred" -> mPresenter.getStarredGists(PAGE_N, ITEMS_PER_PAGE)
                }
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }
    }

    override fun showGists(gistList: List<Gist>) {
        mGistList.addAll(gistList)
        (gist_list_fragment_rv.adapter as GistListAdapter).addGists(gistList)
        if (PAGE_N == 1 && gistList.isEmpty()) showNoGists()
        LOADING = false
    }

    override fun showLoading() {
        gist_list_fragment_progress_bar.visibility = View.VISIBLE
        LOADING_MAIN = true
    }

    override fun hideLoading() {
        if (gist_list_fragment_progress_bar.visibility == View.VISIBLE)
            gist_list_fragment_progress_bar.visibility = View.GONE
        if (gist_list_fragment_swipe.isRefreshing)
            gist_list_fragment_swipe.isRefreshing = false
        LOADING_MAIN = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    private fun showNoGists() {
        gist_list_fragment_no.visibility = View.VISIBLE
        when(MINE_STARRED) {
            "mine" -> gist_list_fragment_no.text = getString(R.string.no_gists_mine)
            "starred" -> gist_list_fragment_no.text = getString(R.string.no_gists_starred)
        }
        NO_SHOWING = true
    }

    private fun hideNoGists() {
        gist_list_fragment_no.visibility = View.GONE
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