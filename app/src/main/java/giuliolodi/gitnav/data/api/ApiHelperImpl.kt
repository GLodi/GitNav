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
import giuliolodi.gitnav.di.scope.AppContext
import io.reactivex.Observable
import org.eclipse.egit.github.core.Authorization
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.event.Event
import org.eclipse.egit.github.core.service.EventService
import org.eclipse.egit.github.core.service.OAuthService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import javax.inject.Inject
import java.io.IOException

/**
 * Created by giulio on 12/05/2017.
 */

class ApiHelperImpl : ApiHelper {

    private val mContext: Context

    @Inject
    constructor(@AppContext context: Context) {
        mContext = context
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

    override fun apiGetUser(token: String, username: String): Observable<User> {
        return Observable.defer {
            val userService: UserService = UserService()
            userService.client.setOAuth2Token(token)
            Observable.just(userService.getUser(username))
        }
    }

    override fun apiPageEvents(token: String, username: String, pageN: Int, itemsPerPage: Int): Observable<List<Event>> {
        return Observable.defer {
            val eventService: EventService = EventService()
            eventService.client.setOAuth2Token(token)
            Observable.just(ArrayList(eventService.pageUserReceivedEvents(username, false, pageN, itemsPerPage).next()))
        }
    }

    override fun apiPageRepos(token: String, username: String, pageN: Int, itemsPerPage: Int, filter: HashMap<String, String>?): Observable<List<Repository>> {
        return Observable.defer {
            val repositoryService: RepositoryService = RepositoryService()
            repositoryService.client.setOAuth2Token(token)
            if (filter?.get("sort") != "starred")
                Observable.just(ArrayList(repositoryService.pageRepositories(username, filter, pageN, itemsPerPage).next()))
            else
                Observable.just(ArrayList(repositoryService.getRepositories(username).sortedByDescending { it.watchers }))
        }
    }

}
