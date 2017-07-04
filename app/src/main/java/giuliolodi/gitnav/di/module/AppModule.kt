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

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.data.DataManagerImpl
import giuliolodi.gitnav.data.api.ApiHelper
import giuliolodi.gitnav.data.api.ApiHelperImpl
import giuliolodi.gitnav.data.prefs.PrefsHelper
import giuliolodi.gitnav.data.prefs.PrefsHelperImpl
import giuliolodi.gitnav.di.scope.AppContext
import javax.inject.Singleton
import giuliolodi.gitnav.di.scope.PreferenceInfo
import giuliolodi.gitnav.di.scope.UrlInfo
import giuliolodi.gitnav.utils.Constants

/**
 * Created by giulio on 12/05/2017.
 */
@Module
class AppModule(private val application: Application) {

    @Provides
    @AppContext
    fun provideContext(): Context {
        return application
    }

    @Provides
    fun provideApplication(): Application {
        return application
    }

    @Provides
    @PreferenceInfo
    fun providePreferenceName(): String {
        return Constants.PREFS_NAME
    }

    @Provides
    @UrlInfo
    fun provideUrlInfo(): Map<String,String> {
        return mapOf("base" to Constants.BASE_URL,
                "daily" to Constants.DAILY_URL,
                "weekly" to Constants.WEEKLY_URL,
                "monthly" to Constants.MONTHLY_URL)
    }

    @Provides
    @Singleton
    fun provideDataManager(dataManagerImpl: DataManagerImpl): DataManager {
        return dataManagerImpl
    }

    @Provides
    @Singleton
    fun provideApiHelper(apiHelperImpl: ApiHelperImpl): ApiHelper {
        return apiHelperImpl
    }

    @Provides
    @Singleton
    fun providePrefsHelper(prefsHelperImpl: PrefsHelperImpl): PrefsHelper {
        return prefsHelperImpl
    }

}