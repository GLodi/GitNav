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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.user_fragment.*
import kotlinx.android.synthetic.main.user_fragment_content.*
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

    private var mMenu: Menu? = null
    private var mMenuInflater: MenuInflater? = null

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

    override fun showUser(user: User, IS_FOLLOWED: Boolean, IS_LOGGED_USER: Boolean) {
        user_fragment_collapsing_toolbar.title = user.name ?: user.login
        Picasso.with(context).load(user.avatarUrl).into(user_fragment_image)

        // FAB follow/unfollow
        user_fragment_fab.visibility = View.VISIBLE
        if (IS_FOLLOWED)
            user_fragment_fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_full_24dp))
        else
            user_fragment_fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_empty_24dp))
        if (IS_LOGGED_USER)
            user_fragment_fab.visibility = View.GONE
        user_fragment_fab.setOnClickListener {
            if (isNetworkAvailable()) {
                if (IS_FOLLOWED)
                    mPresenter.unFollowUser(user.login)
                else
                    mPresenter.followUser(user.login)
            }
            else {
                Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }

        // Full name
        if (user.name != null && !user.name?.isEmpty()!!) {
            user_fragment_content_fullname.text = user.name
        }
        else {
            user_fragment_content_fullname.visibility = View.GONE
            user_fragment_content_fullname_bottom.visibility = View.GONE
        }

        // Username
        user.login?.let { user_fragment_content_username.text = it }

        // Bio
        if (user.bio != null && !user.bio?.isEmpty()!!)
            user_fragment_content_bio.text = user.bio
        else
            user_fragment_content_bio_rl.visibility = View.GONE

        // Mail
        if (user.email != null && !user.email?.isEmpty()!!) {
            user_fragment_content_mail.text = user.email
            user_fragment_content_mail_rl.setOnClickListener {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + user.email)), "Email"))
            }
        }
        else {
            user_fragment_content_mail_rl.visibility = View.GONE
        }

        // Location
        if (user.location != null && !user.location?.isEmpty()!!) {
            user_fragment_content_location.text = user.location
            user_fragment_content_location_rl.setOnClickListener {
                val uriIntent = Uri.parse(Uri.encode(user.location))
                val mapIntent = Intent(Intent.ACTION_VIEW, uriIntent)
                mapIntent.`package` = "com.google.android.apps.maps"
                /*
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                }
                */
            }
        }
        else {
            user_fragment_content_location_rl.visibility = View.GONE
        }

        // Company
        if (user.company != null && !user.company?.isEmpty()!!)
            user_fragment_content_company.text = user.company
        else
            user_fragment_content_company_rl.visibility = View.GONE

        // Blog
        if (user.blog != null && !user.blog?.isEmpty()!!) {
            user_fragment_content_blog.text = user.blog
            user_fragment_content_blog_rl.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(user.blog)))
            }
        }
        else {
            user_fragment_content_blog_rl.visibility = View.GONE
        }

        user_fragment_content_contributionsview.loadUserName(user.login)

        if (user_fragment_bottomnv.selectedItemId == R.id.user_activity_bottom_menu_info)
            user_fragment_content_rl.visibility = View.VISIBLE
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

    override fun createOptionsMenu() {
        mMenuInflater?.inflate(R.menu.user_menu, mMenu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.let { mMenu = it }
        inflater?.let { mMenuInflater = it }
    }

}