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

package giuliolodi.gitnav.ui.contributorlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.user.UserActivity
import kotlinx.android.synthetic.main.contributor_list_fragment.*
import org.eclipse.egit.github.core.Contributor
import javax.inject.Inject

/**
 * Created by giulio on 30/08/2017.
 */
class ContributorListFragment: BaseFragment(), ContributorListContract.View {

    @Inject lateinit var mPresenter: ContributorListContract.Presenter<ContributorListContract.View>

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
        return inflater?.inflate(R.layout.stargazer_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showContributorList(contributorList: List<Contributor>) {
    }

    override fun showLoading() {
        contributor_list_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        contributor_list_progress_bar.visibility = View.GONE
    }

    override fun showListLoading() {
    }

    override fun hideListLoading() {
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