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
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.stargazerlist.StargazerListActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

        repo_about_fragment_gridview.layoutManager = LinearLayoutManager(context)
        repo_about_fragment_gridview.isNestedScrollingEnabled = false

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showRepoAbout(repoOwner: String, repoName: String, repoDescription: String, avatarUrl: String) {
        repo_about_fragment_rl2.visibility = View.VISIBLE
        repo_about_fragment_reponame.text = repoName
        repo_about_fragment_username.text = repoOwner
        repo_about_fragment_description.text = repoDescription
        Picasso.with(context).load(avatarUrl).resize(75, 75).centerCrop().into(repo_about_fragment_image)
    }

    override fun populateGridView(forksString: String, openIssuesString: String, contributorListSize: String, stargazersString: String) {
        val nameList: MutableList<String> = mutableListOf()
        nameList.add(getString(R.string.stargazers))
        nameList.add(getString(R.string.forks))
        nameList.add(getString(R.string.issues))
        nameList.add(getString(R.string.contributors))

        val numberList: MutableList<String> = mutableListOf()
        numberList.add(stargazersString)
        numberList.add(forksString)
        numberList.add(openIssuesString)
        numberList.add(contributorListSize)

        repo_about_fragment_gridview.adapter = RepoAboutAdapter()
        (repo_about_fragment_gridview.adapter as RepoAboutAdapter).set(nameList, numberList)

        (repo_about_fragment_gridview.adapter as RepoAboutAdapter).getStargazersClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ ->
                    startActivity(StargazerListActivity.getIntent(context).putExtra("owner", mOwner).putExtra("name", mName))
                    activity.overridePendingTransition(0,0)
                }
    }

    override fun showLoading() {
        repo_about_fragment_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        repo_about_fragment_progressbar.visibility = View.GONE
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