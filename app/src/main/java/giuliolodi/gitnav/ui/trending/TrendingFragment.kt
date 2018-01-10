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

package giuliolodi.gitnav.ui.trending

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.repository.RepoActivity
import giuliolodi.gitnav.ui.adapters.StarredAdapter
import giuliolodi.gitnav.ui.option.OptionActivity
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.trending_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 24/06/2017.
 */
class TrendingFragment : BaseFragment(), TrendingContract.View {

    @Inject lateinit var mPresenter: TrendingContract.Presenter<TrendingContract.View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.trending_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.trending)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        trending_fragment_rv.layoutManager = llm
        trending_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        trending_fragment_rv.itemAnimator = DefaultItemAnimator()
        trending_fragment_rv.adapter = StarredAdapter()
        (trending_fragment_rv.adapter as StarredAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username -> mPresenter.onImageClick(username) }
        (trending_fragment_rv.adapter as StarredAdapter).getRepoClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repo -> mPresenter.onRepoClick(repo.owner.login, repo.name) }

        trending_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        trending_fragment_swipe.setOnRefreshListener { mPresenter.onSwipeToRefresh(isNetworkAvailable()) }

        trending_fragment_bottomview.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.trending_fragment_bottom_menu_daily -> { mPresenter.onBottomViewDailyClick() }
                R.id.trending_fragment_bottom_menu_weekly -> { mPresenter.onBottomViewWeeklyClick() }
                R.id.trending_fragment_bottom_menu_monthly -> { mPresenter.onBottomViewMonthlyClick() }
            }
            true
        }

        mPresenter.subscribe(isNetworkAvailable())
    }

    override fun addRepo(repo: Repository) {
        (trending_fragment_rv.adapter as StarredAdapter).addRepo(repo)
    }

    override fun addRepoList(repoList: List<Repository>) {
        (trending_fragment_rv.adapter as StarredAdapter).addRepos(repoList)
    }

    override fun showLoading() {
        trending_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        trending_fragment_progress_bar.visibility = View.GONE
        trending_fragment_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoRepo() {
        trending_fragment_no_repo.visibility = View.VISIBLE
    }

    override fun hideNoRepo() {
        trending_fragment_no_repo.visibility = View.GONE
    }

    override fun clearAdapter() {
        (trending_fragment_rv.adapter as StarredAdapter).clear()
    }

    override fun intentToUserActivity(username: String) {
        startActivity(UserActivity.getIntent(context).putExtra("username", username))
        activity.overridePendingTransition(0, 0)
    }

    override fun intentToRepoActivity(repoOwner: String, repoName: String) {
        startActivity(RepoActivity.getIntent(context).putExtra("owner", repoOwner).putExtra("name", repoName))
        activity.overridePendingTransition(0,0)
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {
            startActivity(OptionActivity.getIntent(context))
            activity.overridePendingTransition(0,0)
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