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
        mUser = mapUserFollowed.keys.first()
        if (mapUserFollowed[mUser!!] == "f")
            IS_FOLLOWED = true
        else if (mapUserFollowed[mUser!!] == "u")
            IS_LOGGED_USER = true

        mUser?.let { mPresenter.updateLoggedUser(it) }

        createOptionMenu()

        user_activity_collapsing_toolbar.title = mUser?.name ?: mUser?.login
        Picasso.with(applicationContext).load(mUser?.avatarUrl).into(user_activity_image)

        // FAB follow/unfollow
        user_activity_fab.visibility = View.VISIBLE
        if (IS_FOLLOWED)
            user_activity_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_full_24dp))
        else
            user_activity_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_empty_24dp))
        if (IS_LOGGED_USER)
            user_activity_fab.visibility = View.GONE
        user_activity_fab.setOnClickListener {
            if (isNetworkAvailable()) {
                if (IS_FOLLOWED)
                    mPresenter.unFollowUser(username)
                else
                    mPresenter.followUser(username)
            }
            else
                Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        }

        // Full name
        if (mUser?.name != null && !mUser?.name?.isEmpty()!!)
            user_activity_content_fullname.text = mUser?.name
        else {
            user_activity_content_fullname.visibility = View.GONE
            user_activity_content_fullname_bottom.visibility = View.GONE
        }

        // Username
        mUser?.login?.let { user_activity_content_username.text = it }

        // Bio
        if (mUser?.bio != null && !mUser?.bio?.isEmpty()!!)
            user_activity_content_bio.text = mUser?.bio
        else
            user_activity_content_bio_rl.visibility = View.GONE

        // Mail
        if (mUser?.email != null && !mUser?.email?.isEmpty()!!) {
            user_activity_content_mail.text = mUser?.email
            user_activity_content_mail_rl.setOnClickListener {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mUser?.email)), "Email"))
            }
        }
        else
            user_activity_content_mail_rl.visibility = View.GONE

        // Location
        if (mUser?.location != null && !mUser?.location?.isEmpty()!!) {
            user_activity_content_location.text = mUser?.location
            user_activity_content_location_rl.setOnClickListener {
                val uriIntent = Uri.parse(Uri.encode(mUser?.location))
                val mapIntent = Intent(Intent.ACTION_VIEW, uriIntent)
                mapIntent.`package` = "com.google.android.apps.maps"
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                }
            }
        }
        else
            user_activity_content_location_rl.visibility = View.GONE

        // Company
        if (mUser?.company != null && !mUser?.company?.isEmpty()!!)
            user_activity_content_company.text = mUser?.company
        else
            user_activity_content_company_rl.visibility = View.GONE

        // Blog
        if (mUser?.blog != null && !mUser?.blog?.isEmpty()!!) {
            user_activity_content_blog.text = mUser?.blog
            user_activity_content_blog_rl.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mUser?.blog)))
            }
        }
        else
            user_activity_content_blog_rl.visibility = View.GONE

        user_activity_content_contributionsview.loadUserName(mUser?.login)

        if (user_activity_bottomnv.selectedItemId == R.id.user_activity_bottom_menu_info)
            user_activity_content_rl.visibility = View.VISIBLE
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