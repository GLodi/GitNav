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

package giuliolodi.gitnav.data.api

import io.reactivex.Completable
import io.reactivex.Observable
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.event.Event

/**
 * Created by giulio on 12/05/2017.
 */

interface ApiHelper {

    /**
     * Tries to authenticate to GitHub API.
     * If the authentication is successful, it returns the access token.
     * @param username
     * @param password
     * @return String access token
     */
    fun apiAuthToGitHub(username: String, password: String): String

    /**
     * Get user from username
     * @param token
     * @param username
     * @return Observable<User>
     */
    fun apiGetUser(token: String, username: String): Observable<User>

    /**
     * Downloads events of logged user.
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Observable<List<Event>>
     */
    fun apiPageEvents(token: String, username: String, pageN: Int, itemsPerPage: Int): Observable<List<Event>>

    /**
     * Get logged user's repositories, along with a filter option
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @param filter
     * @return Observable<List<Repository>>
     */
    fun apiPageRepos(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>?): Observable<List<Repository>>

    /**
     * Downloads trending repos (one at a time) from github's websites.
     * @param token
     * @param period (daily, weekly, monthly)
     * @return Observable<Repository>
     */
    fun apiGetTrending(token: String, period: String): Observable<Repository>

    /**
     * Get logged user's starred repos
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @param filter
     * @return Observable<List<Repository>>
     */
    fun apiPageStarred(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>?): Observable<List<Repository>>

    /**
     * Check if user is followed by logged user.
     * Returns
     * "f" -> followed
     * "n" -> not followed
     * "u" -> username is logged user
     * @param token
     * @param username
     * @return Observable<String>
     */
    fun apiGetFollowed(token: String, username: String): Observable<String>

    /**
     * Page followers of specific user (logged user if username is null)
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Observable<List<User>>
     */
    fun apiGetFollowers(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>>

    /**
     * Page users that follow specific user (logged user if username is null)
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Observable<List<User>>
     */
    fun apiGetFollowing(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>>

    /**
     * Follow user
     * @param token
     * @param username
     * @return Completable
     */
    fun apiFollowUser(token: String, username: String): Completable

    /**
     * Unfollow user
     * @param token
     * @param username
     * @return Completable
     */
    fun apiUnfollowUser(token: String, username: String): Completable

    /**
     * Page gists of specific user (logged user if username is null)
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Observable<List<Gist>>
     */
    fun apiPageGists(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<Gist>>

    /**
     * Page starred gists of logged user
     * @param token
     * @param pageN
     * @param itemsPerPage
     * @return Observable<List<Gist>>
     */
    fun apiPageStarredGists(token: String, pageN: Int, itemsPerPage: Int): Observable<List<Gist>>

    /**
     * Returns gist
     * @param token
     * @param gistId
     * @return Observable<Gist>
     */
    fun apiGetGist(token: String, gistId: String): Observable<Gist>

    /**
     * Get gist's comments
     * @param token
     * @param gistId
     * @return Observable<List<Comment>>
     */
    fun apiGetGistComments(token: String, gistId: String): Observable<List<Comment>>

    /**
     * Star gist
     * @param token
     * @param gistId
     * @return Completable
     */
    fun apiStarGist(token: String, gistId: String): Completable

    /**
     * Unstar gist
     * @param token
     * @param gistId
     * @return Completable
     */
    fun apiUnstarGist(token: String, gistId: String): Completable

    /**
     * Returns true if gist is starred, false otherwise.
     * @param token
     * @param gistId
     * @return Observable<Boolean>
     */
    fun apiIsGistStarred(token: String, gistId: String): Observable<Boolean>

    /**
     * Searches for repositories
     * @param token
     * @param query
     * @param filter
     * @return Observable<List<Repository>>
     */
    fun apiSearchRepos(token: String, query: String, filter: HashMap<String,String>): Observable<List<Repository>>

    /**
     * Searches for users
     * @param token
     * @param query
     * @param filter
     * @return Observable<List<User>>
     */
    fun apiSearchUsers(token: String, query: String, filter: HashMap<String,String>): Observable<List<SearchUser>>

}
