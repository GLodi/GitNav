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
            } catch (e: Exception) {
                throw e
            }
            if (!token.isEmpty()) {
                mPrefsHelper.storeAccessToken(token)
                try {
                    val userService: UserService = UserService()
                    userService.client.setOAuth2Token(getToken())
                    user = userService.getUser(username)
                    storeUser(user)
                } catch (e: Exception) {
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

    override fun pageRepos(username: String?, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        if (username == null)
            return mApiHelper.apiPageRepos(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage, filter)
        else
            return mApiHelper.apiPageRepos(mPrefsHelper.getToken(), username, pageN, itemsPerPage, filter)
    }

    override fun getTrending(period: String): Observable<Repository> {
        return mApiHelper.apiGetTrending(mPrefsHelper.getToken(), period)
    }

    override fun pageStarred(pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        return mApiHelper.apiPageStarred(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage, filter)
    }

    override fun getFollowed(username: String): Observable<String> {
        if (mPrefsHelper.getUsername() == username)
            return Observable.just("u")
        else
            return mApiHelper.apiGetFollowed(mPrefsHelper.getToken(), username)
    }

    override fun pageFollowers(username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>> {
        if (username == null)
            return mApiHelper.apiGetFollowers(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage)
        else
            return mApiHelper.apiGetFollowers(mPrefsHelper.getToken(), username, pageN, itemsPerPage)
    }

    override fun pageFollowing(username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>> {
        if (username == null)
            return mApiHelper.apiGetFollowing(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage)
        else
            return mApiHelper.apiGetFollowing(mPrefsHelper.getToken(), username, pageN, itemsPerPage)
    }

    override fun followUser(username: String): Completable {
        if (username != mPrefsHelper.getUsername())
            return mApiHelper.apiFollowUser(mPrefsHelper.getToken(), username)
        return Completable.error(Exception())
    }

    override fun unfollowUser(username: String): Completable {
        if (username != mPrefsHelper.getUsername())
            return mApiHelper.apiUnfollowUser(mPrefsHelper.getToken(), username)
        return Completable.error(Exception())
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

    override fun apiGetTrending(token: String, period: String): Observable<Repository> {
        return mApiHelper.apiGetTrending(token, period)
    }

    override fun apiPageStarred(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        return mApiHelper.apiPageStarred(token, username, pageN, itemsPerPage, filter)
    }

    override fun apiGetFollowed(token: String, username: String): Observable<String> {
        return mApiHelper.apiGetFollowed(token, username)
    }

    override fun apiGetFollowers(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>> {
        return mApiHelper.apiGetFollowers(token, username, pageN, itemsPerPage)
    }

    override fun apiGetFollowing(token: String, username: String?, pageN: Int, itemsPerPage: Int): Observable<List<User>> {
        return mApiHelper.apiGetFollowers(token, username, pageN, itemsPerPage)
    }

    override fun apiFollowUser(token: String, username: String): Completable {
        return mApiHelper.apiFollowUser(token, username)
    }

    override fun apiUnfollowUser(token: String, username: String): Completable {
        return mApiHelper.apiUnfollowUser(token, username)
    }

}