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

package giuliolodi.gitnav.ui.repositorylist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_repo.view.*
import org.eclipse.egit.github.core.Repository
import org.ocpsoft.prettytime.PrettyTime

/**
 * Created by giulio on 18/05/2017.
 */
class RepoListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRepoList: MutableList<Repository?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private var mFilter: HashMap<String,String> = HashMap()
    private val onRepoClick: PublishSubject<Repository> = PublishSubject.create()

    fun getPositionClicks(): Observable<Repository> {
        return onRepoClick
    }

    class RepoHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (repo: Repository, p: PrettyTime, filter: HashMap<String, String>) = with(itemView) {
            row_repo_name.text = repo.owner.login + "/" + repo.name
            if (repo.description != null && repo.description != "")
                row_repo_description.text = repo.description
            else
                row_repo_description.text = context.getString(R.string.no_description)
            if (repo.language == null) {
                row_repo_language.visibility = View.GONE
                row_repo_language_icon.visibility = View.GONE
            }
            else
                row_repo_language.text = repo.language
            row_repo_star_number.text = repo.watchers.toString()
            if (repo.isFork && repo.parent != null)
                row_repo_forked.text = repo.parent.name
            else
                row_repo_forked.visibility = View.GONE
            when (filter["sort"]) {
                "pushed" -> row_repo_date.text = p.format(repo.pushedAt)
                "updated" -> row_repo_date.text = p.format(repo.updatedAt)
                null -> row_repo_date.text = p.format(repo.createdAt)
                else -> row_repo_date.text = p.format(repo.createdAt)
            }
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_repo, parent, false))
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
            holder.itemView.setOnClickListener { onRepoClick.onNext(repo) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mRepoList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mRepoList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addRepos(repoList: List<Repository>) {
        if (!repoList.isEmpty()) {
            val lastItemIndex = if (mRepoList.size > 0) mRepoList.size else 0
            mRepoList.addAll(repoList)
            notifyItemRangeInserted(lastItemIndex, mRepoList.size - 1)
        }
    }

    fun showLoading() {
        mRepoList.add(null)
        notifyItemInserted(mRepoList.size - 1)
    }

    fun hideLoading() {
        val lastNull = mRepoList.lastIndexOf(null)
        if (lastNull != -1) {
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