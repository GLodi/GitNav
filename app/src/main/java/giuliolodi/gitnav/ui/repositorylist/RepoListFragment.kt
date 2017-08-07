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
import giuliolodi.gitnav.ui.repository.RepoActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.repo_list_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 24/06/2017.
 */
class RepoListFragment : BaseFragment(), RepoListContract.View {

    @Inject lateinit var mPresenter: RepoListContract.Presenter<RepoListContract.View>

    private var mMenuItem: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
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
        (repo_list_fragment_rv.adapter as RepoListAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repo -> mPresenter.onRepoClick(repo.owner.login, repo.name) }

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (repo_list_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (repo_list_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (repo_list_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mPresenter.onLastItemVisible(isNetworkAvailable(), dy)
                }
            }
        }
        repo_list_fragment_rv.setOnScrollListener(mScrollListener)

        repo_list_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        repo_list_fragment_swipe.setOnRefreshListener { mPresenter.onSwipeToRefresh(isNetworkAvailable()) }

        mPresenter.subscribe(isNetworkAvailable())
    }

    override fun showRepos(repoList: List<Repository>) {
        (repo_list_fragment_rv.adapter as RepoListAdapter).addRepos(repoList)
    }

    override fun showLoading() {
        repo_list_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        repo_list_fragment_progress_bar.visibility = View.GONE
        repo_list_fragment_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoRepo() {
        repo_list_fragment_no_repo.visibility = View.VISIBLE
    }

    override fun hideNoRepo() {
        repo_list_fragment_no_repo.visibility = View.GONE
    }

    override fun showListLoading() {
        (repo_list_fragment_rv.adapter as RepoListAdapter).showLoading()
    }

    override fun hideListLoading() {
        (repo_list_fragment_rv.adapter as RepoListAdapter).hideLoading()
    }

    override fun setFilter(filter: HashMap<String, String>) {
        (repo_list_fragment_rv.adapter as RepoListAdapter).setFilter(filter)
    }

    override fun clearAdapter() {
        (repo_list_fragment_rv.adapter as RepoListAdapter).clear()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun intentToRepoActivity(repoOwner: String, repoName: String) {
        startActivity(RepoActivity.getIntent(context).putExtra("owner", repoOwner).putExtra("name", repoName))
        activity.overridePendingTransition(0,0)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.repo_list_sort_menu, menu)
        mMenuItem?.let { menu?.findItem(it)?.isChecked = true }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        else if (isNetworkAvailable()) {
            item?.let { mMenuItem = it.itemId }
            when (item?.itemId) {
                R.id.repo_sort_created -> {
                    item.isChecked = true
                    mPresenter.onSortCreatedClick(isNetworkAvailable())
                }
                R.id.repo_sort_updated -> {
                    item.isChecked = true
                    mPresenter.onSortUpdatedClick(isNetworkAvailable())
                }
                R.id.repo_sort_pushed -> {
                    item.isChecked = true
                    mPresenter.onSortPushedClick(isNetworkAvailable())
                }
                R.id.repo_sort_alphabetical -> {
                    item.isChecked = true
                    mPresenter.onSortAlphabeticalClick(isNetworkAvailable())
                }
                R.id.repo_sort_stars -> {
                    item.isChecked = true
                    mPresenter.onSortStarsClick(isNetworkAvailable())
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