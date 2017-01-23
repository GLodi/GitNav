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
