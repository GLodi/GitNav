/*
 * Copyright 2017 GLodi
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

package giuliolodi.gitnav.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.RepoActivity;

public class RepoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Repository> repositoryList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.repo_row_owner) TextView repo_row_owner;
        @BindView(R.id.repo_row_name) TextView repo_row_name;
        @BindView(R.id.repo_row_description) TextView repo_row_description;
        @BindView(R.id.repo_row_language) TextView repo_row_language;
        @BindView(R.id.repo_row_forked) TextView repo_row_forked;
        @BindView(R.id.repo_row_star_number) TextView repo_row_star_number;
        @BindView(R.id.repo_row_date) TextView repo_row_date;
        @BindView(R.id.repo_code) ImageView repo_row_language_icon;
        @BindView(R.id.repo_row_ll) LinearLayout ll;

        private PrettyTime p;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            p = new PrettyTime();

            // Use easy fonts to set Typeface
            repo_row_owner.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_row_name.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_row_description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_row_language.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_row_forked.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_row_star_number.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_row_date.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingHolder(View view) {
            super(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return repositoryList.get(position) != null ? 1 : 0;
    }

    public RepoAdapter(List<Repository> repositoryList, Context context) {
        this.repositoryList = repositoryList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_repo, parent, false);
            vh = new MyViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new LoadingHolder(itemView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {

            // Get repo and parent repo (if available)
            final Repository repo = repositoryList.get(position);
            Repository parent;

            // Set owner
            ((MyViewHolder)holder).repo_row_owner.setText(repo.getOwner().getLogin() + "/");

            // Set repo name
            ((MyViewHolder)holder).repo_row_name.setText(repo.getName());

            // Set repo description
            if (repo.getDescription() != null && !repo.getDescription().equals(""))
                ((MyViewHolder)holder).repo_row_description.setText(repo.getDescription());
            else
                ((MyViewHolder)holder).repo_row_description.setText("No description");

            // Set repo language
            if (repo.getLanguage() == null) {
                ((MyViewHolder)holder).repo_row_language.setVisibility(View.GONE);
                ((MyViewHolder)holder).repo_row_language_icon.setVisibility(View.GONE);
            }
            else
                ((MyViewHolder)holder).repo_row_language.setText(repo.getLanguage());

            // Set star repo number
            ((MyViewHolder)holder).repo_row_star_number.setText(Integer.toString(repo.getWatchers()));

            // Set repo date
            ((MyViewHolder)holder).repo_row_date.setText(((MyViewHolder)holder).p.format(repo.getCreatedAt()));

            // Check if is forked, then prints parent's info
            if (repo.isFork() && repo.getParent() != null) {
                parent = repo.getParent();
                ((MyViewHolder)holder).repo_row_forked.setText(parent.getName());
            }
            else {
                ((MyViewHolder)holder).repo_row_forked.setVisibility(View.GONE);
            }

            ((MyViewHolder)holder).ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, RepoActivity.class).putExtra("owner", repo.getOwner().getLogin()).putExtra("name", repo.getName()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return repositoryList.size();
    }

}
