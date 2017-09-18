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
import io.reactivex.Flowable
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.event.Event

/**
 * Created by giulio on 12/05/2017.
 */
interface DataManager : ApiHelper, PrefsHelper {

    /**
     * Tries to authenticate to GitHub API.
     * If the authentication is successful, it stores the authentication token in shared prefs,
     * along with username and profile picture.
     * @param username
     * @param password
     * @return Completable onNext if successful, onError otherwise
     */
    fun tryAuthentication(username: String, password: String): Completable

    /**
     * Update logged user information. This is called when user opens his own user  page
     * @param user
     * @return Completable
     */
    fun updateUser(user: User): Completable

    /**
     * Get user from username.
     * @param username
     * @return Flowable<User>
     */
    fun getUser(username: String): Flowable<User>

    /**
     * Downloads events received by user. Logged user if username = null.
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Flowable<List<Event>>
     */
    fun pageEvents(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>>

    /**
     * Downloads user events. Logged user if username = null.
     * @param token
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Flowable<List<Event>>
     */
    fun pageUserEvents(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>>

    /**
     * Page repositories of user (logged user if username is null).
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @param filter
     * @return Flowable<List<Repository>>
     */
    fun pageRepos(username: String?, pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>?): Flowable<List<Repository>>

    /**
     * Downloads trending repos (one at a time) from github's websites.
     * @param period (daily, weekly, monthly)
     * @return Flowable<Repository>
     */
    fun getTrending(period: String): Flowable<Repository>

    /**
     * Get logged user's starred repos
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @param filter
     */
    fun pageStarred(pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>?): Flowable<List<Repository>>

    /**
     * Check if user is followed by logged user.
     * Returns
     * "f" -> followed
     * "n" -> not followed
     * "u" -> username is logged user
     * @param username
     * @return String
     */
    fun getFollowed(username: String): Flowable<String>

    /**
     * Page users that follow specific user (logged user if username is null)
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return List<User>
     */
    fun pageFollowers(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>>

    /**
     * Page users that follow specific user (logged user if username is null)
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return List<User>
     */
    fun pageFollowing(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>>

    /**
     * Follow user
     * @param username
     * @return Completable
     */
    fun followUser(username: String): Completable

    /**
     * Unfollow user
     * @param username
     * @return Completable
     */
    fun unfollowUser(username: String): Completable

    /**
     * Page gists of specific user (logged user if username is null)
     * @param username
     * @param pageN
     * @param itemsPerPage
     * @return Flowable<List<Gist>>
     */
    fun pageGists(username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Gist>>

    /**
     * Page starred gists of logged user
     * @param pageN
     * @param itemsPerPage
     * @return Flowable<List<Gist>>
     */
    fun pageStarredGists(pageN: Int, itemsPerPage: Int): Flowable<List<Gist>>

    /**
     * Returns gist
     * @param gistId
     * @return Flowable<Gist>
     */
    fun getGist(gistId: String): Flowable<Gist>

    /**
     * Get gist's comments
     * @param gistId
     * @return Flowable<List<Comment>>
     */
    fun getGistComments(gistId: String): Flowable<List<Comment>>

    /**
     * Star gist
     * @param gistId
     * @return Completable
     */
    fun starGist(gistId: String): Completable

    /**
     * Unstar gist
     * @param gistId
     * @return Completable
     */
    fun unstarGist(gistId: String): Completable

    /**
     * Returns true if gist is starred, false otherwise
     * @param gistId
     * @return Flowable<Boolean>
     */
    fun isGistStarred(gistId: String): Flowable<Boolean>

    /**
     * Searches for repositories
     * @param query
     * @param filter
     * @return Flowable<List<Repository>>
     */
    fun searchRepos(query: String, filter: HashMap<String,String>): Flowable<List<Repository>>

    /**
     * Searches for users
     * @param query
     * @param filter
     * @return Flowable<List<User>>
     */
    fun searchUsers(query: String, filter: HashMap<String,String>): Flowable<List<SearchUser>>

    /**
     * Searches code
     * @param query
     * @return Flowable<List<CodeSearchResult>>
     */
    fun searchCode(query: String): Flowable<List<CodeSearchResult>>

    /**
     * Returns true if specified repo is starred by user
     * @param owner
     * @param name
     * @return Flowable<Boolean>
     */
    fun isRepoStarred(owner: String, name: String): Flowable<Boolean>

    /**
     * Returns repo
     * @param owner
     * @param name
     * @Return Flowable<Repository>
     */
    fun getRepo(owner: String, name: String): Flowable<Repository>

    /**
     * Star repo
     * @param owner
     * @param name
     * @return Completable
     */
    fun starRepo(owner: String, name: String): Completable

    /**
     * Unstar repo
     * @param owner
     * @param name
     * @return Completable
     */
    fun unstarRepo(owner: String, name: String): Completable

    /**
     * Get repo's README
     * @param owner
     * @param name
     * @return Flowable<String>
     */
    fun getReadme(owner: String, name: String): Flowable<String>

    /**
     * Returns repo's commit list
     * @param owner
     * @param name
     * @return Flowable<List<RepositoryCommit>>
     */
    fun getRepoCommits(owner: String, name: String): Flowable<List<RepositoryCommit>>

    /**
     * Returns repo's contributors
     * @param owner
     * @param name
     * @return Flowable<List<Contributor>>
     */
    fun getContributors(owner: String, name: String): Flowable<List<Contributor>>

    /**
     * Return repo's content
     * @param owner
     * @param name
     * @param path
     * @return Flowable<List<RepositoryContents>>
     */
    fun getContent(owner: String, name: String, path: String): Flowable<List<RepositoryContents>>

    /**
     * Returns list of repo's stargazers
     * @param owner
     * @param name
     * @return Flowable<List<User>>
     */
    fun pageStargazers(owner: String, name: String, pageN: Int, itemsPerPage: Int): Flowable<List<User>>

    /**
     * Pages issues
     * @param owner
     * @param name
     * @param pageN
     * @param itemsPerPage
     * @param hashMap
     * @return Flowable<List<Issue>>
     */
    fun pageIssues(owner: String, name: String, pageN: Int, itemsPerPage: Int, hashMap: HashMap<String,String>): Flowable<List<Issue>>

    /**
     * Pages repo's forks
     * @param owner
     * @param name
     * @param pageN
     * @param itemsPerPage
     * @return Flowable<List<Repository>>
     */
    fun pageForks(owner: String, name: String, pageN: Int, itemsPerPage: Int): Flowable<List<Repository>>

}