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


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Gist;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class GistAdapter extends RecyclerView.Adapter<GistAdapter.MyViewHolder>{

    private List<Gist> gistList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.gists_row_description) TextView description;
        @BindView(R.id.gists_row_files_n) TextView filesN;
        @BindView(R.id.gists_row_public) TextView isPublic;
        @BindView(R.id.gists_row_date) TextView date;
        @BindView(R.id.gists_row_id) TextView id;

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
                .inflate(R.layout.gists_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Get pretty time object
        PrettyTime p = new PrettyTime();

        Gist gist = gistList.get(position);

        holder.description.setText(gist.getDescription());
        holder.isPublic.setText(gist.isPublic() ? "Public" : "Private");
        holder.filesN.setText(String.valueOf(gist.getFiles().size()));
        holder.id.setText(gist.getId());
        holder.date.setText(p.format(gist.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return gistList.size();
    }
}
