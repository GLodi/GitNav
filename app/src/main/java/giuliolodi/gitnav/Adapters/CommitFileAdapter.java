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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.CommitFile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class CommitFileAdapter extends RecyclerView.Adapter<CommitFileAdapter.FileAdapter> {

    private List<CommitFile> commitFiles;
    private Context context;

    public class FileAdapter extends RecyclerView.ViewHolder {

        @BindView(R.id.row_commit_file_filename) TextView filename;

        public FileAdapter(View view) {
            super(view);

            ButterKnife.bind(this, view);

            filename.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public CommitFileAdapter(List<CommitFile> commitFiles, Context context) {
        this.commitFiles = commitFiles;
        this.context = context;
    }

    @Override
    public FileAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_commit_file, parent, false);
        return new FileAdapter(itemView);
    }

    @Override
    public void onBindViewHolder(FileAdapter holder, int position) {
        holder.filename.setText(commitFiles.get(position).getFilename().substring(commitFiles.get(position).getFilename().lastIndexOf("/") + 1, commitFiles.get(position).getFilename().length()));
    }

    @Override
    public int getItemCount() {
        return commitFiles.size();
    }
}
