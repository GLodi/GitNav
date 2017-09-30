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

package giuliolodi.gitnav.ui.starred

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Repository
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 19/05/2017.
 */
class StarredPresenter<V: StarredContract.View> : BasePresenter<V>, StarredContract.Presenter<V> {

    private val TAG = "StarredPresenter"

    private var mRepoList: MutableList<Repository> = mutableListOf()
    private var mFilter: HashMap<String,String> = HashMap()
    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false
    private var LOADING_LIST = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean) {
        if (mFilter["sort"] == null)
            mFilter.put("sort", "starred")
        if (!mRepoList.isEmpty()) {
            getView().showRepos(mRepoList)
            getView().setFilter(mFilter)
        }
        else if (NO_SHOWING) getView().showNoRepo()
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                loadStarredRepos()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
            }
        }
    }

    private fun loadStarredRepos() {
        getCompositeDisposable().add(getDataManager().pageStarred(PAGE_N, ITEMS_PER_PAGE, mFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoList ->
                            mRepoList.addAll(repoList)
                            getView().showRepos(repoList)
                            getView().setFilter(mFilter)
                            getView().hideLoading()
                            getView().hideListLoading()
                            if (PAGE_N == 1 && repoList.isEmpty()) {
                                getView().showNoRepo()
                                NO_SHOWING = true
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

    override fun onSwipeToRefresh(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getView().hideNoRepo()
            NO_SHOWING = false
            PAGE_N = 1
            mRepoList.clear()
            getView().clearAdapter()
            LOADING = true
            loadStarredRepos()
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_LIST || mFilter["sort"] == "stars" || mFilter["sort"] == "pushed" || mFilter["sort"] == "alphabetical" || mFilter["sort"] == "updated" )
            return
        if (isNetworkAvailable) {
            LOADING_LIST = true
            getView().showListLoading()
            loadStarredRepos()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSortStarredClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "starred")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadStarredRepos()
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSortUpdatedClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "updated")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadStarredRepos()
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSortPushedClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "pushed")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadStarredRepos()
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSortAlphabeticalClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "alphabetical")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadStarredRepos()
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSortStarsClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "stars")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadStarredRepos()
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onUserClick(username: String) {
        getView().intentToUserActivity(username)
    }

    override fun onRepoClick(repoOwner: String, repoName: String) {
        getView().intentToRepoActivity(repoOwner, repoName)
    }

}