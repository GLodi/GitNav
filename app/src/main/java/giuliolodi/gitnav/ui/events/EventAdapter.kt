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

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.repository.RepoActivity
import giuliolodi.gitnav.ui.user.UserActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_event.view.*
import org.eclipse.egit.github.core.event.*
import org.ocpsoft.prettytime.PrettyTime

/**
 * Created by giulio on 16/05/2017.
 */
class EventAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mEventList: MutableList<Event?> = mutableListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onUserClick: PublishSubject<String> = PublishSubject.create()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()

    fun getUserClicks(): Observable<String> {
        return onUserClick
    }

    fun getImageClicks(): Observable<String> {
        return onImageClick
    }

    class EventHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (event: Event, p: PrettyTime) = with (itemView) {
            row_event_name.text = event.actor.login
            row_event_date.text = p.format(event.createdAt)
            Picasso.with(context).load(event.actor.avatarUrl).resize(100,100).centerCrop().into(row_event_image)

            row_event_name.setOnClickListener {  }

            val s = arrayListOf(event.repo.name.split("/"))

            if (event.payload != null) {
                when (event.type) {
                    "CommitCommentEvent" -> { // #1
                        val commitCommentPayload: CommitCommentPayload = event.payload as CommitCommentPayload
                        row_event_description.text = Html.fromHtml("Commented <b>" + commitCommentPayload.comment.path.substringAfterLast('/') + "</b> in <font color='#326fba'>" + event.repo.name + "</font>: <b>"+ commitCommentPayload.comment.body + "</b>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "CreateEvent" -> { // #2
                        row_event_description.text = Html.fromHtml("Created <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "DeleteEvent" -> { // #3
                        val deletePayload: DeletePayload = event.payload as DeletePayload
                        row_event_description.text = Html.fromHtml("Deleted " + deletePayload.refType + " <b>" + deletePayload.ref + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)                        }
                    }
                    "DeploymentEvent" -> { // #4 DEFAULT
                        row_event_description.text = Html.fromHtml("Deployed in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)                        }
                    }
                    "DeploymentStatus" -> { // #5 DEFAULT
                        row_event_description.text = Html.fromHtml("DeploymentStatus in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)                        }
                    }
                    "DownloadEvent" -> { // #6 NEED TEST
                        val downloadPayload: DownloadPayload = event.payload as DownloadPayload
                        row_event_description.text = Html.fromHtml("Downloaded <font color='#326fba'>" + downloadPayload.download.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)                        }
                    }
                    "FollowEvent" -> { // #7 DEPRECATED
                        val followPayload: FollowPayload = event.payload as FollowPayload
                        row_event_description.text = Html.fromHtml("Followed <font color='#326fba'>" + followPayload.target.login + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(UserActivity.getIntent(context).putExtra("username", followPayload.target.login))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "ForkEvent" -> { // #8
                        val forkPayload: ForkPayload = event.payload as ForkPayload
                        row_event_description.text = Html.fromHtml("Forked <font color='#326fba'>" + event.repo.name + "</font> to <font color='#326fba'>" + forkPayload.forkee.owner.login + "/" + forkPayload.forkee.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "ForkApplyEvent" -> { // #9
                        val forkApplyPayload: ForkApplyPayload = event.payload as ForkApplyPayload
                        row_event_description.text = Html.fromHtml("Fork applied <b>" + forkApplyPayload.head + "</b>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
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
                        row_event_description.text = Html.fromHtml("Updated wiki in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "IssueCommentEvent" -> { // #12
                        val issueCommentPayload: IssueCommentPayload = event.payload as IssueCommentPayload
                        row_event_description.text = Html.fromHtml(issueCommentPayload.action.substring(0,1).toUpperCase() + issueCommentPayload.action.substring(1) + " comment on issue <b>#" + issueCommentPayload.issue.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>: <b>" + issueCommentPayload.comment.body + "</b>")
                        row_event_ll.setOnClickListener {
                            // issue
                        }
                    }
                    "IssuesEvent" -> { // #13
                        val issuesPayload: IssuesPayload = event.payload as IssuesPayload
                        row_event_description.text = Html.fromHtml(issuesPayload.action.substring(0,1).toUpperCase() + issuesPayload.action.substring(1) + " issue <b>#" + issuesPayload.issue.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            // issue
                        }
                    }
                    "LabelEvent" -> { // #14 DEFAULT
                        row_event_description.text = Html.fromHtml("Labeled <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "MemberEvent" -> { // #15
                        val memberPayload: MemberPayload = event.payload as MemberPayload
                        row_event_description.text = Html.fromHtml(memberPayload.action.substring(0,1).toUpperCase() + memberPayload.action.substring(1) + " <b>" + memberPayload.member.login + "</b> to <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "MembershipEvent" -> { // #16 DEFAULT
                        row_event_description.text = "Added or removed from team"
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "MilestoneEvent" -> { // #17 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed milestone in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "OrganizationEvent" -> { // #18 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed event in org <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "PageBuildEvent" -> { // #19 DEFAULT
                        row_event_description.text = Html.fromHtml("Built page in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "ProjectCardEvent" -> { // #20 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed project card event in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "ProjectColumnEvent" -> { // #21 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed column card event in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "Projectevent" -> { // #22 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed project event in: <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "PublicEvent" -> { // #23 DEFAULT
                        row_event_description.text = Html.fromHtml("Made <font color='#326fba'>" + event.repo.name + "</font> open source")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "PullRequestEvent" -> { // #24
                        val pullRequestPayload: PullRequestPayload = event.payload as PullRequestPayload
                        row_event_description.text = Html.fromHtml(pullRequestPayload.action.substring(0,1).toUpperCase() + pullRequestPayload.action.substring(1) + " pull request <b>#" + pullRequestPayload.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "PullRequestReviewEvent" -> { // #25 NEED TEST
                        row_event_description.text = Html.fromHtml("Submitted a pull request review in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "PullRequestReviewCommentEvent" -> { // #26
                        val pullRequestReviewCommentPayload: PullRequestReviewCommentPayload = event.payload as PullRequestReviewCommentPayload
                        row_event_description.text = Html.fromHtml(pullRequestReviewCommentPayload.action.substring(0,1).toUpperCase() + pullRequestReviewCommentPayload.action.substring(1) + " pull request review comment <b>#" + pullRequestReviewCommentPayload.pullRequest.number.toString() + "</b> in <font color='#326fba'>" + event.repo.name + "</font>: <b>" + pullRequestReviewCommentPayload.comment + "</b>")
                    }
                    "PushEvent" -> { // #27
                        val pushPayload: PushPayload = event.payload as PushPayload
                        if (pushPayload.commits.size == 1)
                            row_event_description.text = Html.fromHtml("Pushed a commit to <b>" + pushPayload.ref + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        else
                            row_event_description.text = Html.fromHtml("Pushed " + pushPayload.commits.size.toString() + " commits to <b>" + pushPayload.ref + "</b> in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "ReleaseEvent" -> { // #28 NEED TEST
                        val releasePayload: ReleasePayload = event.payload as ReleasePayload
                        row_event_description.text = Html.fromHtml("Published <b>" + releasePayload.release.name + "</>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "RepositoryEvent" -> { // #29 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed a repository event in <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "StatusEvent" -> { // #30 DEFAULT
                        row_event_description.text = Html.fromHtml("Changed status of a git commit")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "TeamEvent" -> { // #31 DEFAULT
                        row_event_description.text = Html.fromHtml("Performed team event")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "TeamAddEvent" -> { // #32 NEED TEST
                        val teamAddPayload: TeamAddPayload = event.payload as TeamAddPayload
                        row_event_description.text = Html.fromHtml("Added <b>" + teamAddPayload.repo.name + "</b> to <b>" + teamAddPayload.team.name + "</b>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
                        }
                    }
                    "WatchEvent" -> { // #33
                        row_event_description.text = Html.fromHtml("Starred <font color='#326fba'>" + event.repo.name + "</font>")
                        row_event_ll.setOnClickListener {
                            context.startActivity(RepoActivity.getIntent(context).putExtra("owner", s[0][0]).putExtra("name", s[0][1]))
                            (context as Activity).overridePendingTransition(0,0)
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
            holder.itemView.row_event_image.setOnClickListener { event.actor?.login?.let { onImageClick.onNext(it) } }
            holder.itemView.row_event_name.setOnClickListener { event.actor?.login?.let { onImageClick.onNext(it) } }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mEventList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mEventList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addEvents(eventList: List<Event>) {
        if (!eventList.isEmpty()) {
            val lastItemIndex = if (mEventList.size > 0) mEventList.size else 0
            mEventList.addAll(eventList)
            notifyItemRangeInserted(lastItemIndex, mEventList.size - 1)
        }
        /*
        if (mEventList.isEmpty()) {
            mEventList.clear()
            mEventList.addAll(eventList)
            notifyDataSetChanged()
        }
        else if (mEventList.lastIndexOf(null) != -1) {
            val lastNull = mEventList.lastIndexOf(null)
            mEventList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mEventList.addAll(eventList)
            notifyItemRangeInserted(lastNull, eventList.size - 1)
        }
        else {
            val lastItemIndex = mEventList.size - 1
            mEventList.addAll(eventList)
            notifyItemRangeInserted(lastItemIndex, mEventList.size - 1)
        }
        */
    }

    fun showLoading() {
        mEventList.add(null)
        notifyItemInserted(mEventList.size - 1)
    }

    fun hideLoading() {
        val lastNull = mEventList.lastIndexOf(null)
        if (lastNull != -1) {
            mEventList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
        }
    }

    fun clear() {
        mEventList.clear()
        notifyDataSetChanged()
    }

}
