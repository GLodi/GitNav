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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment

/**
 * Created by giulio on 11/07/2017.
 */
class RepoCommitsFragment : BaseFragment() {

    private var mOwner: String? = null
    private var mName: String? = null

    companion object {
        fun newInstance(owner: String, name: String): RepoCommitsFragment {
            val repoCommitsFragment: RepoCommitsFragment = RepoCommitsFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            repoCommitsFragment.arguments = bundle
            return repoCommitsFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mOwner = arguments.getString("owner")
        mName = arguments.getString("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.repo_commits_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
    }

}