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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;

import com.mittsu.markedview.MarkedView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.ContentsService;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepoReadme {

    @BindView(R.id.repo_readme_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_readme_markedview) MarkedView markedView;

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
                markedView.setMDText(markdown);
            }

            progressBar.setVisibility(View.GONE);
        }
    }

}
