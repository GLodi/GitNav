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

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract

/**
 * Created by giulio on 10/07/2017.
 */
interface RepoContract {

    interface View : BaseContract.View {

        fun showTitleAndSubtitle(title: String, subtitle: String)

        fun showError(error: String)

        fun showNoConnectionError()

        fun onRepoStarred()

        fun onRepoUnstarred()

        fun onRepoNotFound()

        fun intentToBrowser(url: String)

        fun createOptionsMenu(isRepoStarred: Boolean)

        fun onRepoForked()

        fun intentToForkedRepo(owner: String, name: String)

    }

    @PerActivity
    interface Presenter<V: RepoContract.View> : BaseContract.Presenter<V> {

        fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?)

        fun onStarRepo(isNetworkAvailable: Boolean)

        fun onUnstarRepo(isNetworkAvailable: Boolean)

        fun onForkRepo(isNetworkAvailable: Boolean)

        fun onOpenInBrowser()

        fun onOptionsMenuCreated()

        fun getTheme(): String

    }

}