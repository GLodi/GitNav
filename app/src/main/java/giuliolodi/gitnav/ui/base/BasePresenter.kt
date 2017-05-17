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

package giuliolodi.gitnav.ui.base

import giuliolodi.gitnav.data.DataManager
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by giulio on 12/05/2017.
 */

open class BasePresenter<V: BaseContract.View> : BaseContract.Presenter<V> {

    private val mCompositeDisposable: CompositeDisposable
    private val mDataManager: DataManager

    private var mBaseView: V? = null

    @Inject
    constructor(compositeDisposable: CompositeDisposable, dataManager: DataManager) {
        mCompositeDisposable = compositeDisposable
        mDataManager= dataManager
    }

    fun getCompositeDisposable(): CompositeDisposable {
        return mCompositeDisposable
    }

    fun getDataManager(): DataManager {
        return mDataManager
    }

    override fun onAttach(view: V) {
        mBaseView = view
        if (mBaseView is BaseDrawerActivity) {
            mBaseView!!.initDrawer(getDataManager().getUsername(), getDataManager().getFullname(), getDataManager().getEmail(), getDataManager().getPic())
        }
    }

    override fun onDetach() {
        mCompositeDisposable.dispose()
        mBaseView = null
    }

    fun isViewAttached(): Boolean {
        return mBaseView != null
    }

    fun getView(): V {
        return mBaseView!!
    }

}