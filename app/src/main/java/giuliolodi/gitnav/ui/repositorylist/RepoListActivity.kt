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

package giuliolodi.gitnav.ui.repositorylist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.activity_base_drawer.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.repo_list_activity.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 18/05/2017.
 */

class RepoListActivity : BaseDrawerActivity(), RepoListContract.View {

    @Inject lateinit var mPresenter: RepoListContract.Presenter<RepoListContract.View>

    private var mFilter: HashMap<String,String> = HashMap()
    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.repo_list_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        mFilter.put("sort", "created")

        if (isNetworkAvailable()) {
            showLoading()
            mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
        }
        else {
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            hideLoading()
        }
    }

    private fun initLayout() {
        supportActionBar?.title = getString(R.string.repositories)

        val llm = LinearLayoutManager(applicationContext)
        llm.orientation = LinearLayoutManager.VERTICAL

        repo_list_activity_rv.layoutManager = llm
        repo_list_activity_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        repo_list_activity_rv.itemAnimator = DefaultItemAnimator()
        repo_list_activity_rv.adapter = RepoListAdapter()

        setupOnScrollListener()

        repo_list_activity_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        repo_list_activity_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                PAGE_N = 1
                (repo_list_activity_rv.adapter as RepoListAdapter).clear()
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
            }
            else {
                Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }
    }

    override fun showRepos(repoList: List<Repository>) {
        (repo_list_activity_rv.adapter as RepoListAdapter).addRepos(repoList)
        (repo_list_activity_rv.adapter as RepoListAdapter).setFilter(mFilter)
        if (PAGE_N == 1 && repoList.isEmpty()) repo_list_activity_no_repo.visibility = View.VISIBLE
        LOADING = false
    }

    override fun showLoading() {
        repo_list_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (repo_list_activity_progress_bar.visibility == View.VISIBLE)
            repo_list_activity_progress_bar.visibility = View.GONE
        if (repo_list_activity_swipe.isRefreshing)
            repo_list_activity_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    private fun setupOnScrollListener() {
        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING || mFilter["sort"] == "stars")
                    return
                val visibleItemCount = (repo_list_activity_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (repo_list_activity_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (repo_list_activity_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING = true
                        PAGE_N += 1
                        (repo_list_activity_rv.adapter as RepoListAdapter).addLoading()
                        mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                        hideLoading()
                    }
                }
            }
        }
        repo_list_activity_rv.setOnScrollListener(mScrollListener)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.repo_sort_created -> {
                    item.isChecked = true
                    mFilter.put("sort", "created")
                    PAGE_N = 1
                    (repo_list_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_updated -> {
                    item.isChecked = true
                    mFilter.put("sort", "updated")
                    PAGE_N = 1
                    (repo_list_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_pushed -> {
                    item.isChecked = true
                    mFilter.put("sort", "pushed")
                    PAGE_N = 1
                    (repo_list_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_alphabetical -> {
                    item.isChecked = true
                    mFilter.put("sort", "full_name")
                    PAGE_N = 1
                    (repo_list_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_stars -> {
                    item.isChecked = true
                    mFilter.put("sort", "stars")
                    PAGE_N = 1
                    (repo_list_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
            }
        }
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(1).isChecked = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.repo_list_sort_menu, menu)
        super.onCreateOptionsMenu(menu)
        return true
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
            return Intent(context, RepoListActivity::class.java)
        }
    }

}