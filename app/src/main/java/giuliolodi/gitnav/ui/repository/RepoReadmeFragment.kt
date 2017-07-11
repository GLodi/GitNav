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
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.repo_readme_fragment.*
import javax.inject.Inject

/**
 * Created by giulio on 11/07/2017.
 */
class RepoReadmeFragment : BaseFragment(), RepoReadmeContract.View {

    @Inject lateinit var mPresenter: RepoReadmeContract.Presenter<RepoReadmeContract.View>

    private var mMarkdown: String? = null
    private var mOwner: String? = null
    private var mName: String? = null
    private var LOADING: Boolean = false
    private var NO_README: Boolean = false

    companion object {
        fun newInstance(owner: String, name: String): RepoReadmeFragment {
            val repoReadmeFragment: RepoReadmeFragment = RepoReadmeFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            repoReadmeFragment.arguments = bundle
            return repoReadmeFragment
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
        return inflater?.inflate(R.layout.repo_readme_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        if (mMarkdown != null) showReadme(mMarkdown!!)
        else if (LOADING) showLoading()
        else if (NO_README) showNoReadme()
        else {
            if (isNetworkAvailable()) {
                if (mOwner != null && mName != null) mPresenter.subscribe(mOwner!!, mName!!)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showReadme(markdown: String) {
        mMarkdown = markdown
        repo_readme_fragment_markedview.visibility = View.VISIBLE
        repo_readme_fragment_markedview.setMarkDownText(markdown)
        repo_readme_fragment_markedview.isOpenUrlInBrowser = true
    }

    override fun showLoading() {
        repo_readme_fragment_progressbar.visibility = View.VISIBLE
        LOADING = true
    }

    override fun hideLoading() {
        if (repo_readme_fragment_progressbar.visibility == View.VISIBLE)
            repo_readme_fragment_progressbar.visibility = View.GONE
        LOADING = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoReadme() {
        repo_readme_fragment_noreadme.visibility = View.VISIBLE
        NO_README = true
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