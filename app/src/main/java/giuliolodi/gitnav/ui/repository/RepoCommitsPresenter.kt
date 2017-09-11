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

package giuliolodi.gitnav.ui.repository

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.RepositoryCommit
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 11/07/2017.
 */
class RepoCommitsPresenter<V: RepoCommitsContract.View> : BasePresenter<V>, RepoCommitsContract.Presenter<V> {

    private val TAG = "RepoCommitsPresenter"

    private var mRepoCommitList: MutableList<RepositoryCommit> = mutableListOf()
    private var mOwner: String? = null
    private var mName: String? = null
    private var PAGE_N: Int = 1
    private var ITEMS_PER_PAGE: Int = 20
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false
    private var NO_COMMITS: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
        mOwner = owner
        mName = name
        if (!mRepoCommitList.isEmpty()) getView().showRepoCommitList(mRepoCommitList)
        else if (LOADING) getView().showLoading()
        else if (NO_COMMITS) getView().showNoCommits()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                if (mOwner != null && mName != null) loadRepoCommits()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadRepoCommits() {
        getCompositeDisposable().add(getDataManager().getRepoCommits(mOwner!!, mName!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoCommitList ->
                            mRepoCommitList.addAll(repoCommitList)
                            getView().showRepoCommitList(repoCommitList)
                            getView().hideLoading()
                            if (PAGE_N == 1 && repoCommitList.isEmpty()) {
                                getView().showNoCommits()
                                NO_COMMITS = true
                            }
                            PAGE_N += 1
                            LOADING = false
                            LOADING_LIST = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            getView().hideListLoading()
                            Timber.e(throwable)
                            LOADING = false
                            LOADING_LIST = false
                        }
                ))

    }

    override fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_LIST)
            return
        else if (isNetworkAvailable) {
            LOADING_LIST = true
            getView().showListLoading()
            if (mOwner != null && mName != null) loadRepoCommits()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onUserClick(username: String) {
        getView().intentToUserActivity(username)
    }

}