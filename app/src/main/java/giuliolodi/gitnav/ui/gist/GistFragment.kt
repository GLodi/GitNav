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
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.option.OptionActivity
import kotlinx.android.synthetic.main.gist_fragment.*
import javax.inject.Inject

/**
 * Created by giulio on 28/06/2017.
 */
class GistFragment : BaseFragment(), GistContract.View {

    @Inject lateinit var mPresenter: GistContract.Presenter<GistContract.View>

    private var mGistId: String? = null
    private var mMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mGistId = activity.intent.getStringExtra("gistId")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.gist_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(gist_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.gist)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        gist_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        gist_fragment_tab_layout.visibility = View.VISIBLE
        gist_fragment_tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        gist_fragment_tab_layout.setupWithViewPager(gist_fragment_viewpager)
        gist_fragment_viewpager.offscreenPageLimit = 2
        mGistId?.let { gist_fragment_viewpager.adapter = MyAdapter(context, it, fragmentManager) }

        mGistId?.let { mPresenter.subscribe(isNetworkAvailable(), it) }
    }

    override fun onGistStarred() {
        mMenu?.findItem(R.id.follow_icon)?.isVisible = true
        mMenu?.findItem(R.id.unfollow_icon)?.isVisible = false
        Toasty.success(context, getString(R.string.gist_starred), Toast.LENGTH_LONG).show()
    }

    override fun onGistUnstarred() {
        mMenu?.findItem(R.id.follow_icon)?.isVisible = false
        mMenu?.findItem(R.id.unfollow_icon)?.isVisible = true
        Toasty.success(context, getString(R.string.gist_unstarred), Toast.LENGTH_LONG).show()
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun intentToBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun createOptionsMenu(isGistStarred: Boolean) {
        when (isGistStarred) {
            true -> mMenu?.findItem(R.id.follow_icon)?.isVisible = true
            false -> mMenu?.findItem(R.id.unfollow_icon)?.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater?.inflate(R.menu.gist_fragment_menu, menu)
        menu?.let { mMenu = it }
        mPresenter.onMenuCreated()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {
            startActivity(OptionActivity.getIntent(context))
            activity.overridePendingTransition(0,0)
        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.follow_icon -> mGistId?.let { mPresenter.unstarGist(it) }
                R.id.unfollow_icon -> mGistId?.let { mPresenter.starGist(it) }
                R.id.open_in_browser -> mPresenter.onOpenInBrowser()
            }
        } else {
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

    private class MyAdapter(context: Context, gistId: String, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val mContext: Context = context
        private val mGistId: String = gistId

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> GistFilesFragment.newInstance(mGistId)
                else -> GistCommentsFragment.newInstance(mGistId)
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.getString(R.string.files)
                1 -> return mContext.getString(R.string.comments)
            }
            return super.getPageTitle(position)
        }

        override fun getCount(): Int {
            return 2
        }

    }

}