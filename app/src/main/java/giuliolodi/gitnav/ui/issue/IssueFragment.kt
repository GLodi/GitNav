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

package giuliolodi.gitnav.ui.issue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import org.eclipse.egit.github.core.Issue
import javax.inject.Inject

/**
 * Created by giulio on 14/11/2017.
 */
class IssueFragment : BaseFragment(), IssueContract.View {

    @Inject lateinit var mPresenter: IssueContract.Presenter<IssueContract.View>

    private var mIssueId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mIssueId = activity.intent.getLongExtra("issueId", 0)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fork_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)
    }

    override fun showIssue(issue: Issue) {
    }

}