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

package giuliolodi.gitnav.ui.gistlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import kotlinx.android.synthetic.main.row_gist.view.*
import org.eclipse.egit.github.core.Gist
import org.ocpsoft.prettytime.PrettyTime
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by giulio on 25/05/2017.
 */

class GistListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mGistList: MutableList<Gist?> = arrayListOf()
    private val mPrettyTime: PrettyTime = PrettyTime()
    private val onClickSubject: PublishSubject<String> = PublishSubject.create()

    fun getPositionClicks(): Observable<String> {
        return onClickSubject
    }

    class RepoHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (gist: Gist, p: PrettyTime) = with(itemView) {
            row_gist_description.text = gist.description
            row_gist_public.text = if (gist.isPublic) context.getString(R.string.publics) else context.getString(R.string.privates)
            row_gist_files_n.text = gist.files.size.toString()
            row_gist_id.text = gist.id
            row_gist_date.text = p.format(gist.createdAt)
            row_gist_comments_n.text = gist.comments.toString()
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_gist, parent, false))
            root = RepoHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RepoHolder) {
            val repo = mGistList[position]!!
            holder.bind(repo, mPrettyTime)
            holder.itemView.setOnClickListener {
                onClickSubject.onNext(mGistList[position]?.id)
            }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mGistList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mGistList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addGists(gistList: List<Gist>) {
        if (mGistList.isEmpty()) {
            mGistList.clear()
            mGistList.addAll(gistList)
            notifyDataSetChanged()
        }
        else {
            val lastNull = mGistList.lastIndexOf(null)
            mGistList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mGistList.addAll(gistList)
            notifyItemRangeInserted(lastNull, mGistList.size - 1)
        }
    }

    fun addGist(gist: Gist) {
        mGistList.add(gist)
        notifyItemInserted(mGistList.size - 1)
    }

    fun addLoading() {
        mGistList.add(null)
        notifyItemInserted(mGistList.size - 1)
    }

    fun clear() {
        mGistList.clear()
        notifyDataSetChanged()
    }
    
}