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

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gist_fragment_comments.*
import org.eclipse.egit.github.core.Comment
import javax.inject.Inject

/**
 * Created by giulio on 03/07/2017.
 */
class GistFragmentComments : BaseFragment(), GistContractComments.View {

    @Inject lateinit var mPresenter: GistContractComments.Presenter<GistContractComments.View>

    private val mGistCommentList: MutableList<Comment> = mutableListOf()

    companion object {
        fun newInstance(gistId: String): GistFragmentComments {
            val gistFragmentComments: GistFragmentComments = GistFragmentComments()
            val bundle: Bundle = Bundle()
            bundle.putString("gistId", gistId)
            gistFragmentComments.arguments = bundle
            return gistFragmentComments
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.gist_fragment_comments, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)

        val llmComments = LinearLayoutManager(context)
        llmComments.orientation = LinearLayoutManager.VERTICAL
        gist_fragment_comments_rv.layoutManager = llmComments
        gist_fragment_comments_rv.itemAnimator = DefaultItemAnimator()
        gist_fragment_comments_rv.adapter = GistCommentAdapter()
        (gist_fragment_comments_rv.adapter as GistCommentAdapter).addGistCommentList(mGistCommentList)

        (gist_fragment_comments_rv.adapter as GistCommentAdapter).getImageClicks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { username ->
                    startActivity(UserActivity.getIntent(context).putExtra("username", username))
                    activity.overridePendingTransition(0,0)
                }
    }

    override fun showComments(gistCommentList: List<Comment>) {
        mGistCommentList.clear()
        mGistCommentList.addAll(gistCommentList.toMutableList())

        if (mGistCommentList.isEmpty()) {
            gist_fragment_comments_nocomments.visibility = View.VISIBLE
        }
    }

    override fun showLoading() {
        gist_fragment_comments_progressbar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if (gist_fragment_comments_progressbar.visibility == View.VISIBLE)
            gist_fragment_comments_progressbar.visibility = View.GONE
    }

    override fun showError(error: String) {
        Toasty.error(context, error, Toast.LENGTH_LONG).show()
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