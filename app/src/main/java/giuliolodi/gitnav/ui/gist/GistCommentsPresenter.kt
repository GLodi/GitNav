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

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Comment
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 04/07/2017.
 */
class GistCommentsPresenter<V: GistCommentsContract.View> : BasePresenter<V>, GistCommentsContract.Presenter<V> {

    private val TAG = "GistCommentsPresenter"

    private val mGistCommentList: MutableList<Comment> = mutableListOf()
    private var mGistId: String? = null
    private var LOADING: Boolean = false
    private var NO_COMMENTS: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, gistId: String) {
        mGistId = gistId
        if (!mGistCommentList.isEmpty()) getView().showComments(mGistCommentList)
        else if (LOADING) getView().showLoading()
        else if (NO_COMMENTS) getView().showNoComments()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                mGistId?.let { getComments(it) }
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
            }
        }
    }

    override fun getComments(gistId: String) {
        getCompositeDisposable().add(getDataManager().getGistComments(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { gistCommentList ->
                            getView().hideLoading()
                            mGistCommentList.addAll(gistCommentList)
                            getView().showComments(gistCommentList)
                            if (mGistCommentList.isEmpty()) {
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
        getView().intentToUserActivity(username)
    }

}