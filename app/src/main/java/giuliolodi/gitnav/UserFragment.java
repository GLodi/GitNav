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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserFragment extends Fragment{

    @BindView(R.id.user_fragment_layout) RelativeLayout relativeLayout;
    @BindView(R.id.user_fragment_name) TextView username;
    @BindView(R.id.user_fragment_description) TextView user_bio;
    @BindView(R.id.user_fragment_image) CircleImageView user_image;
    @BindView(R.id.nts_top) NavigationTabStrip navigationTabStrip;
    @BindView(R.id.user_fragment_progress_bar) ProgressBar progressBar;
    @BindView(R.id.user_fragment_login) TextView login;

    @BindString(R.string.network_error) String network_error;

    public User user;
    private ViewPager mViewPager;
    private Context context;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user_fragment, container, false);
        ButterKnife.bind(this, v);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        sp = PreferenceManager.getDefaultSharedPreferences(context);

        mViewPager = (ViewPager) v.findViewById(R.id.vp);

        // Make sure line divider in StarredFragment appears
        StarredFragment.PREVENT_MULTPLE_SEPARATION_LINE = true;

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
                final View view = new View(getContext());
                container.addView(view);
                return view;
            }
        });

        navigationTabStrip.setTabIndex(0, true);
        navigationTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        navigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {

            }

            @Override
            public void onEndTabSelected(String title, int index) {

            }
        });
        return v;
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
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

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
