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

package giuliolodi.gitnav.ui.gist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import giuliolodi.gitnav.ui.user.UserActivity2
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gist_activity.*
import kotlinx.android.synthetic.main.gist_activity_comments.*
import kotlinx.android.synthetic.main.gist_activity_files.*
import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.Gist
import org.ocpsoft.prettytime.PrettyTime
import javax.inject.Inject

/**
 * Created by giulio on 26/05/2017.
 */

class GistActivity : BaseActivity(), GistContract.View {

    @Inject lateinit var mPresenter: GistContract.Presenter<GistContract.View>

    private val mViews: MutableList<Int> = arrayListOf()

    private lateinit var mGist: Gist
    private lateinit var mGistId: String
    private lateinit var mMenu: Menu
    private var IS_GIST_STARRED: Boolean? = false
    private val mPrettyTime: PrettyTime = PrettyTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gist_activity)

        initLayout()

        mGistId = intent.getStringExtra("gistId")

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (isNetworkAvailable())
            mPresenter.subscribe(mGistId)
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    private fun initLayout() {
        setSupportActionBar(gist_activity_toolbar)
        supportActionBar?.title = "Gist"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        gist_activity_toolbar.setNavigationOnClickListener { onBackPressed() }

        mViews.add(R.layout.gist_activity_files)
        mViews.add(R.layout.gist_activity_comments)

        gist_activity_viewpager.offscreenPageLimit = 2
        gist_activity_viewpager.adapter = MyAdapter(applicationContext, mViews)

        gist_activity_tab_layout.visibility = View.VISIBLE
        gist_activity_tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        gist_activity_tab_layout.setupWithViewPager(gist_activity_viewpager)
    }

    override fun showGist(map: Map<Gist,Boolean>) {
        mGist = map.keys.first()
        IS_GIST_STARRED = map[mGist]
        mPresenter.getComments(mGistId)

        createOptionsMenu()

        val llmFiles = LinearLayoutManager(applicationContext)
        llmFiles.orientation = LinearLayoutManager.VERTICAL
        gist_activity_files_rv.layoutManager = llmFiles
        gist_activity_files_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        gist_activity_files_rv.itemAnimator = DefaultItemAnimator()
        gist_activity_files_rv.adapter = GistFileAdapter()
        (gist_activity_files_rv.adapter as GistFileAdapter).addGistFileList(mGist.files.values.toMutableList())

        gist_activity_files_progress_bar.visibility = View.GONE
        gist_activity_files_nested.visibility = View.VISIBLE
        gist_activity_files_username.text = mGist.owner.login
        gist_activity_files_title.text = mGist.description
        gist_activity_files_date.text = mPrettyTime.format(mGist.createdAt)
        gist_activity_files_sha.text = mGist.id
        gist_activity_files_status.text = if (mGist.isPublic) getString(R.string.publics) else getString(R.string.privates)
        gist_activity_files_date.visibility = View.VISIBLE
        Picasso.with(applicationContext).load(mGist.owner.avatarUrl).centerCrop().resize(75, 75).into(gist_activity_files_image)
    }

    override fun showComments(gistCommentList: List<Comment>) {
        if (gistCommentList.isEmpty())
            gist_activity_comments_nocomments.visibility = View.VISIBLE

        val llmComments = LinearLayoutManager(applicationContext)
        llmComments.orientation = LinearLayoutManager.VERTICAL
        gist_activity_comments_rv.layoutManager = llmComments
        gist_activity_comments_rv.itemAnimator = DefaultItemAnimator()
        gist_activity_comments_rv.adapter = GistCommentAdapter()
        (gist_activity_comments_rv.adapter as GistCommentAdapter).addGistCommentList(gistCommentList)

        (gist_activity_comments_rv.adapter as GistCommentAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity2.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }
    }

    override fun showLoadingComments() {
        gist_activity_comments_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoadingComments() {
        if (gist_activity_comments_progressbar.visibility == View.VISIBLE)
            gist_activity_comments_progressbar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    private class MyAdapter(context: Context, views: List<Int>) : PagerAdapter() {

        private var mContext = context
        private var mViews = views

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            val layout = LayoutInflater.from(mContext).inflate(mViews[position], container, false)
            container?.addView(layout)
            return layout
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.getString(R.string.files)
                1 -> return mContext.getString(R.string.comments)
            }
            return super.getPageTitle(position)
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            container?.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view?.equals(`object`)!!
        }

        override fun getCount(): Int {
            return 2
        }

    }

    override fun onGistStarred() {
        mMenu.findItem(R.id.follow_icon).isVisible = true
        mMenu.findItem(R.id.unfollow_icon).isVisible = false
        Toasty.success(applicationContext, getString(R.string.gist_starred), Toast.LENGTH_LONG).show()
    }

    override fun onGistUnstarred() {
        mMenu.findItem(R.id.follow_icon).isVisible = false
        mMenu.findItem(R.id.unfollow_icon).isVisible = true
        Toasty.success(applicationContext, getString(R.string.gist_unstarred), Toast.LENGTH_LONG).show()
    }

    private fun createOptionsMenu() {
        menuInflater.inflate(R.menu.gist_activity_menu, mMenu)
        if (IS_GIST_STARRED!!)
            mMenu.findItem(R.id.follow_icon).isVisible = true
        else
            mMenu.findItem(R.id.unfollow_icon).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.follow_icon -> mPresenter.unstarGist(mGistId)
                R.id.unfollow_icon -> mPresenter.starGist(mGistId)
                R.id.open_in_browser -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mGist.htmlUrl))
                    startActivity(browserIntent)
                }
            }
        }
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mMenu = menu!!
        return true
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
            return Intent(context, GistActivity::class.java)
        }
    }

}