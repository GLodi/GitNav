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

package giuliolodi.gitnav.ui.webviewer

import android.net.Uri
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import giuliolodi.gitnav.BuildConfig


/**
 * Created by giulio on 13/10/2017.
 */
class WebViewerPresenter<V: WebViewerContract.View> : BasePresenter<V>, WebViewerContract.Presenter<V> {

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe() {
    }

    override fun getAuthorizationUrl(): Uri {
        return Uri.Builder().scheme("https")
                .authority("github.com")
                .appendPath("login")
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("client_id", "")
                .appendQueryParameter("redirect_uri", "")
                .appendQueryParameter("scope", "user,repo,gist")
                .appendQueryParameter("state", BuildConfig.APPLICATION_ID)
                .build()
    }

}