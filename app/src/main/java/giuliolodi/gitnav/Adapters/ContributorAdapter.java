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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Contributor;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.UserActivity;

public class ContributorAdapter extends RecyclerView.Adapter<ContributorAdapter.MyViewHolder> {

    private List<Contributor> contributorList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_contributor_rl) RelativeLayout relativeLayout;
        @BindView(R.id.row_contributor_login) TextView loginView;
        @BindView(R.id.row_contributor_image) CircleImageView imageView;
        @BindView(R.id.row_contributor_contributions) TextView contributionsView;
        @BindString(R.string.contributions) String contributions;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            loginView.setTypeface(EasyFonts.robotoRegular(context));
            contributionsView.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public ContributorAdapter (List<Contributor> contributorList, Context context) {
        this.contributorList = contributorList;
        this.context = context;
    }

    @Override
    public ContributorAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_contributor, parent, false);
        return new ContributorAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (contributorList.get(position).getLogin() != null && !contributorList.get(position).getLogin().isEmpty())
            holder.loginView.setText(contributorList.get(position).getLogin());
        else
            holder.loginView.setText(contributorList.get(position).getName());
        holder.contributionsView.setText(holder.contributions + " " + contributorList.get(position).getContributions());

        Picasso.with(holder.loginView.getContext()).load(contributorList.get(position).getAvatarUrl()).resize(100, 100).centerCrop().into(holder.imageView);

        if (contributorList.get(position).getLogin() != null && !contributorList.get(position).getLogin().isEmpty()) {
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", contributorList.get(position).getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return contributorList.size();
    }

}
