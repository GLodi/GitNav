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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.CommitComment;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;

public class CommitCommentsAdapter extends RecyclerView.Adapter<CommitCommentsAdapter.CommitCommentsHolder> {

    private List<CommitComment> commitComments;
    private Context context;
    private PrettyTime p;

    public class CommitCommentsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_commit_comment_image) CircleImageView imageView;
        @BindView(R.id.row_commit_comment_comment) TextView comment;
        @BindView(R.id.row_commit_comment_username) TextView username;
        @BindView(R.id.row_commit_comment_date) TextView date;

        public CommitCommentsHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            p = new PrettyTime();
            comment.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public CommitCommentsAdapter(List<CommitComment> commitComments, Context context) {
        this.commitComments = commitComments;
        this.context = context;
    }

    @Override
    public CommitCommentsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_commit_comment, parent, false);
        return new CommitCommentsAdapter.CommitCommentsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CommitCommentsHolder holder, int position) {
        holder.username.setText(commitComments.get(position).getUser().getLogin());
        holder.comment.setText(commitComments.get(position).getBodyText());
        holder.date.setText(p.format(commitComments.get(position).getCreatedAt()));
        Picasso.with(context).load(commitComments.get(position).getUser().getAvatarUrl()).resize(75, 75).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return commitComments.size();
    }
}
