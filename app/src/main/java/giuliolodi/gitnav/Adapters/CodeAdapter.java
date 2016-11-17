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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.CodeSearchResult;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class CodeAdapter extends RecyclerView.Adapter<CodeAdapter.MyViewHolder>{

    private Context context;
    private List<CodeSearchResult> searchResultList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.code_row_ll) LinearLayout ll;
        @BindView(R.id.code_row_name) TextView name;
        @BindView(R.id.code_row_path) TextView path;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            name.setTypeface(EasyFonts.robotoRegular(view.getContext()));
            path.setTypeface(EasyFonts.robotoRegular(view.getContext()));
        }

    }

    public CodeAdapter(List<CodeSearchResult> searchResultList, Context context) {
        this.searchResultList = searchResultList;
        this.context = context;
    }

    @Override
    public CodeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_code, parent, false);
        return new CodeAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CodeAdapter.MyViewHolder holder, final int position) {
        holder.name.setText(searchResultList.get(position).getName());
        holder.path.setText(searchResultList.get(position).getRepository().getOwner().getLogin() + "/" + searchResultList.get(position).getRepository().getName());
    }

    @Override
    public int getItemCount() {
        return searchResultList.size();
    }

}
