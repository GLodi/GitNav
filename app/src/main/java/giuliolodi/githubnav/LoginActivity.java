package giuliolodi.githubnav;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.OAuthService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class LoginActivity extends AppCompatActivity {

    public OAuthService oAuthService;
    public SharedPreferences sp;
    public SharedPreferences.Editor editor;

    public String PREF;
    public String USER;
    public String TOKEN;
    public String inputUser;
    public String inputPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        PREF = getString(R.string.pref_key);
        USER = getString(R.string.user_key);
        TOKEN = getString(R.string.token_key);

        final EditText input_user = (EditText) findViewById(R.id.input_user);
        final EditText input_password = (EditText) findViewById(R.id.input_password);
        final Button button = (Button) findViewById(R.id.btn_login);

        sp = getSharedPreferences(PREF, Context.MODE_PRIVATE);

        // TODO: check if old credentials work and Intent to Main Activity

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputUser = input_user.getText().toString();
                inputPass = input_password.getText().toString();

                new newAccess().execute();

                // TODO: Intent to Main Activity
            }
        });

        /*
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oAuthService = new OAuthService();
                oAuthService.getClient().setCredentials(input_user.getText().toString(), input_password.getText().toString());

                Authorization auth = new Authorization();
                auth.setScopes(Arrays.asList("repo", "gist", "user"));
                auth.setNote("Token");

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                try {
                    auth = oAuthService.createAuthorization(auth);
                } catch (IOException e) {e.printStackTrace();}

                GistService gistService = new GistService();
                gistService.getClient().setOAuth2Token(auth.getToken());

                Gist gist = new Gist();
                gist.setPublic(false);
                gist.setDescription("Primo Gist di prova");
                GistFile file = new GistFile();
                file.setContent("Prima prova");
                file.setFilename("PrimoGist.txt");
                gist.setFiles(Collections.singletonMap(file.getFilename(), file));

                try {
                    gist = gistService.createGist(gist);
                } catch (IOException e) {e.printStackTrace();}

                Log.d("QUI", "Created Gist at " + gist.getHtmlUrl());
            }
        });
        */

    }

    class newAccess extends AsyncTask<String, String, String> {

        ProgressDialog progDailog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog.setMessage("Logging in");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }
        @Override
        protected String doInBackground(String... back) {
            PREF = getString(R.string.pref_key);
            USER = getString(R.string.user_key);
            TOKEN = getString(R.string.token_key);

            oAuthService = new OAuthService();
            oAuthService.getClient().setCredentials(inputUser, inputPass);

            Authorization auth = new Authorization();
            auth.setScopes(Arrays.asList("repo", "gist", "user"));
            auth.setNote("GitHubNav");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String error = "";
            editor = sp.edit();

            try {
                auth = oAuthService.createAuthorization(auth);
                if (auth.getToken() != "") {
                    editor.putString(TOKEN, auth.getToken());
                    editor.commit();
                }
            } catch (IOException e) {
                error = e.getMessage();
            }

            if (error == "") {
                editor.putString(USER, inputUser);
                editor.commit();
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
        }

    }

}
