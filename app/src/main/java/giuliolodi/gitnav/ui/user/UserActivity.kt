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
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.user_activity.*
import org.eclipse.egit.github.core.User
import javax.inject.Inject
import android.support.v4.view.ViewPager
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.ui.repositories.RepoListAdapter
import giuliolodi.gitnav.utils.NetworkUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.user_followers.*
import kotlinx.android.synthetic.main.user_following.*
import kotlinx.android.synthetic.main.user_repos.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.eclipse.egit.github.core.Repository

/**
 * Created by giulio on 19/05/2017.
 */

class UserActivity : BaseDrawerActivity(), UserContract.View {

    val TAG = "UserActivity"

    @Inject lateinit var mPresenter: UserContract.Presenter<UserContract.View>

    private val mViews: MutableList<Int> = mutableListOf()
    private lateinit var username: String
    private lateinit var mMenu: Menu
    private lateinit var mUser: User

    private var IS_FOLLOWED: Boolean = false
    private var HAS_CLICKED_BIO: Boolean = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.user_activity, content_frame)

        initLayout()

        username = intent.getStringExtra("username")

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (NetworkUtils.isNetworkAvailable(applicationContext))
            mPresenter.subscribe(username)
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    private fun initLayout() {
        supportActionBar?.title = "Profile"

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        mViews.add(R.layout.user_repos)
        mViews.add(R.layout.user_followers)
        mViews.add(R.layout.user_following)

        user_activity_vp.offscreenPageLimit = 3
        user_activity_vp.adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return 3
            }
            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view == `object`
            }
            override fun destroyItem(container: View?, position: Int, `object`: Any?) {
                (container as ViewPager).removeView(`object` as View?)
            }
            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val inflater = LayoutInflater.from(applicationContext)
                val layout = inflater.inflate(mViews[position], container, false) as ViewGroup
                container.addView(layout)
                return layout
            }
        }
    }

    override fun showUser(mapUserFollowed: Map<User, Boolean>) {
        mUser = mapUserFollowed.keys.first()
        IS_FOLLOWED = mapUserFollowed[mUser]!!

        createOptionMenu()

        // Repos
        val llmRepos = LinearLayoutManager(applicationContext)
        llmRepos.orientation = LinearLayoutManager.VERTICAL
        user_repos_rv.layoutManager = llmRepos
        user_repos_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        user_repos_rv.itemAnimator = DefaultItemAnimator()
        user_repos_rv.adapter = RepoListAdapter()

        // Followers
        val llmFollowers = LinearLayoutManager(applicationContext)
        llmFollowers.orientation = LinearLayoutManager.VERTICAL
        user_followers_rv.layoutManager = llmFollowers
        user_followers_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        user_followers_rv.itemAnimator = DefaultItemAnimator()
        user_followers_rv.adapter = UserAdapter()

        // Following
        val llmFollowing = LinearLayoutManager(applicationContext)
        llmFollowing.orientation = LinearLayoutManager.VERTICAL
        user_following_rv.layoutManager = llmFollowing
        user_following_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        user_following_rv.itemAnimator = DefaultItemAnimator()
        user_following_rv.adapter = UserAdapter()

        (user_followers_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        (user_following_rv.adapter as UserAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        setupOnScrollListener()

        Picasso.with(applicationContext).load(mUser.avatarUrl).resize(150, 150).centerCrop().into(user_activity_image)

        if (mUser.name != null) {
            user_activity_fullname.text = mUser.name
            user_activity_username.text = mUser.login
        }
        else {
            user_activity_fullname.text = mUser.login
            user_activity_username.visibility = View.GONE
        }
        if (mUser.bio != null) {
            user_activity_description.visibility = View.VISIBLE
            user_activity_description.maxLines = 2
            user_activity_description.ellipsize = TextUtils.TruncateAt.END
            user_activity_description.text = mUser.bio
            user_activity_description.setOnClickListener {
                HAS_CLICKED_BIO = !HAS_CLICKED_BIO
                if (HAS_CLICKED_BIO)
                    user_activity_description.maxLines = 2
                else
                    user_activity_description.maxLines = 100
            }
        }
        if (mUser.email != null) {
            user_activity_mail.visibility = View.VISIBLE
            user_activity_mail.text = mUser.email
        }

        val mTitleDataList: MutableList<String> = mutableListOf()
        mTitleDataList.add("REPOSITORIES")
        mTitleDataList.add("FOLLOWERS")
        mTitleDataList.add("FOLLOWING")

        val mNumberDataList: MutableList<String> = mutableListOf()
        mNumberDataList.add(mUser.publicRepos.toString())
        mNumberDataList.add(mUser.followers.toString())
        mNumberDataList.add(mUser.following.toString())

        val commonNavigator: CommonNavigator = CommonNavigator(applicationContext)
        commonNavigator.isAdjustMode = true
        commonNavigator.adapter = object: CommonNavigatorAdapter() {
            override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                val commonPagerTitleView = CommonPagerTitleView(context)
                val customLayout = LayoutInflater.from(context).inflate(R.layout.user_tab, null)
                commonPagerTitleView.setContentView(customLayout)
                val user_tab_n = customLayout.findViewById(R.id.user_tab_n) as TextView
                val user_tab_title = customLayout.findViewById(R.id.user_tab_title) as TextView
                user_tab_n.text = mNumberDataList[index]
                user_tab_title.text = mTitleDataList[index]
                commonPagerTitleView.setOnClickListener { user_activity_vp.currentItem = index }
                return commonPagerTitleView
            }
            override fun getIndicator(p0: Context?): IPagerIndicator {
                val indicator: LinePagerIndicator = LinePagerIndicator(applicationContext)
                indicator.mode = LinePagerIndicator.MODE_MATCH_EDGE
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.setColors(Color.parseColor("#448AFF"))
                return indicator
            }
            override fun getCount(): Int {
                return mTitleDataList.size
            }
        }
        user_activity_magic_indicator.navigator = commonNavigator
        ViewPagerHelper.bind(user_activity_magic_indicator, user_activity_vp)

        mFilterRepos.put("sort","created")

        // Initialize recycler views
        showLoadingUserRepos()
        showLoadingUserFollowers()
        showLoadingUserFollowing()
        mPresenter.getRepos(mUser.login, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
        mPresenter.getFollowers(mUser.login, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS)
        mPresenter.getFollowing(mUser.login, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
    }

    override fun showUserRepos(repoList: List<Repository>) {
        LOADING_REPOS = false
        (user_repos_rv.adapter as RepoListAdapter).addRepos(repoList)
        if (PAGE_N_REPOS == 1 && repoList.isEmpty())
            user_repos_tv.visibility = View.VISIBLE
    }

    override fun showUserFollowers(userList: List<User>) {
        LOADING_FOLLOWERS = false
        (user_followers_rv.adapter as UserAdapter).addUserList(userList)
        if (PAGE_N_FOLLOWERS == 1 && userList.isEmpty())
            user_followers_tv.visibility = View.VISIBLE
    }

    override fun showUserFollowing(userList: List<User>) {
        LOADING_FOLLOWING = false
        (user_following_rv.adapter as UserAdapter).addUserList(userList)
        if (PAGE_N_FOLLOWING == 1 && userList.isEmpty())
            user_following_tv.visibility = View.VISIBLE
    }

    override fun showLoading() {
        user_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun showLoadingUserRepos() {
        user_repos_progressbar.visibility = View.VISIBLE
    }

    override fun showLoadingUserFollowers() {
        user_followers_progressbar.visibility = View.VISIBLE
    }

    override fun showLoadingUserFollowing() {
        user_following_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (user_activity_progress_bar.visibility == View.VISIBLE) {
            user_activity_progress_bar.visibility = View.GONE
            user_activity_layout.visibility = View.VISIBLE
        }
    }

    override fun hideLoadingUserRepos() {
        if (user_repos_progressbar.visibility == View.VISIBLE)
            user_repos_progressbar.visibility = View.GONE
    }

    override fun hideLoadingUserFollowers() {
        if (user_followers_progressbar.visibility == View.VISIBLE)
            user_followers_progressbar.visibility = View.GONE
    }

    override fun hideLoadingUserFollowing() {
        if (user_following_progressbar.visibility == View.VISIBLE)
            user_following_progressbar.visibility = View.GONE
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
        super.onCreateOptionsMenu(mMenu)

        if (mUser.login != username && !IS_FOLLOWED)
            mMenu.findItem(R.id.unfollow_icon).isVisible = true
        else if (mUser.login != username && IS_FOLLOWED)
            mMenu.findItem(R.id.follow_icon).isVisible = false
        if (mUser.login != username && !mUser.email?.isEmpty()!!)
            mMenu.findItem(R.id.send_email).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (NetworkUtils.isNetworkAvailable(applicationContext)) {
            when (item?.itemId) {
                R.id.unfollow_icon -> mPresenter.followUser(username)
                R.id.follow_icon -> mPresenter.unFollowUser(username)
                R.id.send_email -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mUser.email))
                    startActivity(Intent.createChooser(emailIntent, "Email"))
                }
                R.id.open_in_browser -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mUser.htmlUrl))
                    startActivity(browserIntent)
                }
            }
        }
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        return super.onOptionsItemSelected(item)
    }

    private fun setupOnScrollListener() {
        val mScrollListenerRepos = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_REPOS || mFilterRepos["sort"] == "starred")
                    return
                val visibleItemCount = (user_repos_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_repos_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_repos_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (NetworkUtils.isNetworkAvailable(applicationContext)) {
                        LOADING_REPOS = true
                        PAGE_N_REPOS += 1
                        (user_repos_rv.adapter as RepoListAdapter).addLoading()
                        mPresenter.getRepos(username, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_repos_rv.setOnScrollListener(mScrollListenerRepos)
        val mScrollListenerFollowers = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_FOLLOWERS)
                    return
                val visibleItemCount = (user_followers_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_followers_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_followers_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (NetworkUtils.isNetworkAvailable(applicationContext)) {
                        LOADING_FOLLOWERS = true
                        PAGE_N_FOLLOWERS += 1
                        (user_followers_rv.adapter as UserAdapter).addLoading()
                        mPresenter.getFollowers(username, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_followers_rv.setOnScrollListener(mScrollListenerFollowers)
        val mScrollListenerFollowing = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (LOADING_FOLLOWING)
                    return
                val visibleItemCount = (user_following_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (user_following_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (user_following_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (NetworkUtils.isNetworkAvailable(applicationContext)) {
                        LOADING_FOLLOWING = true
                        PAGE_N_FOLLOWING += 1
                        (user_following_rv.adapter as UserAdapter).addLoading()
                        mPresenter.getFollowing(username, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
                    }
                    else if (dy > 0) {
                        Handler(Looper.getMainLooper()).post({ Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show() })
                    }
                }
            }
        }
        user_following_rv.setOnScrollListener(mScrollListenerFollowing)
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
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