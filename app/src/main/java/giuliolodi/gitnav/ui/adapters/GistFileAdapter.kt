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
import kotlinx.android.synthetic.main.row_gist_file.view.*
import org.eclipse.egit.github.core.GistFile

/**
 * Created by giulio on 28/05/2017.
 */
class GistFileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mGistFileList: MutableList<GistFile?> = arrayListOf()
    private val onFileClick: PublishSubject<GistFile> = PublishSubject.create()

    fun getPositionClicks(): Observable<GistFile> {
        return onFileClick
    }

    class GistFileHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (file: GistFile) = with(itemView) {
            row_gist_file_icon.setImageResource(R.drawable.octicons_430_file_256_0_757575_none)
            row_gist_file_name.text = file.filename
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_gist_file, parent, false))
            root = GistFileHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GistFileHolder) {
            val file = mGistFileList[position]!!
            holder.bind(file)
            holder.itemView.setOnClickListener { onFileClick.onNext(file) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mGistFileList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mGistFileList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addGistFileList(gistFileList: List<GistFile>) {
        if (!gistFileList.isEmpty()) {
            val lastItemIndex = if (mGistFileList.size > 0) mGistFileList.size else 0
            mGistFileList.addAll(gistFileList)
            notifyItemRangeInserted(lastItemIndex, mGistFileList.size)
        }
    }

    fun addGistFile(gistFile: GistFile) {
        mGistFileList.add(gistFile)
        notifyItemInserted(mGistFileList.size - 1)
    }

    fun addLoading() {
        mGistFileList.add(null)
        notifyItemInserted(mGistFileList.size - 1)
    }

    fun clear() {
        mGistFileList.clear()
        notifyDataSetChanged()
    }

}