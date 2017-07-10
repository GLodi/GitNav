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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import javax.inject.Inject
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.view.MenuItem
import org.eclipse.egit.github.core.Repository

/**
 * Created by giulio on 10/07/2017.
 */
class RepoFragment : BaseFragment(), RepoContract.View {

    @Inject lateinit var mPresenter : RepoContract.Presenter<RepoContract.View>

    private var mRepo: Repository? = null
    private var mOwner: String? = null
    private var mName: String? = null
    private var IS_REPO_STARRED: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.repo_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
        activity.title = getString(R.string.repository)
    }

    override fun onRepoDownloaded(repo: Repository, isRepoStarred: Boolean) {
        mRepo = repo
        IS_REPO_STARRED = isRepoStarred

    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.star_icon -> if (mOwner != null && mName != null) mPresenter.unstarRepo(mOwner!!, mName!!)
                R.id.unstar_icon -> if (mOwner != null && mName != null) mPresenter.starRepo(mOwner!!, mName!!)
                R.id.open_in_browser -> {
                    mRepo?.let {
                        val browserIntent = Intent(ACTION_VIEW, Uri.parse(it.htmlUrl))
                        startActivity(browserIntent)
                    }
                }
            }
        } else
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        return super.onOptionsItemSelected(item)
    }

}