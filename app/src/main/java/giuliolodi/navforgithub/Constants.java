/*
 * Copyright 2016 Giulio Lodi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package giuliolodi.navforgithub;

import android.content.Context;
import android.preference.PreferenceManager;

public class Constants {

    public static String prefKey (Context context) {
        return context.getString(R.string.pref_key);
    }

    public static String userKey (Context context) {
        return context.getString(R.string.user_key);
    }

    public static String tokenKey (Context context) {
        return context.getString(R.string.token_key);
    }

    public static String authdKey (Context context) {
        return context.getString(R.string.authd_key);
    }

    public static String prefValue (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(prefKey(context), "");
    }

    public static String userValue (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(userKey(context), "");
    }

    public static String tokenValue (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(tokenKey(context), "");
    }

    public static Boolean authdValue (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(authdKey(context), false);
    }

}
