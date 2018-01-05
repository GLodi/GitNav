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

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.CommitComment
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 29/12/2017.
 */
class CommitCommentsPresenter<V: CommitCommentsContract.View> : BasePresenter<V>, CommitCommentsContract.Presenter<V> {

    private val TAG = "CommitCommentsPresenter"

    private var mCommitCommentList: MutableList<CommitComment> = mutableListOf()
    private var mOwner: String? = null
    private var mName: String? = null
    private var mSha: String? = null
    private var NO_COMMENTS: Boolean = false
    private var LOADING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?, sha: String?) {
        mOwner = owner
        mName = name
        mSha = sha
        if (!mCommitCommentList.isEmpty()) getView().showComments(mCommitCommentList)
        else if (NO_COMMENTS) getView().showNoComments()
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                if (mOwner != null && mName != null && mSha != null) loadComments()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadComments() {
        getCompositeDisposable().add(getDataManager().getCommitComments(mOwner!!, mName!!, mSha!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { commitCommentList ->
                            getView().hideLoading()
                            mCommitCommentList.addAll(commitCommentList)
                            getView().showComments(commitCommentList)
                            if (mCommitCommentList.isEmpty()) {
                                getView().showNoComments()
                                NO_COMMENTS = true
                            }
                            LOADING = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            getView().hideLoading()
                            Timber.e(throwable)
                            LOADING = false
                        }
                ))
    }

    override fun onUserClick(username: String) {

    }

}