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

package giuliolodi.gitnav.ui.login

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import giuliolodi.gitnav.utils.NetworkUtils
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject

/**
 * Created by giulio on 12/05/2017.
 */

class LoginActivity : BaseActivity(), LoginContract.View {

    @Inject lateinit var mPresenter: LoginContract.Presenter<LoginContract.View>

    lateinit var progDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        initLayout()

        getActivityComponent().inject(this)

        mPresenter.onAttach(this)
    }

    private fun initLayout() {
        login_activity_loginbtn.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(applicationContext))
                mPresenter.onLoginClick(login_activity_user.text.toString(), login_activity_pass.text.toString())
            else
                Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
        }
    }

    // Contract

    override fun showLoading() {
        progDialog = ProgressDialog(this)
        progDialog.setMessage("Signing in")
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDialog.setCancelable(false)
        progDialog.show()
    }

    override fun hideLoading() {
        if (progDialog.isShowing)
            progDialog.dismiss()
    }

    override fun showSuccess() {
        Toasty.success(applicationContext, getString(R.string.logged_in), Toast.LENGTH_LONG).show()
    }

    override fun showError(error: String) {
        Toasty.error(applicationContext, error, Toast.LENGTH_LONG).show()
    }

}