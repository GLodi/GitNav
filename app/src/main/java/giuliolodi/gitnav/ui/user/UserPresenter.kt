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
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 19/05/2017.
 */
class UserPresenter<V: UserContract.View> : BasePresenter<V>, UserContract.Presenter<V> {

    private val TAG = "UserPresenter"

    private var mUsername: String? = null
    private var MODE: String? = null

    private var mFollowingList: MutableList<User> = mutableListOf()
    private var mFollowersList: MutableList<User> = mutableListOf()
    private var mRepoList: MutableList<Repository> = mutableListOf()
    private var mEventList: MutableList<Event> = mutableListOf()

    private var LOADING: Boolean = false

    private var mUser: User? = null
    private var IS_FOLLOWED: Boolean = false
    private var IS_LOGGED_USER: Boolean = false

    private var PAGE_N_FOLLOWING = 1
    private val ITEMS_PER_PAGE_FOLLOWING = 20
    private var LOADING_FOLLOWING = false
    private var NO_FOLLOWING = false

    private var PAGE_N_FOLLOWERS = 1
    private val ITEMS_PER_PAGE_FOLLOWERS = 20
    private var LOADING_FOLLOWERS = false
    private var NO_FOLLOWERS = false

    private var mFilterRepos: HashMap<String,String> = HashMap()
    private var PAGE_N_REPOS = 1
    private val ITEMS_PER_PAGE_REPOS = 10
    private var LOADING_REPOS = false
    private var NO_REPOS = false

    private var PAGE_N_EVENTS = 1
    private val ITEMS_PER_PAGE_EVENTS = 10
    private var LOADING_EVENTS = false
    private var NO_EVENTS = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, username: String?) {
        mUsername = username

        if (mUsername != null) {
            if (LOADING) getView().showLoading()
            when(MODE) {
                "following" -> {}
                "followers" -> {}
                "info" -> {
                    if (mUser != null)
                        getView().showUser(mUser!!, IS_FOLLOWED, IS_LOGGED_USER)
                    else
                        onInfoNavClick(isNetworkAvailable)
                }
                "repos" -> {}
                "events" -> {}
                null -> {
                    if (isNetworkAvailable) {
                        MODE = "info"
                        LOADING = true
                        getView().showLoading()
                        loadUser()
                    }
                    else {
                        getView().showNoConnectionError()
                        getView().hideLoading()
                        LOADING = false
                    }
                }
            }
        }
        else {
            getView().showError("Error importing username")
            getView().pressBack()
        }
    }

    override fun onFollowingNavClick(isNetworkAvailable: Boolean) {
        unsubscribe()
        MODE = "following"
        PAGE_N_FOLLOWING = 1
        LOADING = true
        setLoadings(false)
        getView().showLoading()
        getView().setupFollowing(mUsername!!)
        loadFollowing()
    }

    override fun onFollowersNavClick(isNetworkAvailable: Boolean) {
        unsubscribe()
        MODE = "followers"
        PAGE_N_FOLLOWERS = 1
        LOADING = true
        setLoadings(false)
        getView().showLoading()
        getView().setupFollowers(mUsername!!)
        loadFollowers()
    }

    override fun onInfoNavClick(isNetworkAvailable: Boolean) {
        unsubscribe()
        MODE = "info"
        LOADING = true
        setLoadings(false)
        getView().showLoading()
        loadUser()
    }

    override fun onReposNavClick(isNetworkAvailable: Boolean) {
        unsubscribe()
        MODE = "repos"
        PAGE_N_REPOS = 1
        LOADING = true
        setLoadings(false)
        getView().showLoading()
        getView().setupRepos(mUsername!!)
        loadRepos()
    }

    override fun onEventsNavClick(isNetworkAvailable: Boolean) {
        unsubscribe()
        MODE = "events"
        PAGE_N_EVENTS = 1
        LOADING = true
        setLoadings(false)
        getView().showLoading()
        getView().setupEvents(mUsername!!)
        loadEvents()
    }

    private fun loadFollowing() {
        getCompositeDisposable().add(getDataManager().pageFollowing(mUsername!!, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { userList ->
                            mFollowingList.addAll(userList)
                            getView().showFollowing(userList)
                            getView().hideLoading()
                            getView().hideUserLoading()
                            if (PAGE_N_FOLLOWING == 1 && userList.isEmpty())  {
                                getView().showNoUsers()
                                NO_FOLLOWING = true
                            }
                            PAGE_N_FOLLOWING += 1
                            LOADING = false
                            LOADING_FOLLOWING = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                            getView().hideLoading()
                            getView().hideUserLoading()
                            LOADING = false
                            LOADING_FOLLOWING = false

                        }
                ))
    }

    private fun loadFollowers() {
        getCompositeDisposable().add(getDataManager().pageFollowers(mUsername, PAGE_N_FOLLOWERS, ITEMS_PER_PAGE_FOLLOWERS)
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

    private fun loadRepos() {
        getCompositeDisposable().add(getDataManager().pageRepos(mUsername, PAGE_N_REPOS, ITEMS_PER_PAGE_REPOS, mFilterRepos)
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

    private fun loadEvents() {
        getCompositeDisposable().add(getDataManager().pageUserEvents(mUsername, PAGE_N_EVENTS, ITEMS_PER_PAGE_EVENTS)
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

    override fun onLastFollowingVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_FOLLOWING)
            return
        if (isNetworkAvailable) {
            LOADING_FOLLOWING = true
            getView().showUserLoading()
            loadFollowing()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onLastFollowersVisible(isNetworkAvailable: Boolean, dy: Int) {
    }

    override fun onLastReposVisible(isNetworkAvailable: Boolean, dy: Int) {
    }

    override fun onLastEventsVisible(isNetworkAvailable: Boolean, dy: Int) {
    }

    private fun loadUser() {
        getCompositeDisposable().add(Flowable.zip<User, String, Map<User, String>>(
                getDataManager().getUser(mUsername!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().getFollowed(mUsername!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { user, string -> return@BiFunction mapOf(user to string) })
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { map ->
                            mUser = map.keys.first()
                            if (map[mUser!!] == "f")
                                IS_FOLLOWED = true
                            else if (map[mUser!!] == "u")
                                IS_LOGGED_USER = true
                            mUser?.let { updateLoggedUser(it) }
                            getView().createOptionsMenu()
                            getView().hideLoading()
                            mUser?.let { getView().showUser(it, IS_FOLLOWED, IS_LOGGED_USER) }
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

    private fun setLoadings(bool: Boolean) {
        LOADING_FOLLOWING = bool
        LOADING_FOLLOWERS = bool
        LOADING_REPOS = bool
        LOADING_EVENTS = bool
    }

    override fun unsubscribe() {
        if (getCompositeDisposable().size() != 0) {
            getCompositeDisposable().clear()
            getView().hideLoading()
        }
    }

}