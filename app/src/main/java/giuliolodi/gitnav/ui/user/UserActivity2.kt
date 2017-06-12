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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import kotlinx.android.synthetic.main.user_activity2.*
import kotlinx.android.synthetic.main.user_activity_content.*
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import javax.inject.Inject

/**
 * Created by giulio on 03/06/2017.
 */

class UserActivity2 : BaseActivity(), UserContract2.View {

    @Inject lateinit var mPresenter: UserContract2.Presenter<UserContract2.View>

    private lateinit var mUser: User
    private lateinit var username: String

    private var IS_FOLLOWED: Boolean = false
    private var IS_LOGGED_USER: Boolean = false

    private var mFilterRepos: HashMap<String,String> = HashMap()
    private var PAGE_N_REPOS = 1
    private val ITEMS_PER_PAGE_REPOS = 10
    private var LOADING_REPOS = false

    private var PAGE_N_FOLLOWERS = 1
    private val ITEMS_PER_PAGE_FOLLOWERS = 10
    private var LOADING_FOLLOWERS = false

    private var PAGE_N_FOLLOWING = 1
    private val ITEMS_PER_PAGE_FOLLOWING = 10
    private var LOADING_FOLLOWING = false

    private var PAGE_N_EVENTS = 1
    private val ITEMS_PER_PAGE_EVENTS = 10
    private var LOADING_EVENTS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity2)

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
        setSupportActionBar(user_activity2_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val llm = LinearLayoutManager(applicationContext)
        llm.orientation = LinearLayoutManager.VERTICAL
        llm.isAutoMeasureEnabled = true
        user_activity_content_rv.layoutManager = llm
        user_activity_content_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        user_activity_content_rv.itemAnimator = DefaultItemAnimator()
        user_activity_content_rv.setHasFixedSize(true)
        user_activity_content_rv.isNestedScrollingEnabled = false

        user_activity2_bottomnv.selectedItemId = R.id.user_activity_bottom_menu_info
        user_activity2_bottomnv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.user_activity_bottom_menu_following -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                    onFollowingNavClick()
                }
                R.id.user_activity_bottom_menu_followers -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                    onFollowersNavClick()
                }
                R.id.user_activity_bottom_menu_info -> {
                    user_activity2_appbar.setExpanded(true)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = true
                    onInfoNavClick()
                }
                R.id.user_activity_bottom_menu_repos -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                    onReposNavClick()
                }
                R.id.user_activity_bottom_menu_events -> {
                    user_activity2_appbar.setExpanded(false)
                    user_activity2_nestedscrollview.isNestedScrollingEnabled = false
                    onEventsNavClick()
                }
            }
            true
        }
    }

    override fun showUser(mapUserFollowed: Map<User, String>) {
        mUser = mapUserFollowed.keys.first()
        if (mapUserFollowed[mUser] == "f")
            IS_FOLLOWED = true
        else if (mapUserFollowed[mUser] == "u")
            IS_LOGGED_USER = true

        user_activity2_fab.visibility = View.VISIBLE

        if (IS_FOLLOWED)
            user_activity2_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_full_24dp))
        else
            user_activity2_fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star_empty_24dp))

        user_activity2_collapsing_toolbar.title = mUser.name ?: mUser.login
        Picasso.with(applicationContext).load(mUser.avatarUrl).into(user_activity2_image)
    }

    private fun onFollowingNavClick() {
        user_activity_content_rl.visibility = View.GONE
        user_activity_content_rv.visibility = View.VISIBLE

        user_activity_content_rv.adapter = UserAdapter()

        val mScrollListenerFollowing = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_FOLLOWING)
                    return
                val visibleItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_content_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_FOLLOWING = true
                        PAGE_N_FOLLOWING += 1
                        (user_activity_content_rv.adapter as UserAdapter).addLoading()
                        mPresenter.getFollowing(username, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_content_rv.setOnScrollListener(mScrollListenerFollowing)

        showLoading()
        mPresenter.getFollowing(mUser.login, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
    }

    private fun onFollowersNavClick() {
        user_activity_content_rl.visibility = View.GONE
        user_activity_content_rv.visibility = View.VISIBLE

        user_activity_content_rv.adapter = UserAdapter()

        val mScrollListenerFollowers = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_FOLLOWERS)
                    return
                val visibleItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_content_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_FOLLOWERS = true
                        PAGE_N_FOLLOWERS += 1
                        (user_activity_content_rv.adapter as UserAdapter).addLoading()
                        mPresenter.getFollowers(username, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_content_rv.setOnScrollListener(mScrollListenerFollowers)

        showLoading()
        mPresenter.getFollowers(mUser.login, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS)
    }

    private fun onInfoNavClick() {
        user_activity_content_rl.visibility = View.VISIBLE
        user_activity_content_rv.visibility = View.GONE

    }

    private fun onReposNavClick() {
        user_activity_content_rl.visibility = View.GONE
        user_activity_content_rv.visibility = View.VISIBLE

        user_activity_content_rv.adapter = RepoListAdapter()

        mFilterRepos.put("sort","created")
        (user_activity_content_rv.adapter as RepoListAdapter).setFilter(mFilterRepos)

        val mScrollListenerRepos = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_REPOS || mFilterRepos["sort"] == "stars")
                    return
                val visibleItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_content_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_REPOS = true
                        PAGE_N_REPOS += 1
                        (user_activity_content_rv.adapter as RepoListAdapter).addLoading()
                        mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_content_rv.setOnScrollListener(mScrollListenerRepos)

        showLoading()
        mPresenter.getRepos(mUser.login, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
    }

    fun onEventsNavClick() {
        user_activity_content_rl.visibility = View.GONE
        user_activity_content_rv.visibility = View.VISIBLE

        user_activity_content_rv.adapter = EventAdapter()

        val mScrollListenerEvents = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_EVENTS)
                    return
                val visibleItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_activity_content_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_activity_content_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        LOADING_EVENTS = true
                        PAGE_N_EVENTS += 1
                        (user_activity_content_rv.adapter as EventAdapter).addLoading()
                        mPresenter.getEvents(username, PAGE_N_EVENTS, ITEMS_PER_PAGE_EVENTS)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_activity_content_rv.setOnScrollListener(mScrollListenerEvents)

        showLoading()
        mPresenter.getEvents(mUser.login, PAGE_N_EVENTS, ITEMS_PER_PAGE_EVENTS)
    }

    override fun showRepos(repoList: List<Repository>) {
        (user_activity_content_rv.adapter as RepoListAdapter).addRepos(repoList)
        if (PAGE_N_REPOS == 1 && repoList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_repositories)
        }
        LOADING_REPOS = false
    }

    override fun showEvents(eventList: List<Event>) {
        (user_activity_content_rv.adapter as EventAdapter).addEvents(eventList)
        if (PAGE_N_EVENTS == 1 && eventList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_events)
        }
        LOADING_EVENTS = false
    }

    override fun showFollowers(followerList: List<User>) {
        (user_activity_content_rv.adapter as UserAdapter).addUserList(followerList)
        if (PAGE_N_FOLLOWERS == 1 && followerList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_users)
        }
        LOADING_FOLLOWERS= false
    }

    override fun showFollowing(followingList: List<User>) {
        (user_activity_content_rv.adapter as UserAdapter).addUserList(followingList)
        if (PAGE_N_FOLLOWING == 1 && followingList.isEmpty()) {
            user_activity_content_no.visibility = View.VISIBLE
            user_activity_content_no.text = getString(R.string.no_users)
        }
        LOADING_FOLLOWING = false
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, UserActivity2::class.java)
        }
    }

}