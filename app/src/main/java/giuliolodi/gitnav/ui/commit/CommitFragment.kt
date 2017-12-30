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

package giuliolodi.gitnav.ui.commit

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
import kotlinx.android.synthetic.main.commit_fragment.*
import org.eclipse.egit.github.core.Commit
import javax.inject.Inject

/**
 * Created by giulio on 20/12/2017.
 */
class CommitFragment : BaseFragment(), CommitContract.View {

    @Inject lateinit var mPresenter: CommitContract.Presenter<CommitContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mSha: String? = null
    private var mCommitUrl: String? = null
    private var mCommitTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
        mSha = activity.intent.getStringExtra("sha")
        mCommitUrl = activity.intent.getStringExtra("commit_url")
        mCommitTitle = activity.intent.getStringExtra("commit_title")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.commit_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(commit_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = mCommitTitle
        (activity as AppCompatActivity).supportActionBar?.subtitle = mSha
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        commit_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        commit_fragment_tab_layout.visibility = View.VISIBLE
        commit_fragment_tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        commit_fragment_tab_layout.setupWithViewPager(commit_fragment_viewpager)
        commit_fragment_viewpager.offscreenPageLimit = 2
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

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater?.inflate(R.menu.commit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
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

    private class MyAdapter(context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val mContext: Context = context

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> CommitFilesFragment.newInstance(mGistId)
                else -> CommitCommentsFragment.newInstance(mGistId)
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