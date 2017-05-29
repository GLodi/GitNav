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

package giuliolodi.gitnav.ui.gistlist

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 23/05/2017.
 */

class GistListPresenter<V: GistListContract.View> : BasePresenter<V>, GistListContract.Presenter<V> {

    val TAG = "GistListPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun getMineGists(pageN: Int, itemsPerPage: Int) {
        getCompositeDisposable().add(getDataManager().pageGists(null, pageN, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoadingMine() }
                .subscribe(
                        { gistList ->
                            getView().showMineGists(gistList)
                            getView().hideLoadingMine()
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoadingMine()
                            Timber.e(throwable)
                        }
                ))
    }

    override fun getStarredGists(pageN: Int, itemsPerPage: Int) {
        getCompositeDisposable().add(getDataManager().pageStarredGists(pageN, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getView().showLoadingStarred() }
                .subscribe(
                        { gistList ->
                            getView().hideLoadingStarred()
                            getView().showStarredGists(gistList)
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoadingStarred()
                            Timber.e(throwable)
                        }
                ))
    }

}