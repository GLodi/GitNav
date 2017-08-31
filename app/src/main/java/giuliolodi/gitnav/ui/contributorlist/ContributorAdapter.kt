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

package giuliolodi.gitnav.ui.contributorlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_contributor.view.*
import org.eclipse.egit.github.core.Contributor

/**
 * Created by giulio on 20/05/2017.
 */
class ContributorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mContributorList: MutableList<Contributor?> = arrayListOf()
    private val onClickSubject: PublishSubject<String> = PublishSubject.create()

    fun getPositionClicks(): Observable<String> {
        return onClickSubject
    }

    class ContributorHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (contributor: Contributor) = with(itemView) {
            row_contributor_login.text = contributor.login
            row_contributor_contributions.visibility = contributor.contributions
            Picasso.with(context).load(contributor.avatarUrl).resize(100,100).centerCrop().into(row_contributor_image)
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_contributor, parent, false))
            root = ContributorHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ContributorHolder) {
            val user = mContributorList[position]!!
            holder.bind(user)
            holder.itemView.setOnClickListener { onClickSubject.onNext(user.login) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mContributorList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mContributorList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addContributorList(contributorList: List<Contributor>) {
        if (mContributorList.isEmpty()) {
            mContributorList.clear()
            mContributorList.addAll(contributorList)
            notifyDataSetChanged()
        }
        else {
            val lastNull = mContributorList.lastIndexOf(null)
            mContributorList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mContributorList.addAll(contributorList)
            notifyItemRangeInserted(lastNull, mContributorList.size - 1)
        }
    }

    fun addContributor(contributor: Contributor) {
        mContributorList.add(contributor)
        notifyItemInserted(mContributorList.size - 1)
    }

    fun addLoading() {
        mContributorList.add(null)
        notifyItemInserted(mContributorList.size - 1)
    }

    fun hideLoading() {
        if (mContributorList.lastIndexOf(null) != -1) {
            val lastNull = mContributorList.lastIndexOf(null)
            mContributorList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
        }
    }

    fun clear() {
        mContributorList.clear()
        notifyDataSetChanged()
    }

}