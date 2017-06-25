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

package giuliolodi.gitnav.ui.search

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.repositorylist.RepoListAdapter
import giuliolodi.gitnav.ui.starred.StarredContract
import kotlinx.android.synthetic.main.search_activity.*
import org.eclipse.egit.github.core.CodeSearchResult
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.SearchUser
import javax.inject.Inject

/**
 * Created by giulio on 25/06/2017.
 */

class SearchFragment : BaseFragment(), SearchContract.View {

    @Inject lateinit var mPresenter: SearchContract.Presenter<SearchContract.View>

    private val mFilter: HashMap<String,String> = HashMap()
    private var mMenu: Menu? = null

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
    }

    override fun showRepos(repoList: List<Repository>) {
        (search_activity_rv.adapter as RepoListAdapter).addRepos(repoList)
        (search_activity_rv.adapter as RepoListAdapter).setFilter(mFilter)
        if (repoList.isEmpty()) {
            search_activity_no.visibility = View.VISIBLE
            search_activity_no.text = getString(R.string.no_repositories)
        }
    }



    override fun showUsers(userList: List<SearchUser>) {
        (search_activity_rv.adapter as SearchUserAdapter).addUsers(userList)
        if (userList.isEmpty()) {
            search_activity_no.visibility = View.VISIBLE
            search_activity_no.text = getString(R.string.no_users)
        }
    }

    override fun showCode(codeList: List<CodeSearchResult>) {
        (search_activity_rv.adapter as SearchCodeAdapter).addCodeList(codeList)
        if (codeList.isEmpty()) {
            search_activity_no.visibility = View.VISIBLE
            search_activity_no.text = getString(R.string.no_code)
        }
    }

    override fun showLoading() {
        search_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (search_activity_progress_bar.visibility == View.VISIBLE)
            search_activity_progress_bar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

}