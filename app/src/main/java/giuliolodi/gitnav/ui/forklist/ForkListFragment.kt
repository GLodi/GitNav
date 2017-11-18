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

package giuliolodi.gitnav.ui.forklist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.repository.RepoActivity
import giuliolodi.gitnav.ui.adapters.RepoListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fork_list_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 19/09/2017.
 */
class ForkListFragment : BaseFragment(), ForkListContract.View {

    @Inject lateinit var mPresenter: ForkListContract.Presenter<ForkListContract.View>

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
        return inflater?.inflate(R.layout.fork_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(fork_list_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.forks)
        (activity as AppCompatActivity).supportActionBar?.subtitle = mOwner + "/" + mName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        fork_list_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        fork_list_fragment_rv.layoutManager = llm
        fork_list_fragment_rv.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).showLastDivider().build())
        fork_list_fragment_rv.itemAnimator = DefaultItemAnimator()
        fork_list_fragment_rv.adapter = RepoListAdapter()
        (fork_list_fragment_rv.adapter as RepoListAdapter).getPositionClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repo -> mPresenter.onRepoClick(repo.owner.login, repo.name) }

        val mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val visibleItemCount = (fork_list_fragment_rv.layoutManager as LinearLayoutManager).childCount
                val totalItemCount = (fork_list_fragment_rv.layoutManager as LinearLayoutManager).itemCount
                val pastVisibleItems = (fork_list_fragment_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mPresenter.onLastItemVisible(isNetworkAvailable(), dy)
                }
            }
        }
        fork_list_fragment_rv.setOnScrollListener(mScrollListener)

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showForkList(forkList: List<Repository>) {
        (fork_list_fragment_rv.adapter as RepoListAdapter).addRepos(forkList)
    }

    override fun showLoading() {
        fork_list_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        fork_list_fragment_progress_bar.visibility = View.GONE
    }

    override fun showListLoading() {
        (fork_list_fragment_rv.adapter as RepoListAdapter).showLoading()
    }

    override fun hideListLoading() {
        (fork_list_fragment_rv.adapter as RepoListAdapter).hideLoading()
    }

    override fun showNoForks() {
        fork_list_fragment_no_forks.visibility = View.VISIBLE
    }

    override fun hideNoForks() {
        fork_list_fragment_no_forks.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun intentToRepoActivity(owner: String, name: String) {
        startActivity(RepoActivity.getIntent(context).putExtra("owner", owner).putExtra("name", name))
        activity.overridePendingTransition(0,0)
    }

    override fun onDestroyView() {
        mPresenter.onDetachView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
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

}