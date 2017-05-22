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

package giuliolodi.gitnav.ui.starred

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import kotlinx.android.synthetic.main.row_starred.view.*
import org.eclipse.egit.github.core.Repository
import org.ocpsoft.prettytime.PrettyTime
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.ui.user.UserActivity
import android.support.v4.content.ContextCompat.startActivity



/**
 * Created by giulio on 19/05/2017.
 */

class StarredAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRepoList: MutableList<Repository?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()

    class RepoHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (repo: Repository, p: PrettyTime) = with(itemView) {
            row_starred_repo_name.text = repo.owner.login + " / " + repo.name
            row_starred_star_number.text = repo.watchers.toString()
            row_starred_repo_date.text = p.format(repo.createdAt)
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
            row_starred_author_icon.setOnClickListener {
                context.startActivity(Intent(context, UserActivity::class.java).putExtra("username", repo.owner.login))
                (context as Activity).overridePendingTransition(0, 0)
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
            holder.bind(repo, mPrettyTime)
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mRepoList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mRepoList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addRepos(repoList: List<Repository>) {
        if (mRepoList.isEmpty()) {
            mRepoList.clear()
            mRepoList.addAll(repoList.toMutableList())
            notifyDataSetChanged()
        }
        else {
            val lastNull = mRepoList.lastIndexOf(null)
            mRepoList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mRepoList.addAll(repoList)
            notifyItemRangeInserted(lastNull, mRepoList.size - 1)
        }
    }

    fun addRepo(repo: Repository) {
        mRepoList.add(repo)
        notifyItemInserted(mRepoList.size - 1)
    }

    fun addLoading() {
        mRepoList.add(null)
        notifyItemInserted(mRepoList.size - 1)
    }

    fun clear() {
        mRepoList.clear()
        notifyDataSetChanged()
    }

}