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

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.Base64
import giuliolodi.gitnav.di.scope.AppContext
import giuliolodi.gitnav.di.scope.UrlInfo
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.event.Event
import org.eclipse.egit.github.core.service.*
import javax.inject.Inject
import java.io.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.UnsupportedEncodingException

/**
 * Created by giulio on 12/05/2017.
 */
class ApiHelperImpl : ApiHelper {

    private val mContext: Context
    private val mUrlMap: Map<String,String>

    @Inject
    constructor(@AppContext context: Context, @UrlInfo urlMap: Map<String,String>) {
        mContext = context
        mUrlMap = urlMap
    }

    override fun apiAuthToGitHub(username: String, password: String): String {
        val oAuthService: OAuthService = OAuthService()
        oAuthService.client.setCredentials(username, password)

        // This will set the token parameters and its permissions
        var auth = Authorization()
        auth.scopes = arrayListOf("repo", "gist", "user")
        val description = "GitNav - " + Build.MANUFACTURER + " " + Build.MODEL
        auth.note = description

        // Required for some reason
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Check if token already exists and deletes it.
        try {
            for (authorization in oAuthService.authorizations) {
                if (authorization.note == description) {
                    oAuthService.deleteAuthorization(authorization.id)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        // Create authorization
        try {
            auth = oAuthService.createAuthorization(auth)
            return auth.token
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    override fun apiGetUser(token: String, username: String): Flowable<User> {
        return Flowable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Flowable.just(userService.getUser(username))
        }
    }

    override fun apiPageEvents(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>> {
        return Flowable.defer {
            val eventService: EventService = EventService()
            eventService.client.setOAuth2Token(token)
            Flowable.just(ArrayList(eventService.pageUserReceivedEvents(username, false, pageN, itemsPerPage).next()))
        }
    }

    override fun apiPageUserEvents(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Event>> {
        return Flowable.defer {
            val eventService: EventService = EventService()
            eventService.client.setOAuth2Token(token)
            Flowable.just(ArrayList(eventService.pageUserEvents(username, false, pageN, itemsPerPage).next()))
        }
    }

    override fun apiGetTrending(token: String, period: String): Flowable<Repository> {
        return Flowable.create({ emitter ->
            var URL: String = ""
            val ownerRepoList: MutableList<String> = mutableListOf()
            when (period) {
                "daily" ->  URL = mUrlMap["base"] + mUrlMap["daily"]
                "weekly" ->  URL = mUrlMap["base"] + mUrlMap["weekly"]
                "monthly" ->  URL = mUrlMap["base"] + mUrlMap["monthly"]
            }
            try {
                val document = Jsoup.connect(URL).get()
                val repoList = document.getElementsByTag("ol")[0].getElementsByTag("li")
                if (repoList != null && !repoList.isEmpty()) {
                    var string: Element
                    var ss: String
                    for (i in 0..repoList.size - 1) {
                        string = repoList[i].getElementsByTag("div")[0].getElementsByTag("h3")[0].getElementsByTag("a")[0]
                        ss = string.children()[0].ownText() + string.ownText()
                        val t = ss.split("/")
                        val a = t[0].replace(" ", "")
                        val b = t[1]
                        ownerRepoList.add(a)
                        ownerRepoList.add(b)
                    }
                    val repositoryService: RepositoryService = RepositoryService()
                    repositoryService.client.setOAuth2Token(token)
                    for (i in 0..ownerRepoList.size - 1 step 2) {
                        emitter.onNext(repositoryService.getRepository(ownerRepoList[i], ownerRepoList[i+1]))
                    }
                    emitter.onComplete()
                }
            } catch (e: Exception) {
                if (!emitter.isCancelled)
                    emitter.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    override fun apiPageRepos(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Flowable<List<Repository>> {
        return Flowable.defer {
            val repositoryService: RepositoryService = RepositoryService()
            repositoryService.client.setOAuth2Token(token)
            when (filter?.get("sort")) {
                "stars" -> Flowable.just(repositoryService.getRepositories(username).sortedByDescending { it.watchers })
                else -> Flowable.just(ArrayList(repositoryService.pageRepositories(username, filter, pageN, itemsPerPage).next()))
            }
        }
    }

    override fun apiPageStarred(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Flowable<List<Repository>> {
        return Flowable.defer {
            val starService: StarService = StarService()
            starService.client.setOAuth2Token(token)
            when (filter?.get("sort")) {
                "stars" -> Flowable.just(starService.getStarred(username).sortedByDescending { it.watchers })
                "pushed" -> Flowable.just(starService.getStarred(username).sortedByDescending { it.pushedAt })
                "updated" -> Flowable.just(starService.getStarred(username).sortedByDescending { it.updatedAt })
                "alphabetical" -> Flowable.just(starService.getStarred(username).sortedBy { it.name })
                else -> Flowable.just(ArrayList(starService.pageStarred(username, filter, pageN, itemsPerPage).next()))
            }
        }
    }

    override fun apiGetFollowed(token: String, username: String): Flowable<String> {
        return Flowable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            if (userService.isFollowing(username))
                Flowable.just("f")
            else
                Flowable.just("n")
        }
    }

    override fun apiGetFollowers(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
        return Flowable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Flowable.just(ArrayList(userService.pageFollowers(username, pageN, itemsPerPage).next()))
        }
    }

    override fun apiGetFollowing(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
        return Flowable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Flowable.just(ArrayList(userService.pageFollowing(username, pageN, itemsPerPage).next()))
        }
    }

    override fun apiFollowUser(token: String, username: String): Completable {
        return Completable.create { subscriber ->
             val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            try {
                userService.follow(username)
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    override fun apiUnfollowUser(token: String, username: String): Completable {
        return Completable.create { subscriber ->
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            try {
                userService.unfollow(username)
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    override fun apiPageGists(token: String, username: String?, pageN: Int, itemsPerPage: Int): Flowable<List<Gist>> {
        return Flowable.defer {
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            Flowable.just(ArrayList(gistService.pageGists(username, pageN, itemsPerPage).next()))
        }
    }

    override fun apiPageStarredGists(token: String, pageN: Int, itemsPerPage: Int): Flowable<List<Gist>> {
        return Flowable.defer {
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            Flowable.just(ArrayList(gistService.pageStarredGists(pageN, itemsPerPage).next()))
        }
    }

    override fun apiGetGist(token: String, gistId: String): Flowable<Gist> {
        return Flowable.defer {
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            Flowable.just(gistService.getGist(gistId))
        }
    }

    override fun apiGetGistComments(token: String, gistId: String): Flowable<List<Comment>> {
        return Flowable.defer {
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            Flowable.just(gistService.getComments(gistId))
        }
    }

    override fun apiStarGist(token: String, gistId: String): Completable {
        return Completable.create { subscriber ->
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            try {
                gistService.starGist(gistId)
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    override fun apiUnstarGist(token: String, gistId: String): Completable {
        return Completable.create { subscriber ->
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            try {
                gistService.unstarGist(gistId)
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    override fun apiIsGistStarred(token: String, gistId: String): Flowable<Boolean> {
        return Flowable.defer {
            val gistService: GistService = GistService()
            gistService.client.setOAuth2Token(token)
            Flowable.just(gistService.isStarred(gistId))
        }
    }

    override fun apiSearchRepos(token: String, query: String, filter: HashMap<String,String>): Flowable<List<Repository>> {
        return Flowable.defer {
            val repoService: RepositoryService = RepositoryService()
            repoService.client.setOAuth2Token(token)
            when (filter["sort"]) {
                "stars" -> Flowable.just(repoService.searchRepositories(query).sortedByDescending { it.watchers })
                "pushed" -> Flowable.just(repoService.searchRepositories(query).sortedByDescending { it.pushedAt })
                "updated" -> Flowable.just(repoService.searchRepositories(query).sortedByDescending { it.updatedAt })
                "full_name" -> Flowable.just(repoService.searchRepositories(query).sortedBy { it.name })
                else -> Flowable.just(repoService.searchRepositories(query))
            }
        }
    }

    override fun apiSearchUsers(token: String, query: String, filter: HashMap<String,String>): Flowable<List<SearchUser>> {
        return Flowable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            when (filter["sort"]) {
                "repos" -> Flowable.just(userService.searchUsers(query).sortedByDescending { it.publicRepos })
                "followers" -> Flowable.just(userService.searchUsers(query).sortedByDescending { it.followers })
                else -> Flowable.just(userService.searchUsers(query))
            }
        }
    }

    override fun apiSearchCode(token: String, query: String): Flowable<List<CodeSearchResult>> {
        return Flowable.defer {
            val repoService: RepositoryService = RepositoryService()
            repoService.client.setOAuth2Token(token)
            Flowable.just(repoService.searchCode(query))
        }
    }

    override fun apiIsRepoStarred(token: String, owner: String, name: String): Flowable<Boolean> {
        return Flowable.defer {
            val starService: StarService = StarService()
            starService.client.setOAuth2Token(token)
            Flowable.just(starService.isStarring(RepositoryId(owner, name)))
        }
    }

    override fun apiGetRepo(token: String, owner: String, name: String): Flowable<Repository> {
        return Flowable.defer {
            val repoService: RepositoryService = RepositoryService()
            repoService.client.setOAuth2Token(token)
            Flowable.just(repoService.getRepository(RepositoryId(owner, name)))
        }
    }

    override fun apiStarRepo(token: String, owner: String, name: String): Completable {
        return Completable.create { subscriber ->
            val starService: StarService = StarService()
            starService.client.setOAuth2Token(token)
            try {
                starService.star(RepositoryId(owner, name))
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    override fun apiUnstarRepo(token: String, owner: String, name: String): Completable {
        return Completable.create { subscriber ->
            val starService: StarService = StarService()
            starService.client.setOAuth2Token(token)
            try {
                starService.unstar(RepositoryId(owner, name))
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

    override fun apiGetReadme(token: String, owner: String, name: String): Flowable<String> {
        return Flowable.create({ emitter ->
            val contentsService: ContentsService = ContentsService()
            contentsService.client.setOAuth2Token(token)
            val markdown64: String = contentsService.getReadme(RepositoryId(owner, name)).content
            try {
                val markdown: String = String(Base64.decode(markdown64, Base64.DEFAULT))
                emitter.onNext(markdown)
            } catch (e: UnsupportedEncodingException) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }

    override fun apiGetRepoCommits(token: String, owner: String, name: String): Flowable<List<RepositoryCommit>> {
        return Flowable.defer {
            val commitService: CommitService = CommitService()
            commitService.client.setOAuth2Token(token)
            Flowable.just(commitService.getCommits(RepositoryId(owner, name)))
        }
    }

    override fun apiGetContributors(token: String, owner: String, name: String): Flowable<List<Contributor>> {
        return Flowable.defer {
            val repoService: RepositoryService = RepositoryService()
            repoService.client.setOAuth2Token(token)
            Flowable.just(repoService.getContributors(RepositoryId(owner, name), true))
        }
    }

    override fun apiGetContent(token: String, owner: String, name: String, path: String): Flowable<List<RepositoryContents>> {
        return Flowable.defer {
            val contentsService: ContentsService = ContentsService()
            contentsService.client.setOAuth2Token(token)
            Flowable.just(contentsService.getContents(RepositoryId(owner, name), path))
        }
    }

    override fun apiPageStargazers(token: String, owner: String, name: String, pageN: Int, itemsPerPage: Int): Flowable<List<User>> {
        return Flowable.defer {
            val stargazerService: StargazerService = StargazerService()
            stargazerService.client.setOAuth2Token(token)
            Flowable.just(stargazerService.pageStargazers(RepositoryId(owner, name), pageN, itemsPerPage).next().toMutableList())
        }
    }

    override fun apiPageIssues(token: String, owner: String, name: String, pageN: Int, itemsPerPage: Int, hashMap: HashMap<String,String>): Flowable<List<Issue>> {
        return Flowable.defer {
            val issueService: IssueService = IssueService()
            issueService.client.setOAuth2Token(token)
            Flowable.just(issueService.pageIssues(RepositoryId(owner, name), hashMap, pageN, itemsPerPage).next().toMutableList())
        }
    }

    override fun apiPageForks(token: String, owner: String, name: String, pageN: Int, itemsPerPage: Int): Flowable<List<Repository>> {
        return Flowable.defer {
            val repoService: RepositoryService = RepositoryService()
            repoService.client.setOAuth2Token(token)
            Flowable.just(repoService.getForks(RepositoryId(owner, name)))
        }
    }

    override fun apiForkRepo(token: String, owner: String, name: String): Completable {
        return Completable.create { subscriber ->
            val repoService: RepositoryService = RepositoryService()
            repoService.client.setOAuth2Token(token)
            try {
                repoService.forkRepository(RepositoryId(owner, name))
                subscriber.onComplete()
            } catch (e: Throwable) {
                subscriber.onError(e)
            }
        }
    }

}
