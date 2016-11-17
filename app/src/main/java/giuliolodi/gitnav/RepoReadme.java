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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;

import com.mukesh.MarkdownView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.ContentsService;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoReadme {

    @BindView(R.id.repo_readme_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_readme_markedview) MarkdownView markedView;

    private ContentsService contentsService;

    private String markdownBase64;
    private String markdown;

    private Context context;
    private Repository repo;

    public void populate(Context context, View v, Repository repo) {
        this.context = context;
        this.repo = repo;

        ButterKnife.bind(this, v);

        new getReadme().execute();
    }

    private class getReadme extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            contentsService = new ContentsService();
            contentsService.getClient().setOAuth2Token(Constants.getToken(context));

            try {
                markdownBase64 = contentsService.getReadme(new RepositoryId(repo.getOwner().getLogin(), repo.getName())).getContent();
            } catch (Exception e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (markdownBase64 != null && !markdownBase64.isEmpty()) {
                try {
                    markdown = new String(Base64.decode(markdownBase64, Base64.DEFAULT), "UTF-8");
                } catch (UnsupportedEncodingException e) {e.printStackTrace();}
                markedView.setMarkDownText(markdown);
                markedView.setOpenUrlInBrowser(true);
            }

            progressBar.setVisibility(View.GONE);
        }
    }

}
