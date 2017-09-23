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

package giuliolodi.gitnav.di.component

import dagger.Component
import giuliolodi.gitnav.di.module.ActivityModule
import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.contributorlist.ContributorListFragment
import giuliolodi.gitnav.ui.events.EventFragment
import giuliolodi.gitnav.ui.fileviewer.FileViewerFragment
import giuliolodi.gitnav.ui.forklist.ForkListFragment
import giuliolodi.gitnav.ui.gist.GistFragment
import giuliolodi.gitnav.ui.gist.GistCommentsFragment
import giuliolodi.gitnav.ui.gist.GistFilesFragment
import giuliolodi.gitnav.ui.gistlist.GistListFragment
import giuliolodi.gitnav.ui.issuelist.IssueClosedFragment
import giuliolodi.gitnav.ui.issuelist.IssueOpenFragment
import giuliolodi.gitnav.ui.login.LoginActivity
import giuliolodi.gitnav.ui.repository.*
import giuliolodi.gitnav.ui.repositorylist.RepoListFragment
import giuliolodi.gitnav.ui.search.SearchFragment
import giuliolodi.gitnav.ui.starred.StarredFragment
import giuliolodi.gitnav.ui.trending.TrendingFragment
import giuliolodi.gitnav.ui.user.UserActivity
import giuliolodi.gitnav.ui.stargazerlist.StargazerListFragment
import giuliolodi.gitnav.ui.user.UserFragment

/**
 * Created by giulio on 12/05/2017.
 */
@PerActivity
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun inject(loginActivity: LoginActivity)

    fun inject(eventFragment: EventFragment)

    fun inject(repoListFragment: RepoListFragment)

    fun inject(starredFragment: StarredFragment)

    fun inject(trendingFragment: TrendingFragment)

    fun inject(gistListFragment: GistListFragment)

    fun inject(gistFragment: GistFragment)

    fun inject(gistFilesFragment: GistFilesFragment)

    fun inject(gistCommentsFragment: GistCommentsFragment)

    fun inject(searchFragment: SearchFragment)

    fun inject(userActivity: UserActivity)

    fun inject(repoFragment: RepoFragment)

    fun inject(repoReadmeFragment: RepoReadmeFragment)

    fun inject(repoCommitsFragment: RepoCommitsFragment)

    fun inject(repoAboutFragment: RepoAboutFragment)

    fun inject(repoContentFragment: RepoContentFragment)

    fun inject(fileViewerFragment: FileViewerFragment)

    fun inject(stargazerListFragment: StargazerListFragment)

    fun inject(contributorListFragment: ContributorListFragment)

    fun inject(issueOpenFragment: IssueOpenFragment)

    fun inject(issueClosedFragment: IssueClosedFragment)

    fun inject(forkListFragment: ForkListFragment)

    fun inject(userFragment: UserFragment)

}