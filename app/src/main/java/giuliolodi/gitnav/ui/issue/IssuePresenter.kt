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

package giuliolodi.gitnav.ui.issue

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.Issue
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 14/11/2017.
 */
class IssuePresenter<V: IssueContract.View> : BasePresenter<V>, IssueContract.Presenter<V> {

    private val TAG = "IssuePresenter"

    private var mOwner: String? = null
    private var mName: String? = null
    private var mIssueNumber: Int? = null
    private var mIssue: Issue? = null
    private var mIssueComments: List<Comment>? = null

    private var LOADING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?, issueNumber: Int?) {
        mOwner = owner
        mName = name
        mIssueNumber = issueNumber
        if (mIssue != null && mIssueComments != null) {
            getView().showIssue(mIssue!!)
            getView().showComments(mIssueComments!!)
        }
        if (LOADING) getView().showLoading()
        else if (mOwner != null && mName != null && mIssueNumber != null){
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                loadIssueAndComments()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }

    }

    private fun loadIssueAndComments() {
        getCompositeDisposable().add(Flowable.zip<Issue, List<Comment>, Map<Issue, List<Comment>>>(
                getDataManager().getIssue(mOwner!!, mName!!, mIssueNumber!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().getIssueComments(mOwner!!, mName!!, mIssueNumber!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { issue, commentList -> return@BiFunction mapOf(issue to commentList) })
                .subscribe(
                        { map ->
                            mIssue = map.keys.first()
                            mIssueComments = map.entries.first().value
                            mIssue?.let { getView().showIssue(it) }
                            mIssueComments?.let { getView().showComments(it) }
                            getView().hideLoading()
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

}