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
import kotlinx.android.synthetic.main.row_commit.view.*
import org.eclipse.egit.github.core.RepositoryCommit
import org.ocpsoft.prettytime.PrettyTime

/**
 * Created by giulio on 11/07/2017.
 */
class RepoCommitAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRepoCommitList: MutableList<RepositoryCommit?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()
    private val onCommitClick: PublishSubject<RepositoryCommit> = PublishSubject.create()

    fun getImageClicks(): Observable<String> {
        return onImageClick
    }

    fun getCommitClicks(): Observable<RepositoryCommit> {
        return onCommitClick
    }
    
    class RepoCommitHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (repoCommit: RepositoryCommit, p: PrettyTime) = with(itemView) {
            var description: String = repoCommit.commit.message
            val pos = description.indexOf('\n')
            if (pos > 0) { description = description.substring(0,pos) }

            val name: String
            if (repoCommit.author != null && repoCommit.author.login != null)
                name = repoCommit.author.login
            else
                name = repoCommit.commit.author.name

            row_commit_author.text = name
            row_commit_description.text = description
            row_commit_sha.text = repoCommit.sha.toString().substring(0,12)
            row_commit_date.text = p.format(repoCommit.commit.author.date)
            if (repoCommit.commit.commentCount == 0) {
                row_commit_commenticon.visibility = View.GONE
                row_commit_commentN.visibility = View.GONE
            }
            else {
                row_commit_commentN.text = repoCommit.commit.commentCount.toString()
            }
            repoCommit.author?.let { Picasso.with(context).load(it.avatarUrl).resize(100,100).centerCrop().into(row_commit_image) }
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_commit, parent, false))
            root = RepoCommitHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RepoCommitHolder) {
            val repoCommit = mRepoCommitList[position]!!
            holder.bind(repoCommit, mPrettyTime)
            if (repoCommit.author != null){
                holder.itemView.row_commit_image.setOnClickListener { onImageClick.onNext(repoCommit.author.login) }
                holder.itemView.row_commit_ll.setOnClickListener { onCommitClick.onNext(repoCommit) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mRepoCommitList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mRepoCommitList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addRepoCommits(repoCommitList: List<RepositoryCommit>) {
        if (mRepoCommitList.isEmpty()) {
            mRepoCommitList.addAll(repoCommitList)
            notifyDataSetChanged()
        }
        else if (!repoCommitList.isEmpty()) {
            val lastItemIndex = if (mRepoCommitList.size > 0) mRepoCommitList.size else 0
            mRepoCommitList.addAll(repoCommitList)
            notifyItemRangeInserted(lastItemIndex, mRepoCommitList.size)
        }
    }

    fun addRepoCommit(repoCommit: RepositoryCommit) {
        mRepoCommitList.add(repoCommit)
        notifyItemInserted(mRepoCommitList.size - 1)
    }

    fun addLoading() {
        mRepoCommitList.add(null)
        notifyItemInserted(mRepoCommitList.size - 1)
    }

    fun hideLoading() {
        if (mRepoCommitList.lastIndexOf(null) != -1) {
            val lastNull = mRepoCommitList.lastIndexOf(null)
            mRepoCommitList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
        }
    }

    fun clear() {
        mRepoCommitList.clear()
        notifyDataSetChanged()
    }
}