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
import kotlinx.android.synthetic.main.gist_fragment.*
import org.eclipse.egit.github.core.Gist
import javax.inject.Inject

/**
 * Created by giulio on 28/06/2017.
 */
class GistFragment : BaseFragment(), GistContract.View {

    @Inject lateinit var mPresenter: GistContract.Presenter<GistContract.View>

    private var mGist: Gist? = null
    private var mGistId: String? = null
    private var mMenu: Menu? = null
    private var IS_GIST_STARRED: Boolean = false

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

        if (isNetworkAvailable()) {
            mGistId?.let { mPresenter.subscribe(it) }
        }
        else {
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        }

    }

    override fun onGistDownloaded(isGistStarred: Boolean) {
        IS_GIST_STARRED = isGistStarred
        createOptionsMenu()
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

    private fun createOptionsMenu() {
        activity.menuInflater.inflate(R.menu.gist_fragment_menu, mMenu)
        if (IS_GIST_STARRED)
            mMenu?.findItem(R.id.follow_icon)?.isVisible = true
        else
            mMenu?.findItem(R.id.unfollow_icon)?.isVisible = true
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menu?.let { mMenu = it }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.follow_icon -> mGistId?.let { mPresenter.unstarGist(it) }
                R.id.unfollow_icon -> mGistId?.let { mPresenter.starGist(it) }
                R.id.open_in_browser -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mGist?.htmlUrl))
                    startActivity(browserIntent)
                }
            }
        } else
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
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
                0 -> GistFragmentFiles.newInstance(mGistId)
                else -> GistFragmentComments.newInstance(mGistId)
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