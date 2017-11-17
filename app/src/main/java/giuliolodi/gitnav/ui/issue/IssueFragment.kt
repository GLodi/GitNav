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

package giuliolodi.gitnav.ui.issue

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.issue_fragment.*
import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.Issue
import javax.inject.Inject
import giuliolodi.gitnav.ui.user.UserActivity
import com.squareup.picasso.Picasso

/**
 * Created by giulio on 14/11/2017.
 */
class IssueFragment : BaseFragment(), IssueContract.View {

    @Inject lateinit var mPresenter: IssueContract.Presenter<IssueContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mIssueNumber: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
        mIssueNumber = activity.intent.getIntExtra("issueNumber", 0)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.issue_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(issue_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.issue) + " #" + mIssueNumber
        (activity as AppCompatActivity).supportActionBar?.subtitle = mOwner + "/" + mName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        issue_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName, mIssueNumber)
    }

    override fun showIssue(issue: Issue) {
        issue_fragment_nested.visibility = View.VISIBLE

        issue_fragment_username.text = issue.user.login
        issue_fragment_title.text = issue.title

        if (!issue.body.isEmpty()) {
            issue_fragment_description.text = issue.body
        }
        else {
            issue_fragment_description.visibility = View.GONE
        }

        Picasso.with(context).load(issue.user.avatarUrl).resize(75, 75).centerCrop().into(issue_fragment_image)
        issue_fragment_image.setOnClickListener {
            startActivity(UserActivity.getIntent(context).putExtra("username", issue.user.login))
            activity.overridePendingTransition(0,0)
        }
    }

    override fun showComments(issueComments: List<Comment>) {
    }

    override fun showLoading() {
        issue_fragment_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        issue_fragment_progressbar.visibility = View.GONE
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