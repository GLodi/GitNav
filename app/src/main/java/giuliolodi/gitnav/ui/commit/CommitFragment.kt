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

package giuliolodi.gitnav.ui.commit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.commit_fragment.*
import org.eclipse.egit.github.core.Commit
import javax.inject.Inject

/**
 * Created by giulio on 20/12/2017.
 */
class CommitFragment : BaseFragment(), CommitContract.View {

    @Inject lateinit var mPresenter: CommitContract.Presenter<CommitContract.View>

    private var mOwner: String? = null
    private var mName: String? = null
    private var mSha: String? = null
    private var mCommitUrl: String? = null
    private var mCommitTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
        mSha = activity.intent.getStringExtra("sha")
        mCommitUrl = activity.intent.getStringExtra("commit_url")
        mCommitTitle = activity.intent.getStringExtra("commit_title")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.event_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(commit_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = mCommitTitle
        (activity as AppCompatActivity).supportActionBar?.subtitle = mSha
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun showCommit(commit: Commit) {
    }

}