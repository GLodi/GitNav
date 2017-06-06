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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import giuliolodi.gitnav.R
import giuliolodi.gitnav.ui.base.BaseActivity
import kotlinx.android.synthetic.main.user_activity2.*
import org.eclipse.egit.github.core.User
import javax.inject.Inject

/**
 * Created by giulio on 03/06/2017.
 */

class UserActivity2 : BaseActivity(), UserContract2.View {

    @Inject lateinit var mPresenter: UserContract2.Presenter<UserContract2.View>

    private lateinit var mUser: User
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
        setSupportActionBar(user_activity2_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        user_activity2_bottomnv.selectedItemId = R.id.user_activity_bottom_menu_info
        user_activity2_bottomnv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

            }
            true
        }
    }

    override fun showUser(mapUserFollowed: Map<User, String>) {
        mUser = mapUserFollowed.keys.first()
        user_activity2_collapsing_toolbar.title = mUser.name ?: mUser.login
        Picasso.with(applicationContext).load(mUser.avatarUrl).into(user_activity2_image)
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showError(error: String) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, UserActivity2::class.java)
        }
    }

}