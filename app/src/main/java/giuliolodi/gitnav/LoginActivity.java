/*
 * MIT License
 *
 * Copyright (c) 2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
            Intent intent = new Intent(this, BaseDrawerActivity.class);
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
                        Toast t = Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG);
                        t.show();
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
                    Toast t = Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG);
                    t.show();
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
                editor.putString(Constants.getUserKey(getApplicationContext()), inputUser);
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
            Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
            toast.show();
            if (result == "Logged in") {
                Intent intent = new Intent(getApplicationContext(), BaseDrawerActivity.class);
                startActivity(intent);
                finish();
            }
        }

    }

}
