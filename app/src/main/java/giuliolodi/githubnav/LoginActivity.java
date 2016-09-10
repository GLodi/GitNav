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

package giuliolodi.githubnav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    public static final String API_LOGIN_URL = "https://github.com/login/oauth/authorize";
    public static final String API_OAUTH_CLIENTID = "";
    public static final String API_OAUTH_CLIENTSECRET = "";
    public static final String API_OAUTH_REDIRECT = "giuliolodi.githubnav://oauth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        final Button button = (Button) findViewById(R.id.btn_login);
        final EditText input_email = (EditText) findViewById(R.id.input_email);
        final EditText input_password = (EditText) findViewById(R.id.input_password);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(API_LOGIN_URL + "?client_id=" + API_OAUTH_CLIENTID));
                // This flag is set to prevent the browser with the login form from showing in the history stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                startActivity(intent);
                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();
        if(uri != null && uri.toString().startsWith(API_OAUTH_REDIRECT)) {
            String code = uri.getQueryParameter("code");
            if(code != null) {
                // TODO We can probably do something with this code! Show the user that we are logging them in

                final SharedPreferences prefs = this.getSharedPreferences(
                        BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

                APIClient client = ServiceGenerator.createService(APIClient.class);
                Call<AccessToken> call = client.getNewAccessToken(code, API_OAUTH_CLIENTID,
                        API_OAUTH_CLIENTSECRET, API_OAUTH_REDIRECT,
                        "authorization_code");
                call.enqueue(new Callback<AccessToken>() {
                    @Override
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                        int statusCode = response.code();
                        if(statusCode == 200) {
                            AccessToken token = response.body();
                            prefs.edit().putBoolean("oauth.loggedin", true).apply();
                            prefs.edit().putString("oauth.accesstoken", token.getAccessToken()).apply();
                            prefs.edit().putString("oauth.refreshtoken", token.getRefreshToken()).apply();
                            prefs.edit().putString("oauth.tokentype", token.getTokenType()).apply();

                            // TODO Show the user they are logged in
                        } else {
                            // TODO Handle errors on a failed response
                        }
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t) {
                        // TODO Handle failure
                    }
                });
            } else {
                // TODO Handle a missing code in the redirect URI
            }
        }
    }

}
