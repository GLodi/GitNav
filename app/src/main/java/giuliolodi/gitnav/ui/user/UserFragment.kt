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
import android.os.Handler
import android.os.Looper
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.events.EventAdapter
import giuliolodi.gitnav.ui.repository.RepoActivity
import giuliolodi.gitnav.ui.repositorylist.RepoListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

        (activity as AppCompatActivity).setSupportActionBar(user_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        user_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        user_fragment_rv.layoutManager = LinearLayoutManager(context)
        user_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        user_fragment_rv.itemAnimator = DefaultItemAnimator()
        user_fragment_rv.setHasFixedSize(true)
        user_fragment_rv.isNestedScrollingEnabled = false

        user_fragment_bottomnv.selectedItemId = R.id.user_activity_bottom_menu_info
        user_fragment_bottomnv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.user_activity_bottom_menu_following -> { mPresenter.onFollowingNavClick(isNetworkAvailable()) }
                R.id.user_activity_bottom_menu_followers -> { mPresenter.onFollowersNavClick(isNetworkAvailable()) }
                R.id.user_activity_bottom_menu_info -> { mPresenter.onInfoNavClick(isNetworkAvailable()) }
                R.id.user_activity_bottom_menu_repos -> { mPresenter.onReposNavClick(isNetworkAvailable()) }
                R.id.user_activity_bottom_menu_events -> { mPresenter.onEventsNavClick(isNetworkAvailable()) }
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
                    mPresenter.unFollowUser()
                else
                    mPresenter.followUser()
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

    override fun setupFollowing(username: String) {
        user_fragment_appbar.setExpanded(false)
        user_fragment_nestedscrollview.isNestedScrollingEnabled = false
        val params = user_fragment_appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        user_fragment_content_rl.visibility = View.GONE
        user_fragment_rv.visibility = View.VISIBLE
        mMenu?.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = false }

        user_fragment_rv.adapter = UserAdapter()
        val mScrollListenerFollowing = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        mPresenter.onLastFollowingVisible(isNetworkAvailable(), dy)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_fragment_rv.setOnScrollListener(mScrollListenerFollowing)

        (user_fragment_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0,0)
                }
    }

    override fun setupFollowers(username: String) {
        user_fragment_appbar.setExpanded(false)
        user_fragment_nestedscrollview.isNestedScrollingEnabled = false
        val params = user_fragment_appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        user_fragment_content_rl.visibility = View.GONE
        user_fragment_rv.visibility = View.VISIBLE
        mMenu?.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = false }

        user_fragment_rv.adapter = UserAdapter()
        val mScrollListenerFollowers = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        mPresenter.onLastFollowerVisible(isNetworkAvailable(), dy)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_fragment_rv.setOnScrollListener(mScrollListenerFollowers)

        (user_fragment_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0,0)
                }
    }

    override fun setupRepos(username: String, filter: HashMap<String,String>) {
        user_fragment_appbar.setExpanded(false)
        user_fragment_nestedscrollview.isNestedScrollingEnabled = false
        val params = user_fragment_appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        user_fragment_content_rl.visibility = View.GONE
        user_fragment_rv.visibility = View.VISIBLE
        mMenu?.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = true }
        mMenu?.findItem(R.id.user_menu_created)?.let { it.isChecked = true }

        user_fragment_rv.adapter = RepoListAdapter()
        (user_fragment_rv.adapter as RepoListAdapter).setFilter(filter)

        val mScrollListenerRepos = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        mPresenter.onLastRepoVisible(isNetworkAvailable(), dy)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_fragment_rv.setOnScrollListener(mScrollListenerRepos)

        (user_fragment_rv.adapter as RepoListAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repo ->
                    startActivity(RepoActivity.getIntent(context).putExtra("owner", repo.owner.login).putExtra("name", repo.name))
                    activity.overridePendingTransition(0,0)
                }
    }

    override fun setupEvents(username: String) {
        user_fragment_appbar.setExpanded(false)
        user_fragment_nestedscrollview.isNestedScrollingEnabled = false
        val params = user_fragment_appbar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        mPresenter.unsubscribe()

        user_fragment_content_rl.visibility = View.GONE
        user_fragment_rv.visibility = View.VISIBLE
        user_fragment_content_no.visibility = View.GONE
        mMenu?.findItem(R.id.user_menu_sort_icon)?.let { it.isVisible = false }

        user_fragment_rv.adapter = EventAdapter()
        val mScrollListenerEvents = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isNetworkAvailable()) {
                        mPresenter.onLastEventVisible(isNetworkAvailable(), dy)
                    } else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_fragment_rv.setOnScrollListener(mScrollListenerEvents)

        (user_fragment_rv.adapter as EventAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0,0)
                }
    }

    override fun showRepos(repoList: List<Repository>) {
        (user_fragment_rv.adapter as RepoListAdapter).addRepos(repoList)
    }

    override fun showEvents(eventList: List<Event>) {
        (user_fragment_rv.adapter as EventAdapter).addEvents(eventList)
    }

    override fun showFollowers(followerList: List<User>) {
        (user_fragment_rv.adapter as UserAdapter).addUserList(followerList)
    }

    override fun showFollowing(followingList: List<User>) {
        (user_fragment_rv.adapter as UserAdapter).addUserList(followingList)
    }

    override fun showLoading() {
        user_fragment_content_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        user_fragment_content_progress_bar.visibility = View.GONE
    }

    override fun showUserLoading() {
        (user_fragment_rv.adapter as UserAdapter).addLoading()
    }

    override fun hideUserLoading() {
        (user_fragment_rv.adapter as UserAdapter).hideLoading()
    }

    override fun showRepoLoading() {
        (user_fragment_rv.adapter as RepoListAdapter).showLoading()
    }

    override fun hideRepoLoading() {
        (user_fragment_rv.adapter as RepoListAdapter).hideLoading()
    }

    override fun showEventLoading() {
        (user_fragment_rv.adapter as EventAdapter).showLoading()
    }

    override fun hideEventLoading() {
        (user_fragment_rv.adapter as EventAdapter).hideLoading()
    }

    override fun onFollowCompleted() {
        user_fragment_fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_full_24dp))
        Toasty.success(context, getString(R.string.user_followed), Toast.LENGTH_LONG).show()
    }

    override fun onUnfollowCompleted() {
        user_fragment_fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_empty_24dp))
        Toasty.success(context, getString(R.string.user_unfollowed), Toast.LENGTH_LONG).show()
    }

    override fun showNoUsers() {
        user_fragment_content_no.visibility = View.VISIBLE
        user_fragment_content_no.text = getString(R.string.no_users)
    }

    override fun showNoRepos() {
        user_fragment_content_no.visibility = View.VISIBLE
        user_fragment_content_no.text = getString(R.string.no_repositories)
    }

    override fun showNoEvents() {
        user_fragment_content_no.visibility = View.VISIBLE
        user_fragment_content_no.text = getString(R.string.no_events)
    }

    override fun hideNoContent() {
        user_fragment_content_no.visibility = View.GONE
    }

    override fun clearRepoList() {
        (user_fragment_rv.adapter as RepoListAdapter).clear()
    }

    override fun intentToBrowser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun pressBack() {
        activity.onBackPressed()
    }

    override fun createOptionsMenu() {
        mMenuInflater?.inflate(R.menu.user_menu, mMenu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.let { mMenu = it }
        inflater?.let { mMenuInflater = it }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (item?.itemId == android.R.id.home) {
            activity.onBackPressed()
            activity.finish()
            activity.overridePendingTransition(0,0)
        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.user_menu_created -> {
                    item.isChecked = true
                    mPresenter.onUserMenuCreatedClick()
                }
                R.id.user_menu_updated -> {
                    item.isChecked = true
                    mPresenter.onUserMenuUpdatedClick()
                }
                R.id.user_menu_pushed -> {
                    item.isChecked = true
                    mPresenter.onUserMenuPushedClick()
                }
                R.id.user_menu_alphabetical -> {
                    item.isChecked = true
                    mPresenter.onUserMenuAlphabeticalClick()
                }
                R.id.user_menu_stars -> {
                    item.isChecked = true
                    mPresenter.onUserMenuStarsClick()
                }
                R.id.open_in_browser -> { mPresenter.onOpenInBrowserClick() }
            }
        }
        else {
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
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