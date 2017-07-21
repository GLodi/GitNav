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
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.repo_content_fragment.*
import org.eclipse.egit.github.core.RepositoryContents
import javax.inject.Inject

/**
 * Created by giulio on 11/07/2017.
 */
class RepoContentFragment : BaseFragment(), RepoContentContract.View {

    @Inject lateinit var mPresenter: RepoContentContract.Presenter<RepoContentContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mRepoContentList: List<RepositoryContents>? = null
    private var pathTree: MutableList<String> = mutableListOf()
    private var path: String = ""
    private var treeText: String = ""
    private var LOADING: Boolean = false
    private var LOADING_CONTENT: Boolean = false
    private var TREE_DEPTH: Int = 1

    companion object {
        fun newInstance(owner: String, name: String): RepoContentFragment {
            val repoContentFragment: RepoContentFragment = RepoContentFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            repoContentFragment.arguments = bundle
            return repoContentFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
        pathTree.add("")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.repo_content_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        repo_content_fragment_rv.layoutManager = llm
        repo_content_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        repo_content_fragment_rv.itemAnimator = DefaultItemAnimator()
        repo_content_fragment_rv.adapter = FileAdapter()

        (repo_content_fragment_rv.adapter as FileAdapter).getRepositoryContentsClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repositoryContents ->
                    when(repositoryContents.type) {
                        "dir" -> {
                            if (!LOADING_CONTENT) {
                                LOADING_CONTENT = true
                                pathTree.add(repositoryContents.path)
                                TREE_DEPTH += 1
                            }
                        }
                        "file" -> {

                        }
                    }
                }

        if (LOADING) showLoading()
        // Check if content has already been downloaded
        else {
            if (isNetworkAvailable()) {
                if (mOwner != null && mName != null) mPresenter.subscribe(mOwner!!, mName!!, path)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showContent(repoContentList: List<RepositoryContents>) {
        if (mRepoContentList == null) mRepoContentList = repoContentList
        mRepoContentList = repoContentList
        mRepoContentList?.let { (repo_content_fragment_rv.adapter as FileAdapter).addRepositoryContentList(it) }
    }

    override fun showLoading() {
        repo_content_fragment_progressbar.visibility = View.VISIBLE
        LOADING = true
    }

    override fun hideLoading() {
        if (repo_content_fragment_progressbar.visibility == View.VISIBLE)
            repo_content_fragment_progressbar.visibility = View.GONE
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

    private fun setTree() {
        repo_content_fragment_tree.text = ""
        treeText = "/"
        treeText += pathTree[pathTree.size - 1]
        repo_content_fragment_tree.text = treeText
        Handler().postDelayed({ repo_content_fragment_scrollview.smoothScrollBy(repo_content_fragment_scrollview.maxScrollAmount, 0) }, 100)
    }

    private fun handleBackPressed() {
        if (isNetworkAvailable()) {

        }
    }

}