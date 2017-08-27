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

package giuliolodi.gitnav.ui.stargazerlist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.User
import javax.inject.Inject

/**
 * Created by giulio on 25/08/2017.
 */
class StargazerListPresenter<V: StargazerListContract.View> : BasePresenter<V>, StargazerListContract.Presenter<V> {

    private val TAG = "StargazerListPresenter"

    private var mStargazerList: MutableList<User> = mutableListOf()
    private var mRepoOwner: String? = null
    private var mRepoName: String? = null
    private var PAGE_N: Int = 1
    private var ITEMS_PER_PAGE: Int = 20
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(repoOwner: String?, repoName: String?, isNetworkAvailable: Boolean) {
        mRepoOwner = repoOwner
        mRepoName = repoName
        if (!mStargazerList.isEmpty()) getView().showStargazerList(mStargazerList)
        else if (NO_SHOWING) getView().showNoStargazers()
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                if (mRepoOwner != null && mRepoName != null) loadStargazerList()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadStargazerList() {
        getCompositeDisposable().add(getDataManager().pageStargazers(mRepoOwner!!, mRepoName!!, PAGE_N, ITEMS_PER_PAGE)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { stargazerList ->
                            mStargazerList.addAll(stargazerList)
                            getView().showStargazerList(mStargazerList)
                            getView().hideLoading()
                            getView().hideListLoading()
                            if (PAGE_N == 1 && stargazerList.isEmpty()) {
                                getView().showNoStargazers()
                                NO_SHOWING = true
                            }
                            LOADING = false
                            LOADING_LIST = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            getView().hideListLoading()
                            LOADING = false
                            LOADING_LIST = false
                        }
                ))
    }

}