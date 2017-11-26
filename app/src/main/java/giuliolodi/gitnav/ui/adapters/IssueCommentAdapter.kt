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

package giuliolodi.gitnav.ui.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_comment.view.*
import org.eclipse.egit.github.core.Comment
import org.ocpsoft.prettytime.PrettyTime
import android.text.Html

/**
 * Created by giulio on 17/11/2017.
 */
class IssueCommentAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mIssueCommentList: MutableList<Comment?> = mutableListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()

    fun getImageClicks(): Observable<String> {
        return onImageClick
    }

    class IssueCommentHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (comment: Comment, p: PrettyTime) = with(itemView) {
            row_comment_username.text = comment.user.login
            row_comment_comment.text = Html.fromHtml(comment.bodyHtml)
            row_comment_date.text = p.format(comment.createdAt)
            Picasso.with(context).load(comment.user.avatarUrl).resize(75, 75).centerCrop().into(row_comment_image)
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_comment, parent, false))
            root = IssueCommentHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IssueCommentHolder) {
            val comment = mIssueCommentList[position]!!
            holder.bind(comment, mPrettyTime)
            holder.itemView.row_comment_ll.setOnClickListener { comment.user?.login?.let { onImageClick.onNext(it) } }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mIssueCommentList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mIssueCommentList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addIssueCommentList(issueCommentList: List<Comment>) {
        if (mIssueCommentList.isEmpty()) {
            mIssueCommentList.addAll(issueCommentList)
            notifyDataSetChanged()
        }
        else if (!issueCommentList.isEmpty()) {
            val lastItemIndex = if (mIssueCommentList.size > 0) mIssueCommentList.size else 0
            mIssueCommentList.addAll(issueCommentList)
            notifyItemRangeInserted(lastItemIndex, mIssueCommentList.size)
        }
    }

    fun addIssueComment(issueComment: Comment) {
        mIssueCommentList.add(issueComment)
        notifyItemInserted(mIssueCommentList.size - 1)
    }

    fun addLoading() {
        mIssueCommentList.add(null)
        notifyItemInserted(mIssueCommentList.size - 1)
    }

    fun clear() {
        mIssueCommentList.clear()
        notifyDataSetChanged()
    }

}