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
import giuliolodi.gitnav.data.model.FileViewerIntent
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
    private var mFilename: String? = null
    private var LOADING: Boolean = false

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(fileViewerIntent: FileViewerIntent, isNetworkAvailable: Boolean) {
        if (fileViewerIntent.repoOwner != null && fileViewerIntent.repoName != null && fileViewerIntent.fileName != null) {
            getView().initRepoFileTitle(fileViewerIntent.fileName!!, fileViewerIntent.repoOwner + "/" + fileViewerIntent.repoName)
            mFilename = fileViewerIntent.fileName
        }

        else if (fileViewerIntent.gistFileName != null && fileViewerIntent.gistContent != null) {
            getView().initGistFileTitleContent(fileViewerIntent.gistFileName!!, fileViewerIntent.gistContent!!)
            mFilename = fileViewerIntent.fileName
        }

        if (LOADING) getView().showLoading()
        else if (mFileContent != null) showFile()
        else {
            if (isNetworkAvailable) {
                getView().showLoading()
                LOADING = true
                if (fileViewerIntent.repoOwner != null && fileViewerIntent.repoName != null && fileViewerIntent.filePath != null) {
                    loadFile(fileViewerIntent)
                }
            }
            else {
                getView().showNoConnectionError()
                getView().hideLoading()
                LOADING = false
            }
        }
    }

    private fun loadFile(fileViewerIntent: FileViewerIntent) {
        getCompositeDisposable().add(getDataManager().getContent(fileViewerIntent.repoOwner!!, fileViewerIntent.repoName!!, fileViewerIntent.filePath!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { repoContent ->
                            var fileDecoded: String = ""
                            try {
                                fileDecoded = Base64.decode(repoContent[0].content, Base64.DEFAULT).toString(charset("UTF-8"))
                                mFileContent = fileDecoded
                            } catch (e: UnsupportedEncodingException) { e.printStackTrace() }
                            showFile()
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

    private fun showFile() {
        if (mFilename?.substring(mFilename?.indexOf('.')!!, mFilename?.length!!)?.equals(".md")!!) {
            mFileContent?.let { getView().showMDFile(it) }
        }
        else {
            mFileContent?.let { getView().showRepoFile(it) }
        }
    }

}