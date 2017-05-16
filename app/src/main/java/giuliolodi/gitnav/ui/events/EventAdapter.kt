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

package giuliolodi.gitnav.ui.events

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.R
import kotlinx.android.synthetic.main.row_event.view.*
import org.eclipse.egit.github.core.event.*
import org.ocpsoft.prettytime.PrettyTime

/**
 * Created by giulio on 16/05/2017.
 */

class EventAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mEventList: MutableList<Event?> = arrayListOf()
    val mPrettyTime: PrettyTime = PrettyTime()

    class EventHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (event: Event, p: PrettyTime) = with (itemView) {
            row_event_name.text = event.actor.login
            row_event_date.text = p.format(event.createdAt)
            Picasso.with(context).load(event.actor.avatarUrl).centerCrop().resize(75,75).into(row_event_image)

            row_event_image.setOnClickListener {  }
            row_event_name.setOnClickListener {  }

            val s = arrayListOf(event.repo.name.split("/"))

            if (event.payload != null) {
                when (event.type) {
                    "CommitCommentEvent" -> { // #1 NEED TEST
                        val commitCommentPayload: CommitCommentPayload = event.payload as CommitCommentPayload
                        row_event_description.text = commitCommentPayload.comment.body
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "CreateEvent" -> { // #2
                        row_event_description.text = Html.fromHtml("Created <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "DeleteEvent" -> { // #3 NEED TEST
                        val deletePayload: DeletePayload = event.payload as DeletePayload
                        row_event_description.text = Html.fromHtml("Deleted <b>" + deletePayload.refType + "</b>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "DeploymentEvent" -> { // #4 DEFAULT
                        row_event_description.text = Html.fromHtml("Deployed in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "DeploymentStatus" -> { // #5 DEFAULT
                        row_event_description.text = Html.fromHtml("DeploymentStatus in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "DownloadEvent" -> { // #6 NEED TEST
                        val downloadPayload: DownloadPayload = event.payload as DownloadPayload
                        row_event_description.text = Html.fromHtml("Downloaded <font color='#326fba'>" + downloadPayload.download.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "FollowEvent" -> { // #7 DEPRECATED
                        val followPayload: FollowPayload = event.payload as FollowPayload
                        row_event_description.text = Html.fromHtml("Followed <font color='#326fba'>" + followPayload.target.login + "</font>")
                        row_event_ll.setOnClickListener {
                            // user
                        }
                    }
                    "ForkEvent" -> { // #8
                        val forkPayload: ForkPayload = event.payload as ForkPayload
                        row_event_description.text = Html.fromHtml("Forked <font color='#326fba'>" + event.repo.name + "</font> to <font color='#326fba'>" + forkPayload.forkee.owner.login + "/" + forkPayload.forkee.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "ForkApplyEvent" -> { // #9
                        val forkApplyPayload: ForkApplyPayload = event.payload as ForkApplyPayload
                        row_event_description.text = Html.fromHtml("Fork applied <b>" + forkApplyPayload.head + "</b>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "GistEvent" -> { // #10
                        val gistPayload: GistPayload = event.payload as GistPayload
                        row_event_description.text = Html.fromHtml(gistPayload.action.substring(0,1).toUpperCase() + gistPayload.action.substring(1) + " <b>" + gistPayload.gist.description + "</b>")
                        row_event_ll.setOnClickListener {
                            // gist
                        }
                    }
                    "GollumEvent" -> { // #11 NEED TEST
                        row_event_description.text = "Updated wiki in <font color='#326fba'>" + event.repo.name + "</font>"
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "IssueCommentEvent" -> { // #12 NEED TEST
                        val issueCommentPayload: IssueCommentPayload = event.payload as IssueCommentPayload
                        row_event_description.text = Html.fromHtml(issueCommentPayload.action.substring(0,1).toUpperCase() + issueCommentPayload.action.substring(1) + " issue comment <b>" + issueCommentPayload.comment.body + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // issue
                        }
                    }
                    "IssuesEvent" -> { // #13 NEED TEST
                        val issuesPayload: IssuesPayload = event.payload as IssuesPayload
                        row_event_description.text = Html.fromHtml(issuesPayload.action.substring(0,1).toUpperCase() + issuesPayload.action.substring(1) + " issue <b>#" + issuesPayload.issue.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // issue
                        }
                    }
                    "LabelEvent" -> { // #14 DEFAULT
                        row_event_description.text = Html.fromHtml("Labeled <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "MemberEvent" -> { // #15 NEED TEST
                        val memberPayload: MemberPayload = event.payload as MemberPayload
                        row_event_description.text = Html.fromHtml(memberPayload.action.substring(0,1).toUpperCase() + memberPayload.action.substring(1) + " <b>" + memberPayload.member.login + "</b> to <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "MembershipEvent" -> { // #16 DEFAULT
                        row_event_description.text = "Added or removed from team"
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "MilestoneEvent" -> { // #17 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed milestone in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "OrganizationEvent" -> { // #18 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed event in org <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "PageBuildEvent" -> { // #19 DEFAULT
                        row_event_description.text = Html.fromHtml("Built page in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "ProjectCardEvent" -> { // #20 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed project card event in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "ProjectColumnEvent" -> { // #21 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed column card event in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "Projectevent" -> { // #22 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed project event in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "PublicEvent" -> { // #23 DEFAULT
                        row_event_description.text = Html.fromHtml("Made <font color='#326fba'>" + event.repo.name + "</font> open source")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "PullRequestEvent" -> { // #24 NEED TEST
                        val pullRequestPayload: PullRequestPayload = event.payload as PullRequestPayload
                        row_event_description.text = Html.fromHtml(pullRequestPayload.action.substring(0,1).toUpperCase() + pullRequestPayload.action.substring(1) + " pull request <b>#" + pullRequestPayload.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "PullRequestReviewEvent" -> { // #25 NEED TEST
                        row_event_description.text = Html.fromHtml("Submitted a pull request review in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "PullRequestReviewCommentEvent" -> { // #26 NEED TEST
                        val pullRequestReviewCommentPayload: PullRequestReviewCommentPayload = event.payload as PullRequestReviewCommentPayload
                        row_event_description.text = Html.fromHtml(pullRequestReviewCommentPayload.action.substring(0,1).toUpperCase() + pullRequestReviewCommentPayload.action.substring(1) + " pull request review comment <b>#" + pullRequestReviewCommentPayload.pullRequest.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                    }
                    "PushEvent" -> { // #27 NEED TEST
                        val pushPayload: PushPayload = event.payload as PushPayload
                        row_event_description.text = Html.fromHtml("Pushed <b>" + pushPayload.ref + "</b>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "ReleaseEvent" -> { // #28 NEED TEST
                        val releasePayload: ReleasePayload = event.payload as ReleasePayload
                        row_event_description.text = Html.fromHtml("Published <b>" + releasePayload.release.name + "</>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "RepositoryEvent" -> { // #29 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed a repository event in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "StatusEvent" -> { // #30 DEFAULT
                        row_event_description.text = Html.fromHtml("Changed status of a git commit")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "TeamEvent" -> { // #31 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed team event")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "TeamAddEvent" -> { // #32 NEED TEST
                        val teamAddPayload: TeamAddPayload = event.payload as TeamAddPayload
                        row_event_description.text = Html.fromHtml("Added <b>" + teamAddPayload.repo.name + "</b> to <b>" + teamAddPayload.team.name + "</b>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }
                    "WatchEvent" -> { // #33
                        row_event_description.text = Html.fromHtml("Starred <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // repo
                        }
                    }

                }
            }
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_event, parent, false))
            root = EventHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EventHolder) {
            val event = mEventList[position]!!
            holder.bind(event, mPrettyTime)
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mEventList.get(position) != null) 1 else 0 }

    override fun getItemCount(): Int { return mEventList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

}
