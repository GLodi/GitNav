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

package giuliolodi.gitnav.ui.fileviewer

import android.util.Base64
import giuliolodi.gitnav.data.DataManager
import giuliolodi.gitnav.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.UnsupportedEncodingException
import javax.inject.Inject

/**
 * Created by giulio on 22/07/2017.
 */
class FileViewerPresenter<V: FileViewerContract.View> : BasePresenter<V>, FileViewerContract.Presenter<V> {

    private val TAG: String = "FileViewerPresenter"

    private var mFileContent: String? = null
    private var LOADING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(repoOwner: String?, repoName: String?, filepath: String?, filename: String?, gistFilename: String?, gistContent: String?, isNetworkAvailable: Boolean) {
        if (repoOwner != null && repoName != null && filename != null) getView().initRepoFileTitle(filename, repoOwner + "/" + repoName)
        else if (gistFilename != null && gistContent != null) getView().initGistFileTitleContent(gistFilename, gistContent)

        if (LOADING) getView().showLoading()
        else if (mFileContent != null) getView().showRepoFile(mFileContent!!)
        // check if gist content has already been downloaded
        else {
            if (isNetworkAvailable) {
                if (repoOwner != null && repoName != null && filepath != null) {
                    getCompositeDisposable().add(getDataManager().getContent(repoOwner, repoName, filepath)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe {
                                getView().showLoading()
                                LOADING = true
                            }
                            .subscribe(
                                    { repoContent ->
                                        var fileDecoded: String = ""
                                        try {
                                            fileDecoded = Base64.decode(repoContent[0].content, Base64.DEFAULT).toString(charset("UTF-8"))
                                            mFileContent = fileDecoded
                                        } catch (e: UnsupportedEncodingException) { e.printStackTrace() }
                                        getView().showRepoFile(fileDecoded)
                                        getView().hideLoading()
                                        LOADING = false
                                    },
                                    { throwable ->
                                        getView().showError(throwable.localizedMessage)
                                        getView().hideLoading()
                                        Timber.e(throwable)
                                        LOADING = false
                                    }
                            ))
                }
            }
            else {
                getView().showNoConnectionError()
            }
        }
    }

}