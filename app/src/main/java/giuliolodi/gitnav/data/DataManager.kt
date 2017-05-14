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

package giuliolodi.gitnav.data

import giuliolodi.gitnav.data.api.ApiHelper
import giuliolodi.gitnav.data.prefs.PrefsHelper
import io.reactivex.Completable

/**
 * Created by giulio on 12/05/2017.
 */
interface DataManager : ApiHelper, PrefsHelper {

    /**
     * Tries to authenticate to GitHub API.
     * If the authentication is successful, it stores the authentication token in shared prefs,
     * along with username and profile picture.
     * @param user
     * @param pass
     * @return Completable onNext if successful, onError otherwise
     */
    fun tryAuthentication(user: String, pass: String): Completable

}