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

package giuliolodi.gitnav.ui.repositories

import android.util.Log
import com.google.firebase.crash.FirebaseCrash
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by giulio on 18/05/2017.
 */

class RepoListPresenter<V: RepoListContract.View> : BasePresenter<V>, RepoListContract.Presenter<V> {

    val TAG = "RepoListPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(pageN: Int, itemsPerPage: Int, filter: HashMap<String,String>) {
        getCompositeDisposable().add(getDataManager().pageRepos(pageN, itemsPerPage, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoList ->
                            getView().showRepos(repoList)
                            getView().hideLoading()
                        },
                        { throwable ->
                            Log.e(TAG, throwable.message, throwable)
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            FirebaseCrash.report(throwable)
                        }
                ))
    }

}