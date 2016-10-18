/*
 * MIT License
 *
 * Copyright (c) 2016 GLodi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package giuliolodi.gitnav;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
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
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
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

public class UserFragment extends Fragment implements MainActivity.OnBackPressedListener{

    @BindView(R.id.user_fragment_layout) RelativeLayout relativeLayout;
    @BindView(R.id.user_fragment_name) TextView username;
    @BindView(R.id.user_fragment_description) TextView user_bio;
    @BindView(R.id.user_fragment_image) CircleImageView user_image;
    @BindView(R.id.user_fragment_progress_bar) ProgressBar progressBar;
    @BindView(R.id.user_fragment_login) TextView login;

    @BindString(R.string.network_error) String network_error;

    public User user;
    private ViewPager mViewPager;
    private Context context;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private List<Integer> views;
    private FragmentManager fm;

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_fragment, container, false);
        ButterKnife.bind(this, v);
        ((MainActivity) getActivity()).setOnBackPressedListener(this);

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        views = new ArrayList<>();
        views.add(R.layout.user_fragment_repos);
        views.add(R.layout.user_fragment_followers);
        views.add(R.layout.user_fragment_following);

        mViewPager = (ViewPager) v.findViewById(R.id.vp);

        username.setTypeface(EasyFonts.robotoRegular(getContext()));
        user_bio.setTypeface(EasyFonts.robotoRegular(getContext()));
        login.setTypeface(EasyFonts.robotoRegular(getContext()));

        if (Constants.isNetworkAvailable(getContext()))
            new getUser().execute();
        else
            Toast.makeText(getContext(), network_error, Toast.LENGTH_LONG).show();

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
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                ViewGroup layout = (ViewGroup) inflater.inflate(views.get(position), container, false);
                container.addView(layout);
                return layout;
            }
        });

        return v;
    }

    @Override
    public void doBack() {
        if (StarredFragment.USER_FRAGMENT_HAS_BEEN_ADEED) {
            StarredFragment.USER_FRAGMENT_HAS_BEEN_ADEED = false;
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Starred");
            fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.remove(UserFragment.this);
            fragmentTransaction.commit();
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAuthdUser(Context context) {
        this.context = context;
        if (Constants.isNetworkAvailable(context))
            new getAuthdUser().execute();
        else
            Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
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
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(Constants.getToken(getContext()));
            try {
                user = userService.getUser(user.getLogin());
            } catch (IOException e) {e.printStackTrace();}

            // Notify 3 fragments
            UserFragmentRepos userFragmentRepos = new UserFragmentRepos();
            userFragmentRepos.populate(user.getLogin(), getContext(), getView());
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");

            // Download and show information
            Picasso.with(getContext()).load(user.getAvatarUrl()).resize(150, 150).centerCrop().into(user_image);

            // If user's name is available show both login and username. Otherwise show only login
            if (user.getName() != null) {
                username.setText(user.getName());
                login.setText("@" + user.getLogin());
            }
            else {
                username.setText(user.getLogin());
                login.setVisibility(View.GONE);
            }
            if (user.getBio() == null || user.getBio().equals(""))
                user_bio.setVisibility(View.GONE);
            user_bio.setText(user.getBio());

            final List<String> mTitleDataList = new ArrayList<>();
            mTitleDataList.add("REPOSITORIES");
            mTitleDataList.add("FOLLOWERS");
            mTitleDataList.add("FOLLOWING");

            final List<String> mNumberDataList = new ArrayList<>();
            mNumberDataList.add(String.valueOf(user.getPublicRepos()));
            mNumberDataList.add(String.valueOf(user.getFollowers()));
            mNumberDataList.add(String.valueOf(user.getFollowing()));

            MagicIndicator magicIndicator = (MagicIndicator) v.findViewById(R.id.magic_indicator);
            CommonNavigator commonNavigator = new CommonNavigator(getContext());
            commonNavigator.setAdjustMode(true);
            commonNavigator.setAdapter(new CommonNavigatorAdapter() {
                @Override
                public int getCount() {
                    return mTitleDataList == null ? 0 : mTitleDataList.size();
                }

                @Override
                public IPagerTitleView getTitleView(Context context, final int index) {
                    CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(context);
                    View customLayout = LayoutInflater.from(context).inflate(R.layout.user_fragment_tab, null);
                    commonPagerTitleView.setContentView(customLayout);
                    final TextView user_fragment_tab_n = (TextView) customLayout.findViewById(R.id.user_fragment_tab_n);
                    final TextView user_fragment_tab_title = (TextView) customLayout.findViewById(R.id.user_fragment_tab_title);
                    user_fragment_tab_n.setText(mNumberDataList.get(index));
                    user_fragment_tab_title.setText(mTitleDataList.get(index));
                    user_fragment_tab_n.setTypeface(EasyFonts.robotoRegular(context));
                    user_fragment_tab_title.setTypeface(EasyFonts.robotoRegular(context));
                    commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {
                        @Override
                        public void onSelected(int i, int i1) {
                        }

                        @Override
                        public void onDeselected(int i, int i1) {
                        }

                        @Override
                        public void onLeave(int i, int i1, float v, boolean b) {

                        }

                        @Override
                        public void onEnter(int i, int i1, float v, boolean b) {

                        }
                    });
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
    class getAuthdUser extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            User authdUser;
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(Constants.getToken(context));
            try {
                authdUser = userService.getUser();
                setUser(authdUser);
            } catch (IOException e) {e.printStackTrace();}

            editor = sp.edit();
            if (user.getName() != null && !user.getName().equals("") && !Constants.getFullName(context).equals(user.getName())) {
                editor.putString((Constants.getFullNameKey(context)), user.getName());
            }
            else if (user.getLogin() != null && !user.getLogin().equals("") && !Constants.getUsername(context).equals(user.getLogin())) {
                editor.putString((Constants.getUserKey(context)), user.getName());
            }
            else if (user.getEmail() != null && !user.getEmail().equals("") && !Constants.getEmail(context).equals(user.getEmail())) {
                editor.putString((Constants.getEmailKey(context)), user.getEmail());
            }
            editor.commit();
            Bitmap thumbnail = new ImageSaver(context)
                    .setFileName("thumbnail.png")
                    .setDirectoryName("images")
                    .load();
            try {
                Bitmap profile_picture = Picasso.with(context).load(user.getAvatarUrl()).get();
                if (!thumbnail.sameAs(profile_picture)) {
                    new ImageSaver(context)
                            .setFileName("thumbnail.png")
                            .setDirectoryName("images")
                            .save(profile_picture);
                }
            } catch (IOException e) {e.printStackTrace();}

            return null;
        }
    }



}
