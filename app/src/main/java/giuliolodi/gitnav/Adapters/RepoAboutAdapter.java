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
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.ContributorsActivity;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.UserListActivity;

public class RepoAboutAdapter extends RecyclerView.Adapter<RepoAboutAdapter.MyViewHolder> {

    private Context context;
    private List<String> nameList, numberList;
    private List<Drawable> imageList;
    private String repoName;
    private String ownerName;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_repo_about_text) TextView text;
        @BindView(R.id.row_repo_about_n) TextView number;
        @BindView(R.id.row_repo_about_image) ImageView imageView;
        @BindView(R.id.row_repo_about_rl) RelativeLayout rl;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            text.setTypeface(EasyFonts.robotoRegular(context));
            number.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public RepoAboutAdapter(Context context, List<String> nameList, List<String> numberList, List<Drawable> imageList, String repoName, String ownerName) {
        this.context = context;
        this.nameList = nameList;
        this.numberList = numberList;
        this.imageList = imageList;
        this.repoName = repoName;
        this.ownerName = ownerName;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_repo_about, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.text.setText(nameList.get(position));
        holder.number.setText(numberList.get(position));

        holder.imageView.setImageDrawable(imageList.get(position));

        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (position) {
                    case 0:
                        context.startActivity(new Intent(context, UserListActivity.class).putExtra("repoName", repoName).putExtra("ownerName", ownerName));
                        ((Activity) context).overridePendingTransition(0, 0);
                        return;
                    case 3:
                        context.startActivity(new Intent(context, ContributorsActivity.class).putExtra("repoName", repoName).putExtra("ownerName", ownerName));
                        ((Activity) context).overridePendingTransition(0, 0);
                        return;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

}
