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

package giuliolodi.gitnav.utils.contributions;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class ContributionsRequest {

    private static final String URL = "https://github.com/users/%s/contributions";

    private final Context mContext;

    public ContributionsRequest(Context context) {
        this.mContext = context;
    }

    public void launchRequest(String username, final OnContributionsRequestListener listener) {

        String url = String.format(URL, username);

        StringRequest strReq = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                ContributionsProvider provider = new ContributionsProvider();
                List<ContributionsDay> contributions = provider.getContributions(response);
                listener.onResponse(contributions);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                listener.onError(volleyError);
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(strReq);
    }

    public interface OnContributionsRequestListener {

        void onResponse(List<ContributionsDay> contributionsDay);

        void onError(VolleyError error);

    }

}
