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

package giuliolodi.gitnav.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.RepoActivity;
import giuliolodi.gitnav.UserActivity;


public class StarredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Repository> starredRepositoryList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.starred_repo_name) TextView starred_name;
        @BindView(R.id.starred_repo_description) TextView starred_description;
        @BindView(R.id.starred_repo_language) TextView starred_language;
        @BindView(R.id.starred_repo_star_number) TextView starred_stars;
        @BindView(R.id.starred_repo_date) TextView starred_repo_date;
        @BindView(R.id.starred_repo_row_ll) LinearLayout linearLayout;
        @BindView(R.id.starred_repo_author_icon) CircleImageView starred_repo_author_icon;
        @BindView(R.id.starred_code) ImageView starred_language_icon;

        private PrettyTime p;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            p = new PrettyTime();

            // Use easy fonts to set Typeface
            starred_name.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_language.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_stars.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_repo_date.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingHolder(View view) {
            super(view);

        }

    }

    @Override
    public int getItemViewType(int position) {
        int a = starredRepositoryList.get(position) != null ? 1 : 0;
        return a;
    }

    public StarredAdapter(List<Repository> starredRepositoryList, Context context) {
        this.starredRepositoryList = starredRepositoryList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_starred, parent, false);
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
            // Set elements on each row
            final Repository repo = starredRepositoryList.get(position);

            // Set starred repo owner profile pic
            Picasso.with(context).load(repo.getOwner().getAvatarUrl()).resize(75, 75).centerCrop().into(((MyViewHolder)holder).starred_repo_author_icon);

            // Set starred repo name
            ((MyViewHolder)holder).starred_name.setText(repo.getName());

            // Set starred repo description
            if (repo.getDescription() != null && !repo.getDescription().equals(""))
                ((MyViewHolder)holder).starred_description.setText(repo.getDescription());
            else
                ((MyViewHolder)holder).starred_description.setText("No description");

            // Set starred repo language
            if (repo.getLanguage() == null) {
                ((MyViewHolder)holder).starred_language.setVisibility(View.GONE);
                ((MyViewHolder)holder).starred_language_icon.setVisibility(View.GONE);
            }
            else {
                ((MyViewHolder)holder).starred_language.setText(repo.getLanguage());
            }

            // Set starred repo star number
            ((MyViewHolder)holder).starred_stars.setText(Integer.toString(repo.getWatchers()));

            // Set starred repo date
            ((MyViewHolder)holder).starred_repo_date.setText(((MyViewHolder)holder).p.format(repo.getCreatedAt()));

            // Set repo click listener
            ((MyViewHolder)holder).linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, RepoActivity.class).putExtra("owner", repo.getOwner().getLogin()).putExtra("name", repo.getName()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });

            // Set user icon click listener. Opens UserActivity
            ((MyViewHolder)holder).starred_repo_author_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", repo.getOwner().getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return starredRepositoryList.size();
    }

}
