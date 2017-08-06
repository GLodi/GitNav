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

package giuliolodi.gitnav.ui.repositorylist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Repository
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 18/05/2017.
 */
class RepoListPresenter<V: RepoListContract.View> : BasePresenter<V>, RepoListContract.Presenter<V> {

    private val TAG = "RepoListPresenter"

    private var mRepoList: MutableList<Repository> = mutableListOf()
    private var mFilter: HashMap<String,String> = HashMap()
    private var PAGE_N = 1
    private val ITEMS_PER_PAGE = 10
    private var LOADING = false
    private var LOADING_MAIN = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean) {
        if (mFilter.get("sort") == null)
            mFilter.put("sort", "created")
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
                loadRepos(isNetworkAvailable)
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
            }
        }
    }

    private fun loadRepos(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getCompositeDisposable().add(getDataManager().pageRepos(null, PAGE_N, ITEMS_PER_PAGE, mFilter)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { repoList ->
                                getView().showRepos(repoList)
                                getView().hideLoading()
                            },
                            { throwable ->
                                getView().showError(throwable.localizedMessage)
                                getView().hideLoading()
                                Timber.e(throwable)
                            }
                    ))
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING || mFilter["sort"] == "stars")
            return
        if (isNetworkAvailable) {
            LOADING = true
            PAGE_N += 1
            getView().showLoading()
            loadRepos(isNetworkAvailable)
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSwipeToRefresh(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getView().hideNoRepo()
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            LOADING_MAIN = true
            loadRepos(isNetworkAvailable)
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onRepoClick(repoOwner: String, repoName: String) {
        getView().intentToRepoActivity(repoOwner, repoName)
    }

    override fun onSortCreatedClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "created")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadRepos(isNetworkAvailable)
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
            loadRepos(isNetworkAvailable)
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
            loadRepos(isNetworkAvailable)
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onSortAlphabeticalClick(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            mFilter.put("sort", "full_name")
            PAGE_N = 1
            getView().clearAdapter()
            mRepoList.clear()
            getView().showLoading()
            getView().hideNoRepo()
            loadRepos(isNetworkAvailable)
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
            loadRepos(isNetworkAvailable)
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

}