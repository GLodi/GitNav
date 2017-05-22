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

import io.reactivex.Observable
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
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
     * Check if user is followed by logged user
     * @param token
     * @param username
     * @return Boolean
     */
    fun apiGetFollowed(token: String, username: String): Observable<Boolean>

    /**
     * Page followers of specific user (logged user if username is null)
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return List<User>
     */
    fun apiGetFollowers(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>>

    /**
     * Page users that follow specific user (logged user if username is null)
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return List<User>
     */
    fun apiGetFollowing(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>>

}
