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
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.R;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.MyViewHolder> {

    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.option_row_ll) LinearLayout ll;
        @BindView(R.id.option_row_name) TextView optionName;
        @BindView(R.id.option_row_description) TextView optionDescription;

        @BindString(R.string.logout) String logout;
        @BindString(R.string.confirm_logout) String confirmLogout;
        @BindString(R.string.yes) String yes;
        @BindString(R.string.no) String no;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            optionName.setTypeface(EasyFonts.robotoRegular(context));
            optionDescription.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public OptionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public OptionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.option_row, parent, false);
        return new OptionAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final OptionAdapter.MyViewHolder holder, final int position) {
        // Depending on what option the user select, open contextual menu
        switch(position) {
            case 0:
                holder.optionName.setText(holder.logout);
                holder.optionDescription.setVisibility(View.GONE);
                holder.ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context)
                                .setTitle(holder.logout)
                                .setMessage(holder.confirmLogout)
                                .setPositiveButton(holder.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(context, "Bella", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setNegativeButton(holder.no, null).show();
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }



}
