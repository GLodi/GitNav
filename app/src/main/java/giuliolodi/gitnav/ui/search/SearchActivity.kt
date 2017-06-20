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
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.View
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.search_activity.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.view.MenuItem
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.ui.repositorylist.RepoListAdapter
import giuliolodi.gitnav.ui.user.UserActivity2
import giuliolodi.gitnav.utils.RxUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.base_activity.*
import org.eclipse.egit.github.core.CodeSearchResult
import org.eclipse.egit.github.core.SearchUser
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by giulio on 26/05/2017.
 */

class SearchActivity : BaseDrawerActivity(), SearchContract.View {

    @Inject lateinit var mPresenter: SearchContract.Presenter<SearchContract.View>

    private val mFilter: HashMap<String,String> = HashMap()
    private var mMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.search_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)
    }

    private fun initLayout() {
        supportActionBar?.title = getString(R.string.search)

        val llm = LinearLayoutManager(applicationContext)
        llm.orientation = LinearLayoutManager.VERTICAL
        search_activity_rv.layoutManager = llm
        search_activity_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        search_activity_rv.itemAnimator = DefaultItemAnimator()
        search_activity_rv.adapter = RepoListAdapter()

        mFilter.put("sort", "default")

        tab_layout.visibility = View.VISIBLE
        tab_layout.setSelectedTabIndicatorHeight(0)
        tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.repositories)))
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.users)))
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.code)))
        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        mFilter.put("sort", "default")
                        search_activity_no.visibility = View.GONE
                        search_activity_rv.adapter = RepoListAdapter()
                        onRepoSearch()
                    }
                    1 -> {
                        mFilter.put("sort", "default")
                        search_activity_no.visibility = View.GONE
                        search_activity_rv.adapter = SearchUserAdapter()
                        onUserSearch()
                        (search_activity_rv.adapter as SearchUserAdapter).getPositionClicks()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { username ->
                                    startActivity(UserActivity2.getIntent(applicationContext).putExtra("username", username))
                                    overridePendingTransition(0,0)
                                }
                    }
                    2 -> {
                        search_activity_no.visibility = View.GONE
                        search_activity_rv.adapter = SearchCodeAdapter()
                        onCodeSearch()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        (search_activity_rv.adapter as RepoListAdapter).clear()
                        mPresenter.unsubscribe()
                    }
                    1 -> {
                        (search_activity_rv.adapter as SearchUserAdapter).clear()
                        mPresenter.unsubscribe()
                    }
                    2 -> {
                        (search_activity_rv.adapter as SearchCodeAdapter).clear()
                        mPresenter.unsubscribe()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
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
        (search_activity_rv.adapter as RepoListAdapter).addRepos(repoList)
        (search_activity_rv.adapter as RepoListAdapter).setFilter(mFilter)
        if (repoList.isEmpty()) {
            search_activity_no.visibility = View.VISIBLE
            search_activity_no.text = getString(R.string.no_repositories)
        }
    }

    override fun showUsers(userList: List<SearchUser>) {
        (search_activity_rv.adapter as SearchUserAdapter).addUsers(userList)
        if (userList.isEmpty()) {
            search_activity_no.visibility = View.VISIBLE
            search_activity_no.text = getString(R.string.no_users)
        }
    }

    override fun showCode(codeList: List<CodeSearchResult>) {
        (search_activity_rv.adapter as SearchCodeAdapter).addCodeList(codeList)
        if (codeList.isEmpty()) {
            search_activity_no.visibility = View.VISIBLE
            search_activity_no.text = getString(R.string.no_code)
        }
    }

    override fun showLoading() {
        search_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (search_activity_progress_bar.visibility == View.VISIBLE)
            search_activity_progress_bar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mMenu = menu
        menuInflater.inflate(R.menu.search_menu, mMenu)
        val searchView = MenuItemCompat.getActionView(menu?.findItem(R.id.action_search)) as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        RxUtils.fromSearchView(searchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter  { it -> it.length > 2 }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { query ->
                            when (tab_layout.selectedTabPosition) {
                                0 -> {
                                    (search_activity_rv.adapter as RepoListAdapter).clear()
                                    search_activity_no.visibility = View.GONE
                                    mPresenter.onSearchRepos(query, mFilter)
                                }
                                1 -> {
                                    (search_activity_rv.adapter as SearchUserAdapter).clear()
                                    search_activity_no.visibility = View.GONE
                                    mPresenter.onSearchUsers(query, mFilter)
                                }
                                2 -> {
                                    (search_activity_rv.adapter as SearchCodeAdapter).clear()
                                    search_activity_no.visibility = View.GONE
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
        return super.onCreateOptionsMenu(menu)
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
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        return super.onOptionsItemSelected(item)
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
            return Intent(context, SearchActivity::class.java)
        }
    }

}