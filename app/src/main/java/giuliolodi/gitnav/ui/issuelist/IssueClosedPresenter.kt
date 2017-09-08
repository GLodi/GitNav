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

package giuliolodi.gitnav.ui.issuelist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Issue
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 02/09/2017.
 */
class IssueClosedPresenter<V: IssueClosedContract.View> : BasePresenter<V>, IssueClosedContract.Presenter<V> {

    private val TAG = "IssueClosedPresenter"

    private var mOwner: String? = null
    private var mName: String? = null
    private var PAGE_N: Int = 1
    private var ITEMS_PER_PAGE: Int = 10
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false
    private var mHashMap: HashMap<String,String> = hashMapOf()
    private var mIssueList: MutableList<Issue> = mutableListOf()
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
        mOwner = owner
        mName = name
        mHashMap.put("state", "closed")
        if (!mIssueList.isEmpty()) getView().showClosedIssues(mIssueList)
        else if (LOADING) getView().showLoading()
        else if (NO_SHOWING) getView().showNoClosedIssues()
        else {
            if (isNetworkAvailable) {
                getView().showLoading()
                LOADING = true
                loadClosedIssues()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadClosedIssues() {
        getCompositeDisposable().add(getDataManager().pageIssues(mOwner!!, mName!!, PAGE_N, ITEMS_PER_PAGE, mHashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { openIssueList ->
                            mIssueList.addAll(openIssueList)
                            getView().showClosedIssues(openIssueList)
                            getView().hideLoading()
                            getView().hideListLoading()
                            if (PAGE_N == 1 && openIssueList.isEmpty()) {
                                getView().showNoClosedIssues()
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

    override fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_LIST)
            return
        if (isNetworkAvailable) {
            LOADING_LIST = true
            getView().showListLoading()
            loadClosedIssues()
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