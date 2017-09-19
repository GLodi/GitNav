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

package giuliolodi.gitnav.ui.forklist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fork_list_fragment.*
import org.eclipse.egit.github.core.Repository
import javax.inject.Inject

/**
 * Created by giulio on 19/09/2017.
 */
class ForkListFragment : BaseFragment(), ForkListContract.View {

    @Inject lateinit var mPresenter: ForkListContract.Presenter<ForkListContract.View>

    private var mOwner: String? = null
    private var mName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        getActivityComponent()?.inject(this)
        mOwner = activity.intent.getStringExtra("owner")
        mName = activity.intent.getStringExtra("name")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fork_list_fragment, container, false)
    }

    override fun initLayout(view: View?, savedInstanceState: Bundle?) {
        mPresenter.onAttach(this)
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(fork_list_fragment_toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.forks)
        (activity as AppCompatActivity).supportActionBar?.subtitle = mOwner + "/" + mName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        fork_list_fragment_toolbar.setNavigationOnClickListener { activity.onBackPressed() }

        mPresenter.subscribe(isNetworkAvailable(), mOwner, mName)
    }

    override fun showForkList(forkList: List<Repository>) {
    }

}