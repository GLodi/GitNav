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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.repo_about_fragment.*
import org.eclipse.egit.github.core.Contributor
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 11/07/2017.
 */
class RepoAboutFragment : BaseFragment(), RepoAboutContract.View {

    @Inject lateinit var mPresenter: RepoAboutContract.Presenter<RepoAboutContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mRepoContributor: Map<Repository, List<Contributor>>? = null
    private var mRepo: Repository? = null
    private var mContributorList: List<Contributor>? = null
    private var LOADING: Boolean = false

    companion object {
        fun newInstance(owner: String, name: String): RepoAboutFragment {
            val repoAboutFragment: RepoAboutFragment = RepoAboutFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            repoAboutFragment.arguments = bundle
            return repoAboutFragment
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
        return inflater?.inflate(R.layout.repo_about_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        if (mRepoContributor != null) showRepoNContributors(mRepoContributor!!)
        else if (LOADING) showLoading()
        else {
            if (isNetworkAvailable()) {
                if (mOwner != null && mName != null) mPresenter.subscribe(mOwner!!, mName!!)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showRepoNContributors(repoContributors: Map<Repository, List<Contributor>>) {
        mRepoContributor = repoContributors
        mRepo = repoContributors.keys.first()
        mRepo?.let { mContributorList = repoContributors[it] }

        repo_about_fragment_rl2.visibility = View.VISIBLE
        repo_about_fragment_reponame.text = mRepo?.name
        repo_about_fragment_username.text = mRepo?.owner?.login
        repo_about_fragment_description.text = mRepo?.description
        Picasso.with(context).load(mRepo?.owner?.avatarUrl).resize(75, 75).centerCrop().into(repo_about_fragment_image)
    }

    override fun showLoading() {
        repo_about_fragment_progressbar.visibility = View.VISIBLE
        LOADING = true
    }

    override fun hideLoading() {
        if (repo_about_fragment_progressbar.visibility == View.VISIBLE)
            repo_about_fragment_progressbar.visibility = View.GONE
        LOADING = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
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