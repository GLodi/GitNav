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

package giuliolodi.gitnav;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.GistFileAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GistFiles {

    @BindView(R.id.gist_files_progress_bar) ProgressBar progressBar;
    @BindView(R.id.gist_files_filelist_rv) RecyclerView recyclerView;
    @BindView(R.id.gist_files_nested) NestedScrollView nested;
    @BindView(R.id.gist_files_username) TextView username;
    @BindView(R.id.gist_files_title) TextView title;
    @BindView(R.id.gist_files_sha) TextView sha;
    @BindView(R.id.gist_files_status) TextView status;
    @BindView(R.id.gist_files_date) TextView date;
    @BindView(R.id.gist_files_image) CircleImageView imageView;
    @BindView(R.id.gist_files_date_icon) ImageView dateIcon;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.gist_starred) String gist_starred;
    @BindString(R.string.gist_unstarred) String gist_unstarred;
    @BindString(R.string.publics) String publics;
    @BindString(R.string.privates) String privates;
    
    private Context context;

    private Gist gist;
    private List<GistFile> gistFiles;
    private GistFileAdapter gistFileAdapter;
    private LinearLayoutManager linearLayoutManager;
    private PrettyTime p = new PrettyTime();

    public void populate(final Context context, View v, Gist gist){
        this.context = context;
        this.gist = gist;

        ButterKnife.bind(this, v);
        
        username.setTypeface(EasyFonts.robotoRegular(context));
        title.setTypeface(EasyFonts.robotoRegular(context));
        sha.setTypeface(EasyFonts.robotoRegular(context));
        status.setTypeface(EasyFonts.robotoRegular(context));
        date.setTypeface(EasyFonts.robotoRegular(context));

        progressBar.setVisibility(View.GONE);
        nested.setVisibility(View.VISIBLE);

        gistFiles = new ArrayList<>(gist.getFiles().values());
        gistFileAdapter = new GistFileAdapter(gistFiles);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(gistFileAdapter);
        gistFileAdapter.notifyDataSetChanged();

        username.setText(gist.getOwner().getLogin());
        title.setText(gist.getDescription());
        Picasso.with(context).load(gist.getOwner().getAvatarUrl()).centerCrop().resize(75, 75).into(imageView);
        sha.setText(gist.getId());
        status.setText(gist.isPublic() ? publics : privates);
        dateIcon.setVisibility(View.VISIBLE);
        date.setText(p.format(gist.getCreatedAt()));

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (Constants.isNetworkAvailable(context)) {
                    context.startActivity(new Intent(context, FileViewerActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("mode", "gistfile")
                            .putExtra("filenameGist", gistFiles.get(position).getFilename())
                            .putExtra("urlGist", gistFiles.get(position).getRawUrl())
                            .putExtra("contentGist", gistFiles.get(position).getContent()));
                    ((Activity)context).overridePendingTransition(0, 0);
                } else
                    Toasty.warning(context, network_error, Toast.LENGTH_LONG).show();
            }
        });
    }

}
