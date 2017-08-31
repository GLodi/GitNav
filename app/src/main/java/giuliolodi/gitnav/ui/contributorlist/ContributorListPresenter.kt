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

package giuliolodi.gitnav.ui.contributorlist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Contributor
import javax.inject.Inject

/**
 * Created by giulio on 30/08/2017.
 */
class ContributorListPresenter<V: ContributorListContract.View> : BasePresenter<V>, ContributorListContract.Presenter<V> {

    private val TAG = "ContributorListPresenter"

    private var mContributorList: MutableList<Contributor> = mutableListOf()
    private var mRepoOwner: String? = null
    private var mRepoName: String? = null
    private var LOADING: Boolean = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, repoOwner: String?, repoName: String?) {
        mRepoOwner = repoOwner
        mRepoName = repoName
        if (!mContributorList.isEmpty()) getView().showContributorList(mContributorList)
        else if (NO_SHOWING) getView().showNoContributor()
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                if (mRepoOwner != null && mRepoName != null) loadContributorList()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadContributorList() {
        getCompositeDisposable().add(getDataManager().getContributors(mRepoOwner!!, mRepoName!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { contributorList ->
                            mContributorList.addAll(contributorList)
                            getView().showContributorList(contributorList)
                            getView().hideLoading()
                            if (contributorList.isEmpty()) {
                                getView().showNoContributor()
                                NO_SHOWING = true
                            }
                            LOADING = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            LOADING = false
                        }
                ))
    }

    override fun onUserClick(username: String) {
        getView().intentToUserActivity(username)
    }

}