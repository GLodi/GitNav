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

package giuliolodi.gitnav.di.module

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import giuliolodi.gitnav.di.scope.ActivityContext
import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.events.EventContract
import giuliolodi.gitnav.ui.events.EventPresenter
import giuliolodi.gitnav.ui.fileviewer.FileViewerContract
import giuliolodi.gitnav.ui.fileviewer.FileViewerPresenter
import giuliolodi.gitnav.ui.gist.*
import giuliolodi.gitnav.ui.gistlist.GistListContract
import giuliolodi.gitnav.ui.gistlist.GistListPresenter
import giuliolodi.gitnav.ui.login.LoginContract
import giuliolodi.gitnav.ui.login.LoginPresenter
import giuliolodi.gitnav.ui.repository.*
import giuliolodi.gitnav.ui.repositorylist.RepoListContract
import giuliolodi.gitnav.ui.repositorylist.RepoListPresenter
import giuliolodi.gitnav.ui.search.SearchContract
import giuliolodi.gitnav.ui.search.SearchPresenter
import giuliolodi.gitnav.ui.starred.StarredContract
import giuliolodi.gitnav.ui.starred.StarredPresenter
import giuliolodi.gitnav.ui.trending.TrendingContract
import giuliolodi.gitnav.ui.trending.TrendingPresenter
import giuliolodi.gitnav.ui.user.UserContract
import giuliolodi.gitnav.ui.user.UserPresenter
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by giulio on 12/05/2017.
 */
@Module
class ActivityModule(val activity: Activity) {

    @Provides
    @ActivityContext
    fun provideContext(): Context {
        return activity
    }

    @Provides
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Provides
    @PerActivity
    fun provideLoginPresenter(presenter: LoginPresenter<LoginContract.View>): LoginContract.Presenter<LoginContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideEventPresenter(presenter: EventPresenter<EventContract.View>): EventContract.Presenter<EventContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideRepoListPresenter(presenter: RepoListPresenter<RepoListContract.View>): RepoListContract.Presenter<RepoListContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideTrendingPresenter(presenter: TrendingPresenter<TrendingContract.View>): TrendingContract.Presenter<TrendingContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideStarredPresenter(presenter: StarredPresenter<StarredContract.View>): StarredContract.Presenter<StarredContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideGistListPresenter(presenter: GistListPresenter<GistListContract.View>): GistListContract.Presenter<GistListContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideGistPresenter(presenter: GistPresenter<GistContract.View>): GistContract.Presenter<GistContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideGistFilesPresenter(presenter: GistFilesPresenter<GistFilesContract.View>): GistFilesContract.Presenter<GistFilesContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideGistCommentsPresenter(presenter: GistCommentsPresenter<GistCommentsContract.View>): GistCommentsContract.Presenter<GistCommentsContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideSearchPresenter(presenter: SearchPresenter<SearchContract.View>): SearchContract.Presenter<SearchContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideUserPresenter(presenter: UserPresenter<UserContract.View>): UserContract.Presenter<UserContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideRepoPresenter(presenter: RepoPresenter<RepoContract.View>): RepoContract.Presenter<RepoContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideRepoReadmePresenter(presenter: RepoReadmePresenter<RepoReadmeContract.View>): RepoReadmeContract.Presenter<RepoReadmeContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideRepoCommitsPresenter(presenter: RepoCommitsPresenter<RepoCommitsContract.View>): RepoCommitsContract.Presenter<RepoCommitsContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideRepoAboutPresenter(presenter: RepoAboutPresenter<RepoAboutContract.View>): RepoAboutContract.Presenter<RepoAboutContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideRepoContentPresenter(presenter: RepoContentPresenter<RepoContentContract.View>): RepoContentContract.Presenter<RepoContentContract.View> {
        return presenter
    }

    @Provides
    @PerActivity
    fun provideFileViewerPresenter(presenter: FileViewerPresenter<FileViewerContract.View>): FileViewerContract.Presenter<FileViewerContract.View> {
        return presenter
    }

}