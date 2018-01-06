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

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_commit_file.view.*
import org.eclipse.egit.github.core.CommitFile
import android.text.style.ForegroundColorSpan

/**
 * Created by giulio on 06/01/2018.
 */
class CommitFileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mCommitFileList: MutableList<CommitFile?> = arrayListOf()
    private val onFileClick: PublishSubject<CommitFile> = PublishSubject.create()

    fun getPositionClicks(): Observable<CommitFile> {
        return onFileClick
    }

    class CommitFileHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (file: CommitFile) = with(itemView) {
            row_commit_file_filename.text = file.filename.substring(file.filename.lastIndexOf("/") + 1, file.filename.length)
            if (file.patch != null && !file.patch.isEmpty()) {
                val raw = file.patch
                val cleaned = raw.substring(raw.lastIndexOf("@@") + 3, raw.length)
                val spannable = SpannableString(cleaned)
                val c = cleaned.toCharArray()
                val backslash: MutableList<String> = mutableListOf()
                val piu: MutableList<String> = mutableListOf()
                val meno:  MutableList<String> = mutableListOf()
                for (i in 0 until c.size - 1) {
                    if (c[i] == '\n') {
                        backslash.add(i.toString())
                    }
                    if (c[i] == '\n' && c[i + 1] == '+') {
                        piu.add(i.toString())
                    }
                    if (c[i] == '\n' && c[i + 1] == '-') {
                        meno.add(i.toString())
                    }
                }
                for (i in 0 until piu.size) {
                    for (j in 0 until backslash.size) {
                        if (Integer.valueOf(piu[i]) < Integer.valueOf(backslash[j])) {
                            spannable.setSpan(BackgroundColorSpan(Color.parseColor("#cff7cf")), Integer.valueOf(piu[i]), Integer.valueOf(backslash[j]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            break
                        }
                    }
                }
                for (i in 0 until meno.size) {
                    for (j in 0 until backslash.size) {
                        if (Integer.valueOf(meno[i]) < Integer.valueOf(backslash[j])) {
                            spannable.setSpan(BackgroundColorSpan(Color.parseColor("#f7cdcd")), Integer.valueOf(meno[i]), Integer.valueOf(backslash[j]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            break
                        }
                    }
                }
                row_commit_file_cv.setOnClickListener {
                    if (row_commit_file_content.text == "...")
                        row_commit_file_content.text = spannable
                    else
                        row_commit_file_content.text = "..."
                }
            }
            val changed = "+ " + file.additions.toString() + "   - " + file.deletions.toString()
            val changedString = SpannableString(changed)
            changedString.setSpan(ForegroundColorSpan(Color.parseColor("#099901")), 0, changed.indexOf("-"), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            changedString.setSpan(ForegroundColorSpan(Color.parseColor("#c4071a")), changed.indexOf("-"), changedString.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            row_commit_file_changes.text = changedString
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_commit_file, parent, false))
            root = CommitFileHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommitFileHolder) {
            val file = mCommitFileList[position]!!
            holder.bind(file)
            holder.itemView.setOnClickListener { onFileClick.onNext(file) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mCommitFileList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mCommitFileList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addCommitFileList(commitFileList: List<CommitFile>) {
        if (mCommitFileList.isEmpty()) {
            mCommitFileList.addAll(commitFileList)
            notifyDataSetChanged()
        }
        else if (!commitFileList.isEmpty()) {
            val lastItemIndex = if (mCommitFileList.size > 0) mCommitFileList.size else 0
            mCommitFileList.addAll(commitFileList)
            notifyItemRangeInserted(lastItemIndex, mCommitFileList.size)
        }
    }

    fun addCommitFile(commitFile: CommitFile) {
        mCommitFileList.add(commitFile)
        notifyItemInserted(mCommitFileList.size - 1)
    }

    fun addLoading() {
        mCommitFileList.add(null)
        notifyItemInserted(mCommitFileList.size - 1)
    }

    fun clear() {
        mCommitFileList.clear()
        notifyDataSetChanged()
    }
    
}