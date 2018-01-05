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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_comment.view.*
import kotlinx.android.synthetic.main.row_commit.view.*
import org.eclipse.egit.github.core.CommitComment
import org.ocpsoft.prettytime.PrettyTime
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter

/**
 * Created by giulio on 05/01/2018.
 */
class CommitCommentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mCommitCommentList: MutableList<CommitComment?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()

    fun getImageClicks(): Observable<String> {
        return onImageClick
    }

    class CommitCommitCommentHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (commitComment: CommitComment, p: PrettyTime) = with(itemView) {
            row_comment_username.text = commitComment.user.login
            row_comment_date.text = p.format(commitComment.createdAt)
            Picasso.with(context).load(commitComment.user.avatarUrl).resize(75, 75).centerCrop().into(row_comment_image)
            row_comment_comment.setHtml(commitComment.bodyHtml, HtmlHttpImageGetter(row_comment_comment))
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_comment, parent, false))
            root = CommitCommitCommentHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommitCommitCommentHolder) {
            val commitComment = mCommitCommentList[position]!!
            holder.bind(commitComment, mPrettyTime)
            holder.itemView.row_comment_ll.setOnClickListener { commitComment.user?.login?.let { onImageClick.onNext(it) } }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mCommitCommentList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mCommitCommentList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addCommitCommentList(commitCommentList: List<CommitComment>) {
        if (mCommitCommentList.isEmpty()) {
            mCommitCommentList.addAll(commitCommentList)
            notifyDataSetChanged()
        }
        else if (!commitCommentList.isEmpty()) {
            val lastItemIndex = if (mCommitCommentList.size > 0) mCommitCommentList.size else 0
            mCommitCommentList.addAll(commitCommentList)
            notifyItemRangeInserted(lastItemIndex, mCommitCommentList.size)
        }
    }

    fun addCommitComment(commitComment: CommitComment) {
        mCommitCommentList.add(commitComment)
        notifyItemInserted(mCommitCommentList.size - 1)
    }

    fun addLoading() {
        mCommitCommentList.add(null)
        notifyItemInserted(mCommitCommentList.size - 1)
    }

    fun clear() {
        mCommitCommentList.clear()
        notifyDataSetChanged()
    }

}