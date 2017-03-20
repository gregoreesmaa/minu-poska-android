package ee.tartu.jpg.minuposka.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.NotificationService;
import ee.tartu.jpg.minuposka.ui.base.StuudiumBaseActivity;
import ee.tartu.jpg.minuposka.ui.fragment.AssignmentsFragment;
import ee.tartu.jpg.minuposka.ui.fragment.EventsFragment;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Person;

/**
 * Created by gregor on 9/14/2015.
 */
public class EventsAndAssignmentsActivity extends StuudiumBaseActivity {

    private static final String headerDateFormat = "dd.MM";

    private EventsFragment eventsFragment;
    private AssignmentsFragment assignmentsFragment;
    private TextView assignmentsTitle;

    @Override
    protected int getLayout() {
        return R.layout.activity_events_and_assignments;
    }

    @Override
    public String title() {
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsFragment = (EventsFragment) getFragmentManager().findFragmentById(R.id.events_fragment);
        assignmentsFragment = (AssignmentsFragment) getFragmentManager().findFragmentById(R.id.assignments_fragment);
        assignmentsTitle = (TextView) findViewById(R.id.assignments_title);
        ViewCompat.setElevation(mToolbar, 0);
        refreshTitle();
        if (!DataUtils.hideDrawer(this)) {
            mToolbar.setVisibility(View.GONE);
        }
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
        if (ms - AssignmentsFragment.lastUpdateMs > 10 * 60 * 1000) {
            assignmentsFragment.vSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    assignmentsFragment.vSwipeRefreshLayout.setRefreshing(true);
                }
            });
            assignmentsFragment.onRefresh();
            AssignmentsFragment.lastUpdateMs = ms;
        }
        NotificationService.dismissGradeNotifications(this);
    }

    @Override
    public void refreshTitle() {
        if (assignmentsFragment != null) {
            assignmentsTitle.setText(getString(R.string.activity_assignments_title) + " (" + getString(R.string.since) + " " + new SimpleDateFormat(headerDateFormat).format(assignmentsFragment.getSince()) + ")");
        }
        super.refreshTitle();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        eventsFragment.setEventsOnThread(Stuudium.getUserEvents());
        assignmentsFragment.setAssignmentsOnThread(Stuudium.getUserAssignments());
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_events_and_assignments;
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
    public void onAssignmentsLoaded(Person p, DataSet<Assignment> assignments, boolean manualUpdate) {
        super.onAssignmentsLoaded(p, assignments, manualUpdate);
        if (p.equals(Stuudium.getUser().getIdentity())) {
            assignmentsFragment.setAssignments(assignments);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                assignmentsFragment.vSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onStuudiumLogout() {
        super.onStuudiumLogout();
        eventsFragment.clearEvents();
        assignmentsFragment.clearAssignments();
    }
}
