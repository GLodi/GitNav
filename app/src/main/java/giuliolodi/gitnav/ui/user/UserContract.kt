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

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event

/**
 * Created by giulio on 03/06/2017.
 */
interface UserContract {

    interface View : BaseContract.View {

        fun showUser(user: User, IS_FOLLOWED: Boolean, IS_LOGGED_USER: Boolean)

        fun showLoading()

        fun hideLoading()

        fun showError(error: String)

        fun showNoConnectionError()

        fun showRepos(repoList: List<Repository>)

        fun showEvents(eventList: List<Event>)

        fun showFollowers(followerList: List<User>)

        fun showFollowing(followingList: List<User>)

        fun onFollowCompleted()

        fun onUnfollowCompleted()

        fun setupFollowing(username: String, user: User)

        fun setupFollowers(username: String, user: User)

        fun setupRepos(username: String, filter: HashMap<String,String>, user: User)

        fun setupEvents(username: String, user: User)

        fun showUserLoading()

        fun hideUserLoading()

        fun showRepoLoading()

        fun hideRepoLoading()

        fun showEventLoading()

        fun hideEventLoading()

        fun showNoUsers()

        fun showNoRepos()

        fun showNoEvents()

        fun hideNoContent()

        fun clearRepoList()

        fun intentToBrowser(url: String)

        fun pressBack()
    }

    @PerActivity
    interface Presenter<V: UserContract.View> : BaseContract.Presenter<V> {

        fun subscribe(isNetworkAvailable: Boolean, username: String?)

        fun onLastFollowingVisible(isNetworkAvailable: Boolean, dy: Int)

        fun onLastFollowerVisible(isNetworkAvailable: Boolean, dy: Int)

        fun onLastRepoVisible(isNetworkAvailable: Boolean, dy: Int)

        fun onLastEventVisible(isNetworkAvailable: Boolean, dy: Int)

        fun onFollowingNavClick(isNetworkAvailable: Boolean)

        fun onFollowersNavClick(isNetworkAvailable: Boolean)

        fun onInfoNavClick(isNetworkAvailable: Boolean)

        fun onReposNavClick(isNetworkAvailable: Boolean)

        fun onEventsNavClick(isNetworkAvailable: Boolean)

        fun followUser()

        fun unFollowUser()

        fun updateLoggedUser()

        fun onUserMenuCreatedClick()

        fun onUserMenuUpdatedClick()

        fun onUserMenuPushedClick()

        fun onUserMenuAlphabeticalClick()

        fun onUserMenuStarsClick()

        fun onOpenInBrowserClick()

        fun unsubscribe()

    }

}