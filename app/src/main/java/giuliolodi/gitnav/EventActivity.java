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

package giuliolodi.gitnav;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import giuliolodi.gitnav.Adapters.EventAdapter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventActivity extends BaseDrawerActivity {

    @BindView(R.id.event_activity_progress_bar) ProgressBar progressBar;
    @BindView(R.id.event_activity_rv) RecyclerView recyclerView;
    @BindView(R.id.event_activity_no_events) TextView noEvents;

    @BindString(R.string.events) String eventString;
    @BindString(R.string.network_error) String network_error;

    private EventService eventService;
    private List<Event> t, eventList = new ArrayList<>();
    private EventAdapter eventAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Observable<List<Event>> observable;
    private Observer<List<Event>> observer;
    private Subscription subscription;

    private int DOWNLOAD_PAGE_N = 1;
    private int ITEMS_PER_PAGE = 10;
    private boolean LOADING = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.event_activity, frameLayout);

        ButterKnife.bind(this);

        getSupportActionBar().setTitle(eventString);

        eventAdapter = new EventAdapter(EventActivity.this, eventList);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /*
            Note: the following line prevents the recycler view from recycling basically all views.
            This is a terrible idea. I include it, because I've noticed that those events whose type hasn't been implemented
            yet, are bound to wrong events. Like a theoretical FollowEvent in which a WatchEvent description appears.
            I suspect that when (and if) I implement all types of events, this should disappear.
         */
        recyclerView.getRecycledViewPool().setMaxRecycledViews(1, 0);

        recyclerView.setAdapter(eventAdapter);

        progressBar.setVisibility(View.VISIBLE);

        observable = Observable.create(new Observable.OnSubscribe<List<Event>>() {
            @Override
            public void call(Subscriber<? super List<Event>> subscriber) {
                eventService = new EventService();
                eventService.getClient().setOAuth2Token(Constants.getToken(getApplicationContext()));
                t = new ArrayList<>(eventService.pageUserReceivedEvents(Constants.getUsername(getApplicationContext()), false, DOWNLOAD_PAGE_N, ITEMS_PER_PAGE).next());
                subscriber.onNext(t);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<List<Event>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                if (eventList.isEmpty() && events.isEmpty()) {
                    noEvents.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
                    noEvents.setVisibility(View.VISIBLE);
                    subscription.unsubscribe();
                } else if (eventList.isEmpty() && events != null && !events.isEmpty()) {
                    eventList.addAll(events);
                    eventAdapter.notifyDataSetChanged();
                    LOADING = false;
                } else if (events != null && !events.isEmpty()) {
                    eventList.remove(eventList.lastIndexOf(null));
                    eventList.addAll(events);
                    eventAdapter.notifyItemChanged(eventList.size() - 1);
                    LOADING = false;
                } else {
                    eventList.remove(eventList.lastIndexOf(null));
                    eventAdapter.notifyDataSetChanged();
                    subscription.unsubscribe();
                }
            }
        };

        setupOnScrollListener();

        if (Constants.isNetworkAvailable(getApplicationContext()))
            subscription = observable.subscribe(observer);
        else
            Toasty.warning(getApplicationContext(), network_error, Toast.LENGTH_LONG).show();

    }

    private void setupOnScrollListener() {

        RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (LOADING)
                    return;
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    LOADING = true;
                    DOWNLOAD_PAGE_N += 1;
                    eventList.add(null);
                    eventAdapter.notifyItemInserted(eventList.size() - 1);
                    subscription = observable.subscribe(observer);
                }
            }
        };

        recyclerView.setOnScrollListener(mScrollListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

}
