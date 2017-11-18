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
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_file.view.*
import org.eclipse.egit.github.core.RepositoryContents

/**
 * Created by giulio on 18/07/2017.
 */
class FileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mRepoContentList: MutableList<RepositoryContents?> = mutableListOf()
    private val onClickSubject: PublishSubject<RepositoryContents> = PublishSubject.create()

    fun getRepositoryContentsClicks(): Observable<RepositoryContents> {
        return onClickSubject
    }

    class FileHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind(repoContent: RepositoryContents) = with(itemView) {
            row_file_name.text = repoContent.name
            if (repoContent.type == "dir")
                row_file_icon.setImageResource(R.drawable.octicons_430_filedirectory_256_0_81a7cb_none)
            else
                row_file_icon.setImageResource(R.drawable.octicons_430_file_256_0_757575_none)
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_file, parent, false))
            root = FileHolder(view)
        } else {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FileHolder) {
            val repoContent = mRepoContentList[position]!!
            holder.bind(repoContent)
            holder.itemView.setOnClickListener { onClickSubject.onNext(repoContent) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mRepoContentList[position] != null) 1 else 0
    }

    override fun getItemCount(): Int {
        return mRepoContentList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun addRepositoryContentList(repoContentList: List<RepositoryContents>) {
        if (!repoContentList.isEmpty()) {
            val lastItemIndex = if (mRepoContentList.size > 0) mRepoContentList.size else 0
            mRepoContentList.addAll(repoContentList)
            notifyItemRangeInserted(lastItemIndex, mRepoContentList.size)
        }
    }

    fun addRepositoryContent(repoContent: RepositoryContents) {
        mRepoContentList.add(repoContent)
        notifyItemInserted(mRepoContentList.size - 1)
    }

    fun addLoading() {
        mRepoContentList.add(null)
        notifyItemInserted(mRepoContentList.size - 1)
    }

    fun clear() {
        mRepoContentList.clear()
        notifyDataSetChanged()
    }

}