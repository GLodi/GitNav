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

        fun showUser(mapUserFollowed: Map<User, String>)

        fun showLoading()

        fun hideLoading()

        fun showError(error: String)

        fun showRepos(repoList: List<Repository>)

        fun showEvents(eventList: List<Event>)

        fun showFollowers(followerList: List<User>)

        fun showFollowing(followingList: List<User>)

        fun onFollowCompleted()

        fun onUnfollowCompleted()

    }

    @PerActivity
    interface Presenter<V: BaseContract.View> : BaseContract.Presenter<V> {

        fun subscribe(username: String)

        fun getRepos(username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>)

        fun getEvents(username: String, pageN: Int, itemsPerPage: Int)

        fun getFollowers(username: String, pageN: Int, itemsPerPage: Int)

        fun getFollowing(username: String, pageN: Int, itemsPerPage: Int)

        fun followUser(username: String)

        fun unFollowUser(username: String)

        fun updateLoggedUser(user: User)

        fun unsubscribe()

    }

}