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


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.RepositoryContents;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<RepositoryContents> repositoryContentsList;

    public class FileViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_file_name) TextView textView;
        @BindView(R.id.row_file_icon) ImageView imageView;

        public FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            textView.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public FileAdapter (List<RepositoryContents> repositoryContentsList) {
        this.repositoryContentsList = repositoryContentsList;
    }

    @Override
    public FileAdapter.FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_file, parent, false);
        return new FileAdapter.FileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {

        // Set directory/file name
        holder.textView.setText(repositoryContentsList.get(position).getName());

        // Set icon
        holder.imageView.setImageResource(repositoryContentsList.get(position).getType().equals("dir") ? R.drawable.octicons_430_filedirectory_256_0_81a7cb_none : R.drawable.octicons_430_file_256_0_757575_none);

    }

    @Override
    public int getItemCount() {
        return repositoryContentsList.size();
    }
}
