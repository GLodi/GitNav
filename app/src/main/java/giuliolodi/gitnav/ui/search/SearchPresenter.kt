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

package giuliolodi.gitnav.ui.search

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 26/05/2017.
 */
class SearchPresenter<V: SearchContract.View> : BasePresenter<V>, SearchContract.Presenter<V> {

    val TAG = "SearchPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun onSearchRepos(query: String, filter: HashMap<String,String>) {
        getCompositeDisposable().add(getDataManager().searchRepos(query, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { repoList ->
                            getView().hideLoading()
                            getView().showRepos(repoList)
                        },
                        { throwable ->
                            getView().hideLoading()
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                        }
                ))
    }

    override fun onSearchUsers(query: String, filter: HashMap<String,String>) {
        getCompositeDisposable().add(getDataManager().searchUsers(query, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { userList ->
                            getView().hideLoading()
                            getView().showUsers(userList)
                        },
                        { throwable ->
                            getView().hideLoading()
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                        }
                ))
    }

    override fun onSearchCode(query: String) {
        getCompositeDisposable().add(getDataManager().searchCode(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { codeList ->
                            getView().hideLoading()
                            getView().showCode(codeList)
                        },
                        { throwable ->
                            getView().hideLoading()
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                        }
                ))
    }

    override fun unsubscribe() {
        if (getCompositeDisposable().size() != 0)
            getCompositeDisposable().clear()
    }

}