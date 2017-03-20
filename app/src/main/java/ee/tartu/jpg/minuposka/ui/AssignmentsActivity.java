package ee.tartu.jpg.minuposka.ui;

import android.os.Bundle;
import android.os.Handler;

import java.text.SimpleDateFormat;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.StuudiumBaseActivity;
import ee.tartu.jpg.minuposka.ui.fragment.AssignmentsFragment;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Person;

/**
 * Created by gregor on 9/14/2015.
 */
public class AssignmentsActivity extends StuudiumBaseActivity {

    private static final String headerDateFormat = "dd.MM";

    private AssignmentsFragment assignmentsFragment;

    @Override
    public String title() {
        if (assignmentsFragment != null) {
            return super.title() + " (" + getString(R.string.since) + " " + new SimpleDateFormat(headerDateFormat).format(assignmentsFragment.getSince()) + ")";
        }
        return super.title();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_assignments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assignmentsFragment = (AssignmentsFragment) getFragmentManager().findFragmentById(R.id.mainContent);
        refreshTitle();
    }

    @Override
    protected void onStart() {
        super.onStart();
        long ms = System.currentTimeMillis();
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
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        assignmentsFragment.setAssignmentsOnThread(Stuudium.getUserAssignments());
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_assignments;
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
        assignmentsFragment.setAssignments(null);
    }


}
