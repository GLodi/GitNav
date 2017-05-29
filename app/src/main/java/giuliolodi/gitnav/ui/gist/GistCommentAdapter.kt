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

package giuliolodi.gitnav.ui.gist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import kotlinx.android.synthetic.main.row_comment.view.*
import org.eclipse.egit.github.core.Comment
import org.ocpsoft.prettytime.PrettyTime
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by giulio on 28/05/2017.
 */

class GistCommentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mGistCommentList: MutableList<Comment?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()

    fun getImageClicks(): Observable<String> {
        return onImageClick
    }

    class GistCommentHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (comment: Comment, p: PrettyTime) = with(itemView) {
            row_comment_username.text = comment.user.login
            row_comment_comment.text = comment.body
            row_comment_date.text = p.format(comment.createdAt)
            Picasso.with(context).load(comment.user.avatarUrl).resize(75, 75).centerCrop().into(row_comment_image)

        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_comment, parent, false))
            root = GistCommentHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GistCommentHolder) {
            val comment = mGistCommentList[position]!!
            holder.bind(comment, mPrettyTime)
            holder.itemView.row_comment_image.setOnClickListener { onImageClick.onNext(mGistCommentList[position]?.user?.login) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mGistCommentList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mGistCommentList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addGistCommentList(gistCommentList: List<Comment>) {
        if (mGistCommentList.isEmpty()) {
            mGistCommentList.clear()
            mGistCommentList.addAll(gistCommentList.toMutableList())
            notifyDataSetChanged()
        }
        else {
            val lastNull = mGistCommentList.lastIndexOf(null)
            mGistCommentList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mGistCommentList.addAll(gistCommentList)
            notifyItemRangeInserted(lastNull, mGistCommentList.size - 1)
        }
    }

    fun addGistComment(gistComment: Comment) {
        mGistCommentList.add(gistComment)
        notifyItemInserted(mGistCommentList.size - 1)
    }

    fun addLoading() {
        mGistCommentList.add(null)
        notifyItemInserted(mGistCommentList.size - 1)
    }

    fun clear() {
        mGistCommentList.clear()
        notifyDataSetChanged()
    }

}