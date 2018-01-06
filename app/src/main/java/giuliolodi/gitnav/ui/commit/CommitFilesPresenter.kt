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

package giuliolodi.gitnav.ui.commit

import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.CommitFile
import org.eclipse.egit.github.core.RepositoryCommit
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 29/12/2017.
 */
class CommitFilesPresenter<V: CommitFilesContract.View> : BasePresenter<V>, CommitFilesContract.Presenter<V> {

    private val TAG = "CommitFilesPresenter"

    private var mCommitFileList: List<CommitFile> = mutableListOf()
    private var mOwner: String? = null
    private var mName: String? = null
    private var mSha: String? = null
    private var LOADING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?, sha: String?) {
        mOwner = owner
        mName = name
        mSha = sha
        if (!mCommitFileList.isEmpty()) getView().showFiles(mCommitFileList)
        else if (LOADING) getView().showLoading()
        else {
            if (isNetworkAvailable) {
                LOADING = true
                getView().showLoading()
                if (mOwner != null && mName != null && mSha != null) loadFiles()
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadFiles() {
        getCompositeDisposable().add(getDataManager().getCommit(mOwner!!, mName!!, mSha!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { commit ->
                            mCommitFileList = commit.files
                            getView().showFiles(mCommitFileList)
                            getView().hideLoading()
                            LOADING = false
                        },
                        { throwable ->
                            throwable?.localizedMessage?.let { getView().showError(it) }
                            getView().hideLoading()
                            Timber.e(throwable)
                            LOADING = false
                        }
                ))
    }

}