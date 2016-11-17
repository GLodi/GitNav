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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import giuliolodi.gitnav.Constants;
import giuliolodi.gitnav.LoginActivity;
import giuliolodi.gitnav.R;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.MyViewHolder> {

    private Context context;
    private Activity activity;
    private SharedPreferences sp;
    public SharedPreferences.Editor editor;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.option_row_ll) LinearLayout ll;
        @BindView(R.id.option_row_name) TextView optionName;
        @BindView(R.id.option_row_description) TextView optionDescription;

        @BindString(R.string.logout) String logout;
        @BindString(R.string.confirm_logout) String confirmLogout;
        @BindString(R.string.currently_logged_in_as) String currentlyLoggedInAs;
        @BindString(R.string.yes) String yes;
        @BindString(R.string.no) String no;

        public MyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            optionName.setTypeface(EasyFonts.robotoRegular(context));
            optionDescription.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public OptionAdapter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public OptionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_option, parent, false);
        return new OptionAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final OptionAdapter.MyViewHolder holder, final int position) {
        // Depending on what option the user select, open contextual menu
        switch(position) {
            case 0:
                sp = PreferenceManager.getDefaultSharedPreferences(context);
                editor = sp.edit();
                holder.optionName.setText(holder.logout);
                holder.optionDescription.setText(holder.currentlyLoggedInAs + " " + Constants.getUsername(context));
                holder.ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(activity)
                                .setTitle(holder.logout)
                                .setMessage(holder.confirmLogout)
                                .setPositiveButton(holder.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Delete all sp info stored
                                        editor.putString(Constants.getTokenKey(context), "");
                                        editor.putString(Constants.getUserKey(context), "");
                                        editor.putBoolean(Constants.getAuthdKey(context), false);
                                        editor.putString(Constants.getEmailKey(context), "");
                                        editor.putString(Constants.getFullNameKey(context), "");
                                        editor.commit();

                                        // Intent to LoginActivity
                                        context.startActivity(new Intent(context, LoginActivity.class));
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
