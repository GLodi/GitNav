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

class UserPresenter<V: UserContract.View> : BasePresenter<V>, UserContract.Presenter<V> {

    val TAG = "UserPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(username: String) {
        getCompositeDisposable().add(Observable.zip<User,Boolean,Map<User,Boolean>>(
                getDataManager().getUser(username)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().getFollowed(username)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { user, boolean -> return@BiFunction mapOf(user to boolean) })
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

    override fun getRepos(username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>) {
        getCompositeDisposable().add(getDataManager().pageRepos(username, pageN, itemsPerPage, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoList ->
                            getView().hideLoadingUserRepos()
                            getView().showUserRepos(repoList)
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoadingUserRepos()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun getFollowers(username: String, pageN: Int, itemsPerPage: Int) {
        getCompositeDisposable().add(getDataManager().pageFollowers(username, pageN, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { userList ->
                            getView().hideLoadingUserFollowers()
                            getView().showUserFollowers(userList)
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoadingUserFollowers()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun getFollowing(username: String, pageN: Int, itemsPerPage: Int) {
        getCompositeDisposable().add(getDataManager().pageFollowing(username, pageN, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoList ->
                            getView().hideLoadingUserFollowing()
                            getView().showUserFollowing(repoList)
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoadingUserFollowing()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun followUser(username: String) {
    }

    override fun unFollowUser(username: String) {
    }

}