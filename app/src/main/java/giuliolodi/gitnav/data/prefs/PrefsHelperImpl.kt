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

package giuliolodi.gitnav.data.prefs

import android.content.Context
import android.content.SharedPreferences
import giuliolodi.gitnav.di.scope.AppContext
import giuliolodi.gitnav.di.scope.PreferenceInfo
import java.io.IOException
import javax.inject.Inject

/**
 * Created by giulio on 12/05/2017.
 */

class PrefsHelperImpl : PrefsHelper {

    private val PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"

    private val mContext: Context
    private val mPrefs: SharedPreferences

    @Inject
    constructor(@AppContext context: Context, @PreferenceInfo prefFileName: String) {
        mContext = context
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)
    }

    override fun storeAccessToken(token: String) {
        mPrefs.edit().putString(PREF_KEY_ACCESS_TOKEN, token).apply()
    }

    override fun getToken(): String {
        var token: String
        try {
            token = mPrefs.getString(PREF_KEY_ACCESS_TOKEN, null)
        } catch (e: IOException) {
            token = ""
        }
        return token
    }

}
