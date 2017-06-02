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

package giuliolodi.gitnav.ui.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_search_user.view.*
import org.eclipse.egit.github.core.SearchUser

/**
 * Created by giulio on 31/05/2017.
 */

class SearchUserAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mUserList: MutableList<SearchUser?> = arrayListOf()
    private val onImageClick: PublishSubject<String> = PublishSubject.create()

    fun getPositionClicks(): Observable<String> {
        return onImageClick
    }

    class UserHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun bind (user: SearchUser) = with(itemView) {
            if (user.name == null) {
                row_search_user_fullname.text = user.login
                row_search_user_username.visibility = View.GONE
            }
            else {
                row_search_user_fullname.text = user.name
                row_search_user_username.text = user.login
            }
            row_search_user_followers.text = user.followers.toString()
            row_search_user_repos.text = user.publicRepos.toString()
        }
    }

    class LoadingHolder(root: View) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val root: RecyclerView.ViewHolder
        if (viewType == 1) {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_search_user, parent, false))
            root = UserHolder(view)
        } else  {
            val view = (LayoutInflater.from(parent?.context).inflate(R.layout.row_loading, parent, false))
            root = LoadingHolder(view)
        }
        return root
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserHolder) {
            val user = mUserList[position]!!
            holder.bind(user)
            holder.itemView.row_search_user_ll.setOnClickListener { onImageClick.onNext(mUserList[position]?.login) }
        }
    }

    override fun getItemViewType(position: Int): Int { return if (mUserList[position] != null) 1 else 0 }

    override fun getItemCount(): Int { return mUserList.size }

    override fun getItemId(position: Int): Long { return position.toLong() }

    fun addUsers(userList: List<SearchUser>) {
        if (mUserList.isEmpty()) {
            mUserList.clear()
            mUserList.addAll(userList.toMutableList())
            notifyDataSetChanged()
        }
        else {
            val lastNull = mUserList.lastIndexOf(null)
            mUserList.removeAt(lastNull)
            notifyItemRemoved(lastNull)
            mUserList.addAll(userList)
            notifyItemRangeInserted(lastNull, mUserList.size - 1)
        }
    }

    fun addUser(user: SearchUser) {
        mUserList.add(user)
        notifyItemInserted(mUserList.size - 1)
    }

    fun addLoading() {
        mUserList.add(null)
        notifyItemInserted(mUserList.size - 1)
    }

    fun clear() {
        mUserList.clear()
        notifyDataSetChanged()
    }

}