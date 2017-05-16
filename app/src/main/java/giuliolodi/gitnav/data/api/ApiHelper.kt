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
     * Downloads events of logged user.
     * @param token
     * @return Observable<List<Event>>
     */
    fun apiDownloadEvents(token: String, pageN: Int, itemsPerPage: Int): Observable<List<Event>>

    /**
     * Get user from username
     * @param token
     * @param username
     * @return Observable<User>
     */
    fun apiGetUser(token: String, username: String): Observable<User>

}
