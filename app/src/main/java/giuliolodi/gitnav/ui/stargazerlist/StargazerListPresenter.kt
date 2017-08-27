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

package giuliolodi.gitnav.ui.stargazerlist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by giulio on 25/08/2017.
 */
class StargazerListPresenter<V: StargazerListContract.View> : BasePresenter<V>, StargazerListContract.Presenter<V> {

    private val TAG = "StargazerListPresenter"

    private var mRepoOwner: String? = null
    private var mRepoName: String? = null
    private var PAGE_N: Int = 1
    private var ITEMS_PER_PAGE: Int = 20
    private var LOADING: Boolean = false
    private var LOADING_LIST: Boolean = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(repoOwner: String?, repoName: String?, isNetworkAvailable: Boolean) {

    }

}