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

package giuliolodi.gitnav.ui.starred

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
import giuliolodi.gitnav.ui.user.UserActivity2
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.starred_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 20/06/2017.
 */

class StarredFragment : BaseFragment(), StarredContract.View {

    @Inject lateinit var mPresenter: StarredContract.Presenter<StarredContract.View>

    private var mRepoList: MutableList<Repository>? = null
    private var mFilter: HashMap<String,String> = HashMap()
    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)

        mFilter.put("sort", "created")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.starred_fragment, container, false)
    }

    override fun initLayout(view: View?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        activity?.title = getString(R.string.starred)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL

        starred_fragment_rv.layoutManager = llm
        starred_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        starred_fragment_rv.itemAnimator = DefaultItemAnimator()
        starred_fragment_rv.adapter = StarredAdapter()
        (starred_fragment_rv.adapter as StarredAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity2.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0,0)
                }

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING || mFilter["sort"] == "stars" || mFilter["sort"] == "pushed" || mFilter["sort"] == "alphabetical" || mFilter["sort"] == "updated" )
                    return
                val visibleItemCount = (starred_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (starred_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (starred_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING = true
                        PAGE_N += 1
                        (starred_fragment_rv.adapter as StarredAdapter).addLoading()
                        mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                        hideLoading()
                    }
                }
            }
        }
        starred_fragment_rv.setOnScrollListener(mScrollListener)

        starred_fragment_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        starred_fragment_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                PAGE_N = 1
                (starred_fragment_rv.adapter as StarredAdapter).clear()
                mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                hideLoading()
            }
        }

        if (isNetworkAvailable()) {
            showLoading()
            mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
        }
        else {
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            hideLoading()
        }
    }

    override fun showRepos(repoList: List<Repository>) {
        mRepoList?.addAll(repoList)
        (starred_fragment_rv.adapter as StarredAdapter).addRepos(repoList)
        (starred_fragment_rv.adapter as StarredAdapter).setFilter(mFilter)
        if (PAGE_N == 1 && repoList.isEmpty()) starred_fragment_no_repo.visibility = View.VISIBLE
        LOADING = false
    }

    override fun showLoading() {
        starred_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (starred_fragment_progress_bar.visibility == View.VISIBLE)
            starred_fragment_progress_bar.visibility = View.GONE
        if (starred_fragment_swipe.isRefreshing)
            starred_fragment_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoRepo() {
        starred_fragment_no_repo.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.starred_sort_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.starred_sort_created -> {
                    item.isChecked = true
                    mFilter.put("sort", "created")
                    PAGE_N = 1
                    (starred_fragment_rv.adapter as StarredAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.starred_sort_updated -> {
                    item.isChecked = true
                    mFilter.put("sort", "updated")
                    PAGE_N = 1
                    (starred_fragment_rv.adapter as StarredAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.starred_sort_pushed -> {
                    item.isChecked = true
                    mFilter.put("sort", "pushed")
                    PAGE_N = 1
                    (starred_fragment_rv.adapter as StarredAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.starred_sort_alphabetical -> {
                    item.isChecked = true
                    mFilter.put("sort", "alphabetical")
                    PAGE_N = 1
                    (starred_fragment_rv.adapter as StarredAdapter).clear()
                    showLoading()
                    mPresenter.subscribe(PAGE_N, ITEMS_PER_PAGE, mFilter)
                }
                R.id.starred_sort_starred-> {
                    item.isChecked = true
                    mFilter.put("sort", "stars")
                    PAGE_N = 1
                    (starred_fragment_rv.adapter as StarredAdapter).clear()
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