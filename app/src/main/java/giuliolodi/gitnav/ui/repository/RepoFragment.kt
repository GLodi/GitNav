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

package giuliolodi.gitnav.ui.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import javax.inject.Inject
import android.content.Intent.ACTION_VIEW
import android.graphics.Color
import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import kotlinx.android.synthetic.main.repo_fragment.*

/**
 * Created by giulio on 10/07/2017.
 */
class RepoFragment : BaseFragment(), RepoContract.View {

    @Inject lateinit var mPresenter : RepoContract.Presenter<RepoContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mMenu: Menu? = null

    private var mRepoContentFragment: RepoContentFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.repo_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(repo_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.repository)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        repo_fragment_toolbar.setNavigationOnClickListener { (activity as RepoActivity).needToBack(true) }

        repo_fragment_tab_layout.visibility = View.VISIBLE
        repo_fragment_tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        repo_fragment_tab_layout.setupWithViewPager(repo_fragment_viewpager)
        repo_fragment_viewpager.offscreenPageLimit = 4
        if (mOwner != null && mName != null && mRepoContentFragment == null) {
            mRepoContentFragment = RepoContentFragment.newInstance(mOwner!!, mName!!)
            mRepoContentFragment?.let { repo_fragment_viewpager.adapter = MyAdapter(context, fragmentManager, mOwner!!, mName!!, it) }
        }
        else if (mOwner != null && mName != null && mRepoContentFragment != null) {
            mRepoContentFragment?.let { repo_fragment_viewpager.adapter = MyAdapter(context, fragmentManager, mOwner!!, mName!!, it) }
        }
        repo_fragment_viewpager.currentItem = 1

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showTitleAndSubtitle(title: String, subtitle: String) {
        (activity as AppCompatActivity).supportActionBar?.title = title
        (activity as AppCompatActivity).supportActionBar?.subtitle= subtitle
        mMenu?.findItem(R.id.fork_icon)?.isVisible = true
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun onRepoStarred() {
        mMenu?.findItem(R.id.star_icon)?.isVisible = true
        mMenu?.findItem(R.id.unstar_icon)?.isVisible = false
        Toasty.success(context, getString(R.string.repo_starred), Toast.LENGTH_LONG).show()
    }

    override fun onRepoUnstarred() {
        mMenu?.findItem(R.id.star_icon)?.isVisible = false
        mMenu?.findItem(R.id.unstar_icon)?.isVisible = true
        Toasty.success(context, getString(R.string.repo_unstarred), Toast.LENGTH_LONG).show()
    }

    override fun onRepoNotFound() {
        activity.finish()
    }

    override fun intentToBrowser(url: String) {
        val browserIntent = Intent(ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun onRepoForked() {
        Toasty.success(context, getString(R.string.repo_forked), Toast.LENGTH_LONG).show()
    }

    fun onActivityBackPress() {
        mRepoContentFragment?.onActivityBackPress()
    }

    override fun intentToForkedRepo(owner: String, name: String) {
        startActivity(RepoActivity.getIntent(context).putExtra("owner", owner).putExtra("name", name))
        activity.overridePendingTransition(0,0)
    }

    override fun createOptionsMenu(isRepoStarred: Boolean) {
        when (isRepoStarred) {
            true -> mMenu?.findItem(R.id.star_icon)?.isVisible = true
            false -> mMenu?.findItem(R.id.unstar_icon)?.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater?.inflate(R.menu.repo_fragment_menu, menu)
        menu?.let { mMenu = it }
        mPresenter.onOptionsMenuCreated()
        menu?.let { it.findItem(R.id.fork_icon).setOnMenuItemClickListener { _ ->
            val dialog: AlertDialog = AlertDialog.Builder(this.context)
                    .setTitle(getString(R.string.fork))
                    .setMessage(getString(R.string.confirm_fork) + "\n" + mOwner + "/" + mName + "?")
                    .setPositiveButton(getString(R.string.yes), { dialog, which ->
                        mPresenter.onForkRepo(isNetworkAvailable())
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .create()
            dialog.setOnShowListener {
                when (mPresenter.getTheme()) {
                    "light" -> {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.textColorPrimary))
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.textColorPrimary))
                    }
                    "dark" -> {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.textColorPrimaryInverse))
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.textColorPrimaryInverse))
                    }
                }
            }
            dialog.show()
            true
        } }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        if (isNetworkAvailable()) {
            when (item?.itemId) {
                R.id.star_icon -> mPresenter.onUnstarRepo(isNetworkAvailable())
                R.id.unstar_icon -> mPresenter.onStarRepo(isNetworkAvailable())
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

    private class MyAdapter(context: Context, fragmentManager: FragmentManager, owner: String, name: String, repoContentFragment: RepoContentFragment) : FragmentPagerAdapter(fragmentManager) {

        private val mContext: Context = context
        private val mOwner: String = owner
        private val mName: String = name
        private val mRepoContentFragment: RepoContentFragment = repoContentFragment

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> RepoAboutFragment.newInstance(mOwner, mName)
                1 -> RepoReadmeFragment.newInstance(mOwner, mName)
                2 -> mRepoContentFragment
                else -> RepoCommitsFragment.newInstance(mOwner, mName)
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.getString(R.string.about)
                1 -> return mContext.getString(R.string.readme)
                2 -> return mContext.getString(R.string.content)
                3 -> return mContext.getString(R.string.commits)
            }
            return super.getPageTitle(position)
        }

        override fun getCount(): Int {
            return 4
        }

    }

}