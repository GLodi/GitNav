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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class GistsMine {

    @BindView(R.id.gists_mine_progress_bar) ProgressBar progressBar;
    @BindView(R.id.gists_mine_rv) RecyclerView recyclerView;
    @BindView(R.id.gists_mine_no) TextView noMineGists;

    private List<Gist> gistsList;
    private Context context;

    public void populate(Context context) {
        this.context = context;
        new getMineGists().execute();
    }

    public class getMineGists extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            noMineGists.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            GistService gistService = new GistService();
            gistService.getClient().setOAuth2Token(Constants.getToken(context));

            gistsList = new ArrayList<>(gistService.pageGists(Constants.getUsername(context)).next());

            return null;
        }
    }

}
