/*
 * Copyright (c)  2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package giuliolodi.navforgithub;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Repository;

import java.util.List;

public class RepoAdapter extends RecyclerView.Adapter<RepoAdapter.MyViewHolder> {

    private List<Repository> repositoryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // Get reference of repo_row elements
        TextView repo_list_name, repo_list_description, repo_list_language;

        public MyViewHolder(View view) {
            super(view);
            repo_list_name = (TextView) view.findViewById(R.id.repo_list_name);
            repo_list_description = (TextView) view.findViewById(R.id.repo_list_description);
            repo_list_language = (TextView) view.findViewById(R.id.repo_list_language);

            // Use easy fonts to set Typeface
            repo_list_name.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            repo_list_description.setTypeface(EasyFonts.robotoRegular(view.getContext()));

            // Set colors and opacity of text
            repo_list_name.setTextColor(Color.parseColor("#000000"));
            repo_list_name.setAlpha(0.87f);
            repo_list_description.setTextColor(Color.parseColor("#000000"));
            repo_list_description.setAlpha(0.54f);
            repo_list_language.setTextColor(Color.parseColor("#000000"));
            repo_list_language.setAlpha(0.54f);
        }
    }

    public RepoAdapter(List<Repository> repositoryList) {
        this.repositoryList = repositoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.repo_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Set elements on each row
        Repository repo = repositoryList.get(position);
        holder.repo_list_name.setText(repo.getName());
        if (repo.getDescription() != null && !repo.getDescription().equals(""))
            holder.repo_list_description.setText(repo.getDescription());
        else
            holder.repo_list_description.setText("No description");
        if (repo.getLanguage() == null)
            holder.repo_list_language.setVisibility(View.GONE);
        else
            holder.repo_list_language.setText(repo.getLanguage());

    }

    @Override
    public int getItemCount() {
        return repositoryList.size();
    }

}
