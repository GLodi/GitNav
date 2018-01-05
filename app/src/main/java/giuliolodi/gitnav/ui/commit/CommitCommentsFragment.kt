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

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.adapters.CommitCommentAdapter
import giuliolodi.gitnav.ui.base.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.commit_fragment_comments.*
import org.eclipse.egit.github.core.CommitComment
import javax.inject.Inject

/**
 * Created by giulio on 29/12/2017.
 */
class CommitCommentsFragment : BaseFragment(), CommitCommentsContract.View {

    @Inject lateinit var mPresenter: CommitCommentsContract.Presenter<CommitCommentsContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mSha: String? = null

    companion object {
        fun newInstance(owner: String, name: String, sha: String): CommitCommentsFragment {
            val commitCommentsFragment: CommitCommentsFragment = CommitCommentsFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            bundle.putString("sha", sha)
            commitCommentsFragment.arguments = bundle
            return commitCommentsFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
        mSha = arguments.getString("sha")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.commit_fragment_comments, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        val llmComments = LinearLayoutManager(context)
        llmComments.orientation = LinearLayoutManager.VERTICAL
        commit_fragment_comments_rv.layoutManager = llmComments
        commit_fragment_comments_rv.itemAnimator = DefaultItemAnimator()
        commit_fragment_comments_rv.adapter = CommitCommentAdapter()

        (commit_fragment_comments_rv.adapter as CommitCommentAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username -> mPresenter.onUserClick(username)}

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName, mSha)
    }

    override fun showComments(commitCommentList: List<CommitComment>) {
        (commit_fragment_comments_rv.adapter as CommitCommentAdapter).addCommitCommentList(commitCommentList)
    }

    override fun showLoading() {
        commit_fragment_comments_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        commit_fragment_comments_progressbar.visibility = View.GONE
    }

    override fun showNoComments() {
        commit_fragment_comments_nocomments.visibility = View.VISIBLE
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
    }

    override fun showNoConnectionError() {
        Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        mPresenter.onDetachView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
    }

}