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

package giuliolodi.gitnav.ui.gist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Gist
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 26/05/2017.
 */
class GistPresenter<V: GistContract.View> : BasePresenter<V>, GistContract.Presenter<V> {

    private val TAG = "GistPresenter"

    private var mGist: Gist? = null
    private var mGistId: String? = null
    private var IS_GIST_STARRED: Boolean? = null
    private var IS_MENU_CREATED: Boolean? = null

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, gistId: String) {
        mGistId = gistId
        if (IS_GIST_STARRED != null) {
            tryToCreateMenu()
        }
        else if (isNetworkAvailable) {
            mGistId?.let { isGistStarred(it) }
        }
        else {
            getView().showNoConnectionError()
        }
    }

    private fun isGistStarred(gistId: String) {
        getCompositeDisposable().add(Flowable.zip<Gist, Boolean, Map<Gist, Boolean>>(
                getDataManager().getGist(gistId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().isGistStarred(gistId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { gist, boolean -> return@BiFunction mapOf(gist to boolean) })
                .subscribe(
                        { map ->
                            IS_GIST_STARRED = map.entries.first().value
                            mGist = map.keys.first()
                            tryToCreateMenu()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    private fun tryToCreateMenu() {
        if (IS_MENU_CREATED != null &&
                IS_MENU_CREATED == true &&
                IS_GIST_STARRED != null) {
            getView().createOptionsMenu(IS_GIST_STARRED!!)
        }
    }

    override fun onMenuCreated() {
        IS_MENU_CREATED = true
        tryToCreateMenu()
    }

    override fun starGist(gistId: String) {
        getCompositeDisposable().add(getDataManager().starGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            getView().onGistStarred()
                            IS_GIST_STARRED = true
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    override fun unstarGist(gistId: String) {
        getCompositeDisposable().add(getDataManager().unstarGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            getView().onGistUnstarred()
                            IS_GIST_STARRED = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    override fun onOpenInBrowser() {
        mGist?.htmlUrl?.let { getView().intentToBrowser(it) }
    }

}