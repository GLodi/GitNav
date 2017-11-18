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
import giuliolodi.gitnav.data.model.FileViewerIntent
import giuliolodi.gitnav.ui.adapters.FileAdapter
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.fileviewer.FileViewerActivity
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
        getActivityComponent()?.inject(this)
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
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
                .subscribe { repoContents ->
                    when(repoContents.type) {
                        "dir" -> mPresenter.onDirClick(isNetworkAvailable(), repoContents.path)
                        "file" -> mPresenter.onFileClick(repoContents.path, repoContents.name)
                    }
                }

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showContent(repoContentList: List<RepositoryContents>) {
        (repo_content_fragment_rv.adapter as FileAdapter).addRepositoryContentList(repoContentList)
    }

    override fun showLoading() {
        repo_content_fragment_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        repo_content_fragment_progressbar.visibility = View.GONE
    }

    override fun clearContent() {
        (repo_content_fragment_rv.adapter as FileAdapter).clear()
    }

    override fun onTreeSet(treeText: String) {
        repo_content_fragment_tree.text = ""
        repo_content_fragment_tree.text = treeText
        Handler().postDelayed({ repo_content_fragment_scrollview.smoothScrollBy(repo_content_fragment_scrollview.maxScrollAmount, 0) }, 100)
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun pressBack(needTo: Boolean) {
        (activity as RepoActivity).needToBack(needTo)
    }

    override fun onActivityBackPress() {
        mPresenter.onBackPressed(isNetworkAvailable())
    }

    override fun intentToViewerActivity(fileViewerIntent: FileViewerIntent, repoUrl: String) {
        startActivity(FileViewerActivity.getIntent(context)
                .putExtra("owner", fileViewerIntent.repoOwner)
                .putExtra("name", fileViewerIntent.repoName)
                .putExtra("path", fileViewerIntent.filePath)
                .putExtra("filename", fileViewerIntent.fileName)
                .putExtra("file_url", repoUrl))
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