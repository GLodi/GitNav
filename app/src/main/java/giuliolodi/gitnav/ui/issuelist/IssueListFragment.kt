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
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.issue_list_fragment.*
import javax.inject.Inject

/**
 * Created by giulio on 01/09/2017.
 */
class IssueListFragment : BaseFragment(), IssueListContract.View {

    @Inject lateinit var mPresenter: IssueListContract.Presenter<IssueListContract.View>

    private var mOwner: String? = null
    private var mName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.issue_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(issue_list_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.issues)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        issue_list_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        issue_list_fragment_tab_layout.visibility = View.VISIBLE
        issue_list_fragment_tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        issue_list_fragment_tab_layout.setupWithViewPager(issue_list_fragment_viewpager)
        issue_list_fragment_viewpager.offscreenPageLimit = 2
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, menuInflater: MenuInflater?) {
        menuInflater?.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

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