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
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.pddstudio.highlightjs.models.Language
import com.pddstudio.highlightjs.models.Theme
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
    private var mFilename: String? = null
    private var mFileUrl: String? = null
    private var mFilenameGist: String? = null
    private var mContentGist: String? = null
    private var LOADING: Boolean = false

    companion object {
        fun newInstanceRepoFile(fileNameGist: String, contentGist: String, fileUrl: String): FileViewerFragment {
            val fileViewerFragment: FileViewerFragment = FileViewerFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("filename_gist", fileNameGist)
            bundle.putString("content_gist", contentGist)
            bundle.putString("file_url", fileUrl)
            fileViewerFragment.arguments = bundle
            return fileViewerFragment
        }
        fun newInstanceGistFile(owner: String, name: String, path: String, filename: String, fileUrl: String): FileViewerFragment {
            val fileViewerFragment: FileViewerFragment = FileViewerFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            bundle.putString("path", path)
            bundle.putString("filename", filename)
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
        mFilename = arguments.getString("filename")
        mFileUrl = arguments.getString("file_url")
        mFilenameGist = arguments.getString("filename_gist")
        mContentGist = arguments.getString("content_gist")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.file_viewer_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        if (LOADING) showLoading()
        else {
            if (isNetworkAvailable()) {
                if (mOwner != null && mName != null && mPath != null && mFilename != null) initRepoFile()
                else if (mFilenameGist != null && mContentGist != null) initGistFile()
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initRepoFile() {
        mPresenter.subscribe(mOwner!!, mName!!, mPath!!)

        (activity as AppCompatActivity).supportActionBar?.title = mFilename
        (activity as AppCompatActivity).supportActionBar?.subtitle = mOwner + "/" + mName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initGistFile() {
        (activity as AppCompatActivity).supportActionBar?.title = mFilenameGist
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        file_viewer_fragment_highlightview.setZoomSupportEnabled(true)
        file_viewer_fragment_highlightview.theme = Theme.ANDROID_STUDIO
        file_viewer_fragment_highlightview.highlightLanguage = Language.AUTO_DETECT
        file_viewer_fragment_highlightview.setSource(mContentGist)
        file_viewer_fragment_highlightview.visibility = View.VISIBLE

        hideLoading()
    }

    override fun showRepoFile(repoContent: RepositoryContents) {
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