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

package giuliolodi.gitnav.ui.issuelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_issue.view.*
import org.eclipse.egit.github.core.Issue
import org.ocpsoft.prettytime.PrettyTime
import com.squareup.picasso.Picasso

/**
 * Created by giulio on 01/09/2017.
 */
class IssueAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private var mIssueList: MutableList<Issue?> = mutableListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onIssueClick: PublishSubject<Long> = PublishSubject.create()
    private val onUserClick: PublishSubject<String> = PublishSubject.create()

    fun getIssueClick(): Observable<Long> {
        return onIssueClick
    }

    fun getUserClick(): Observable<String> {
        return onUserClick
    }
    
    class IssueHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind(issue: Issue, prettyTime: PrettyTime) = with(itemView) {
            row_issue_username.text = issue.user.login
            row_issue_issuename.text = issue.title
            row_issue_n.text = "#" + issue.number.toString()
            row_issue_date.text = prettyTime.format(issue.createdAt)
            if (issue.comments != 0) {
                row_issue_comment_n.text = issue.comments.toString()
            } else {
                row_issue_comment_n.visibility = View.GONE
                row_issue_forum_icon.visibility = View.GONE
            }
            Picasso.with(context).load(issue.user.avatarUrl).resize(75, 75).centerCrop().into(row_issue_image)
        }
    }
    
    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_issue, parent, false))
            root = IssueHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is IssueHolder) {
            val issue = mIssueList[position]!!
            holder.bind(issue, mPrettyTime)
            holder.itemView.row_issue_image.setOnClickListener { onUserClick.onNext(issue.user.login) }
            holder.itemView.row_issue_ll.setOnClickListener { onIssueClick.onNext(issue.id) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mIssueList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mIssueList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addIssueList(issueList: List<Issue>) {
        if (mIssueList.isEmpty()) {
            mIssueList.clear()
            mIssueList.addAll(issueList)
            notifyDataSetChanged()
        }
        else {
            val lastNull = mIssueList.lastIndexOf(null)
            mIssueList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mIssueList.addAll(issueList)
            notifyItemRangeInserted(lastNull, mIssueList.size - 1)
        }
    }

    fun addIssue(issue: Issue) {
        mIssueList.add(issue)
        notifyItemInserted(mIssueList.size - 1)
    }

    fun addLoading() {
        mIssueList.add(null)
        notifyItemInserted(mIssueList.size - 1)
    }

    fun hideLoading() {
        if (mIssueList.lastIndexOf(null) != -1) {
            val lastNull = mIssueList.lastIndexOf(null)
            mIssueList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
        }
    }

    fun clear() {
        mIssueList.clear()
        notifyDataSetChanged()
    }
    
}