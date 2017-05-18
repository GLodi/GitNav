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

import android.content.Context
import android.graphics.Bitmap
import giuliolodi.gitnav.di.scope.AppContext
import giuliolodi.gitnav.data.api.ApiHelper
import giuliolodi.gitnav.data.prefs.PrefsHelper
import io.reactivex.Completable
import io.reactivex.Observable
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import org.eclipse.egit.github.core.service.UserService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by giulio on 12/05/2017.
 */

@Singleton
class DataManagerImpl : DataManager {

    private val TAG = "DataManager"

    private val mContext: Context
    private val mApiHelper: ApiHelper
    private val mPrefsHelper: PrefsHelper

    @Inject
    constructor(@AppContext context: Context, apiHelper: ApiHelper, prefsHelper: PrefsHelper) {
        mContext = context
        mApiHelper = apiHelper
        mPrefsHelper = prefsHelper
    }

    override fun tryAuthentication(username: String, password: String): Completable {
        return Completable.fromAction {
            val token: String
            val user: User
            try {
                token = apiAuthToGitHub(username, password)
            } catch (e: IOException) {
                throw e
            }
            if (!token.isEmpty()) {
                mPrefsHelper.storeAccessToken(token)
                try {
                    val userService: UserService = UserService()
                    userService.client.setOAuth2Token(getToken())
                    user = userService.getUser(username)
                    storeUser(user)
                } catch (e: IOException) {
                    throw e
                }
            }
        }
    }

    override fun storeAccessToken(token: String) {
        mPrefsHelper.storeAccessToken(token)
    }

    override fun getToken(): String {
        return mPrefsHelper.getToken()
    }

    override fun pageEvents(pageN: Int, itemsPerPage: Int): Observable<List<Event>> {
        return apiPageEvents(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage)
    }

    override fun getUser(username: String): Observable<User> {
        return apiGetUser(mPrefsHelper.getToken(), username)
    }

    override fun storeUser(user: User) {
        mPrefsHelper.storeUser(user)
    }

    override fun getFullname(): String? {
        return mPrefsHelper.getFullname()
    }

    override fun getEmail(): String? {
        return mPrefsHelper.getEmail()
    }

    override fun getUsername(): String {
        return mPrefsHelper.getUsername()
    }

    override fun getPic(): Bitmap {
        return mPrefsHelper.getPic()
    }

    override fun pageRepos(pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        return mApiHelper.apiPageRepos(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage, filter)
    }

    override fun apiAuthToGitHub(username: String, password: String): String {
        return mApiHelper.apiAuthToGitHub(username, password)
    }

    override fun apiPageEvents(token: String, username: String, pageN: Int, itemsPerPage: Int): Observable<List<Event>> {
        return mApiHelper.apiPageEvents(token, username, pageN, itemsPerPage)
    }

    override fun apiGetUser(token: String, username: String): Observable<User> {
        return mApiHelper.apiGetUser(token, username)
    }

    override fun apiPageRepos(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>?): Observable<List<Repository>> {
        return mApiHelper.apiPageRepos(token, username, pageN, itemsPerPage, filter)
    }

}