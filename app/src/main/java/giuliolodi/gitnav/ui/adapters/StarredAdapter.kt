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
import giuliolodi.gitnav.R
import kotlinx.android.synthetic.main.row_starred.view.*
import org.eclipse.egit.github.core.Repository
import org.ocpsoft.prettytime.PrettyTime
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by giulio on 19/05/2017.
 */
class StarredAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRepoList: MutableList<Repository?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private var mFilter: HashMap<String,String> = HashMap()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()
    private val onRepoClick: PublishSubject<Repository> = PublishSubject.create()

    fun getImageClicks(): Observable<String> {
        return onImageClick
    }

    fun getRepoClicks(): Observable<Repository> {
        return onRepoClick
    }

    class RepoHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (repo: Repository, p: PrettyTime, filter: HashMap<String, String>) = with(itemView) {
            row_starred_repo_name.text = repo.owner.login + "/" + repo.name
            row_starred_star_number.text = repo.watchers.toString()
            Picasso.with(context).load(repo.owner.avatarUrl).resize(100, 100).centerCrop().into(row_starred_author_icon)
            if (repo.description != null && repo.description != "")
                row_starred_repo_description.text = repo.description
            else
                row_starred_repo_description.text = context.getString(R.string.no_description)
            if (repo.language == null) {
                row_starred_language.visibility = View.GONE
                row_starred_code.visibility = View.GONE
            }
            else
                row_starred_language.text = repo.language
            when (filter["sort"]) {
                "updated" -> row_starred_repo_date.text = p.format(repo.updatedAt)
                "pushed" -> row_starred_repo_date.text = p.format(repo.pushedAt)
                null -> row_starred_repo_date.text = p.format(repo.createdAt)
                else -> row_starred_repo_date.text = p.format(repo.createdAt)
            }
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_starred, parent, false))
            root = RepoHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RepoHolder) {
            val repo = mRepoList[position]!!
            holder.bind(repo, mPrettyTime, mFilter)
            holder.itemView.row_starred_author_icon.setOnClickListener { repo.owner?.login?.let { onImageClick.onNext(it) } }
            holder.itemView.row_starred_ll.setOnClickListener { onRepoClick.onNext(repo) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mRepoList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mRepoList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addRepos(repoList: List<Repository>) {
        if (mRepoList.isEmpty()) {
            mRepoList.addAll(repoList)
            notifyDataSetChanged()
        }
        else if (!repoList.isEmpty()) {
            val lastItemIndex = if (mRepoList.size > 0) mRepoList.size else 0
            mRepoList.addAll(repoList)
            notifyItemRangeInserted(lastItemIndex, mRepoList.size)
        }
    }

    fun addRepo(repo: Repository) {
        mRepoList.add(repo)
        notifyItemInserted(mRepoList.size - 1)
    }

    fun showLoading() {
        mRepoList.add(null)
        notifyItemInserted(mRepoList.size - 1)
    }

    fun hideLoading() {
        if (mRepoList.lastIndexOf(null) != -1) {
            val lastNull = mRepoList.lastIndexOf(null)
            mRepoList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
        }
    }

    fun clear() {
        mRepoList.clear()
        notifyDataSetChanged()
    }

    fun setFilter(filter: HashMap<String,String>) {
        mFilter = filter
    }

}