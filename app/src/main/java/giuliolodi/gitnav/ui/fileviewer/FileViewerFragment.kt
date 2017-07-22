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

import android.os.Bundle
import android.view.View
import giuliolodi.gitnav.ui.base.BaseFragment
import org.eclipse.egit.github.core.RepositoryContents

/**
 * Created by giulio on 22/07/2017.
 */
class FileViewerFragment : BaseFragment(), FileViewerContract.View {

    companion object {
        fun newInstance(owner: String, name: String): FileViewerFragment {
            val fileViewerFragment: FileViewerFragment = FileViewerFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("owner", owner)
            bundle.putString("name", name)
            fileViewerFragment.arguments = bundle
            return fileViewerFragment
        }
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
    }

    override fun showContent(repoContent: RepositoryContents) {
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showError(error: String) {
    }
}