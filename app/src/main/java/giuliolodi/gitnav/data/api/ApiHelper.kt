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

/**
 * Created by giulio on 12/05/2017.
 */

interface ApiHelper {

    /**
     * Tries to authenticate to GitHub API.
     * If the authentication is successful, it returns the access token.
     * @param user
     * @param pass
     * @return String access token
     */
    fun authToGitHub(user: String, pass: String): String

}
