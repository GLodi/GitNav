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

import org.eclipse.egit.github.core.CodeSearchResult;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchCode  {

    @BindView(R.id.search_code_progress_bar) ProgressBar progressBar;
    @BindView(R.id.search_code_rv) RecyclerView recyclerView;
    @BindString(R.string.no_code) String noCode;

    private String query;
    private List<CodeSearchResult> searchResultList;
    private Context context;

    private boolean PREVENT_MULTIPLE_SEPARATOR_LINE;
    private boolean LOADING = false;

    public void populate(String query, Context context, View v, boolean PREVENT_MULTIPLE_SEPARATOR_LINE) {
        this.query = query;
        this.context = context;
        this.PREVENT_MULTIPLE_SEPARATOR_LINE = PREVENT_MULTIPLE_SEPARATOR_LINE;
        ButterKnife.bind(this, v);
        LOADING = true;
    }

    public boolean isLOADING() {
        return LOADING;
    }

    public class getCode extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(context));

            try {
                searchResultList = repositoryService.searchCode(query);
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }
    }

}
