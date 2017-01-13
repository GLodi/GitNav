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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import butterknife.BindString;

public class IssueListActivity extends BaseDrawerActivity {

    @BindString(R.string.issues) String issuesString;

    private Intent intent;
    private String owner, repo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.issuelist_activity, frameLayout);

        intent = getIntent();
        owner = intent.getExtras().getString("owner");
        repo = intent.getExtras().getString("repo");

        getSupportActionBar().setTitle(issuesString);
        getSupportActionBar().setSubtitle(owner + "/" + repo);

    }
}
