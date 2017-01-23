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

package giuliolodi.gitnav;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class Constants {

    public static String getPrefKey (Context context) {
        return context.getString(R.string.pref_key);
    }

    public static String getUserKey (Context context) {
        return context.getString(R.string.user_key);
    }

    public static String getTokenKey (Context context) {
        return context.getString(R.string.token_key);
    }

    public static String getAuthdKey (Context context) {
        return context.getString(R.string.authd_key);
    }

    public static String getEmailKey (Context context) {
        return context.getString(R.string.email_key);
    }

    public static String getFullNameKey (Context context) {
        return context.getString(R.string.login_key);
    }

    public static String getPrefValue (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(getPrefKey(context), "");
    }

    public static String getUsername (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(getUserKey(context), "");
    }

    public static String getToken (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(getTokenKey(context), "");
    }

    public static Boolean getAuthdValue (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getAuthdKey(context), false);
    }

    public static String getEmail (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(getEmailKey(context), "");
    }

    public static String getFullName (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(getFullNameKey(context), "");
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

}
