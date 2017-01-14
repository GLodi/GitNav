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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IssueListOpen {

    @BindView(R.id.issuelist_open_progressbar) ProgressBar progressBar;
    @BindView(R.id.issuelist_open_rv) RecyclerView rv;

    private Context context;

    public void populate(final Context context, View view) {
        this.context = context;

        ButterKnife.bind(this, view);

        progressBar.setVisibility(View.VISIBLE);


    }

}
