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
    private var IS_DARK_THEME_ON: Boolean = false

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

        if (getDataManager().getTheme() == "dark")
            IS_DARK_THEME_ON = true

        if (mUsername != null) {
            if (LOADING) getView().showLoading()
            when(MODE) {
                "following" -> {
                    if (!mFollowingList.isEmpty()) {
                        LOADING = false
                        getView().hideLoading()
                        getView().setupFollowing(mUsername!!, mUser!!)
                        getView().showFollowing(mFollowingList)
                    }
                    else {
                        onFollowingNavClick(isNetworkAvailable)
                    }
                }
                "followers" -> {
                    if (!mFollowersList.isEmpty()) {
                        LOADING = false
                        getView().hideLoading()
                        getView().setupFollowers(mUsername!!, mUser!!)
                        getView().showFollowers(mFollowersList)
                    }
                    else {
                        onFollowersNavClick(isNetworkAvailable)
                    }
                }
                "info" -> {
                    if (mUser != null) {
                        LOADING = false
                        getView().hideLoading()
                        getView().showUser(mUser!!, IS_FOLLOWED, IS_LOGGED_USER, IS_DARK_THEME_ON)
                    }
                    else {
                        onInfoNavClick(isNetworkAvailable)
                    }
                }
                "repos" -> {
                    if (!mRepoList.isEmpty()) {
                        LOADING = false
                        getView().hideLoading()
                        getView().setupRepos(mUsername!!, mFilterRepos, mUser!!)
                        getView().showRepos(mRepoList)
                    }
                    else {
                        onReposNavClick(isNetworkAvailable)
                    }
                }
                "events" -> {
                    if (!mEventList.isEmpty()) {
                        LOADING = false
                        getView().hideLoading()
                        getView().setupEvents(mUsername!!, mUser!!)
                        getView().showEvents(mEventList)
                    }
                    else {
                        onEventsNavClick(isNetworkAvailable)
                    }
                }
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
        mUser?.let {
            unsubscribe()
            MODE = "following"
            PAGE_N_FOLLOWING = 1
            LOADING = true
            setLoadings(false)
            clearLists()
            hideNoContents()
            getView().hideNoContent()
            getView().showLoading()
            getView().setupFollowing(mUsername!!, it)
            loadFollowing()
        }
    }

    override fun onFollowersNavClick(isNetworkAvailable: Boolean) {
        mUser?.let {
            unsubscribe()
            MODE = "followers"
            PAGE_N_FOLLOWERS = 1
            LOADING = true
            setLoadings(false)
            clearLists()
            hideNoContents()
            getView().hideNoContent()
            getView().showLoading()
            getView().setupFollowers(mUsername!!, it)
            loadFollowers()
        }
    }

    override fun onInfoNavClick(isNetworkAvailable: Boolean) {
        unsubscribe()
        MODE = "info"
        LOADING = true
        setLoadings(false)
        clearLists()
        hideNoContents()
        getView().hideNoContent()
        getView().showLoading()
        loadUser()
    }

    override fun onReposNavClick(isNetworkAvailable: Boolean) {
        mUser?.let {
            unsubscribe()
            MODE = "repos"
            PAGE_N_REPOS = 1
            LOADING = true
            setLoadings(false)
            clearLists()
            hideNoContents()
            getView().hideNoContent()
            getView().showLoading()
            mFilterRepos.put("sort","created")
            getView().setupRepos(mUsername!!, mFilterRepos, it)
            loadRepos()
        }
    }

    override fun onEventsNavClick(isNetworkAvailable: Boolean) {
        mUser?.let {
            unsubscribe()
            MODE = "events"
            PAGE_N_EVENTS = 1
            LOADING = true
            setLoadings(false)
            clearLists()
            hideNoContents()
            getView().hideNoContent()
            getView().showLoading()
            getView().setupEvents(mUsername!!, it)
            loadEvents()
        }
    }

    private fun loadFollowing() {
        getCompositeDisposable().add(getDataManager().pageFollowing(mUsername!!, PAGE_N_FOLLOWING, ITEMS_PER_PAGE_FOLLOWING)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { userList ->
                            getView().hideLoading()
                            getView().hideUserLoading()
                            mFollowingList.addAll(userList)
                            getView().showFollowing(userList)
                            if (PAGE_N_FOLLOWING == 1 && userList.isEmpty())  {
                                getView().showNoUsers()
                                NO_FOLLOWING = true
                            }
                            PAGE_N_FOLLOWING += 1
                            LOADING = false
                            LOADING_FOLLOWING = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
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
                            getView().hideLoading()
                            getView().hideUserLoading()
                            mFollowersList.addAll(userList)
                            getView().showFollowers(userList)
                            if (PAGE_N_FOLLOWERS == 1 && userList.isEmpty()) {
                                getView().showNoUsers()
                                NO_FOLLOWERS = true
                            }
                            PAGE_N_FOLLOWERS += 1
                            LOADING = false
                            LOADING_FOLLOWERS = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            getView().hideLoading()
                            Timber.e(throwable)
                            getView().hideLoading()
                            getView().hideUserLoading()
                            LOADING = false
                            LOADING_FOLLOWERS = false
                        }
                ))
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
                            mUser?.let { updateLoggedUser() }
                            getView().hideLoading()
                            mUser?.let { getView().showUser(it, IS_FOLLOWED, IS_LOGGED_USER, IS_DARK_THEME_ON) }
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
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
                            getView().hideLoading()
                            getView().hideRepoLoading()
                            mRepoList.addAll(repoList)
                            getView().showRepos(repoList)
                            if (PAGE_N_REPOS == 1 && repoList.isEmpty()) {
                                getView().showNoRepos()
                                NO_REPOS = true
                            }
                            PAGE_N_REPOS+= 1
                            LOADING = false
                            LOADING_REPOS = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            getView().hideLoading()
                            Timber.e(throwable)
                            getView().hideLoading()
                            getView().hideRepoLoading()
                            LOADING = false
                            LOADING_REPOS = false
                        }
                ))
    }

    private fun loadEvents() {
        getCompositeDisposable().add(getDataManager().pageUserEvents(mUsername, PAGE_N_EVENTS, ITEMS_PER_PAGE_EVENTS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { eventList ->
                            getView().hideLoading()
                            getView().hideEventLoading()
                            mEventList.addAll(eventList)
                            getView().showEvents(eventList)
                            if (PAGE_N_EVENTS == 1 && eventList.isEmpty()) {
                                getView().showNoEvents()
                                NO_EVENTS= true
                            }
                            PAGE_N_EVENTS += 1
                            LOADING = false
                            LOADING_EVENTS = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            getView().hideLoading()
                            Timber.e(throwable)
                            getView().hideLoading()
                            getView().hideEventLoading()
                            LOADING = false
                            LOADING_EVENTS = false
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

    override fun onLastFollowerVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_FOLLOWERS)
            return
        if (isNetworkAvailable) {
            LOADING_FOLLOWERS = true
            getView().showUserLoading()
            loadFollowers()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onLastRepoVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_REPOS || mFilterRepos["sort"] == "stars")
            return
        if (isNetworkAvailable) {
            LOADING_REPOS = true
            getView().showRepoLoading()
            loadRepos()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onLastEventVisible(isNetworkAvailable: Boolean, dy: Int) {
        if (LOADING_EVENTS)
            return
        if (isNetworkAvailable) {
            LOADING_EVENTS = true
            getView().showEventLoading()
            loadEvents()
        }
        else if (dy > 0) {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun followUser() {
        getCompositeDisposable().add(getDataManager().followUser(mUsername!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            IS_FOLLOWED = true
                            getView().onFollowCompleted()
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                        }
                ))
    }

    override fun unFollowUser() {
        getCompositeDisposable().add(getDataManager().unfollowUser(mUsername!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            IS_FOLLOWED = false
                            getView().onUnfollowCompleted()
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                        }
                ))
    }

    override fun updateLoggedUser() {
        getCompositeDisposable().add(getDataManager().updateUser(mUser!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {},
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            Timber.e(throwable)
                        }
                ))
    }

    override fun onUserMenuCreatedClick() {
        mFilterRepos.put("sort", "created")
        PAGE_N_REPOS = 1
        getView().clearRepoList()
        getView().showLoading()
        loadRepos()
    }

    override fun onUserMenuUpdatedClick() {
        mFilterRepos.put("sort", "updated")
        PAGE_N_REPOS = 1
        getView().clearRepoList()
        getView().showLoading()
        loadRepos()
    }

    override fun onUserMenuPushedClick() {
        mFilterRepos.put("sort", "pushed")
        PAGE_N_REPOS = 1
        getView().clearRepoList()
        getView().showLoading()
        loadRepos()
    }

    override fun onUserMenuAlphabeticalClick() {
        mFilterRepos.put("sort", "full_name")
        PAGE_N_REPOS = 1
        getView().clearRepoList()
        getView().showLoading()
        loadRepos()
    }

    override fun onUserMenuStarsClick() {
        mFilterRepos.put("sort", "stars")
        PAGE_N_REPOS = 1
        getView().clearRepoList()
        getView().showLoading()
        loadRepos()
    }

    override fun onOpenInBrowserClick() {
        mUser?.htmlUrl?.let { getView().intentToBrowser(it) }
    }

    private fun hideNoContents() {
        NO_FOLLOWING = false
        NO_FOLLOWERS = false
        NO_REPOS = false
        NO_EVENTS = false
    }

    private fun clearLists() {
        mFollowingList = mutableListOf()
        mFollowersList = mutableListOf()
        mRepoList = mutableListOf()
        mEventList = mutableListOf()
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