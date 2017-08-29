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

package giuliolodi.gitnav.ui.stargazerlist

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.user.UserActivity
import giuliolodi.gitnav.ui.user.UserAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.stargazer_list_fragment.*
import org.eclipse.egit.github.core.User
import javax.inject.Inject

/**
 * Created by giulio on 25/08/2017.
 */
class StargazerListFragment : BaseFragment(), StargazerListContract.View {

    @Inject lateinit var mPresenter: StargazerListContract.Presenter<StargazerListContract.View>

    private var mRepoOwner: String? = null
    private var mRepoName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mRepoOwner = activity.intent.getStringExtra("repoOwner")
        mRepoName = activity.intent.getStringExtra("repoName")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.stargazer_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        activity?.title = getString(R.string.stargazers)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL

        stargazer_list_fragment_rv.layoutManager = llm
        stargazer_list_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        stargazer_list_fragment_rv.itemAnimator = DefaultItemAnimator()
        stargazer_list_fragment_rv.adapter = UserAdapter()

        (stargazer_list_fragment_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username -> mPresenter.onUserClick(username) }

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (stargazer_list_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (stargazer_list_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (stargazer_list_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mPresenter.onLastItemVisible(isNetworkAvailable(), dy)
                }
            }
        }
        stargazer_list_fragment_rv.setOnScrollListener(mScrollListener)

        mPresenter.subscribe(isNetworkAvailable(), mRepoOwner, mRepoName)
    }

    override fun showStargazerList(stargazerList: List<User>) {
        (stargazer_list_fragment_rv.adapter as UserAdapter).addUserList(stargazerList)
    }

    override fun showLoading() {
        stargazer_list_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        stargazer_list_fragment_progress_bar.visibility = View.GONE
    }

    override fun showListLoading() {
        (stargazer_list_fragment_rv.adapter as UserAdapter).addLoading()
    }

    override fun hideListLoading() {
        (stargazer_list_fragment_rv.adapter as UserAdapter).hideLoading()
    }

    override fun showNoStargazers() {
        stargazer_list_no_stargazers.visibility = View.VISIBLE
    }

    override fun hideNoStargazers() {
        stargazer_list_no_stargazers.visibility = View.GONE
    }

    override fun clearAdapter() {
        (stargazer_list_fragment_rv.adapter as UserAdapter).clear()
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun intentToUserActivity(username: String) {
        startActivity(UserActivity.getIntent(context).putExtra("username", username))
        activity.overridePendingTransition(0,0)
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