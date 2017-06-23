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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import giuliolodi.gitnav.ui.events.EventAdapter
import giuliolodi.gitnav.ui.repositorylist.RepoListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.user_activity.*
import kotlinx.android.synthetic.main.user_activity_content.*
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import javax.inject.Inject

/**
 * Created by giulio on 03/06/2017.
 */

class UserActivity : BaseActivity(), UserContract.View {

    @Inject lateinit var mPresenter: UserContract.Presenter<UserContract.View>

    private var mUser: User? = null
    private lateinit var username: String
    private lateinit var mMenu: Menu

    private var IS_FOLLOWED: Boolean = false
    private var IS_LOGGED_USER: Boolean = false

    private var PAGE_N_FOLLOWERS = 1
    private val ITEMS_PER_PAGE_FOLLOWERS = 20
    private var LOADING_FOLLOWERS = false

    private var PAGE_N_FOLLOWING = 1
    private val ITEMS_PER_PAGE_FOLLOWING = 20
    private var LOADING_FOLLOWING = false

    private var mFilterRepos: HashMap<String,String> = HashMap()
    private var PAGE_N_REPOS = 1
    private val ITEMS_PER_PAGE_REPOS = 10
    private var LOADING_REPOS = false

    private var PAGE_N_EVENTS = 1
    private val ITEMS_PER_PAGE_EVENTS = 10
    private var LOADING_EVENTS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity)

        initLayout()

        username = intent.getStringExtra("username")

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (isNetworkAvailable())
            mPresenter.subscribe(username)
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    private fun initLayout() {
        setSupportActionBar(user_activity_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        user_activity_rv.layoutManager = LinearLayoutManager(applicationContext)
        user_activity_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        user_activity_rv.itemAnimator = DefaultItemAnimator()
        user_activity_rv.setHasFixedSize(true)
        user_activity_rv.isNestedScrollingEnabled = false

        user_activity_bottomnv.selectedItemId = R.id.user_activity_bottom_menu_info
        user_activity_bottomnv.setOnNavigationItemSelectedListener { item ->
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
        user_activity_appbar.setExpanded(false)
        user_activity_nestedscrollview.isNestedScrollingEnabled = false

        mPresenter.unsubscribe()

        user_activity_content_rl.visibility = View.GONE
        user_activity_rv.visibility = View.VISIBLE
        user_activity_content_no.visibility = View.GONE
        mMenu.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = false }

        PAGE_N_FOLLOWING = 1
        LOADING_FOLLOWING = false

        user_activity_rv.adapter = UserAdapter()
        val mScrollListenerFollowing = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_FOLLOWING)
                    return
                val visibleItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_FOLLOWING = true
                        PAGE_N_FOLLOWING += 1
                        (user_activity_rv.adapter as UserAdapter).addLoading()
                        mPresenter.getFollowing(username, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_rv.setOnScrollListener(mScrollListenerFollowing)

        (user_activity_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        showLoading()
        mUser?.login?.let { mPresenter.getFollowing(it, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING) }
    }

    private fun onFollowersNavClick() {
        user_activity_appbar.setExpanded(false)
        user_activity_nestedscrollview.isNestedScrollingEnabled = false

        mPresenter.unsubscribe()

        user_activity_content_rl.visibility = View.GONE
        user_activity_rv.visibility = View.VISIBLE
        user_activity_content_no.visibility = View.GONE
        mMenu.findItem(R.id.user_menu_sort_icon).isVisible = false

        PAGE_N_FOLLOWERS = 1
        LOADING_FOLLOWERS = false

        user_activity_rv.adapter = UserAdapter()
        val mScrollListenerFollowers = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_FOLLOWERS)
                    return
                val visibleItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_FOLLOWERS = true
                        PAGE_N_FOLLOWERS += 1
                        (user_activity_rv.adapter as UserAdapter).addLoading()
                        mPresenter.getFollowers(username, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_rv.setOnScrollListener(mScrollListenerFollowers)

        (user_activity_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        showLoading()
        mUser?.login?.let { mPresenter.getFollowers(it, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS) }
    }

    private fun onInfoNavClick() {
        user_activity_appbar.setExpanded(true)
        user_activity_nestedscrollview.isNestedScrollingEnabled = true

        mPresenter.unsubscribe()

        user_activity_content_rl.visibility = View.VISIBLE
        user_activity_rv.visibility = View.GONE
        user_activity_content_no.visibility = View.GONE
        mMenu.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = false }
    }

    private fun onReposNavClick() {
        user_activity_appbar.setExpanded(false)
        user_activity_nestedscrollview.isNestedScrollingEnabled = false

        mPresenter.unsubscribe()

        user_activity_content_rl.visibility = View.GONE
        user_activity_rv.visibility = View.VISIBLE
        user_activity_content_no.visibility = View.GONE
        mMenu.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = true }
        mMenu.findItem(R.id.user_menu_created)?.let { it.isChecked = true }

        PAGE_N_REPOS = 1
        LOADING_REPOS = false

        user_activity_rv.adapter = RepoListAdapter()
        mFilterRepos.put("sort","created")
        (user_activity_rv.adapter as RepoListAdapter).setFilter(mFilterRepos)

        val mScrollListenerRepos = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_REPOS || mFilterRepos["sort"] == "stars")
                    return
                val visibleItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_REPOS = true
                        PAGE_N_REPOS += 1
                        (user_activity_rv.adapter as RepoListAdapter).addLoading()
                        mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_rv.setOnScrollListener(mScrollListenerRepos)

        showLoading()
        mUser?.login?.let { mPresenter.getRepos(it, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos) }
    }

    fun onEventsNavClick() {
        user_activity_appbar.setExpanded(false)
        user_activity_nestedscrollview.isNestedScrollingEnabled = false

        mPresenter.unsubscribe()

        user_activity_content_rl.visibility = View.GONE
        user_activity_rv.visibility = View.VISIBLE
        user_activity_content_no.visibility = View.GONE
        mMenu.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = false }

        PAGE_N_EVENTS = 1
        LOADING_EVENTS = false

        user_activity_rv.adapter = EventAdapter()
        val mScrollListenerEvents = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_EVENTS)
                    return
                val visibleItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_EVENTS = true
                        PAGE_N_EVENTS += 1
                        (user_activity_rv.adapter as EventAdapter).addLoading()
                        mPresenter.getEvents(username, PAGE_N_EVENTS, ITEMS_PER_PAGE_EVENTS)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_rv.setOnScrollListener(mScrollListenerEvents)

        (user_activity_rv.adapter as EventAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        showLoading()
        mUser?.login?.let { mPresenter.getEvents(it, PAGE_N_EVENTS, ITEMS_PER_PAGE_EVENTS) }
    }

    override fun showFollowing(followingList: List<User>) {
        (user_activity_rv.adapter as UserAdapter).addUserList(followingList)
        if (PAGE_N_FOLLOWING == 1 && followingList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_users)
        }
        LOADING_FOLLOWING = false
    }

    override fun showFollowers(followerList: List<User>) {
        (user_activity_rv.adapter as UserAdapter).addUserList(followerList)
        if (PAGE_N_FOLLOWERS == 1 && followerList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_users)
        }
        LOADING_FOLLOWERS = false
    }

    override fun showRepos(repoList: List<Repository>) {
        (user_activity_rv.adapter as RepoListAdapter).addRepos(repoList)
        if (PAGE_N_REPOS == 1 && repoList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_repositories)
        }
        LOADING_REPOS = false
    }

    override fun showEvents(eventList: List<Event>) {
        (user_activity_rv.adapter as EventAdapter).addEvents(eventList)
        if (PAGE_N_EVENTS == 1 && eventList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_events)
        }
        LOADING_EVENTS = false
    }

    override fun onFollowCompleted() {
        IS_FOLLOWED = true
        user_activity_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_full_24dp))
        Toasty.success(applicationContext, getString(R.string.user_followed), Toast.LENGTH_LONG).show()
    }

    override fun onUnfollowCompleted() {
        IS_FOLLOWED = false
        user_activity_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_empty_24dp))
        Toasty.success(applicationContext, getString(R.string.user_unfollowed), Toast.LENGTH_LONG).show()
    }

    override fun showLoading() {
        user_activity_content_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (user_activity_content_progress_bar.visibility == View.VISIBLE)
            user_activity_content_progress_bar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mMenu = menu!!
        return true
    }

    private fun createOptionMenu() {
        menuInflater.inflate(R.menu.user_menu, mMenu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            finish()
            overridePendingTransition(0,0)
        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.user_menu_created -> {
                    item.isChecked = true
                    mFilterRepos.put("sort", "created")
                    PAGE_N_REPOS = 1
                    (user_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                }
                R.id.user_menu_updated -> {
                    item.isChecked = true
                    mFilterRepos.put("sort", "updated")
                    PAGE_N_REPOS = 1
                    (user_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                }
                R.id.user_menu_pushed -> {
                    item.isChecked = true
                    mFilterRepos.put("sort", "pushed")
                    PAGE_N_REPOS = 1
                    (user_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                }
                R.id.user_menu_alphabetical -> {
                    item.isChecked = true
                    mFilterRepos.put("sort", "full_name")
                    PAGE_N_REPOS = 1
                    (user_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                }
                R.id.user_menu_stars -> {
                    item.isChecked = true
                    mFilterRepos.put("sort", "stars")
                    PAGE_N_REPOS = 1
                    (user_activity_rv.adapter as RepoListAdapter).clear()
                    showLoading()
                    mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                }
                R.id.open_in_browser -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mUser?.htmlUrl)))
                }
            }
        }
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, UserActivity::class.java)
        }
    }

}