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
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.event.CommitCommentPayload;
import org.eclipse.egit.github.core.event.DeletePayload;
import org.eclipse.egit.github.core.event.DownloadPayload;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.FollowPayload;
import org.eclipse.egit.github.core.event.ForkPayload;
import org.eclipse.egit.github.core.event.GistPayload;
import org.eclipse.egit.github.core.event.GollumPayload;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.GistActivity;
import giuliolodi.gitnav.R;
import giuliolodi.gitnav.RepoActivity;
import giuliolodi.gitnav.UserActivity;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Event> eventList;
    private PrettyTime p = new PrettyTime();

    public class EventHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row_event_ll) LinearLayout linearLayout;
        @BindView(R.id.row_event_name) TextView name;
        @BindView(R.id.row_event_description) TextView description;
        @BindView(R.id.row_event_image) CircleImageView imageView;
        @BindView(R.id.row_event_date) TextView date;

        public EventHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            name.setTypeface(EasyFonts.robotoRegular(context));
            description.setTypeface(EasyFonts.robotoRegular(context));
        }

    }

    public class EventLoadHolder extends RecyclerView.ViewHolder {

        public EventLoadHolder(View view) {
            super(view);
        }

    }

    @Override
    public int getItemViewType(final int position) {
        return eventList.get(position) != null ? 1 : 0;
    }

    public EventAdapter(Context context, final List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_event, parent, false);
            vh = new EventHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new EventLoadHolder(itemView);
        }
        return vh;
    }

    private View.OnClickListener RepoListener (final String owner, final String repo) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, RepoActivity.class).putExtra("owner", owner).putExtra("name", repo));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        };
    }

    private View.OnClickListener UserListener (final String user) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", user));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        };
    }

    private View.OnClickListener GistListener (final String gistId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, GistActivity.class).putExtra("GistId", gistId));
                ((Activity) context).overridePendingTransition(0, 0);
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof EventHolder) {

            final EventHolder h = ((EventHolder)holder);
            final Event e = eventList.get(position);
            h.name.setText(e.getActor().getLogin());
            Picasso.with(context).load(e.getActor().getAvatarUrl()).centerCrop().resize(75,75).into(h.imageView);
            h.date.setText(p.format(e.getCreatedAt()));
            h.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", e.getActor().getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });
            h.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, UserActivity.class).putExtra("userS", e.getActor().getLogin()));
                    ((Activity) context).overridePendingTransition(0, 0);
                }
            });

            String[] s = e.getRepo().getName().split("/");

            if (e.getPayload() != null) {

                switch (eventList.get(position).getType()) {

                    case "CommitCommentEvent":  // #1   NEED TEST
                        CommitCommentPayload commitCommentPayload = (CommitCommentPayload) e.getPayload();
                        h.description.setText(commitCommentPayload.getComment().getBody());
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "CreateEvent": // #2
                        h.description.setText(Html.fromHtml("Created repository <b>" + e.getRepo().getName() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "DeleteEvent": // #3   NEED TEST
                        DeletePayload deletePayload = (DeletePayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Deleted <b>" + deletePayload.getRefType() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    // #4
                    // #5

                    case "DownloadEvent":   // #6 NEED TEST
                        DownloadPayload downloadPayload = (DownloadPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Downloaded <b>" + downloadPayload.getDownload().getName() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "FollowEvent": // #7   DEPRECATED
                        FollowPayload followPayload = (FollowPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Followed <b>" + followPayload.getTarget().getLogin() + "</b>"));
                        h.linearLayout.setOnClickListener(UserListener(followPayload.getTarget().getLogin()));
                        break;

                    case "ForkEvent":   // #8
                        ForkPayload p = (ForkPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Forked <b>" + e.getRepo().getName() + "</b> to <b>" + p.getForkee().getOwner().getLogin() + "/" + p.getForkee().getName() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(p.getForkee().getOwner().getLogin(), p.getForkee().getName()));
                        break;

                    // #9

                    case "GistEvent":   // #10  DEPRECATED
                        GistPayload gistPayload = (GistPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(gistPayload.getAction() + "<b>" + gistPayload.getGist().getDescription() + "</b>"));
                        h.linearLayout.setOnClickListener(GistListener(gistPayload.getGist().getId()));
                        break;

                    case "GollumEvent": // #11  NEED TEST
                        GollumPayload gollumPayload = (GollumPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Updated wiki in <b>" + e.getRepo().getName() + "</b>"));
                        break;

                    case "WatchEvent":
                        h.description.setText(Html.fromHtml("Starred <b>" + e.getRepo().getName() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                }

            }

        }

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
