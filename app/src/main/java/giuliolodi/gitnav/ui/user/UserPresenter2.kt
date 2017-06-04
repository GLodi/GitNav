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

package giuliolodi.gitnav.ui.user

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.User
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 19/05/2017.
 */

class UserPresenter2<V: UserContract2.View> : BasePresenter<V>, UserContract2.Presenter<V> {

    val TAG = "UserPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(username: String) {
        getCompositeDisposable().add(Observable.zip<User, String, Map<User, String>>(
                getDataManager().getUser(username)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().getFollowed(username)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { user, string -> return@BiFunction mapOf(user to string) })
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { map ->
                            getView().hideLoading()
                            getView().showUser(map)
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                        }
                ))
    }

}