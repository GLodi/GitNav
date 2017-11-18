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
import kotlinx.android.synthetic.main.row_code.view.*
import org.eclipse.egit.github.core.CodeSearchResult

/**
 * Created by giulio on 02/06/2017.
 */
class SearchCodeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mCodeList: MutableList<CodeSearchResult?> = arrayListOf()

    class CodeHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (code: CodeSearchResult) = with(itemView) {
            row_code_name.text = code.name
            row_code_path.text = code.path
            row_code_repo.text = code.repository.owner.login + "/" + code.repository.name
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_code, parent, false))
            root = CodeHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CodeHolder) {
            val repo = mCodeList[position]!!
            holder.bind(repo)
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mCodeList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mCodeList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addCodeList(repoList: List<CodeSearchResult>) {
        if (mCodeList.isEmpty()) {
            mCodeList.clear()
            mCodeList.addAll(repoList)
            notifyDataSetChanged()
        }
        else {
            val lastNull = mCodeList.lastIndexOf(null)
            mCodeList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mCodeList.addAll(repoList)
            notifyItemRangeInserted(lastNull, mCodeList.size - 1)
        }
    }

    fun addCode(code: CodeSearchResult) {
        mCodeList.add(code)
        notifyItemInserted(mCodeList.size - 1)
    }

    fun addLoading() {
        mCodeList.add(null)
        notifyItemInserted(mCodeList.size - 1)
    }

    fun clear() {
        mCodeList.clear()
        notifyDataSetChanged()
    }
}