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

import org.eclipse.egit.github.core.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.UserActivity;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> userList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_row_ll) LinearLayout ll;
        @BindView(R.id.user_row_image) CircleImageView image;
        @BindView(R.id.user_row_login) TextView username;
        @BindView(R.id.user_row_fullname) TextView fullname;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            username.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            fullname.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        public LoadingHolder(View view) {
            super(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return userList.get(position) != null ? 1 : 0;
    }

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
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
            // Set username and fullname
            if (userList.get(position).getName() == null) {
                ((MyViewHolder)holder).fullname.setText(userList.get(position).getLogin());
                ((MyViewHolder)holder).username.setVisibility(View.GONE);
            }
            else {
                ((MyViewHolder)holder).fullname.setText(userList.get(position).getName());
                ((MyViewHolder)holder).username.setText(userList.get(position).getLogin());
            }

            // Set picture
            Picasso.with(((MyViewHolder)holder).username.getContext()).load(userList.get(position).getAvatarUrl()).resize(100, 100).centerCrop().into(((MyViewHolder)holder).image);

            // Set listener to invoke UserActivity
            ((MyViewHolder)holder).ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", userList.get(position).getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}
