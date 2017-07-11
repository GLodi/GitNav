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

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.client.RequestException
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 11/07/2017.
 */
class RepoReadmePresenter<V: RepoReadmeContract.View> : BasePresenter<V>, RepoReadmeContract.Presenter<V> {

    val TAG = "RepoReadmePresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(owner: String, name: String) {
        getCompositeDisposable().add(getDataManager().getReadme(owner, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoading() }
                .subscribe(
                        { markdown ->
                            getView().showReadme(markdown)
                            getView().hideLoading()
                        },
                        { throwable ->
                            if ((throwable as RequestException).status == 404)
                                getView().showNoReadme()
                            else {
                                getView().showError(throwable.localizedMessage)
                                Timber.e(throwable)
                            }
                            getView().hideLoading()
                        }
                ))
    }

}