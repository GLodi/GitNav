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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.RepositoryCommit;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.UserActivity;

public class CommitAdapter extends RecyclerView.Adapter<CommitAdapter.MyViewHolder>{

    private List<RepositoryCommit> repositoryCommitList;
    private PrettyTime p;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_commit_ll) LinearLayout ll;
        @BindView(R.id.row_commit_image) CircleImageView image;
        @BindView(R.id.row_commit_description) TextView description;
        @BindView(R.id.row_commit_author) TextView author;
        @BindView(R.id.row_commit_date) TextView date;
        @BindView(R.id.row_commit_sha) TextView sha;


        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            author.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            date.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            sha.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public CommitAdapter(List<RepositoryCommit> repositoryCommitList, Context context) {
        this.repositoryCommitList = repositoryCommitList;
        this.context = context;
    }

    @Override
    public CommitAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_commit, parent, false);
        return new CommitAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CommitAdapter.MyViewHolder holder, final int position) {

        p = new PrettyTime();

        String description = repositoryCommitList.get(position).getCommit().getMessage();

        int pos = description.indexOf('\n');
        if (pos > 0) {
            description = description.substring(0, pos);
        }

        // Set texts
        holder.author.setText(repositoryCommitList.get(position).getAuthor().getLogin());
        holder.description.setText(description);
        holder.sha.setText(repositoryCommitList.get(position).getSha().substring(0, 12));
        holder.date.setText(p.format(repositoryCommitList.get(position).getCommit().getAuthor().getDate()));

        // Set picture
        Picasso.with(holder.author.getContext()).load(repositoryCommitList.get(position).getAuthor().getAvatarUrl()).resize(150, 150).centerCrop().into(holder.image);

        // Set listener to invoke UserActivity
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", repositoryCommitList.get(position).getAuthor().getLogin()));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });

    }

    @Override
    public int getItemCount() {
        return repositoryCommitList.size();
    }

}
