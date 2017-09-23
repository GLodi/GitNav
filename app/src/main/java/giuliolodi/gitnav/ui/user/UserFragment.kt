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

package giuliolodi.gitnav.ui.user

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.user_fragment.*
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import javax.inject.Inject

/**
 * Created by giulio on 22/09/2017.
 */
class UserFragment : BaseFragment(), UserContract.View {

    @Inject lateinit var mPresenter: UserContract.Presenter<UserContract.View>

    private var mUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mUsername = activity.intent.getStringExtra("username")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.user_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        user_fragment_rv.layoutManager = LinearLayoutManager(context)
        user_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        user_fragment_rv.itemAnimator = DefaultItemAnimator()
        user_fragment_rv.setHasFixedSize(true)
        user_fragment_rv.isNestedScrollingEnabled = false

        user_fragment_bottomnv.selectedItemId = R.id.user_activity_bottom_menu_info
        user_fragment_bottomnv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.user_activity_bottom_menu_following -> {
                    onFollowingNavClick()
                }
                R.id.user_activity_bottom_menu_followers -> {
                    onFollowersNavClick()
                }
                R.id.user_activity_bottom_menu_info -> {
                    onInfoNavClick()
                }
                R.id.user_activity_bottom_menu_repos -> {
                    onReposNavClick()
                }
                R.id.user_activity_bottom_menu_events -> {
                    onEventsNavClick()
                }
            }
            true
        }

        mPresenter.subscribe(isNetworkAvailable(), mUsername)
    }

    override fun showUser(mapUserFollowed: Map<User, String>) {
    }

    private fun onFollowingNavClick() {

    }

    private fun onFollowersNavClick() {

    }

    private fun onInfoNavClick() {

    }

    private fun onReposNavClick() {

    }

    private fun onEventsNavClick() {

    }

    override fun showRepos(repoList: List<Repository>) {
    }

    override fun showEvents(eventList: List<Event>) {
    }

    override fun showFollowers(followerList: List<User>) {
    }

    override fun showFollowing(followingList: List<User>) {
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun onFollowCompleted() {
    }

    override fun onUnfollowCompleted() {
    }

    override fun showError(error: String) {
    }
}