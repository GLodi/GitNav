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
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryContents
import org.eclipse.egit.github.core.client.RequestException
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by giulio on 17/07/2017.
 */
class RepoContentPresenter<V: RepoContentContract.View> : BasePresenter<V>, RepoContentContract.Presenter<V> {

    private val TAG = "RepoContentPresenter"

    private var mOwner: String? = null
    private var mName: String? = null
    private var mRepo: Repository? = null
    private var mRepoContentList: MutableList<RepositoryContents> = mutableListOf()
    private var pathTree: MutableList<String> = mutableListOf()
    private var path: String = ""
    private var treeText: String = ""
    private var LOADING: Boolean = false
    private var LOADING_CONTENT: Boolean = false
    private var TREE_DEPTH: Int = 1

    @Inject
    constructor(mCompositeDisposable: CompositeDisposable, mDataManager: DataManager) : super(mCompositeDisposable, mDataManager)

    override fun subscribe(isNetworkAvailable: Boolean, owner: String?, name: String?) {
        owner?.let { mOwner = it }
        name?.let { mName = it }
        if (LOADING) getView().showLoading()
        // Check if content has already been downloaded
        else {
            if (isNetworkAvailable) {
                if (mOwner != null && mName != null) loadRepoContent()
            }
            else {
                getView().showNoConnectionError()
                LOADING = false
            }
        }
    }

    private fun loadRepoContent() {
        getCompositeDisposable().add(Flowable.zip<Repository, List<RepositoryContents>, Map<Repository, List<RepositoryContents>>>(
                getDataManager().getRepo(mOwner!!, mName!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                getDataManager().getContent(mOwner!!, mName!!, path)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { repo, repoContentList -> return@BiFunction mapOf(repo to repoContentList) })
                .doOnSubscribe {
                    getView().showLoading()
                    LOADING = true
                }
                .subscribe(
                        { map ->
                            getView().showContent(map)
                            getView().hideLoading()
                            getView().hideBottomLoading()
                            LOADING = false
                            LOADING_CONTENT = false
                        },
                        { throwable ->
                            getView().showError(throwable.localizedMessage)
                            getView().hideLoading()
                            Timber.e(throwable)
                        }
                ))
    }

    private fun setTree() {
        treeText = "/"
        treeText += pathTree[pathTree.size - 1]
        getView().onTreeSet(treeText)
    }

    override fun onBackPressed(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            if (!LOADING_CONTENT) {
                LOADING_CONTENT = true
                path = pathTree[pathTree.size - 2]
                pathTree.removeAt(pathTree.size - 1)
                TREE_DEPTH -= 1
                mRepoContentList.clear()
                repo_content_fragment_progressbar_bottom.visibility = View.VISIBLE
                loadRepoContent()
            }
        }
        else {
            Toasty.warning(context, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        }
    }

}