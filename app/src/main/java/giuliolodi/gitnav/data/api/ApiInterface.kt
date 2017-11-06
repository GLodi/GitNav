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

package giuliolodi.gitnav.data.api

import giuliolodi.gitnav.data.model.RequestAccessTokenResponse
import io.reactivex.Flowable
import retrofit2.http.*

/**
 * Created by giulio on 06/11/2017.
 */
interface ApiInterface {

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("login/oauth/access_token")
    fun requestAccessToken(@Field("client_id") clientId: String,
                           @Field("client_secret") clientSecret: String,
                           @Field("code") code: String,
                           @Field("redirect_uri") redirectUri: String,
                           @Field("state") state: String): Flowable<RequestAccessTokenResponse>

}