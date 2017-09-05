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
import android.view.View
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.user.UserActivity
import kotlinx.android.synthetic.main.issue_list_closed.*
import org.eclipse.egit.github.core.Issue
import javax.inject.Inject

/**
 * Created by giulio on 02/09/2017.
 */
class IssueClosedFragment : BaseFragment(), IssueClosedContract.View {

    @Inject lateinit var mPresenter: IssueClosedContract.Presenter<IssueClosedContract.View>

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
    }

    override fun showClosedIssues(issueList: List<Issue>) {
        (issue_list_closed_rv.adapter as IssueAdapter).addIssueList(issueList)
    }

    override fun showLoading() {
        issue_list_closed_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        issue_list_closed_progressbar.visibility = View.GONE
    }

    override fun showListLoading() {
        (issue_list_closed_rv.adapter as IssueAdapter).addLoading()
    }

    override fun hideListLoading() {
        (issue_list_closed_rv.adapter as IssueAdapter).hideLoading()
    }

    override fun showNoClosedIssues() {
        issue_list_closed_noissues.visibility = View.VISIBLE
    }

    override fun hideNoClosedIssues() {
        issue_list_closed_noissues.visibility = View.GONE
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