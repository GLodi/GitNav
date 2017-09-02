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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
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
            bundle.putString("name", owner)
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

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showOpenIssues(issueList: List<Issue>) {

    }

    override fun showLoading() {
        issue_list_open_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        issue_list_open_progressbar.visibility = View.GONE
    }

    override fun showNoOpenIssues() {
        issue_list_open_noissues.visibility = View.VISIBLE
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
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