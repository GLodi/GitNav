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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Gist;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.GistActivity;
import giuliolodi.gitnav.R;

public class GistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Gist> gistList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.gists_row_description) TextView description;
        @BindView(R.id.gists_row_files_n) TextView filesN;
        @BindView(R.id.gists_row_public) TextView isPublic;
        @BindView(R.id.gists_row_date) TextView date;
        @BindView(R.id.gists_row_id) TextView id;
        @BindView(R.id.gists_row_ll) LinearLayout ll;

        @BindString(R.string.publics) String publics;
        @BindString(R.string.privates) String privates;

        private PrettyTime p;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            p = new PrettyTime();

            description.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            filesN.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            isPublic.setTypeface(EasyFonts.robotoRegular(view.getContext()));
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
        return gistList.get(position) != null ? 1 : 0;
    }

    public GistAdapter (List<Gist> gistList) {
        this.gistList = gistList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gist, parent, false);
            vh = new MyViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new LoadingHolder(itemView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {

            final Context context = ((MyViewHolder)holder).description.getContext();
            final Gist gist = gistList.get(position);

            ((MyViewHolder)holder).description.setText(gist.getDescription());
            ((MyViewHolder)holder).isPublic.setText(gist.isPublic() ? ((MyViewHolder)holder).publics : ((MyViewHolder)holder).privates);
            ((MyViewHolder)holder).filesN.setText(String.valueOf(gist.getFiles().size()));
            ((MyViewHolder)holder).id.setText(gist.getId());
            ((MyViewHolder)holder).date.setText(((MyViewHolder)holder).p.format(gist.getCreatedAt()));

            ((MyViewHolder)holder).ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, GistActivity.class).putExtra("GistId", gist.getId()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return gistList.size();
    }
}
