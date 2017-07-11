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

package giuliolodi.gitnav.ui.gist

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.gist_fragment_files.*
import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.Repository
import org.ocpsoft.prettytime.PrettyTime
import javax.inject.Inject

/**
 * Created by giulio on 03/07/2017.
 */
class GistFragmentFiles: BaseFragment(), GistContractFiles.View {
    
    @Inject lateinit var mPresenter: GistContractFiles.Presenter<GistContractFiles.View>

    private val mPrettyTime: PrettyTime = PrettyTime()
    private var mGistId: String? = null
    private var mGist: Gist? = null
    private var LOADING: Boolean = false

    companion object {
        fun newInstance(gistId: String): GistFragmentFiles {
            val gistFragmentFiles: GistFragmentFiles = GistFragmentFiles()
            val bundle: Bundle = Bundle()
            bundle.putString("gistId", gistId)
            gistFragmentFiles.arguments = bundle
            return gistFragmentFiles
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mGistId = arguments.getString("gistId")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.gist_fragment_files, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        val llmFiles = LinearLayoutManager(context)
        llmFiles.orientation = LinearLayoutManager.VERTICAL
        gist_fragment_files_rv.layoutManager = llmFiles
        gist_fragment_files_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        gist_fragment_files_rv.itemAnimator = DefaultItemAnimator()
        gist_fragment_files_rv.adapter = GistFileAdapter()

        if (mGist != null) mGist?.let { showGist(it) }
        else if (LOADING) showLoading()
        else {
            if (isNetworkAvailable()) {
                mGistId?.let { mPresenter.getGist(it) }
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun showGist(gist: Gist) {
        mGist = gist

        gist_fragment_files_nested.visibility = View.VISIBLE
        gist_fragment_files_username.text = gist.owner.login
        gist_fragment_files_title.text = gist.description
        gist_fragment_files_date.text = mPrettyTime.format(gist.createdAt)
        gist_fragment_files_sha.text = gist.id
        gist_fragment_files_status.text = if (gist.isPublic) getString(R.string.publics) else getString(R.string.privates)
        gist_fragment_files_date.visibility = View.VISIBLE
        Picasso.with(context).load(gist.owner.avatarUrl).centerCrop().resize(75, 75).into(gist_fragment_files_image)

        (gist_fragment_files_rv.adapter as GistFileAdapter).addGistFileList(gist.files.values.toMutableList())
    }

    override fun showLoading() {
        gist_fragment_files_progress_bar.visibility = View.VISIBLE
        LOADING = true
    }

    override fun hideLoading() {
        if (gist_fragment_files_progress_bar.visibility == View.VISIBLE)
            gist_fragment_files_progress_bar.visibility = View.GONE
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