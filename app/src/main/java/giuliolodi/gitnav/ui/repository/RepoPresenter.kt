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
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.client.RequestException
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 10/07/2017.
 */
class RepoPresenter<V: RepoContract.View> : BasePresenter<V>, RepoContract.Presenter<V> {

    private val TAG = "RepoPresenter"

    private var mOwner: String? = null
    private var mName: String? = null
    private var mRepo: Repository? = null
    private var IS_REPO_STARRED: Boolean? = null
    private var IS_OPTIONS_MENU_CREATED: Boolean? = null

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
        owner?.let { mOwner = it }
        name?.let { mName = it }
        if (IS_REPO_STARRED != null) {
            tryToCreateMenu()
        }
        else if (isNetworkAvailable) {
            if (mOwner != null && mName != null) loadRepoAndStarred()
        }
        else {
            getView().showNoConnectionError()
        }
    }

    private fun loadRepoAndStarred() {
        getCompositeDisposable().add(Flowable.zip<Repository, Boolean, Map<Repository, Boolean>>(
                getDataManager().getRepo(mOwner!!, mName!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().isRepoStarred(mOwner!!, mName!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { repo, boolean -> return@BiFunction mapOf(repo to boolean) })
                .subscribe(
                        { map ->
                            mRepo = map.keys.first()
                            mRepo?.let {
                                IS_REPO_STARRED = map[it]!!
                                getView().showTitleAndSubtitle(it.name, it.owner.login)
                                tryToCreateMenu()
                            }
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            if ((throwable as? RequestException)?.status == 404)
                                getView().onRepoNotFound()
                            else
                                Timber.e(throwable)
                        }
                ))
    }

    private fun tryToCreateMenu() {
        if (IS_OPTIONS_MENU_CREATED != null &&
                IS_OPTIONS_MENU_CREATED == true &&
                IS_REPO_STARRED != null) {
            getView().createOptionsMenu(IS_REPO_STARRED!!)
            mRepo?.let { getView().showTitleAndSubtitle(it.name, it.owner.login) }
        }
    }

    override fun onOptionsMenuCreated() {
        IS_OPTIONS_MENU_CREATED = true
        tryToCreateMenu()
    }

    override fun onStarRepo(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getCompositeDisposable().add(getDataManager().starRepo(mOwner!!, mName!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                getView().onRepoStarred()
                                IS_REPO_STARRED = true
                            },
                            { throwable ->
                                getView().showError(throwable.localizedMessage)
                                Timber.e(throwable)
                            }
                    ))

        }
        else {
            getView().showNoConnectionError()
        }
    }

    override fun onUnstarRepo(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getCompositeDisposable().add(getDataManager().unstarRepo(mOwner!!, mName!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                getView().onRepoUnstarred()
                                IS_REPO_STARRED = false
                            },
                            { throwable ->
                                getView().showError(throwable.localizedMessage)
                                Timber.e(throwable)
                            }
                    ))
        }
        else {
            getView().showNoConnectionError()
        }
    }

    override fun onOpenInBrowser() {
        mRepo?.let { getView().intentToBrowser(it.htmlUrl) }
    }

}