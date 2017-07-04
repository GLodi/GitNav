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
import io.reactivex.Flowable
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
        getCompositeDisposable().add(Flowable.zip<User, String, Map<User, String>>(
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

    override fun getRepos(username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>) {
        getCompositeDisposable().add(getDataManager().pageRepos(username, pageN, itemsPerPage, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoList ->
                            getView().showRepos(repoList)
                            getView().hideLoading()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun getEvents(username: String, pageN: Int, itemsPerPage: Int) {
        getCompositeDisposable().add(getDataManager().pageUserEvents(username, pageN, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { eventList ->
                            getView().showEvents(eventList)
                            getView().hideLoading()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
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
                            getView().showFollowers(userList)
                            getView().hideLoading()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
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
                            getView().showFollowing(repoList)
                            getView().hideLoading()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun followUser(username: String) {
        getCompositeDisposable().add(getDataManager().followUser(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            getView().onFollowCompleted()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    override fun unFollowUser(username: String) {
        getCompositeDisposable().add(getDataManager().unfollowUser(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            getView().onUnfollowCompleted()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    override fun updateLoggedUser(user: User) {
        getCompositeDisposable().add(getDataManager().updateUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {},
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            Timber.e(throwable)
                        }
                ))
    }

    override fun unsubscribe() {
        if (getCompositeDisposable().size() != 0) {
            getCompositeDisposable().clear()
            getView().hideLoading()
        }
    }

}