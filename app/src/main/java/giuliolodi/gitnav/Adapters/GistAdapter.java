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

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Gist;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.GistActivity;
import giuliolodi.gitnav.R;

public class GistAdapter extends RecyclerView.Adapter<GistAdapter.MyViewHolder>{

    private List<Gist> gistList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.gists_row_description) TextView description;
        @BindView(R.id.gists_row_files_n) TextView filesN;
        @BindView(R.id.gists_row_public) TextView isPublic;
        @BindView(R.id.gists_row_date) TextView date;
        @BindView(R.id.gists_row_id) TextView id;
        @BindView(R.id.gists_row_ll) LinearLayout ll;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            filesN.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            isPublic.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            date.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public GistAdapter (List<Gist> gistList) {
        this.gistList = gistList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_gist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Get pretty time object
        PrettyTime p = new PrettyTime();

        final Context context = holder.description.getContext();
        final Gist gist = gistList.get(position);

        holder.description.setText(gist.getDescription());
        holder.isPublic.setText(gist.isPublic() ? "Public" : "Private");
        holder.filesN.setText(String.valueOf(gist.getFiles().size()));
        holder.id.setText(gist.getId());
        holder.date.setText(p.format(gist.getCreatedAt()));

        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, GistActivity.class).putExtra("GistId", gist.getId()));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gistList.size();
    }
}
