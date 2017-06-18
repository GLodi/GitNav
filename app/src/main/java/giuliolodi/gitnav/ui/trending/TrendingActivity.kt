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

package giuliolodi.gitnav.ui.trending

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseDrawerActivity
import giuliolodi.gitnav.ui.starred.StarredAdapter
import giuliolodi.gitnav.ui.user.UserActivity2
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_base_drawer.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.trending_activity.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 18/05/2017.
 */

class TrendingActivity : BaseDrawerActivity(), TrendingContract.View {

    @Inject lateinit var mPresenter: TrendingContract.Presenter<TrendingContract.View>

    var period: String = "daily"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.trending_activity, content_frame)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)
    }

    private fun initLayout() {
        supportActionBar?.title = ""

        val llm = LinearLayoutManager(applicationContext)
        llm.orientation = LinearLayoutManager.VERTICAL

        trending_activity_rv.layoutManager = llm
        trending_activity_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).showLastDivider().build())
        trending_activity_rv.itemAnimator = DefaultItemAnimator()
        trending_activity_rv.adapter = StarredAdapter()
        (trending_activity_rv.adapter as StarredAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity2.getIntent(applicationContext).putExtra("username", username))
                    overridePendingTransition(0,0)
                }

        main_spinner.visibility = View.VISIBLE
        val spinnerAdapter = ArrayAdapter<String>(supportActionBar!!.themedContext, R.layout.spinner_list_style, resources.getStringArray(R.array.trending_array))
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        main_spinner.adapter = spinnerAdapter
        main_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isNetworkAvailable()) {
                    showLoading()
                    trending_activity_swipe.isRefreshing = false
                    trending_activity_no_repo.visibility = View.GONE
                    (trending_activity_rv.adapter as StarredAdapter).clear()
                    mPresenter.unsubscribe()
                    when (position) {
                        0 -> mPresenter.subscribe("daily")
                        1 -> mPresenter.subscribe("weekly")
                        2 -> mPresenter.subscribe("monthly")
                    }
                }
                else
                    Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        trending_activity_swipe.setColorSchemeColors(Color.parseColor("#448AFF"))
        trending_activity_swipe.setOnRefreshListener {
            if (isNetworkAvailable()) {
                (trending_activity_rv.adapter as StarredAdapter).clear()
                mPresenter.subscribe(period)
            }
            else
                Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        }

    }

    override fun addRepo(repo: Repository) {
        (trending_activity_rv.adapter as StarredAdapter).addRepo(repo)
    }

    override fun showLoading() {
        trending_activity_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (trending_activity_progress_bar.visibility == View.VISIBLE)
            trending_activity_progress_bar.visibility = View.GONE
        if (trending_activity_swipe.isRefreshing)
            trending_activity_swipe.isRefreshing = false
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    override fun onComplete() {
        if ((trending_activity_rv.adapter as StarredAdapter).itemCount == 0)
            trending_activity_no_repo.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_options) {

        }
        return super.onOptionsItemSelected(item)
    }

    override fun showNoRepo() {
        trending_activity_no_repo.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(4).isChecked = true
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
            return Intent(context, TrendingActivity::class.java)
        }
    }

}