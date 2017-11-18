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

package giuliolodi.gitnav.ui.issuelist

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
import giuliolodi.gitnav.ui.adapters.IssueAdapter
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.issue.IssueActivity
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.issue_list_open.*
import org.eclipse.egit.github.core.Issue
import javax.inject.Inject

/**
 * Created by giulio on 02/09/2017.
 */
class IssueOpenFragment : BaseFragment(), IssueOpenContract.View {

    @Inject lateinit var mPresenter: IssueOpenContract.Presenter<IssueOpenContract.View>

    private var mOwner: String? = null
    private var mName: String? = null

    companion object {
        fun newInstance(owner: String, name: String): IssueOpenFragment {
            val issueOpenFragment: IssueOpenFragment = IssueOpenFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            issueOpenFragment.arguments = bundle
            return issueOpenFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.issue_list_open, container, false)
    }
    
    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        issue_list_open_rv.layoutManager = llm
        issue_list_open_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        issue_list_open_rv.itemAnimator = DefaultItemAnimator()
        issue_list_open_rv.adapter = IssueAdapter()

        (issue_list_open_rv.adapter as IssueAdapter).getUserClick()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username -> mPresenter.onUserClick(username) }

        (issue_list_open_rv.adapter as IssueAdapter).getIssueClick()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { issueNumber -> mPresenter.onIssueClick(issueNumber) }

        val mScrollListenerStarred = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (issue_list_open_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (issue_list_open_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (issue_list_open_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mPresenter.onLastItemVisible(isNetworkAvailable(), dy)
                }
            }
        }
        issue_list_open_rv.setOnScrollListener(mScrollListenerStarred)


        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showOpenIssues(issueList: List<Issue>) {
        (issue_list_open_rv.adapter as IssueAdapter).addIssueList(issueList)
    }

    override fun showLoading() {
        issue_list_open_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        issue_list_open_progressbar.visibility = View.GONE
    }

    override fun showListLoading() {
        (issue_list_open_rv.adapter as IssueAdapter).addLoading()
    }

    override fun hideListLoading() {
        (issue_list_open_rv.adapter as IssueAdapter).hideLoading()
    }

    override fun showNoOpenIssues() {
        issue_list_open_noissues.visibility = View.VISIBLE
    }

    override fun hideNoOpenIssues() {
        issue_list_open_noissues.visibility = View.GONE
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

    override fun intentToIssueActivity(issueNumber: Int) {
        startActivity(IssueActivity.getIntent(context)
                .putExtra("owner", mOwner)
                .putExtra("name", mName)
                .putExtra("issueNumber", issueNumber))
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