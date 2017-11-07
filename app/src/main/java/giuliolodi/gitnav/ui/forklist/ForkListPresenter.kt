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

package giuliolodi.gitnav.ui.forklist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Repository
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 19/09/2017.
 */
class ForkListPresenter<V: ForkListContract.View> : BasePresenter<V>, ForkListContract.Presenter<V> {

    private val TAG = "ForkListPresenter"

    private var mForkList: MutableList<Repository> = mutableListOf()
    private var mOwner: String? = null
    private var mName: String? = null
    private var PAGE_N: Int = 1
    private var ITEMS_PER_PAGE: Int = 10
    private var NO_SHOWING: Boolean = false
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
        mOwner = owner
        mName = name
        if (!mForkList.isEmpty()) getView().showForkList(mForkList)
        else if (LOADING) getView().showLoading()
        else if (NO_SHOWING) getView().showNoForks()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                if (mOwner != null && mName != null) loadForks()
            } else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadForks() {
        getCompositeDisposable().add(getDataManager().pageForks(mOwner!!, mName!!, PAGE_N, ITEMS_PER_PAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { forkList ->
                            mForkList.addAll(forkList)
                            getView().showForkList(forkList)
                            getView().hideLoading()
                            getView().hideListLoading()
                            if (PAGE_N == 1 && forkList.isEmpty()) {
                                getView().showNoForks()
                                NO_SHOWING = true
                            }
                            PAGE_N += 1
                            LOADING = false
                            LOADING_LIST = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
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
            loadForks()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onRepoClick(owner: String, name: String) {
        getView().intentToRepoActivity(owner, name)
    }

}