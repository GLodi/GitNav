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

package giuliolodi.gitnav.ui.repositorylist

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract
import org.eclipse.egit.github.core.Repository

/**
 * Created by giulio on 18/05/2017.
 */
interface RepoListContract {

    interface View : BaseContract.View {

        fun showRepos(repoList: List<Repository>)

        fun showLoading()

        fun hideLoading()

        fun showListLoading()

        fun hideListLoading()

        fun showNoRepo()

        fun hideNoRepo()

        fun setFilter(filter: HashMap<String,String>)

        fun clearAdapter()

        fun showError(error: String)

        fun intentToRepoActivity(repoOwner: String, repoName: String)

        fun showNoConnectionError()

    }

    @PerActivity
    interface Presenter<V: RepoListContract.View> : BaseContract.Presenter<V> {

        fun subscribe(isNetworkAvailable: Boolean)

        fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int)

        fun onSwipeToRefresh(isNetworkAvailable: Boolean)

        fun onRepoClick(repoOwner: String, repoName: String)

        fun onSortCreatedClick(isNetworkAvailable: Boolean)

        fun onSortUpdatedClick(isNetworkAvailable: Boolean)

        fun onSortPushedClick(isNetworkAvailable: Boolean)

        fun onSortAlphabeticalClick(isNetworkAvailable: Boolean)

        fun onSortStarsClick(isNetworkAvailable: Boolean)

    }

}