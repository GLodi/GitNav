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

package giuliolodi.gitnav.ui.fileviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.file_viewer_fragment.*
import org.eclipse.egit.github.core.RepositoryContents
import javax.inject.Inject

/**
 * Created by giulio on 22/07/2017.
 */
class FileViewerFragment : BaseFragment(), FileViewerContract.View {

    @Inject lateinit var mPresenter: FileViewerContract.Presenter<FileViewerContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mPath: String? = null
    private var mFileUrl: String? = null
    private var mFilenameGist: String? = null
    private var mContentGist: String? = null
    private var LOADING: Boolean = false

    companion object {
        fun newInstanceRepoFile(fileNameGist: String, fileUrl: String): FileViewerFragment {
            val fileViewerFragment: FileViewerFragment = FileViewerFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("filename_gist", fileNameGist)
            bundle.putString("file_url", fileUrl)
            fileViewerFragment.arguments = bundle
            return fileViewerFragment
        }
        fun newInstanceGistFile(owner: String, name: String, path: String, fileUrl: String): FileViewerFragment {
            val fileViewerFragment: FileViewerFragment = FileViewerFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            bundle.putString("path", path)
            bundle.putString("file_url", fileUrl)
            fileViewerFragment.arguments = bundle
            return fileViewerFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
        mPath = arguments.getString("path")
        mFileUrl = arguments.getString("file_url")
        mFilenameGist = arguments.getString("filename_gist")
        mContentGist = arguments.getString("content_gist")

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.file_viewer_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        if (LOADING) showLoading()
        else {
            if (isNetworkAvailable()) {
                if (mOwner != null && mName != null && mPath != null) mPresenter.subscribe(mOwner!!, mName!!, mPath!!)
                else if (mFilenameGist != null && mContentGist != null) showGistFile(mContentGist!!, mFilenameGist!!)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showRepoFile(repoContent: RepositoryContents) {
    }

    override fun showGistFile(contentGist: String, filenameGist: String) {
    }

    override fun showLoading() {
        file_viewer_fragment_progressbar.visibility = View.VISIBLE
        LOADING = true
    }

    override fun hideLoading() {
        if (file_viewer_fragment_progressbar.visibility == View.VISIBLE)
            file_viewer_fragment_progressbar.visibility = View.GONE
        LOADING = false
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

}