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

package giuliolodi.gitnav.ui.gist

import giuliolodi.gitnav.di.scope.PerActivity
import giuliolodi.gitnav.ui.base.BaseContract
import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.Gist

/**
 * Created by giulio on 26/05/2017.
 */

interface GistContract {

    interface View : BaseContract.View {

        fun showGist(map: Map<Gist,Boolean>)

        fun showComments(gistCommentList: List<Comment>)

        fun showLoading()

        fun hideLoading()

        fun showLoadingComments()

        fun hideLoadingComments()

        fun showError(error: String)

        fun onGistStarred()

        fun onGistUnstarred()

    }

    @PerActivity
    interface Presenter<V: GistContract.View> : BaseContract.Presenter<V> {

        fun subscribe(gistId: String)

        fun getComments(gistId: String)

        fun starGist(gistId: String)

        fun unstarGist(gistId: String)

    }

}