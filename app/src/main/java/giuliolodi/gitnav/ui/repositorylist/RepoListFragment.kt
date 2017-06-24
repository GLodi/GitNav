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
import kotlinx.android.synthetic.main.repo_list_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 24/06/2017.
 */

class RepoListFragment : BaseFragment(), RepoListContract.View {

    @Inject lateinit var mPresenter: RepoListContract.Presenter<RepoListContract.View>

    private var mRepoList: MutableList<Repository> = mutableListOf()
    private var mFilter: HashMap<String,String> = HashMap()
    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false
    private var SORT_OPTION: String = "created"
    private var mMenuItem: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)

        mFilter.put("sort", "created")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.repo_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.repositories)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        repo_list_fragment_rv.layoutManager = llm
        repo_list_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        repo_list_fragment_rv.itemAnimator = DefaultItemAnimator()
        repo_list_fragment_rv.adapter = RepoListAdapter()

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING || mFilter["sort"] == "stars")
                    return
                val visibleItemCount = (repo_list_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (repo_list_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (repo_list_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING = true
                        PAGE_N += 1
                        (repo_list_fragment_rv.adapter as RepoListAdapter).addLoading()
                        mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                        hideLoading()
                    }
                }
            }
        }
        repo_list_fragment_rv.setOnScrollListener(mScrollListener)

        repo_list_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        repo_list_fragment_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                PAGE_N = 1
                (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
                mRepoList.clear()
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }

        if (!mRepoList.isEmpty()) {
            (repo_list_fragment_rv.adapter as RepoListAdapter).addRepos(mRepoList)
            (repo_list_fragment_rv.adapter as RepoListAdapter).setFilter(mFilter)
        }
        else {
            if (isNetworkAvailable()) {
                showLoading()
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }

    }

    override fun showRepos(repoList: List<Repository>) {
        mRepoList.addAll(repoList)
        (repo_list_fragment_rv.adapter as RepoListAdapter).addRepos(repoList)
        (repo_list_fragment_rv.adapter as RepoListAdapter).setFilter(mFilter)
        if (PAGE_N == 1 && repoList.isEmpty()) repo_list_fragment_no_repo.visibility = View.VISIBLE
        LOADING = false
    }

    override fun showLoading() {
        repo_list_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (repo_list_fragment_progress_bar.visibility == View.VISIBLE)
            repo_list_fragment_progress_bar.visibility = View.GONE
        if (repo_list_fragment_swipe.isRefreshing)
            repo_list_fragment_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.repo_list_sort_menu, menu)
        mMenuItem?.let { menu?.findItem(it)?.isChecked = true }
        super.onCreateOptionsMenu(menu, inflater)
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
                    (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
                    mRepoList.clear()
                    SORT_OPTION = "created"
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_updated -> {
                    item.isChecked = true
                    mFilter.put("sort", "updated")
                    PAGE_N = 1
                    (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
                    mRepoList.clear()
                    SORT_OPTION = "updated"
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_pushed -> {
                    item.isChecked = true
                    mFilter.put("sort", "pushed")
                    PAGE_N = 1
                    (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
                    mRepoList.clear()
                    SORT_OPTION = "pushed"
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_alphabetical -> {
                    item.isChecked = true
                    mFilter.put("sort", "full_name")
                    PAGE_N = 1
                    (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
                    mRepoList.clear()
                    SORT_OPTION = "full_name"
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.repo_sort_stars -> {
                    item.isChecked = true
                    mFilter.put("sort", "stars")
                    PAGE_N = 1
                    (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
                    mRepoList.clear()
                    SORT_OPTION = "stars"
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
            }
        }
        else
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
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