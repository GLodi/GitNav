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

package giuliolodi.gitnav.ui.search

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.repositorylist.RepoListAdapter
import giuliolodi.gitnav.ui.user.UserActivity
import giuliolodi.gitnav.utils.RxUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.search_fragment.*
import org.eclipse.egit.github.core.CodeSearchResult
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.SearchUser
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by giulio on 25/06/2017.
 */
class SearchFragment : BaseFragment(), SearchContract.View {

    @Inject lateinit var mPresenter: SearchContract.Presenter<SearchContract.View>

    private val mRepoList: MutableList<Repository> = mutableListOf()
    private val mUserList: MutableList<SearchUser> = mutableListOf()
    private val mCodeList: MutableList<CodeSearchResult> = mutableListOf()
    private val mFilter: HashMap<String,String> = HashMap()
    private var mMenu: Menu? = null
    private var LOADING: Boolean = false
    private var TAB_SELECTION: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.search_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.search)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        search_fragment_rv.layoutManager = llm
        search_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        search_fragment_rv.itemAnimator = DefaultItemAnimator()

        getBaseDrawerActivity()?.tab_layout?.visibility = View.VISIBLE
        getBaseDrawerActivity()?.tab_layout?.setSelectedTabIndicatorHeight(0)
        getBaseDrawerActivity()?.tab_layout?.setSelectedTabIndicatorColor(Color.WHITE)
        getBaseDrawerActivity()?.tab_layout?.addTab(getBaseDrawerActivity()?.tab_layout?.newTab()?.setText(getString(R.string.repositories))!!)
        getBaseDrawerActivity()?.tab_layout?.addTab(getBaseDrawerActivity()?.tab_layout?.newTab()?.setText(getString(R.string.users))!!)
        getBaseDrawerActivity()?.tab_layout?.addTab(getBaseDrawerActivity()?.tab_layout?.newTab()?.setText(getString(R.string.code))!!)
        when(TAB_SELECTION) {
            0 -> getBaseDrawerActivity()?.tab_layout?.getTabAt(0)?.select()
            1 -> getBaseDrawerActivity()?.tab_layout?.getTabAt(1)?.select()
            2 -> getBaseDrawerActivity()?.tab_layout?.getTabAt(2)?.select()
        }
        getBaseDrawerActivity()?.tab_layout?.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        TAB_SELECTION = 0
                        mFilter.put("sort", "default")
                        search_fragment_no.visibility = View.GONE
                        search_fragment_rv.adapter = RepoListAdapter()
                        onRepoSearch()
                    }
                    1 -> {
                        TAB_SELECTION = 1
                        mFilter.put("sort", "default")
                        search_fragment_no.visibility = View.GONE
                        search_fragment_rv.adapter = SearchUserAdapter()
                        onUserSearch()
                        (search_fragment_rv.adapter as SearchUserAdapter).getUserClicks()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { username ->
                                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                                    activity.overridePendingTransition(0,0)
                                }
                    }
                    2 -> {
                        TAB_SELECTION = 2
                        search_fragment_no.visibility = View.GONE
                        search_fragment_rv.adapter = SearchCodeAdapter()
                        onCodeSearch()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        (search_fragment_rv.adapter as RepoListAdapter).clear()
                        mRepoList.clear()
                        mPresenter.unsubscribe()
                    }
                    1 -> {
                        (search_fragment_rv.adapter as SearchUserAdapter).clear()
                        mUserList.clear()
                        mPresenter.unsubscribe()
                    }
                    2 -> {
                        (search_fragment_rv.adapter as SearchCodeAdapter).clear()
                        mCodeList.clear()
                        mPresenter.unsubscribe()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        if (mFilter.isEmpty())
            mFilter.put("sort", "default")

        if (!mRepoList.isEmpty()) {
            search_fragment_rv.adapter = RepoListAdapter()
            (search_fragment_rv.adapter as RepoListAdapter).addRepos(mRepoList)
            (search_fragment_rv.adapter as RepoListAdapter).setFilter(mFilter)
        }
        else if (!mUserList.isEmpty()) {
            search_fragment_rv.adapter = SearchUserAdapter()
            (search_fragment_rv.adapter as SearchUserAdapter).addUsers(mUserList)
        }
        else if (!mCodeList.isEmpty()) {
            search_fragment_rv.adapter = SearchCodeAdapter()
            (search_fragment_rv.adapter as SearchCodeAdapter).addCodeList(mCodeList)
        }
        else {
            search_fragment_rv.adapter = RepoListAdapter()
        }
        if (LOADING)
            search_fragment_progress_bar.visibility = View.VISIBLE
    }

    private fun onRepoSearch() {
        mMenu?.findItem(R.id.search_sort_icon)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_default)?.isChecked = true
        mMenu?.findItem(R.id.search_sort_alphabetical)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_updated)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_pushed)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_stars)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_repos)?.isVisible = false
        mMenu?.findItem(R.id.search_sort_followers)?.isVisible = false
    }

    private fun onUserSearch() {
        mMenu?.findItem(R.id.search_sort_icon)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_default)?.isChecked = true
        mMenu?.findItem(R.id.search_sort_alphabetical)?.isVisible = false
        mMenu?.findItem(R.id.search_sort_updated)?.isVisible = false
        mMenu?.findItem(R.id.search_sort_pushed)?.isVisible = false
        mMenu?.findItem(R.id.search_sort_stars)?.isVisible = false
        mMenu?.findItem(R.id.search_sort_repos)?.isVisible = true
        mMenu?.findItem(R.id.search_sort_followers)?.isVisible = true
    }

    private fun onCodeSearch() {
        mMenu?.findItem(R.id.search_sort_icon)?.isVisible = false
    }

    override fun showRepos(repoList: List<Repository>) {
        mRepoList.addAll(repoList)
        (search_fragment_rv.adapter as RepoListAdapter).addRepos(repoList)
        (search_fragment_rv.adapter as RepoListAdapter).setFilter(mFilter)
        if (repoList.isEmpty()) {
            search_fragment_no.visibility = View.VISIBLE
            search_fragment_no.text = getString(R.string.no_repositories)
        }
    }

    override fun showUsers(userList: List<SearchUser>) {
        mUserList.addAll(userList)
        (search_fragment_rv.adapter as SearchUserAdapter).addUsers(userList)
        if (userList.isEmpty()) {
            search_fragment_no.visibility = View.VISIBLE
            search_fragment_no.text = getString(R.string.no_users)
        }
    }

    override fun showCode(codeList: List<CodeSearchResult>) {
        mCodeList.addAll(codeList)
        (search_fragment_rv.adapter as SearchCodeAdapter).addCodeList(codeList)
        if (codeList.isEmpty()) {
            search_fragment_no.visibility = View.VISIBLE
            search_fragment_no.text = getString(R.string.no_code)
        }
    }

    override fun showLoading() {
        search_fragment_progress_bar.visibility = View.VISIBLE
        LOADING = true
    }

    override fun hideLoading() {
        if (search_fragment_progress_bar.visibility == View.VISIBLE)
            search_fragment_progress_bar.visibility = View.GONE
        LOADING = false
    }

    override fun showError(error: String) {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        mMenu = menu
        inflater?.inflate(R.menu.search_menu, mMenu)
        val searchView = MenuItemCompat.getActionView(menu?.findItem(R.id.action_search)) as SearchView
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        RxUtils.fromSearchView(searchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter  { it -> it.length > 2 }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { query ->
                            when (getBaseDrawerActivity()?.tab_layout?.selectedTabPosition) {
                                0 -> {
                                    (search_fragment_rv.adapter as RepoListAdapter).clear()
                                    mRepoList.clear()
                                    search_fragment_no.visibility = View.GONE
                                    mPresenter.onSearchRepos(query, mFilter)
                                }
                                1 -> {
                                    (search_fragment_rv.adapter as SearchUserAdapter).clear()
                                    mUserList.clear()
                                    search_fragment_no.visibility = View.GONE
                                    mPresenter.onSearchUsers(query, mFilter)
                                }
                                2 -> {
                                    (search_fragment_rv.adapter as SearchCodeAdapter).clear()
                                    mCodeList.clear()
                                    search_fragment_no.visibility = View.GONE
                                    mPresenter.onSearchCode(query)
                                }
                            }
                        },
                        { throwable ->
                            hideLoading()
                            showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                )
        when (mFilter["sort"]) {
            "default" -> mMenu?.findItem(R.id.search_sort_default)?.isChecked = true
            "alphabetical" -> mMenu?.findItem(R.id.search_sort_alphabetical)?.isChecked = true
            "updated" -> mMenu?.findItem(R.id.search_sort_updated)?.isChecked = true
            "pushed" -> mMenu?.findItem(R.id.search_sort_pushed)?.isChecked = true
            "stars" -> mMenu?.findItem(R.id.search_sort_stars)?.isChecked = true
            "repos" -> mMenu?.findItem(R.id.search_sort_repos)?.isChecked = true
            "followers" -> mMenu?.findItem(R.id.search_sort_followers)?.isChecked = true
        }
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.search_sort_default -> {
                    item.isChecked = true
                    mFilter.put("sort", "default")
                }
                R.id.search_sort_updated -> {
                    item.isChecked = true
                    mFilter.put("sort", "updated")
                }
                R.id.search_sort_pushed -> {
                    item.isChecked = true
                    mFilter.put("sort", "pushed")
                }
                R.id.search_sort_alphabetical -> {
                    item.isChecked = true
                    mFilter.put("sort", "full_name")
                }
                R.id.search_sort_stars -> {
                    item.isChecked = true
                    mFilter.put("sort", "stars")
                }
                R.id.search_sort_repos -> {
                    item.isChecked = true
                    mFilter.put("sort", "repos")
                }
                R.id.search_sort_followers -> {
                    item.isChecked = true
                    mFilter.put("sort", "followers")
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