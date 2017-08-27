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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
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
        return inflater?.inflate(R.layout.user_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        mPresenter.subscribe(mRepoOwner, mRepoName, isNetworkAvailable())
    }

    override fun showStargazerList(stargazerList: List<User>) {
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showListLoading() {
    }

    override fun hideListLoading() {
    }

    override fun showNoStargazers() {
    }

    override fun hideNoStargazers() {
    }

    override fun showError(error: String) {
    }

    override fun showNoConnectionError() {
    }

}