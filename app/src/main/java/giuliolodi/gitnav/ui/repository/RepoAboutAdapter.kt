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

package giuliolodi.gitnav.ui.repository

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_repo_about.view.*

/**
 * Created by giulio on 23/08/2017.
 */
class RepoAboutAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNameList: MutableList<String> = mutableListOf()
    private var mNumberList: MutableList<String> = mutableListOf()

    private val onStargazersClick: PublishSubject<String> = PublishSubject.create()
    private val onContributorsClick: PublishSubject<String> = PublishSubject.create()

    fun getStargazersClicks(): Observable<String> {
        return onStargazersClick
    }

    fun getContributorsClicks(): Observable<String> {
        return onContributorsClick
    }

    class MyViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind(name: String, number: String) = with(itemView) {
            row_repo_about_text.text = name
            row_repo_about_n.text = number
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder((LayoutInflater.from(parent?.context).inflate(R.layout.row_repo_about, parent, false)))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MyViewHolder) {
            holder.bind(mNameList[position], mNumberList[position])
            if (position == 0) holder.itemView.row_repo_about_rl.setOnClickListener { onStargazersClick.onNext("0") }
            if (position == 3) holder.itemView.row_repo_about_rl.setOnClickListener { onContributorsClick.onNext("0") }
        }
    }

    override fun getItemCount(): Int { return mNameList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun set(nameList: MutableList<String>, numberList: MutableList<String>) {
        mNameList = nameList
        mNumberList = numberList
    }

}