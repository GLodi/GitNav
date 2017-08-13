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
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Contributor
import org.eclipse.egit.github.core.Repository
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 15/07/2017.
 */
class RepoAboutPresenter<V: RepoAboutContract.View>: BasePresenter<V>, RepoAboutContract.Presenter<V> {

    private val TAG = "RepoPresenter"

    private var mOwner: String? = null
    private var mName: String? = null
    private var mRepoContributor: Map<Repository, List<Contributor>>? = null
    private var mRepo: Repository? = null
    private var mContributorList: List<Contributor>? = null
    private var LOADING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)


    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
        owner?.let { mOwner = it }
        name?.let { mName = it }

        if (mRepo != null) getView().showRepoAbout(mRepo?.name!!, mRepo?.owner?.login!!, mRepo?.description!!, mRepo?.owner?.avatarUrl!!)
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                if (mOwner != null && mName != null) loadRepoAbout()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadRepoAbout() {
        getCompositeDisposable().add(Flowable.zip<Repository, List<Contributor>, Map<Repository, List<Contributor>>>(
                getDataManager().getRepo(mOwner!!, mName!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().getContributors(mOwner!!, mName!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { repo, contributorList -> return@BiFunction mapOf(repo to contributorList) })
                .doOnSubscribe {
                    getView().showLoading()
                    LOADING = true
                }
                .subscribe(
                        { repoContributors ->
                            mRepoContributor = repoContributors
                            mRepo = repoContributors.keys.first()
                            mRepo?.let { mContributorList = repoContributors[it] }
                            mRepo?.let { getView().showRepoAbout(mRepo?.name!!, mRepo?.owner?.login!!, mRepo?.description!!, mRepo?.owner?.avatarUrl!!) }
                            getView().hideLoading()
                            LOADING = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                            LOADING = false
                        }
                ))
    }

}