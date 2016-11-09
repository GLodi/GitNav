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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eclipse.egit.github.core.CodeSearchResult;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.CodeAdapter;

public class SearchCode  {

    @BindView(R.id.search_code_progress_bar) ProgressBar progressBar;
    @BindView(R.id.search_code_rv) RecyclerView recyclerView;
    @BindView(R.id.no_code) TextView noCode;

    private CodeAdapter codeAdapter;
    private LinearLayoutManager linearLayoutManager;

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
        new getCode().execute();
    }

    public boolean isLOADING() {
        return LOADING;
    }

    public class getCode extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            noCode.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(Constants.getToken(context));

            try {
                searchResultList = repositoryService.searchCode(query);
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (searchResultList == null ||searchResultList.isEmpty())
                noCode.setVisibility(View.VISIBLE);

            codeAdapter = new CodeAdapter(searchResultList, context);
            linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            if (PREVENT_MULTIPLE_SEPARATOR_LINE)
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(codeAdapter);
            progressBar.setVisibility(View.GONE);
            codeAdapter.notifyDataSetChanged();

            LOADING = false;
        }
    }

}
