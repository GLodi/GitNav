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

package giuliolodi.gitnav.data.prefs

import android.graphics.Bitmap
import org.eclipse.egit.github.core.User

/**
 * Created by giulio on 12/05/2017.
 */
interface PrefsHelper {

    /**
     * Stores access token in SharedPreferences
     * @param token
     */
    fun storeAccessToken(token: String)

    /**
     * Returns access token
     * @return String
     */
    fun getToken(): String

    /**
     * Store authenticated user info
     * @param user
     */
    fun storeUser(user: User)

    /**
     * Get stored username
     * @return String
     */
    fun getUsername(): String

    /**
     * Get stored fullname (if exists)
     * @return String?
     */
    fun getFullname(): String?

    /**
     * Get stored email address (if exists)
     * @return String?
     */
    fun getEmail(): String?

    /**
     * Get stored profile pic
     * @return pic
     */
    fun getPic(): Bitmap

    /**
     * Set theme to either light or dark
     * @param String
     */
    fun setTheme(theme: String)

}