/*
 * Copyright 2016 GLodi
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

package giuliolodi.gitnav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends BaseDrawerActivity {

    @BindView(R.id.user_fragment_layout) RelativeLayout relativeLayout;
    @BindView(R.id.user_activity_name) TextView username;
    @BindView(R.id.user_activity_description) TextView user_bio;
    @BindView(R.id.user_activity_image) CircleImageView user_image;
    @BindView(R.id.user_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.user_activity_login) TextView login;
    @BindView(R.id.user_activity_location) TextView location;
    @BindView(R.id.user_activity_mail) TextView mail;
    @BindView(R.id.user_activity_location_icon) ImageView location_icon;
    @BindView(R.id.user_activity_mail_icon) ImageView mail_icon;
    @BindView(R.id.user_vp) ViewPager mViewPager;

    @BindString(R.string.network_error) String network_error;
    @BindString(R.string.user_followed) String user_followed;
    @BindString(R.string.user_unfollowed) String user_unfollowed;
    @BindString(R.string.profile) String profile;

    private User user;
    private String userS;
    private String email;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private List<Integer> views;
    private UserService userService;
    private Intent intent;
    private Menu menu;

    private boolean IS_FOLLOWED = false;
    private boolean HAS_EMAIL = false;
    private boolean HAS_CLICKED_ON_BIO = false;

    /*
        Most of UI items are created onPostExecute() in getUser()
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.user_activity, frameLayout);

        ButterKnife.bind(this);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Get username string from intent.putExtra()
        intent = getIntent();
        userS = intent.getStringExtra("userS");

        username.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
        user_bio.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
        login.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));

        // Setup adapter for ViewPager. It will handle the three fragments below.
        views = new ArrayList<>();
        views.add(R.layout.user_repos);
        views.add(R.layout.user_followers);
        views.add(R.layout.user_following);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(View container, int position, Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                ViewGroup layout = (ViewGroup) inflater.inflate(views.get(position), container, false);
                container.addView(layout);
                return layout;
            }
        });

        if (Constants.isNetworkAvailable(getApplicationContext())) {
            new getUser().execute();
        }
        else
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();
    }

    private void setHasClickedOnBio(boolean set) {
        this.HAS_CLICKED_ON_BIO = set;
    }

    private boolean getHasClickedOnBio() {
        return HAS_CLICKED_ON_BIO;
    }

    /*
            This is used only to get the menu object from the Intent call.
            The menu is created through createOptionMenu(), which is called after
            getUser() has checked if the user if followed.
         */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    private void createOptionMenu() {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        super.onCreateOptionsMenu(menu);

        if (!Constants.getUsername(getApplicationContext()).equals(userS) && !IS_FOLLOWED) {
            menu.findItem(R.id.unfollow_icon).setVisible(true);
        }

        else if (!Constants.getUsername(getApplicationContext()).equals(userS) && IS_FOLLOWED) {
            menu.findItem(R.id.follow_icon).setVisible(true);
        }

        if (!Constants.getUsername(getApplicationContext()).equals(userS) && HAS_EMAIL)
            menu.findItem(R.id.send_email).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_options) {
            startActivity(new Intent(getApplicationContext(), OptionActivity.class));
            overridePendingTransition(0,0);
        }
        if (Constants.isNetworkAvailable(getApplicationContext())) {
            switch (item.getItemId()) {
                case R.id.unfollow_icon:
                    new followUser().execute();
                    return true;
                case R.id.follow_icon:
                    new unfollowUser().execute();
                    return true;
                case R.id.send_email:
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    startActivity(Intent.createChooser(emailIntent, "Email"));
                    return true;
                case R.id.open_in_broswer:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(user.getHtmlUrl()));
                    startActivity(browserIntent);
                    return true;
            }
        }
        else
            Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    public void setUser(String userS) {
        this.userS = userS;
    }

    // Get User object after setUser(User user) is called
    class getUser extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show progress bar while information is downloaded
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            userService = new UserService();
            userService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));

            try {
                user = userService.getUser(userS);
                // Check if authdUser is following user
                IS_FOLLOWED = userService.isFollowing(user.getLogin());
            } catch (IOException e) {e.printStackTrace();}

            /*
                Notify 3 fragments.
                This will pass to each fragment the User object that
                they will use to get info about Repos, Followers and Following.

             */
            UserRepos userRepos = new UserRepos();
            userRepos.populate(user.getLogin(), UserActivity.this, findViewById(android.R.id.content));

            UserFollowers userFollowers = new UserFollowers();
            userFollowers.populate(user.getLogin(), UserActivity.this, findViewById(android.R.id.content));

            UserFollowing userFollowing = new UserFollowing();
            userFollowing.populate(user.getLogin(), UserActivity.this, findViewById(android.R.id.content));

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // If user is the one currently logged in, update his info
            if (user.getLogin().equals(Constants.getUsername(getApplicationContext())))
                new getAuthdUser().execute();

            // Download and show information
            Picasso.with(getApplicationContext()).load(user.getAvatarUrl()).resize(150, 150).centerCrop().into(user_image);

            // If user's name is available show both login and username. Otherwise show only login
            if (user.getName() != null) {
                username.setText(user.getName());
                login.setText(user.getLogin());
            }
            else {
                username.setText(user.getLogin());
                login.setVisibility(View.GONE);
            }

            if (user.getBio() != null) {
                user_bio.setVisibility(View.VISIBLE);
                user_bio.setMaxLines(2);
                user_bio.setEllipsize(TextUtils.TruncateAt.END);
                user_bio.setText(user.getBio());
                user_bio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setHasClickedOnBio(!getHasClickedOnBio());
                        if (getHasClickedOnBio())
                            user_bio.setMaxLines(100);
                        else
                            user_bio.setMaxLines(2);
                    }
                });
            }

            // If available set location
            if (user.getLocation() != null) {
                location_icon.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
                location.setText(user.getLocation());
            }

            // If available set email
            if (user.getEmail() != null) {
                HAS_EMAIL = true;
                mail_icon.setVisibility(View.VISIBLE);
                mail.setVisibility(View.VISIBLE);
                mail.setText(user.getEmail());
                email = user.getEmail();
            }

            // Fix padding if user has only email
            if (user.getEmail() != null && user.getLocation() == null) {
                int paddingPixel = 13;
                float density = getApplicationContext().getResources().getDisplayMetrics().density;
                int paddingDp = (int)(paddingPixel * density);
                mail_icon.setPadding(0, paddingDp, 0, 0);
                mail_icon.setVisibility(View.VISIBLE);
                mail.setVisibility(View.VISIBLE);
                mail.setText(user.getEmail());
                email = user.getEmail();
            }

            /*
                The creation of the option menu is triggered after the getUser() is called to check
                if the user is followed. This allows to show follow_icon and unfollow.
             */
            createOptionMenu();

            // Setup lists for tabs
            final List<String> mTitleDataList = new ArrayList<>();
            mTitleDataList.add("REPOSITORIES");
            mTitleDataList.add("FOLLOWERS");
            mTitleDataList.add("FOLLOWING");

            final List<String> mNumberDataList = new ArrayList<>();
            mNumberDataList.add(String.valueOf(user.getPublicRepos()));
            mNumberDataList.add(String.valueOf(user.getFollowers()));
            mNumberDataList.add(String.valueOf(user.getFollowing()));

            // Setup navigator for tabs. It will set a view to the corresponding tab (Repos, Followers and Following).
            MagicIndicator magicIndicator = (MagicIndicator) findViewById(R.id.magic_indicator_user);
            CommonNavigator commonNavigator = new CommonNavigator(getApplicationContext());
            commonNavigator.setAdjustMode(true);
            commonNavigator.setAdapter(new CommonNavigatorAdapter() {
                @Override
                public int getCount() {
                    return mTitleDataList == null ? 0 : mTitleDataList.size();
                }

                @Override
                public IPagerTitleView getTitleView(Context context, final int index) {
                    CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);

                    /*
                        Get reference of custom layout for three tabs.
                        This is due to the fact that default Pager doesn't allow multi-line
                        tabs. user_tab includes 2 TextViews one on top of the other.
                     */
                    View customLayout = LayoutInflater.from(context).inflate(R.layout.user_tab, null);
                    commonPagerTitleView.setContentView(customLayout);
                    final TextView user_fragment_tab_n = (TextView) customLayout.findViewById(R.id.user_fragment_tab_n);
                    final TextView user_fragment_tab_title = (TextView) customLayout.findViewById(R.id.user_fragment_tab_title);
                    user_fragment_tab_n.setText(mNumberDataList.get(index));
                    user_fragment_tab_title.setText(mTitleDataList.get(index));
                    user_fragment_tab_n.setTypeface(EasyFonts.robotoRegular(context));
                    user_fragment_tab_title.setTypeface(EasyFonts.robotoRegular(context));
                    commonPagerTitleView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mViewPager.setCurrentItem(index);
                        }
                    });
                    return commonPagerTitleView;
                }

                @Override
                public IPagerIndicator getIndicator(Context context) {
                    LinePagerIndicator indicator = new LinePagerIndicator(context);
                    indicator.setMode(LinePagerIndicator.MODE_MATCH_EDGE);
                    indicator.setStartInterpolator(new AccelerateInterpolator());
                    indicator.setColors(Color.parseColor("#448AFF"));
                    return indicator;
                }
            });

            // These two lines are required (see GitHub MagicIndicator
            magicIndicator.setNavigator(commonNavigator);
            ViewPagerHelper.bind(magicIndicator, mViewPager);

            // Make progress bar invisible and layout visible
            progressBar.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    /*
        AsyncTask for authenticated user. Used when user clicks on its own picture in nav drawer.
        After getting user info, checks if retrieved info equals what is stored in SharedPreferences.
        If that's not the case it updates the info.
     */
    class getAuthdUser extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = sp.edit();
            if (user.getName() != null && !user.getName().equals("") && !Constants.getFullName(getApplicationContext()).equals(user.getName())) {
                editor.putString((Constants.getFullNameKey(getApplicationContext())), user.getName());
            }
            else if (user.getLogin() != null && !user.getLogin().equals("") && !Constants.getUsername(getApplicationContext()).equals(user.getLogin())) {
                editor.putString((Constants.getUserKey(getApplicationContext())), user.getName());
            }
            else if (user.getEmail() != null && !user.getEmail().equals("") && !Constants.getEmail(getApplicationContext()).equals(user.getEmail())) {
                editor.putString((Constants.getEmailKey(getApplicationContext())), user.getEmail());
            }
            editor.commit();
            Bitmap thumbnail = new ImageSaver(getApplicationContext())
                    .setFileName("thumbnail.png")
                    .setDirectoryName("images")
                    .load();
            try {
                Bitmap profile_picture = Picasso.with(getApplicationContext()).load(user.getAvatarUrl()).get();
                if (!thumbnail.sameAs(profile_picture)) {
                    new ImageSaver(getApplicationContext())
                            .setFileName("thumbnail.png")
                            .setDirectoryName("images")
                            .save(profile_picture);
                }
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }
    }

    class followUser extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                userService.follow(user.getLogin());
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            menu.findItem(R.id.follow_icon).setVisible(true);
            menu.findItem(R.id.unfollow_icon).setVisible(false);
            Toast.makeText(getApplicationContext(), user_followed, Toast.LENGTH_LONG).show();
        }
    }

    class unfollowUser extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                userService.unfollow(user.getLogin());
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            menu.findItem(R.id.unfollow_icon).setVisible(true);
            menu.findItem(R.id.follow_icon).setVisible(false);
            Toast.makeText(getApplicationContext(), user_unfollowed, Toast.LENGTH_LONG).show();

        }
    }

}
