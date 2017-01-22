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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Issue;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.UserActivity;

public class IssueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Issue> issueList;
    private PrettyTime p = new PrettyTime();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_issue_username) TextView username;
        @BindView(R.id.row_issue_issuename) TextView issueName;
        @BindView(R.id.row_issue_comment_n) TextView commentN;
        @BindView(R.id.row_issue_date) TextView date;
        @BindView(R.id.row_issue_n) TextView number;
        @BindView(R.id.row_issue_image) CircleImageView profilePic;
        @BindView(R.id.row_issue_forum_icon) ImageView icon;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            username.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            issueName.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            commentN.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            date.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingHolder(View view) {
            super(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return issueList.get(position) != null ? 1 : 0;
    }

    public IssueAdapter(List<Issue> issueList) {
        this.issueList = issueList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_issue, parent, false);
            vh = new MyViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new LoadingHolder(itemView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof MyViewHolder) {
            final Context context = ((MyViewHolder)holder).username.getContext();
            ((MyViewHolder)holder).username.setText(issueList.get(position).getUser().getLogin());
            ((MyViewHolder)holder).issueName.setText(issueList.get(position).getTitle());
            ((MyViewHolder)holder).number.setText("#" + issueList.get(position).getNumber());
            if (issueList.get(position).getComments() != 0)
                ((MyViewHolder)holder).commentN.setText(String.valueOf(issueList.get(position).getComments()));
            else {
                ((MyViewHolder)holder).commentN.setVisibility(View.GONE);
                ((MyViewHolder)holder).icon.setVisibility(View.GONE);
            }
            ((MyViewHolder)holder).date.setText(p.format(issueList.get(position).getCreatedAt()));
            Picasso.with(context).load(issueList.get(position).getUser().getAvatarUrl()).resize(75, 75).centerCrop().into(((MyViewHolder)holder).profilePic);

            ((MyViewHolder)holder).profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", issueList.get(position).getUser().getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }
}
