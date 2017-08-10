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

package giuliolodi.gitnav.ui.events

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract
import org.eclipse.egit.github.core.event.Event

/**
 * Created by giulio on 15/05/2017.
 */
interface EventContract {

    interface View : BaseContract.View {

        fun showEvents(eventList: List<Event>)

        fun showLoading()

        fun showListLoading()

        fun hideListLoading()

        fun hideLoading()

        fun showNoEvents()

        fun hideNoEvents()

        fun showError(error: String)

        fun showNoConnectionError()

        fun clearAdapter()

        fun intentToUserActivity(username: String)

    }

    @PerActivity
    interface Presenter<V: EventContract.View> : BaseContract.Presenter<V> {

        fun subscribe(isNetworkAvailable: Boolean)

        fun onLastItemVisible(isNetworkAvailable: Boolean, dy: Int)

        fun onSwipeToRefresh(isNetworkAvailable: Boolean)

        fun onUserClick(username: String)

    }

}
