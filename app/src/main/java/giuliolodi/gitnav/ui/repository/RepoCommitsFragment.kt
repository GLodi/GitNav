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

package giuliolodi.gitnav.ui.repository

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.adapters.RepoCommitAdapter
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.repo_commits_fragment.*
import org.eclipse.egit.github.core.RepositoryCommit
import javax.inject.Inject

/**
 * Created by giulio on 11/07/2017.
 */
class RepoCommitsFragment : BaseFragment(), RepoCommitsContract.View {

    @Inject lateinit var mPresenter: RepoCommitsContract.Presenter<RepoCommitsContract.View>

    private var mOwner: String? = null
    private var mName: String? = null

    companion object {
        fun newInstance(owner: String, name: String): RepoCommitsFragment {
            val repoCommitsFragment: RepoCommitsFragment = RepoCommitsFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            repoCommitsFragment.arguments = bundle
            return repoCommitsFragment
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
        return inflater?.inflate(R.layout.repo_commits_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        repo_commits_fragment_rv.layoutManager = llm
        repo_commits_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        repo_commits_fragment_rv.itemAnimator = DefaultItemAnimator()
        repo_commits_fragment_rv.adapter = RepoCommitAdapter()

        (repo_commits_fragment_rv.adapter as RepoCommitAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username -> mPresenter.onUserClick(username) }

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showRepoCommitList(repoCommitList: List<RepositoryCommit>) {
        (repo_commits_fragment_rv.adapter as RepoCommitAdapter).addRepoCommits(repoCommitList)
    }

    override fun showLoading() {
        repo_commits_fragment_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        repo_commits_fragment_progressbar.visibility = View.GONE
    }

    override fun showNoCommits() {
        repo_commits_fragment_nocommits.visibility = View.VISIBLE
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