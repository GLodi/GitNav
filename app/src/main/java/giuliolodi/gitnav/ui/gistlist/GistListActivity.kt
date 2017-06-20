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

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import org.eclipse.egit.github.core.Gist
import javax.inject.Inject
import giuliolodi.gitnav.R.string.network_error
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.gist.GistActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.base_activity_drawer.*
import kotlinx.android.synthetic.main.gist_list_activity.*
import kotlinx.android.synthetic.main.gist_list_mine.*
import kotlinx.android.synthetic.main.gist_list_starred.*

/**
 * Created by giulio on 23/05/2017.
 */

class GistListActivity : BaseDrawerActivity(), GistListContract.View {

    @Inject lateinit var mPresenter: GistListContract.Presenter<GistListContract.View>

    private val mViews: MutableList<Int> = arrayListOf()

    private var PAGE_N_MINE = 1
    private var ITEMS_PER_PAGE_MINE = 10
    private var LOADING_MINE: Boolean = false

    private var PAGE_N_STARRED = 1
    private var ITEMS_PER_PAGE_STARRED = 10
    private var LOADING_STARRED: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.gist_list_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)
    }

    private fun initLayout() {
        supportActionBar?.title = getString(R.string.gists)

        mViews.add(R.layout.gist_list_mine)
        mViews.add(R.layout.gist_list_starred)

        gist_list_viewpager.offscreenPageLimit = 2
        gist_list_viewpager.adapter = MyAdapter(applicationContext, mViews)

        tab_layout.visibility = View.VISIBLE
        tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        tab_layout.setupWithViewPager(gist_list_viewpager)
    }

    override fun showMineGists(gistList: List<Gist>) {
        (gist_list_mine_rv.adapter as GistListAdapter).addGists(gistList)
        if (PAGE_N_MINE == 1 && gistList.isEmpty()) gist_list_mine_no.visibility = View.VISIBLE
        LOADING_MINE = false
    }

    override fun showStarredGists(gistList: List<Gist>) {
        (gist_list_starred_rv.adapter as GistListAdapter).addGists(gistList)
        if (PAGE_N_STARRED == 1 && gistList.isEmpty()) gist_list_starred_no.visibility = View.VISIBLE
        LOADING_STARRED = false
    }

    override fun showLoadingMine() {
        gist_list_mine_progress_bar.visibility = View.VISIBLE
    }

    override fun showLoadingStarred() {
        gist_list_starred_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoadingMine() {
        if (gist_list_mine_progress_bar.visibility == View.VISIBLE)
            gist_list_mine_progress_bar.visibility = View.GONE
    }

    override fun hideLoadingStarred() {
        if (gist_list_starred_progress_bar.visibility == View.VISIBLE)
            gist_list_starred_progress_bar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    private class MyAdapter(context: Context, views: List<Int>) : PagerAdapter() {

        private var mContext = context
        private var mViews = views

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            val layout = LayoutInflater.from(mContext).inflate(mViews[position], container, false)
            container?.addView(layout)
            return layout
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.getString(R.string.mine)
                1 -> return mContext.getString(R.string.starred)
            }
            return super.getPageTitle(position)
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            container?.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view?.equals(`object`)!!
        }

        override fun getCount(): Int {
            return 2
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (isNetworkAvailable()) {
            val llmMine = LinearLayoutManager(applicationContext)
            llmMine.orientation = LinearLayoutManager.VERTICAL
            gist_list_mine_rv.layoutManager = llmMine
            gist_list_mine_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
            gist_list_mine_rv.itemAnimator = DefaultItemAnimator()
            gist_list_mine_rv.adapter = GistListAdapter()

            val llmStarred = LinearLayoutManager(applicationContext)
            llmStarred.orientation = LinearLayoutManager.VERTICAL
            gist_list_starred_rv.layoutManager = llmStarred
            gist_list_starred_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
            gist_list_starred_rv.itemAnimator = DefaultItemAnimator()
            gist_list_starred_rv.adapter = GistListAdapter()

            setupOnScrollListener()

            mPresenter.getMineGists(PAGE_N_MINE, ITEMS_PER_PAGE_MINE)
            mPresenter.getStarredGists(PAGE_N_STARRED, ITEMS_PER_PAGE_STARRED)

            (gist_list_mine_rv.adapter as GistListAdapter).getPositionClicks()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { gistId ->
                        startActivity(GistActivity.getIntent(applicationContext).putExtra("gistId", gistId))
                        overridePendingTransition(0,0)
                    }

            (gist_list_starred_rv.adapter as GistListAdapter).getPositionClicks()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { gistId ->
                        startActivity(GistActivity.getIntent(applicationContext).putExtra("gistId", gistId))
                        overridePendingTransition(0,0)
                    }
        } else
            Toasty.warning(applicationContext, getString(network_error), Toast.LENGTH_LONG).show()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupOnScrollListener() {
        val mScrollListenerMine = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_MINE)
                    return
                val visibleItemCount = (gist_list_mine_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (gist_list_mine_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (gist_list_mine_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_MINE = true
                        PAGE_N_MINE += 1
                        (gist_list_mine_rv.adapter as GistListAdapter).addLoading()
                        mPresenter.getMineGists(PAGE_N_MINE, ITEMS_PER_PAGE_MINE)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        gist_list_mine_rv.setOnScrollListener(mScrollListenerMine)
        val mScrollListenerStarred = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_STARRED)
                    return
                val visibleItemCount = (gist_list_starred_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (gist_list_starred_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (gist_list_starred_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_STARRED= true
                        PAGE_N_STARRED += 1
                        (gist_list_starred_rv.adapter as GistListAdapter).addLoading()
                        mPresenter.getStarredGists(PAGE_N_STARRED, ITEMS_PER_PAGE_STARRED)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        gist_list_starred_rv.setOnScrollListener(mScrollListenerStarred)
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(5).isChecked = true
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
            return Intent(context, GistListActivity::class.java)
        }
    }
}