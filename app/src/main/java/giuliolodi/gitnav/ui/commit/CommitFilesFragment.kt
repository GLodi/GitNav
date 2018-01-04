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

package giuliolodi.gitnav.ui.commit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import javax.inject.Inject

/**
 * Created by giulio on 29/12/2017.
 */
class CommitFilesFragment : BaseFragment(), CommitFilesContract.View {

    @Inject lateinit var mPresenter: CommitFilesContract.Presenter<CommitFilesContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mSha: String? = null

    companion object {
        fun newInstance(owner: String, name: String, sha: String): CommitFilesFragment {
            val commitFilesFragment: CommitFilesFragment = CommitFilesFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            bundle.putString("sha", sha)
            commitFilesFragment.arguments = bundle
            return commitFilesFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
        mSha = arguments.getString("sha")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.commit_fragment_filelist, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

    }

    override fun showFiles() {
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