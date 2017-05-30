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
import giuliolodi.gitnav.ui.starred.StarredAdapter
import giuliolodi.gitnav.ui.user.UserAdapter
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.search_activity.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.utils.RxUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by giulio on 26/05/2017.
 */

class SearchActivity : BaseDrawerActivity(), SearchContract.View {

    @Inject lateinit var mPresenter: SearchContract.Presenter<SearchContract.View>

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
        search_activity_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(applicationContext).showLastDivider().build())
        search_activity_rv.itemAnimator = DefaultItemAnimator()
        search_activity_rv.adapter = StarredAdapter()

        tab_layout.visibility = View.VISIBLE
        tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.repositories)))
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.users)))
        tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.code)))
        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        search_activity_rv.adapter = StarredAdapter()
                    }
                    1 -> {
                        search_activity_rv.adapter = UserAdapter()
                    }
                    2 -> {

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        (search_activity_rv.adapter as StarredAdapter).clear()
                    }
                    1 -> {
                        (search_activity_rv.adapter as UserAdapter).clear()
                    }
                    2 -> {

                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun showRepos(repoList: List<Repository>) {
        (search_activity_rv.adapter as StarredAdapter).addRepos(repoList)
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
        menuInflater.inflate(R.menu.search_menu, menu)

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
                            (search_activity_rv.adapter as StarredAdapter).clear()
                            mPresenter.onSearchRepos(query)
                        },
                        { throwable ->
                            hideLoading()
                            showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                )
        return super.onCreateOptionsMenu(menu)
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