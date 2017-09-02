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
import android.support.v7.app.AppCompatActivity
import android.view.*
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.issue_list_fragment.*
import org.eclipse.egit.github.core.Issue
import javax.inject.Inject

/**
 * Created by giulio on 01/09/2017.
 */
class IssueListFragment : BaseFragment(), IssueListContract.View {

    @Inject lateinit var mPresenter: IssueListContract.Presenter<IssueListContract.View>

    private var mOwner: String? = null
    private var mName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.issue_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(issue_list_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.gist)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        issue_list_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }
    }

    override fun showIssueList(issueList: List<Issue>) {

    }

    override fun showLoading() {

    }

    override fun hideLoading() {
    }

    override fun showError(error: String) {
    }

    override fun showNoConnectionError() {
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater?.inflate(R.menu.main, menu)
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