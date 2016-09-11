package giuliolodi.githubnav;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        final EditText input_user = (EditText) findViewById(R.id.input_user);
        final EditText input_password = (EditText) findViewById(R.id.input_password);
        final Button button = (Button) findViewById(R.id.btn_login);

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

    }

}
