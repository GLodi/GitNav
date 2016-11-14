/*
 * MIT License
 *
 * Copyright (c) 2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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


public class StarredAdapter extends RecyclerView.Adapter<StarredAdapter.MyViewHolder> {

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

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Use easy fonts to set Typeface
            starred_name.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_language.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_stars.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_repo_date.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }
    }

    public StarredAdapter(List<Repository> starredRepositoryList, Context context) {
        this.starredRepositoryList = starredRepositoryList;
        this.context = context;
    }

    @Override
    public StarredAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.starred_row, parent, false);
        return new StarredAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StarredAdapter.MyViewHolder holder, int position) {
        // Create PrettyTime object
        PrettyTime p = new PrettyTime();

        // Set elements on each row
        final Repository repo = starredRepositoryList.get(position);

        // Set starred repo owner profile pic
        Picasso.with(context).load(repo.getOwner().getAvatarUrl()).resize(150, 150).centerCrop().into(holder.starred_repo_author_icon);

        // Set starred repo name
        holder.starred_name.setText(repo.getName());

        // Set starred repo description
        if (repo.getDescription() != null && !repo.getDescription().equals(""))
            holder.starred_description.setText(repo.getDescription());
        else
            holder.starred_description.setText("No description");

        // Set starred repo language
        if (repo.getLanguage() == null) {
            holder.starred_language.setVisibility(View.GONE);
            holder.starred_language_icon.setVisibility(View.GONE);
        }
        else {
            holder.starred_language.setText(repo.getLanguage());
        }

        // Set starred repo star number
        holder.starred_stars.setText(Integer.toString(repo.getWatchers()));

        // Set starred repo date
        holder.starred_repo_date.setText(p.format(repo.getCreatedAt()));

        // Set repo click listener
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, RepoActivity.class).putExtra("owner", repo.getOwner().getLogin()).putExtra("name", repo.getName()));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });

        // Set user icon click listener. Opens UserActivity
        holder.starred_repo_author_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", repo.getOwner().getLogin()));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return starredRepositoryList.size();
    }

}
