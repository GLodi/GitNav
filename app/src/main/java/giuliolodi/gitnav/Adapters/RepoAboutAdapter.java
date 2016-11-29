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
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class RepoAboutAdapter extends RecyclerView.Adapter<RepoAboutAdapter.MyViewHolder> {

    private Context context;
    private List<String> nameList, numberList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_repo_about_text) TextView text;
        @BindView(R.id.row_repo_about_n) TextView number;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            text.setTypeface(EasyFonts.robotoRegular(context));
            number.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public RepoAboutAdapter(Context context, List<String> nameList, List<String> numberList) {
        this.context = context;
        this.nameList = nameList;
        this.numberList = numberList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_repo_about, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.text.setText(nameList.get(position));
        holder.number.setText(numberList.get(position));
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

}
