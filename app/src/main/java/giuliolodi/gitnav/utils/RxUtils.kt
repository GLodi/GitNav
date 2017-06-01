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

package giuliolodi.gitnav.utils

import android.support.v7.widget.SearchView
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by giulio on 30/05/2017.
 */

class RxUtils {

    companion object {
        fun fromSearchView(searchView: SearchView): Observable<String> {
            val subject: BehaviorSubject<String> = BehaviorSubject.create()
            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!newText?.isEmpty()!!) {
                        subject.onNext(newText)
                    }
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    subject.onNext(query)
                    return true
                }
            })
            return subject
        }
    }

}