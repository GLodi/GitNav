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
import org.eclipse.egit.github.core.event.ForkApplyPayload;
import org.eclipse.egit.github.core.event.ForkPayload;
import org.eclipse.egit.github.core.event.GistPayload;
import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.eclipse.egit.github.core.event.IssuesPayload;
import org.eclipse.egit.github.core.event.MemberPayload;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.event.PullRequestReviewCommentPayload;
import org.eclipse.egit.github.core.event.PushPayload;
import org.eclipse.egit.github.core.event.ReleasePayload;
import org.eclipse.egit.github.core.event.TeamAddPayload;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import giuliolodi.gitnav.GistActivity;
import giuliolodi.gitnav.IssueActivity;
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

    private View.OnClickListener IssueListener (final String owner, final String repo, final String issueNumber) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, IssueActivity.class).putExtra("owner", owner).putExtra("repo", repo).putExtra("issueNumber", issueNumber));
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

                    case "CommitCommentEvent": // #1   NEED TEST
                        CommitCommentPayload commitCommentPayload = (CommitCommentPayload) e.getPayload();
                        h.description.setText(commitCommentPayload.getComment().getBody());
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "CreateEvent": // #2
                        h.description.setText(Html.fromHtml("Created <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "DeleteEvent": // #3 NEED TEST
                        DeletePayload deletePayload = (DeletePayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Deleted <b>" + deletePayload.getRefType() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "DeploymentEvent": // #4 DEFAULT
                        h.description.setText(Html.fromHtml("Deployed in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "DeploymentStatus": // #5 DEFAULT
                        h.description.setText(Html.fromHtml("DeploymentStatus in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "DownloadEvent": // #6 NEED TEST
                        DownloadPayload downloadPayload = (DownloadPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Downloaded <font color='#326fba'>" + downloadPayload.getDownload().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "FollowEvent": // #7   DEPRECATED
                        FollowPayload followPayload = (FollowPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Followed <font color='#326fba'>" + followPayload.getTarget().getLogin() + "</font>"));
                        h.linearLayout.setOnClickListener(UserListener(followPayload.getTarget().getLogin()));
                        break;

                    case "ForkEvent": // #8
                        ForkPayload p = (ForkPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Forked <font color='#326fba'>" + e.getRepo().getName() + "</font> to <font color='#326fba'>" + p.getForkee().getOwner().getLogin() + "/" + p.getForkee().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(p.getForkee().getOwner().getLogin(), p.getForkee().getName()));
                        break;

                    case "ForkApplyEvent": // #9
                        ForkApplyPayload forkApplyPayload = (ForkApplyPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Fork applied <b>" + forkApplyPayload.getHead() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "GistEvent": // #10  DEPRECATED
                        GistPayload gistPayload = (GistPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(gistPayload.getAction().substring(0,1).toUpperCase() + gistPayload.getAction().substring(1) + " <b>" + gistPayload.getGist().getDescription() + "</b>"));
                        h.linearLayout.setOnClickListener(GistListener(gistPayload.getGist().getId()));
                        break;

                    case "GollumEvent": // #11 NEED TEST
                        h.description.setText(Html.fromHtml("Updated wiki in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "IssueCommentEvent": // #12 NEED TEST
                        IssueCommentPayload issueCommentPayload = (IssueCommentPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(issueCommentPayload.getAction().substring(0,1).toUpperCase() + issueCommentPayload.getAction().substring(1) + " issue comment <b>" + issueCommentPayload.getComment().getBody() + "</b> in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(IssueListener(s[0], s[1], String.valueOf(issueCommentPayload.getIssue().getNumber())));
                        break;

                    case "IssuesEvent": // #13 NEED TEST
                        IssuesPayload issuesPayload = (IssuesPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(issuesPayload.getAction().substring(0,1).toUpperCase() + issuesPayload.getAction().substring(1) + " issue <b>#" + String.valueOf(issuesPayload.getIssue().getNumber()) + "</b> in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(IssueListener(s[0], s[1], String.valueOf(issuesPayload.getIssue().getNumber())));
                        break;

                    case "LabelEvent": // #14 DEFAULT
                        h.description.setText(Html.fromHtml("Labeled <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "MemberEvent": // #15 NEED TEST
                        MemberPayload memberPayload = (MemberPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(memberPayload.getAction().substring(0,1).toUpperCase() + memberPayload.getAction().substring(1) + " <b>" + memberPayload.getMember().getLogin() + "</b> to <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "MembershipEvent": // #16 DEFAULT
                        h.description.setText("Added or removed from team");
                        h.linearLayout.setOnClickListener(RepoListener(s[0],s[1]));
                        break;

                    case "MilestoneEvent": // #17 DEFAULT
                        h.description.setText(Html.fromHtml("Performed milestone in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "OrganizationEvent": // #18 DEFAULT
                        h.description.setText(Html.fromHtml("Performed event in org <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "PageBuildEvent": // #19 DEFAULT
                        h.description.setText(Html.fromHtml("Built page in: <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "ProjectCardEvent": // #20 DEFAULT
                        h.description.setText(Html.fromHtml("Performed project card event in: <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "ProjectColumnEvent": // #21 DEFAULT
                        h.description.setText(Html.fromHtml("Performed column card event in: <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "ProjectEvent": // #22 DEFAULT
                        h.description.setText(Html.fromHtml("Performed project event in: <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "PublicEvent": // #23 DEFAULT
                        h.description.setText(Html.fromHtml("Made <font color='#326fba'>" + e.getRepo().getName() + "</font> open source"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "PullRequestEvent": // #24 NEED TEST
                        PullRequestPayload pullRequestPayload = (PullRequestPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(pullRequestPayload.getAction().substring(0,1).toUpperCase() + pullRequestPayload.getAction().substring(1) + " pull request <b>#" + String.valueOf(pullRequestPayload.getNumber()) + "</b> in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "PullRequestReviewEvent": // #25 NEED TEST
                        h.description.setText(Html.fromHtml("Submitted a pull request review in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "PullRequestReviewCommentEvent": // #26 NEED TEST
                        PullRequestReviewCommentPayload prrcp = (PullRequestReviewCommentPayload)e.getPayload();
                        h.description.setText(Html.fromHtml(prrcp.getAction().substring(0,1).toUpperCase() + prrcp.getAction().substring(1) + " pull request review comment <b>#" + String.valueOf(prrcp.getPullRequest().getNumber()) + "</b> in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        break;

                    case "PushEvent": // #27 NEED TEST
                        PushPayload pushPayload = (PushPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Pushed <b>" + pushPayload.getRef() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "ReleaseEvent": // #28 NEED TEST
                        ReleasePayload releasePayload = (ReleasePayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Published <b>" + releasePayload.getRelease().getName() + "</>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "RepositoryEvent": // #29 DEFAULT
                        h.description.setText(Html.fromHtml("Performed a repository event in <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "StatusEvent": // #30 DEFAULT
                        h.description.setText(Html.fromHtml("Changed status of a git commit"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "TeamEvent": // #31 DEFAULT
                        h.description.setText(Html.fromHtml("Performed team event"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "TeamAddEvent": // #32 NEED TEST
                        TeamAddPayload teamAddPayload = (TeamAddPayload)e.getPayload();
                        h.description.setText(Html.fromHtml("Added <b>" + teamAddPayload.getRepo().getName() + "</b> to <b>" + teamAddPayload.getTeam().getName() + "</b>"));
                        h.linearLayout.setOnClickListener(RepoListener(s[0], s[1]));
                        break;

                    case "WatchEvent":  // #33
                        h.description.setText(Html.fromHtml("Starred <font color='#326fba'>" + e.getRepo().getName() + "</font>"));
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
