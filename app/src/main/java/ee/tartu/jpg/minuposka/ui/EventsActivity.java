package ee.tartu.jpg.minuposka.ui;

import android.os.Bundle;
import android.os.Handler;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.NotificationService;
import ee.tartu.jpg.minuposka.ui.base.StuudiumBaseActivity;
import ee.tartu.jpg.minuposka.ui.fragment.EventsFragment;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Person;

/**
 * Created by gregor on 9/14/2015.
 */
public class EventsActivity extends StuudiumBaseActivity {

    private EventsFragment eventsFragment;

    @Override
    protected int getLayout() {
        return R.layout.activity_events;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsFragment = (EventsFragment) getFragmentManager().findFragmentById(R.id.mainContent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        long ms = System.currentTimeMillis();
        if (ms - EventsFragment.lastUpdateMs > 10 * 60 * 1000) {
            eventsFragment.vSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    eventsFragment.vSwipeRefreshLayout.setRefreshing(true);
                }
            });
            eventsFragment.onRefresh();
            EventsFragment.lastUpdateMs = ms;
        }
        NotificationService.dismissGradeNotifications(this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        eventsFragment.setEventsOnThread(Stuudium.getUserEvents());
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_events;
    }

    @Override
    public void onEventsLoaded(Person p, DataSet<Event> events) {
        super.onEventsLoaded(p, events);
        if (p.equals(Stuudium.getUser().getIdentity())) {
            eventsFragment.setEvents(events);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                eventsFragment.vSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onStuudiumLogout() {
        super.onStuudiumLogout();
        eventsFragment.setEvents(null);
    }
}
