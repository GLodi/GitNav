/*
 * Copyright (c)  2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package giuliolodi.navforgithub;

import android.content.Context;
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

}
