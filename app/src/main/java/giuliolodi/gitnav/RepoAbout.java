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
import android.support.v7.widget.*;
import android.view.View;
import android.widget.ProgressBar;

import org.eclipse.egit.github.core.Repository;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Adapters.RepoAboutAdapter;

public class RepoAbout {

    @BindView(R.id.repo_about_progressbar) ProgressBar progressBar;
    @BindView(R.id.repo_about_gridview) RecyclerView gridView;

    private Context context;
    private Repository repo;
    private List<String> nameList, numberList;
    private int stargazerNumber;

    public void populate (Context context, View v, Repository repo, int stargazerNumber) {
        this.context = context;
        this.repo = repo;
        this.stargazerNumber = stargazerNumber;

        ButterKnife.bind(this, v);

        nameList = new ArrayList<>();
        nameList.add("Stargazers");
        nameList.add("Forks");
        nameList.add("Issues");
        nameList.add("Prova");

        numberList = new ArrayList<>();
        numberList.add(String.valueOf(stargazerNumber));
        numberList.add(String.valueOf(repo.getForks()));
        numberList.add(String.valueOf(repo.getOpenIssues()));
        numberList.add("2000");

        gridView.setLayoutManager(new GridLayoutManager(context, 3));
        gridView.setHasFixedSize(true);
        gridView.setAdapter(new RepoAboutAdapter(context, nameList, numberList, repo.getName(), repo.getOwner().getLogin()));

    }

}
