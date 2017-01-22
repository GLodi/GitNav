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
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Comment;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.UserActivity;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Comment> commentList;
    private Context context;
    private PrettyTime p = new PrettyTime();

    public class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_comment_username) TextView username;
        @BindView(R.id.row_comment_comment) TextView comment;
        @BindView(R.id.row_comment_date) TextView date;
        @BindView(R.id.row_comment_image) CircleImageView imageView;

        public CommentHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            username.setTypeface(EasyFonts.robotoRegular(context));
            comment.setTypeface(EasyFonts.robotoRegular(context));
            date.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingHolder(View view) {
            super(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return commentList.get(position) != null ? 1 : 0;
    }

    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment, parent, false);
            vh = new CommentHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new LoadingHolder(itemView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof CommentHolder) {
            ((CommentHolder)holder).username.setText(commentList.get(position).getUser().getLogin());
            ((CommentHolder)holder).comment.setText(commentList.get(position).getBody());
            ((CommentHolder)holder).date.setText(p.format(commentList.get(position).getCreatedAt()));
            Picasso.with(context).load(commentList.get(position).getUser().getAvatarUrl()).resize(75, 75).centerCrop().into(((CommentHolder)holder).imageView);
            ((CommentHolder)holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", commentList.get(position).getUser().getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
