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
import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.di.scope.AppContext
import giuliolodi.gitnav.di.scope.PreferenceInfo
import giuliolodi.gitnav.utils.ImageSaver
import org.eclipse.egit.github.core.User
import javax.inject.Inject

/**
 * Created by giulio on 12/05/2017.
 */
class PrefsHelperImpl : PrefsHelper {

    private val PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"
    private val PREF_KEY_FULLNAME= "PREF_KEY_FULLNAME"
    private val PREF_KEY_USERNAME = "PREF_KEY_USERNAME"
    private val PREF_KEY_EMAIL = "PREF_KEY_EMAIL"

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
        val token: String
        if (mPrefs.getString(PREF_KEY_ACCESS_TOKEN, null) != null)
            token = mPrefs.getString(PREF_KEY_ACCESS_TOKEN, null)
        else
            token = ""
        return token
    }

    override fun storeUser(user: User) {
        mPrefs.edit().putString(PREF_KEY_USERNAME, user.login).apply()
        if (user.name != null && !user.name.isEmpty())
            mPrefs.edit().putString(PREF_KEY_FULLNAME, user.name).apply()
        if (user.email != null && !user.email.isEmpty())
            mPrefs.edit().putString(PREF_KEY_EMAIL, user.email).apply()
        else
            mPrefs.edit().putString(PREF_KEY_EMAIL, "No public email address").apply()
        val profilePic: Bitmap = Picasso.with(mContext).load(user.avatarUrl).get()
        ImageSaver(mContext).setFileName("thumbnail.png").setDirectoryName("images").save(profilePic)
    }

    override fun getUsername(): String {
        return mPrefs.getString(PREF_KEY_USERNAME, null)
    }

    override fun getFullname(): String? {
        return mPrefs.getString(PREF_KEY_FULLNAME, null)
    }

    override fun getEmail(): String? {
        return mPrefs.getString(PREF_KEY_EMAIL, null)
    }

    override fun getPic(): Bitmap {
        return ImageSaver(mContext).setFileName("thumbnail.png").setDirectoryName("images").load()
    }

}
