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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    public OAuthService oAuthService;
    public SharedPreferences sp;
    public SharedPreferences.Editor editor;

    public String inputUser;
    public String inputPass;

    @BindView(R.id.input_user) EditText input_user;
    @BindView(R.id.input_password) EditText input_password;
    @BindView(R.id.btn_login) Button button;
    @BindString(R.string.network_error) String network_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        ButterKnife.bind(this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // If user is already logged in, Intent to BaseDrawerActivity
        if (Constants.getAuthdValue(getApplicationContext())) {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
            finish();
        }

        input_password.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    inputUser = input_user.getText().toString();
                    inputPass = input_password.getText().toString();
                    if (Constants.isNetworkAvailable(getApplicationContext()))
                        new newAccess().execute();
                    else {
                        Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputUser = input_user.getText().toString();
                inputPass = input_password.getText().toString();
                if (Constants.isNetworkAvailable(getApplicationContext()))
                    new newAccess().execute();
                else {
                    Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    class newAccess extends AsyncTask<String, String, String> {

        ProgressDialog progDailog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog.setMessage("Signing in");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }
        @Override
        protected String doInBackground(String... back) {
            oAuthService = new OAuthService();
            oAuthService.getClient().setCredentials(inputUser, inputPass);

            // This will set the token parameters and its permissions
            Authorization auth = new Authorization();
            auth.setScopes(Arrays.asList("repo", "gist", "user"));
            String description = "GitNav - " + Build.MANUFACTURER + " " + Build.MODEL;
            auth.setNote(description);

            // Required for some reason
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String error = "";
            editor = sp.edit();

            // Check if token already exists and deletes it.
            try {
                for (Authorization authorization : oAuthService.getAuthorizations()) {
                    if (authorization.getNote() != null && authorization.getNote().equals(description)) {
                        oAuthService.deleteAuthorization(authorization.getId());
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }

            // Creates new token. Saves login and token.
            try {
                auth = oAuthService.createAuthorization(auth);
                if (auth.getToken() != "") {
                    editor.putString(Constants.getTokenKey(getApplicationContext()), auth.getToken());
                    editor.commit();
                }
            } catch (IOException e) { error = e.getMessage(); }

            if (error == "") {
                // Save username from EditText
                editor.putBoolean(Constants.getAuthdKey(getApplicationContext()), true);

                // Save email, login and profile picture
                UserService userService = new UserService();
                userService.getClient().setOAuth2Token(Constants.getToken(getBaseContext()));
                try {
                    User user = userService.getUser();
                    if (user.getEmail() != null && !user.getEmail().isEmpty())
                        editor.putString(Constants.getEmailKey(getApplicationContext()), user.getEmail());
                    else
                        editor.putString(Constants.getEmailKey(getApplicationContext()), "No public email address");
                    if (user.getName() != null && !user.getName().isEmpty())
                        editor.putString(Constants.getFullNameKey(getApplicationContext()), user.getName());
                    editor.putString(Constants.getUserKey(getApplicationContext()), user.getLogin());
                    editor.commit();
                    Bitmap profile_picture = Picasso.with(getApplicationContext()).load(user.getAvatarUrl()).get();
                    new ImageSaver(getApplicationContext())
                            .setFileName("thumbnail.png")
                            .setDirectoryName("images")
                            .save(profile_picture);
                } catch (IOException e) {e.printStackTrace();}

                return "Logged in";
            }
            else {
                return error;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progDailog.dismiss();
            if (result == "Logged in") {
                Toasty.success(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), BaseDrawerActivity.class);
                startActivity(intent);
                finish();
            } else
                Toasty.error(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }

    }

}
