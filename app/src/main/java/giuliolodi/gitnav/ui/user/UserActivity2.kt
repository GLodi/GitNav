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

package giuliolodi.gitnav.ui.user

import android.os.Bundle
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import org.eclipse.egit.github.core.User
import javax.inject.Inject

/**
 * Created by giulio on 03/06/2017.
 */

class UserActivity2 : BaseActivity(), UserContract2.View {

    @Inject lateinit var mPresenter: UserContract2.Presenter<UserContract2.View>

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity2)

        initLayout()

        username = intent.getStringExtra("username")

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)

        if (isNetworkAvailable())
            mPresenter.subscribe(username)
        else
            Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
    }

    private fun initLayout() {

    }

    override fun showUser(mapUserFollowed: Map<User, String>) {
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showError(error: String) {
    }
}