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
class TrendingPresenter<V: TrendingContract.View> : BasePresenter<V>, TrendingContract.Presenter<V> {

    private val TAG = "TrendingPresenter"

    private var mRepoList: MutableList<Repository> = mutableListOf()
    private var mPeriod: String = "daily"
    private var LOADING: Boolean = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean) {
        if (!mRepoList.isEmpty()) getView().addRepoList(mRepoList)
        else if (LOADING) getView().showLoading()
        else if (NO_SHOWING) getView().showNoRepo()
        else {
            if (isNetworkAvailable) {
                getView().showLoading()
                loadTrendingRepos(mPeriod)
                LOADING = true
            } else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    fun loadTrendingRepos(period: String) {
        getCompositeDisposable().add(getDataManager().getTrending(period)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repo ->
                            mRepoList.add(repo)
                            getView().addRepo(repo)
                            getView().hideLoading()
                        },
                        { throwable ->
                            if (throwable is IndexOutOfBoundsException)
                                getView().showNoRepo()
                            else
                                throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                            getView().hideLoading()
                        },
                        {
                            if (mRepoList.isEmpty()) {
                                getView().showNoRepo()
                                NO_SHOWING = true
                            }
                            getView().hideLoading()
                        }
                ))
    }

    override fun onImageClick(username: String) {
        getView().intentToUserActivity(username)
    }

    override fun onRepoClick(repoOwner: String, repoName: String) {
        getView().intentToRepoActivity(repoOwner, repoName)
    }

    override fun onSwipeToRefresh(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getView().hideNoRepo()
            getView().clearAdapter()
            mRepoList.clear()
            unsubscribe()
            LOADING = true
            loadTrendingRepos(mPeriod)
        } else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onBottomViewDailyClick() {
        getView().showLoading()
        getView().hideNoRepo()
        getView().clearAdapter()
        mRepoList.clear()
        unsubscribe()
        mPeriod = "daily"
        loadTrendingRepos(mPeriod)
    }

    override fun onBottomViewWeeklyClick() {
        getView().showLoading()
        getView().hideNoRepo()
        getView().clearAdapter()
        mRepoList.clear()
        unsubscribe()
        mPeriod = "weekly"
        loadTrendingRepos(mPeriod)
    }

    override fun onBottomViewMonthlyClick() {
        getView().showLoading()
        getView().hideNoRepo()
        getView().clearAdapter()
        mRepoList.clear()
        unsubscribe()
        mPeriod = "monthly"
        loadTrendingRepos(mPeriod)
    }

    override fun unsubscribe() {
        if (getCompositeDisposable().size() != 0)
            getCompositeDisposable().clear()
    }

}