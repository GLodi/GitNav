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

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.CommitFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class CommitFileAdapter extends RecyclerView.Adapter<CommitFileAdapter.FileAdapter> {

    private List<CommitFile> commitFiles;
    private Context context;

    public class FileAdapter extends RecyclerView.ViewHolder {

        @BindView(R.id.row_commit_file_filename) TextView filename;
        @BindView(R.id.row_commit_file_content) TextView content;
        @BindView(R.id.row_commit_file_changes) TextView changes;

        public FileAdapter(View view) {
            super(view);

            ButterKnife.bind(this, view);

            filename.setTypeface(EasyFonts.robotoRegular(context));
            content.setTypeface(EasyFonts.robotoRegular(context));
            changes.setTypeface(EasyFonts.robotoRegular(context));
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
    public void onBindViewHolder(final FileAdapter holder, int position) {
        holder.filename.setText(commitFiles.get(position).getFilename().substring(commitFiles.get(position).getFilename().lastIndexOf("/") + 1, commitFiles.get(position).getFilename().length()));
        if (commitFiles.get(position).getPatch() != null && !commitFiles.get(position).getPatch().isEmpty()) {
            String raw = commitFiles.get(position).getPatch();
            String cleaned = raw.substring(raw.lastIndexOf("@@") + 3, raw.length());
            final Spannable spannable = new SpannableString(cleaned);
            char[] c = cleaned.toCharArray();
            List<String> backslash = new ArrayList<>();
            List<String> piu = new ArrayList<>();
            List<String> meno = new ArrayList<>();
            for (int i = 0; i < c.length - 1; i++) {
                if (c[i] == '\n') {
                    backslash.add(String.valueOf(i));
                }
                if (c[i] == '\n' && c[i+1] == '+') {
                    piu.add(String.valueOf(i));
                }
                if (c[i] == '\n' && c[i+1] == '-') {
                    meno.add(String.valueOf(i));
                }
            }
            for (int i = 0; i < piu.size(); i++) {
                for (int j = 0; j < backslash.size(); j++) {
                    if (Integer.valueOf(piu.get(i)) < Integer.valueOf(backslash.get(j))) {
                        spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#cff7cf")), Integer.valueOf(piu.get(i)), Integer.valueOf(backslash.get(j)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                }
            }
            for (int i = 0; i < meno.size(); i++) {
                for (int j = 0; j < backslash.size(); j++) {
                    if (Integer.valueOf(meno.get(i)) < Integer.valueOf(backslash.get(j))) {
                        spannable.setSpan(new BackgroundColorSpan(Color.parseColor("#f7cdcd")), Integer.valueOf(meno.get(i)), Integer.valueOf(backslash.get(j)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                }
            }
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.content.getText().equals("..."))
                        holder.content.setText(spannable);
                    else
                        holder.content.setText("...");
                }
            });
        }
        String changed = "+ " + String.valueOf(commitFiles.get(position).getAdditions()) + "   - " + String.valueOf(commitFiles.get(position).getDeletions());
        Spannable changedString = new SpannableString(changed);
        changedString.setSpan(new ForegroundColorSpan(Color.parseColor("#099901")), 0, changed.indexOf("-"), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        changedString.setSpan(new ForegroundColorSpan(Color.parseColor("#c4071a")), changed.indexOf("-"), changedString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        holder.changes.setText(changedString);
    }

    @Override
    public int getItemCount() {
        return commitFiles.size();
    }
}
