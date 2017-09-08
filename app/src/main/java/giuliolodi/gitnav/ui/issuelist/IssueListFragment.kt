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

package giuliolodi.gitnav.ui.issuelist

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.*
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.issue_list_fragment.*

/**
 * Created by giulio on 01/09/2017.
 */
class IssueListFragment : BaseFragment() {

    private var mOwner: String? = null
    private var mName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.issue_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(issue_list_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.issues)
        (activity as AppCompatActivity).supportActionBar?.subtitle = mOwner + "/" + mName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        issue_list_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        issue_list_fragment_tab_layout.visibility = View.VISIBLE
        issue_list_fragment_tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        issue_list_fragment_tab_layout.setupWithViewPager(issue_list_fragment_viewpager)
        issue_list_fragment_viewpager.offscreenPageLimit = 2
        if (mOwner != null && mName != null) issue_list_fragment_viewpager.adapter = MyAdapter(mOwner!!, mName!!, context, fragmentManager)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        return super.onOptionsItemSelected(item)
    }

    private class MyAdapter(owner: String, name: String, context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val mContext: Context = context
        private val mOwner: String = owner
        private val mName: String = name

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> IssueOpenFragment.newInstance(mOwner, mName)
                else -> IssueClosedFragment.newInstance(mOwner, mName)
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.getString(R.string.open)
                1 -> return mContext.getString(R.string.closed)
            }
            return super.getPageTitle(position)
        }

        override fun getCount(): Int {
            return 2
        }

    }

}