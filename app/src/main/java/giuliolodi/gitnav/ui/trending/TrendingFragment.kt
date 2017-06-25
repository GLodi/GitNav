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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.starred.StarredAdapter
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.trending_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 24/06/2017.
 */

class TrendingFragment : BaseFragment(), TrendingContract.View {

    @Inject lateinit var mPresenter: TrendingContract.Presenter<TrendingContract.View>

    private var mRepoList: MutableList<Repository> = mutableListOf()
    private var mPeriod: String = "daily"

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
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0, 0)
                }

        trending_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        trending_fragment_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                (trending_fragment_rv.adapter as StarredAdapter).clear()
                mRepoList.clear()
                mPresenter.unsubscribe()
                mPresenter.subscribe(mPeriod)
            } else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }

        trending_fragment_bottomview.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.trending_fragment_menu_daily -> {
                    showLoading()
                    (trending_fragment_rv.adapter as StarredAdapter).clear()
                    mRepoList.clear()
                    mPresenter.unsubscribe()
                    mPeriod = "daily"
                    mPresenter.subscribe(mPeriod)
                }
                R.id.trending_fragment_menu_weekly -> {
                    showLoading()
                    (trending_fragment_rv.adapter as StarredAdapter).clear()
                    mRepoList.clear()
                    mPresenter.unsubscribe()
                    mPeriod = "weekly"
                    mPresenter.subscribe(mPeriod)
                }

                R.id.trending_fragment_menu_monthly -> {
                    showLoading()
                    (trending_fragment_rv.adapter as StarredAdapter).clear()
                    mRepoList.clear()
                    mPresenter.unsubscribe()
                    mPeriod = "monthly"
                    mPresenter.subscribe(mPeriod)
                }
            }
            true
        }

        if (!mRepoList.isEmpty()) {
            (trending_fragment_rv.adapter as StarredAdapter).addRepos(mRepoList)
        }
        else {
            if (isNetworkAvailable()) {
                showLoading()
                mPresenter.subscribe(mPeriod)
            } else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }
    }

    override fun addRepo(repo: Repository) {
        mRepoList.add(repo)
        (trending_fragment_rv.adapter as StarredAdapter).addRepo(repo)
    }

    override fun showLoading() {
        trending_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (trending_fragment_progress_bar.visibility == View.VISIBLE)
            trending_fragment_progress_bar.visibility = View.GONE
        if (trending_fragment_swipe.isRefreshing)
            trending_fragment_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun onComplete() {
        if ((trending_fragment_rv.adapter as StarredAdapter).itemCount == 0)
            trending_fragment_no_repo.visibility = View.VISIBLE
    }

    override fun showNoRepo() {
        trending_fragment_no_repo.visibility = View.VISIBLE
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