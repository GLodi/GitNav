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

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.event.Event
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 15/05/2017.
 */
class EventPresenter<V: EventContract.View> : BasePresenter<V>, EventContract.Presenter<V> {

    private val TAG = "EventPresenter"

    private var mEventList: MutableList<Event> = mutableListOf()
    private var PAGE_N: Int = 1
    private val ITEMS_PER_PAGE: Int = 10
    private var LOADING: Boolean = false
    private var NO_SHOWING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean) {
        if (!mEventList.isEmpty()) getView().showEvents(mEventList)
        else if (NO_SHOWING) getView().showNoEvents()
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                loadEvents(isNetworkAvailable)
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
            }
        }
    }

    override fun loadEvents(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getCompositeDisposable().add(getDataManager().pageEvents(null, PAGE_N, ITEMS_PER_PAGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { if (!mEventList.isEmpty()) getView().showListLoading() }
                    .subscribe(
                            { eventList ->
                                mEventList.addAll(eventList)
                                getView().showEvents(eventList)
                                getView().hideLoading()
                                LOADING = false
                                if (PAGE_N == 1 && eventList.isEmpty()) {
                                    getView().showNoEvents()
                                    NO_SHOWING = true
                                }
                                PAGE_N += 1
                            },
                            { throwable ->
                                getView().showError(throwable.localizedMessage)
                                getView().hideLoading()
                                Timber.e(throwable)
                                PAGE_N -= 1
                            }
                    ))
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

    override fun onImageClick(username: String) {
        getView().intentToUserActivity(username)
    }

    override fun onSwipeToRefresh(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getView().hideNoEvents()
            NO_SHOWING = false
            PAGE_N = 1
            mEventList.clear()
            getView().clearAdapter()
            LOADING = true
            loadEvents(isNetworkAvailable)
        }
        else {
            getView().showNoConnectionError()
            getView().hideLoading()
        }
    }

}
