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
import io.reactivex.Flowable
import org.eclipse.egit.github.core.*
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

    override fun updateUser(user: User): Completable {
        return Completable.fromAction {
            if (mPrefsHelper.getUsername() == user.login) {
                try {
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

    override fun pageEvents(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>> {
        return mApiHelper.apiPageEvents(mPrefsHelper.getToken(), username?.let { username } ?: mPrefsHelper.getUsername(), pageN, itemsPerPage)
    }

    override fun pageUserEvents(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>> {
        return mApiHelper.apiPageUserEvents(mPrefsHelper.getToken(), username?.let { username } ?: mPrefsHelper.getUsername(), pageN, itemsPerPage)
    }

    override fun getUser(username: String): Flowable<User> {
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

    override fun pageRepos(username: String?, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Flowable<List<Repository>> {
        if (username == null)
            return mApiHelper.apiPageRepos(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage, filter)
        else
            return mApiHelper.apiPageRepos(mPrefsHelper.getToken(), username, pageN, itemsPerPage, filter)
    }

    override fun getTrending(period: String): Flowable<Repository> {
        return mApiHelper.apiGetTrending(mPrefsHelper.getToken(), period)
    }

    override fun pageStarred(pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Flowable<List<Repository>> {
        return mApiHelper.apiPageStarred(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage, filter)
    }

    override fun getFollowed(username: String): Flowable<String> {
        if (mPrefsHelper.getUsername() == username)
            return Flowable.just("u")
        else
            return mApiHelper.apiGetFollowed(mPrefsHelper.getToken(), username)
    }

    override fun pageFollowers(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
        if (username == null)
            return mApiHelper.apiGetFollowers(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage)
        else
            return mApiHelper.apiGetFollowers(mPrefsHelper.getToken(), username, pageN, itemsPerPage)
    }

    override fun pageFollowing(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
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

    override fun pageGists(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Gist>> {
        if (username == null)
            return mApiHelper.apiPageGists(mPrefsHelper.getToken(), mPrefsHelper.getUsername(), pageN, itemsPerPage)
        else
            return mApiHelper.apiPageGists(mPrefsHelper.getToken(), username, pageN, itemsPerPage)
    }

    override fun pageStarredGists(pageN: Int, itemsPerPage: Int): Flowable<List<Gist>> {
        return mApiHelper.apiPageStarredGists(mPrefsHelper.getToken(), pageN, itemsPerPage)
    }

    override fun getGist(gistId: String): Flowable<Gist> {
        return mApiHelper.apiGetGist(mPrefsHelper.getToken(), gistId)
    }

    override fun getGistComments(gistId: String): Flowable<List<Comment>> {
        return mApiHelper.apiGetGistComments(mPrefsHelper.getToken(), gistId)
    }

    override fun starGist(gistId: String): Completable {
        return mApiHelper.apiStarGist(mPrefsHelper.getToken(), gistId)
    }

    override fun unstarGist(gistId: String): Completable {
        return mApiHelper.apiUnstarGist(mPrefsHelper.getToken(), gistId)
    }

    override fun isGistStarred(gistId: String): Flowable<Boolean> {
        return mApiHelper.apiIsGistStarred(mPrefsHelper.getToken(), gistId)
    }

    override fun searchRepos(query: String, filter: HashMap<String,String>): Flowable<List<Repository>> {
        return mApiHelper.apiSearchRepos(mPrefsHelper.getToken(), query, filter)
    }

    override fun searchUsers(query: String, filter: HashMap<String,String>): Flowable<List<SearchUser>> {
        return mApiHelper.apiSearchUsers(mPrefsHelper.getToken(), query, filter)
    }

    override fun searchCode(query: String): Flowable<List<CodeSearchResult>> {
        return mApiHelper.apiSearchCode(mPrefsHelper.getToken(), query)
    }

    override fun isRepoStarred(owner: String, name: String): Flowable<Boolean> {
        return mApiHelper.apiIsRepoStarred(mPrefsHelper.getToken(), owner, name)
    }

    override fun getRepo(owner: String, name: String): Flowable<Repository> {
        return mApiHelper.apiGetRepo(mPrefsHelper.getToken(), owner, name)
    }

    override fun starRepo(owner: String, name: String): Completable {
        return mApiHelper.apiStarRepo(mPrefsHelper.getToken(), owner, name)
    }

    override fun unstarRepo(owner: String, name: String): Completable {
        return mApiHelper.apiUnstarRepo(mPrefsHelper.getToken(), owner, name)
    }

    override fun getReadme(owner: String, name: String): Flowable<String> {
        return mApiHelper.apiGetReadme(mPrefsHelper.getToken(), owner, name)
    }

    override fun getRepoCommits(owner: String, name: String): Flowable<List<RepositoryCommit>> {
        return mApiHelper.apiGetRepoCommits(mPrefsHelper.getToken(), owner, name)
    }

    override fun getContributors(owner: String, name: String): Flowable<List<Contributor>> {
        return mApiHelper.apiGetContributors(mPrefsHelper.getToken(), owner, name)
    }

    override fun getContent(owner: String, name: String, path: String): Flowable<List<RepositoryContents>> {
        return mApiHelper.apiGetContent(mPrefsHelper.getToken(), owner, name, path)
    }

    override fun apiAuthToGitHub(username: String, password: String): String {
        return mApiHelper.apiAuthToGitHub(username, password)
    }

    override fun apiPageEvents(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>> {
        return mApiHelper.apiPageEvents(token, username, pageN, itemsPerPage)
    }

    override fun apiPageUserEvents(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>> {
        return mApiHelper.apiPageUserEvents(token, username, pageN, itemsPerPage)
    }

    override fun apiGetUser(token: String, username: String): Flowable<User> {
        return mApiHelper.apiGetUser(token, username)
    }

    override fun apiPageRepos(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>?): Flowable<List<Repository>> {
        return mApiHelper.apiPageRepos(token, username, pageN, itemsPerPage, filter)
    }

    override fun apiGetTrending(token: String, period: String): Flowable<Repository> {
        return mApiHelper.apiGetTrending(token, period)
    }

    override fun apiPageStarred(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Flowable<List<Repository>> {
        return mApiHelper.apiPageStarred(token, username, pageN, itemsPerPage, filter)
    }

    override fun apiGetFollowed(token: String, username: String): Flowable<String> {
        return mApiHelper.apiGetFollowed(token, username)
    }

    override fun apiGetFollowers(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
        return mApiHelper.apiGetFollowers(token, username, pageN, itemsPerPage)
    }

    override fun apiGetFollowing(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
        return mApiHelper.apiGetFollowers(token, username, pageN, itemsPerPage)
    }

    override fun apiFollowUser(token: String, username: String): Completable {
        return mApiHelper.apiFollowUser(token, username)
    }

    override fun apiUnfollowUser(token: String, username: String): Completable {
        return mApiHelper.apiUnfollowUser(token, username)
    }

    override fun apiPageGists(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Gist>> {
        return mApiHelper.apiPageGists(token, username, pageN, itemsPerPage)
    }

    override fun apiPageStarredGists(token: String, pageN: Int, itemsPerPage: Int): Flowable<List<Gist>> {
        return mApiHelper.apiPageStarredGists(token, pageN, itemsPerPage)
    }

    override fun apiGetGist(token: String, gistId: String): Flowable<Gist> {
        return mApiHelper.apiGetGist(token, gistId)
    }

    override fun apiGetGistComments(token: String, gistId: String): Flowable<List<Comment>> {
        return mApiHelper.apiGetGistComments(token, gistId)
    }

    override fun apiStarGist(token: String, gistId: String): Completable {
        return mApiHelper.apiStarGist(token, gistId)
    }

    override fun apiUnstarGist(token: String, gistId: String): Completable {
        return mApiHelper.apiUnstarGist(token, gistId)
    }

    override fun apiIsGistStarred(token: String, gistId: String): Flowable<Boolean> {
        return mApiHelper.apiIsGistStarred(token, gistId)
    }

    override fun apiSearchRepos(token: String, query: String, filter: HashMap<String,String>): Flowable<List<Repository>> {
        return mApiHelper.apiSearchRepos(token, query, filter)
    }

    override fun apiSearchUsers(token: String, query: String, filter: HashMap<String,String>): Flowable<List<SearchUser>> {
        return mApiHelper.apiSearchUsers(token, query, filter)
    }

    override fun apiSearchCode(token: String, query: String): Flowable<List<CodeSearchResult>> {
        return mApiHelper.apiSearchCode(token, query)
    }

    override fun apiIsRepoStarred(token: String, owner: String, name: String): Flowable<Boolean> {
        return mApiHelper.apiIsRepoStarred(token, owner, name)
    }

    override fun apiGetRepo(token: String, owner: String, name: String): Flowable<Repository> {
        return mApiHelper.apiGetRepo(token, owner, name)
    }

    override fun apiStarRepo(token: String, owner: String, name: String): Completable {
        return mApiHelper.apiStarRepo(token, owner, name)
    }

    override fun apiUnstarRepo(token: String, owner: String, name: String): Completable {
        return mApiHelper.apiUnstarRepo(token, owner, name)
    }

    override fun apiGetReadme(token: String, owner: String, name: String): Flowable<String> {
        return mApiHelper.apiGetReadme(token, owner, name)
    }

    override fun apiGetRepoCommits(token: String, owner: String, name: String): Flowable<List<RepositoryCommit>> {
        return mApiHelper.apiGetRepoCommits(token, owner, name)
    }

    override fun apiGetContributors(token: String, owner: String, name: String): Flowable<List<Contributor>> {
        return mApiHelper.apiGetContributors(token, owner, name)
    }

    override fun apiGetContent(token: String, owner: String, name: String, path: String): Flowable<List<RepositoryContents>> {
        return mApiHelper.apiGetContent(token, owner, name, path)
    }

}