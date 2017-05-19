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
import giuliolodi.gitnav.ui.events.EventActivity
import giuliolodi.gitnav.ui.login.LoginActivity
import giuliolodi.gitnav.ui.repositories.RepoListActivity
import giuliolodi.gitnav.ui.trending.TrendingActivity

/**
 * Created by giulio on 12/05/2017.
 */

@PerActivity
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun inject(loginActivity: LoginActivity)

    fun inject(eventActivity: EventActivity)

    fun inject(repoListActivity: RepoListActivity)

    fun inject(trendingActivity: TrendingActivity)

}