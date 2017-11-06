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

import android.app.Application
import android.content.Context
import dagger.Component
import giuliolodi.gitnav.di.module.AppModule
import giuliolodi.gitnav.di.scope.AppContext
import giuliolodi.gitnav.App
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.di.module.NetModule
import javax.inject.Singleton

/**
 * Created by giulio on 12/05/2017.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, NetModule::class))
interface AppComponent {

    fun inject(app: App)

    @AppContext
    fun context(): Context

    fun application(): Application

    fun dataManager(): DataManager

}