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
import kotlinx.android.synthetic.main.row_gist_file.view.*
import org.eclipse.egit.github.core.GistFile

/**
 * Created by giulio on 28/05/2017.
 */

class GistFileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mFileList: MutableList<GistFile?> = arrayListOf()

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
            val file = mFileList[position]!!
            holder.bind(file)
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mFileList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mFileList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addRepos(gistFileList: List<GistFile>) {
        if (mFileList.isEmpty()) {
            mFileList.clear()
            mFileList.addAll(gistFileList.toMutableList())
            notifyDataSetChanged()
        }
        else {
            val lastNull = mFileList.lastIndexOf(null)
            mFileList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mFileList.addAll(gistFileList)
            notifyItemRangeInserted(lastNull, mFileList.size - 1)
        }
    }

    fun addRepo(gistFile: GistFile) {
        mFileList.add(gistFile)
        notifyItemInserted(mFileList.size - 1)
    }

    fun addLoading() {
        mFileList.add(null)
        notifyItemInserted(mFileList.size - 1)
    }

    fun clear() {
        mFileList.clear()
        notifyDataSetChanged()
    }

}