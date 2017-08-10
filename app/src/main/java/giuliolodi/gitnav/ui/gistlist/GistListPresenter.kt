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

package giuliolodi.gitnav.ui.gistlist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Gist
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 23/05/2017.
 */
class GistListPresenter<V: GistListContract.View> : BasePresenter<V>, GistListContract.Presenter<V> {

    private val TAG = "GistListPresenter"

    private var mGistList: MutableList<Gist> = mutableListOf()
    private var PAGE_N = 1
    private var ITEMS_PER_PAGE = 20
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false
    private var MINE_STARRED: String = "mine"
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean) {
        if (!mGistList.isEmpty()) getView().showGists(mGistList)
        else if (LOADING) getView().showLoading()
        else if (NO_SHOWING) getView().showNoGists(MINE_STARRED)
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                when (MINE_STARRED) {
                    "mine" -> getMineGists()
                    "starred" -> getStarredGists()
                }
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
            }
        }
    }

    override fun getMineGists() {
        getCompositeDisposable().add(getDataManager().pageGists(null, PAGE_N, ITEMS_PER_PAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { gistList ->
                            mGistList.addAll(gistList)
                            getView().showGists(gistList)
                            getView().hideLoading()
                            getView().hideListLoading()
                            if (PAGE_N == 1 && gistList.isEmpty()) {
                                getView().showNoGists(MINE_STARRED)
                                NO_SHOWING
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

    override fun getStarredGists() {
        getCompositeDisposable().add(getDataManager().pageStarredGists(PAGE_N, ITEMS_PER_PAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { gistList ->
                            mGistList.addAll(gistList)
                            getView().showGists(gistList)
                            getView().hideLoading()
                            getView().hideListLoading()
                            if (PAGE_N == 1 && gistList.isEmpty()) {
                                getView().showNoGists(MINE_STARRED)
                                NO_SHOWING
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
            getView().hideNoGists()
            PAGE_N = 1
            getView().clearAdapter()
            mGistList.clear()
            LOADING = true
            when (MINE_STARRED) {
                "mine" -> getMineGists()
                "starred" -> getStarredGists()
            }
        } else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_LIST)
            return
        if (isNetworkAvailable) {
            LOADING_LIST = true
            PAGE_N += 1
            getView().showListLoading()
            when (MINE_STARRED) {
                "mine" -> getMineGists()
                "starred" -> getStarredGists()
            }
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onBottomViewMineGistClick(isNetworkAvailable: Boolean) {
        getView().showLoading()
        getView().hideNoGists()
        getView().clearAdapter()
        getView().setAdapterAndClickListener()
        PAGE_N = 1
        mGistList.clear()
        MINE_STARRED = "mine"
        getMineGists()
    }

    override fun onBottomViewStarredGistClick(isNetworkAvailable: Boolean) {
        getView().showLoading()
        getView().hideNoGists()
        getView().clearAdapter()
        getView().setAdapterAndClickListener()
        PAGE_N = 1
        mGistList.clear()
        MINE_STARRED = "starred"
        getStarredGists()
    }

    override fun onGistClick(gistId: String) {
        getView().intentToGistActivitiy(gistId)
    }

    override fun unsubscribe() {
        if (getCompositeDisposable().size() != 0)
            getCompositeDisposable().clear()
    }

}