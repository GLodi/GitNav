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

package giuliolodi.gitnav.ui.gists

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.*
import android.widget.Toast
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import kotlinx.android.synthetic.main.activity_base_drawer.*
import org.eclipse.egit.github.core.Gist
import javax.inject.Inject
import giuliolodi.gitnav.R.string.network_error
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.utils.NetworkUtils
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.gist_list_activity.*

/**
 * Created by giulio on 23/05/2017.
 */

class GistListActivity : BaseDrawerActivity(), GistListContract.View {

    @Inject lateinit var mPresenter: GistListContract.Presenter<GistListContract.View>

    private val mViews: MutableList<Int> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.gist_list_activity, content_frame)

        mViews.add(R.layout.gist_list_mine)
        mViews.add(R.layout.gist_list_starred)

        gist_list_viewpager.offscreenPageLimit = 2
        gist_list_viewpager.adapter = GistListAdapter(applicationContext, mViews)

        tab_layout.visibility = View.VISIBLE
        tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        tab_layout.setupWithViewPager(gist_list_viewpager)
    }

    override fun showMineGists(gistList: List<Gist>) {
    }

    override fun showStarredGists(gistList: List<Gist>) {
    }

    override fun showLoadingMine() {
    }

    override fun showLoadingStarred() {
    }

    override fun hideLoadingMine() {
    }

    override fun hideLoadingStarred() {
    }

    override fun showError(error: String) {
    }

    private  class GistListAdapter(context: Context, views: List<Int>) : PagerAdapter() {

        private var mContext = context
        private var mViews = views

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            val layout = LayoutInflater.from(mContext).inflate(mViews[position], container, false)
            container?.addView(layout)
            return layout
        }

        override fun getPageTitle(position: Int): CharSequence {
            when (position) {
                0 -> return mContext.getString(R.string.mine)
                1 -> return mContext.getString(R.string.starred)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (NetworkUtils.isNetworkAvailable(applicationContext)) {

        } else
            Toasty.warning(applicationContext, getString(network_error), Toast.LENGTH_LONG).show()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(5).isChecked = true
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
            return Intent(context, GistListActivity::class.java)
        }
    }
}