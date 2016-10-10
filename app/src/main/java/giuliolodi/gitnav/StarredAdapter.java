/*
 * Copyright (c)  2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package giuliolodi.gitnav;

import android.content.Context;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class StarredAdapter extends RecyclerView.Adapter<StarredAdapter.MyViewHolder>{

    private List<Repository> starredRepositoryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // Get reference of repo_row elements
        TextView starred_name, starred_description, starred_language, starred_stars, starred_repo_date;
        CircleImageView starred_repo_author_icon;
        ImageView starred_language_icon;

        public MyViewHolder(View view) {
            super(view);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            starred_name = (TextView) view.findViewById(R.id.starred_repo_name);
            starred_description = (TextView) view.findViewById(R.id.starred_repo_description);
            starred_repo_author_icon = (CircleImageView) view.findViewById(R.id.starred_repo_author_icon);
            starred_language = (TextView) view.findViewById(R.id.starred_repo_language);
            starred_stars = (TextView) view.findViewById(R.id.starred_repo_star_number);
            starred_language_icon = (ImageView) view.findViewById(R.id.starred_code);
            starred_repo_date = (TextView) view.findViewById(R.id.starred_repo_date);

            // Use easy fonts to set Typeface
            starred_name.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_language.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_stars.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            starred_repo_date.setTypeface(EasyFonts.robotoRegular(view.getContext()));


            // Set colors and opacity of text
            starred_name.setTextColor(Color.parseColor("#000000"));
            starred_name.setAlpha(0.87f);
            starred_description.setTextColor(Color.parseColor("#000000"));
            starred_description.setAlpha(0.54f);
            starred_language.setTextColor(Color.parseColor("#000000"));
            starred_language.setAlpha(0.54f);
            starred_stars.setTextColor(Color.parseColor("#000000"));
            starred_stars.setAlpha(0.54f);
            starred_repo_date.setTextColor(Color.parseColor("#000000"));
            starred_repo_date.setAlpha(0.54f);
        }
    }

    public StarredAdapter(List<Repository> starredRepositoryList) {
        this.starredRepositoryList = starredRepositoryList;
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
        Context context = holder.starred_repo_author_icon.getContext();
        Repository repo = starredRepositoryList.get(position);

        // Set starred repo owner profile pic
        Picasso.with(context).load(repo.getOwner().getAvatarUrl()).into(holder.starred_repo_author_icon);

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
    }

    @Override
    public int getItemCount() {
        return starredRepositoryList.size();
    }

}
