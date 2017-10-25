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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import android.widget.Toast
import com.hardikgoswami.oauthLibGithub.GithubOauth
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import giuliolodi.gitnav.ui.events.EventActivity
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

        mPresenter.subscribe()
    }

    private fun initLayout() {
        login_activity_loginbtn.setOnClickListener {
            if (login_activity_user.text.isEmpty() || login_activity_pass.text.isEmpty())
                Toasty.warning(applicationContext, getString(R.string.insert_credentials), Toast.LENGTH_LONG).show()
            else {
                if (isNetworkAvailable())
                    mPresenter.onLoginClick(login_activity_user.text.toString(), login_activity_pass.text.toString())
                else
                    Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
            }
        }
        login_activity_pass.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (login_activity_user.text.isEmpty() || login_activity_pass.text.isEmpty())
                    Toasty.warning(applicationContext, getString(R.string.insert_credentials), Toast.LENGTH_LONG).show()
                else {
                    if (isNetworkAvailable())
                        mPresenter.onLoginClick(login_activity_user.text.toString(), login_activity_pass.text.toString())
                    else
                        Toasty.warning(applicationContext, getString(R.string.network_error), Toast.LENGTH_LONG).show()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        login_activity_signup.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
            startActivity(browserIntent)
        }
        login_activity_web.setOnClickListener {
            //mPresenter.authorize()
            /*
            val githubOauth: GithubOauth = GithubOauth.Builder()
            githubOauth.scopeList = arrayListOf("repo", "gist", "user")
            githubOauth
                    .withClientId("")
                    .withClientSecret("")
                    .withContext(this)
                    .packageName("giuliolodi.gitnav")
                    .nextActivity("giuliolodi.gitnav.EventActivity")
                    .debug(true)
                    .execute()
            */
            val intent = CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setShowTitle(true)
                    .build()
            try {
                intent.launchUrl(this, mPresenter.getAuthorizationUrl())
            } catch (ignored: ActivityNotFoundException) {

            }
        }
    }

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

    override fun intentToEventActivity() {
        startActivity(EventActivity.getIntent(applicationContext))
        finish()
        overridePendingTransition(0,0)
    }

    override fun onDestroy() {
        mPresenter.onDetach()
        super.onDestroy()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

}