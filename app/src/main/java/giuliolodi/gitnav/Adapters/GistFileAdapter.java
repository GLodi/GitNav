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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.GistFile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class GistFileAdapter extends RecyclerView.Adapter<GistFileAdapter.GistFileHolder>{

    private List<GistFile> gistFiles;

    public class GistFileHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_file_name) TextView textView;
        @BindView(R.id.row_file_icon) ImageView imageView;

        public GistFileHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            textView.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public GistFileAdapter (List<GistFile> gistFiles) {
        this.gistFiles = gistFiles;
    }

    @Override
    public GistFileAdapter.GistFileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_file, parent, false);
        return new GistFileAdapter.GistFileHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GistFileAdapter.GistFileHolder holder, int position) {

        // Set directory/file name
        holder.textView.setText(gistFiles.get(position).getFilename());

        // Set icon
        holder.imageView.setImageResource(R.drawable.octicons_430_file_256_0_757575_none);

    }

    @Override
    public int getItemCount() {
        return gistFiles.size();
    }

}
