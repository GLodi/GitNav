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

package giuliolodi.gitnav.ui.issuelist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.disposables.CompositeDisposable
import org.eclipse.egit.github.core.Issue
import javax.inject.Inject

/**
 * Created by giulio on 02/09/2017.
 */
class IssueClosedPresenter<V: IssueClosedContract.View> : BasePresenter<V>, IssueClosedContract.Presenter<V> {

    private val TAG = "IssueClosedPresenter"

    private var mOwner: String? = null
    private var mName: String? = null
    private var PAGE_N: Int = 1
    private var ITEMS_PER_PAGE: Int = 10
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false
    private var mHashMap: HashMap<String,String> = hashMapOf()
    private var mIssueList: MutableList<Issue> = mutableListOf()
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
    }

    override fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int) {
    }

    override fun onUserClick(username: String) {
    }
}