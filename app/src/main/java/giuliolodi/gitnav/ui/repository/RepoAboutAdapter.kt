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

/**
 * Created by giulio on 23/08/2017.
 */
class RepoAboutAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNameList: MutableList<String> = mutableListOf()
    private var mNumberList: MutableList<String> = mutableListOf()

    class MyViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind(name: String, number: String) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder((LayoutInflater.from(parent?.context).inflate(R.layout.row_repo, parent, false)))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MyViewHolder) {
            holder.bind(mNameList[position], mNumberList[position])
        }
    }

    override fun getItemCount(): Int { return mNameList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun set(nameList: MutableList<String>, numberList: MutableList<String>) {
        mNameList = nameList
        mNumberList = numberList
    }

}