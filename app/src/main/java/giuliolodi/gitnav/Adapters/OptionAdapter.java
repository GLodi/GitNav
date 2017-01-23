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
import giuliolodi.gitnav.AboutActivity;
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

        @BindString(R.string.sign_out) String logout;
        @BindString(R.string.confirm_logout) String confirmLogout;
        @BindString(R.string.currently_logged_in_as) String currentlyLoggedInAs;
        @BindString(R.string.yes) String yes;
        @BindString(R.string.no) String no;
        @BindString(R.string.about) String about;

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
        switch(position) {
            case 0:
                sp = PreferenceManager.getDefaultSharedPreferences(context);
                editor = sp.edit();
                holder.optionName.setText(holder.logout);
                holder.optionDescription.setVisibility(View.VISIBLE);
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
                                .setNegativeButton(holder.no, null)
                                .show();
                    }
                });
                return;
            case 1:
                holder.optionName.setText(holder.about);
                holder.ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startActivity(new Intent(context, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
