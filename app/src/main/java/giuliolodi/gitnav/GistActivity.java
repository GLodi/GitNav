/*
 * Copyright 2016 GLodi
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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GistActivity extends BaseDrawerActivity {

    @BindView(R.id.gist_activity_progress_bar) ProgressBar progressBar;

    private Gist gist;

    private Intent intent;
    private String gistId;
    private GistService gistService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.gist_activity, frameLayout);

        ButterKnife.bind(this);

        intent = getIntent();
        gistId = intent.getStringExtra("GistId");

        new getGist().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    private class getGist extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            gistService = new GistService();
            gistService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

            try {
                gist = gistService.getGist(gistId);
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getSupportActionBar().setTitle(gist.getDescription());
            progressBar.setVisibility(View.GONE);
        }
    }
}
