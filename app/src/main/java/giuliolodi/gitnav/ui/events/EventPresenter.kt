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

import android.util.Log
import com.google.firebase.crash.FirebaseCrash
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by giulio on 15/05/2017.
 */

class EventPresenter<V: EventContract.View> : BasePresenter<V>, EventContract.Presenter<V> {

    val TAG = "EventPresenter"

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(pageN: Int, itemsPerPage: Int) {
        getCompositeDisposable().add(getDataManager().pageEvents(pageN, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { eventList ->
                            getView().showEvents(eventList)
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
