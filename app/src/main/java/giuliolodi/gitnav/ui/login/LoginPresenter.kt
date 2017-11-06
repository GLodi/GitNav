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

package giuliolodi.gitnav.ui.login

import android.content.Intent
import android.net.Uri
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.client.RequestException
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by giulio on 12/05/2017.
 */
class LoginPresenter<V: LoginContract.View> : BasePresenter<V>, LoginContract.Presenter<V> {

    val TAG = "LoginPresenter"

    private lateinit var stateSent: String

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe() {
        if (!getDataManager().getToken().isEmpty()) getView().intentToEventActivity()
    }

    override fun onLoginClick(user: String, pass: String) {
        getCompositeDisposable().add(getDataManager().tryAuthentication(user, pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        {
                            getView().hideLoading()
                            getView().showSuccess()
                            getView().intentToEventActivity()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            if ((throwable as? RequestException)?.status != 401)
                                Timber.e(throwable)
                        }
                ))
    }

    override fun getFirstStepUri(): Uri {
        stateSent = UUID.randomUUID().toString()
        return Uri.Builder().scheme("https")
                .authority("github.com")
                .appendPath("login")
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("client_id", "")
                .appendQueryParameter("redirect_uri", "gitnav://login")
                .appendQueryParameter("scope", "user,repo,gist")
                .appendQueryParameter("state", stateSent)
                .build()
    }

    override fun onHandleAuthIntent(intent: Intent?) {
        val a = intent?.data
        intent?.data?.let {
            if (it.toString().startsWith("gitnav://login")) {
                if (it.getQueryParameter("access_token") == null || it.getQueryParameter("access_token").toString().isEmpty()) {
                    val code = it.getQueryParameter("code")
                    val stateReceived = it.getQueryParameter("state")
                    if (stateSent == stateReceived) {
                        getView().makeSecondRequest(Uri.Builder().scheme("https")
                                .authority("github.com")
                                .appendPath("login")
                                .appendPath("oauth")
                                .appendPath("access_token")
                                .appendQueryParameter("client_id", "")
                                .appendQueryParameter("client_secret", "")
                                .appendQueryParameter("code", code)
                                .appendQueryParameter("redirect_uri", "gitnav://login")
                                .appendQueryParameter("state", stateReceived)
                                .build())
                    }
                }
                else {
                    val access_token = it.getQueryParameter("access_token")
                    getDataManager().storeAccessToken(access_token)
                    getView().intentToEventActivity()
                }
            }
        }
    }

}
