package ee.tartu.jpg.minuposka.ui;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.TimeTableBaseActivity;
import ee.tartu.jpg.minuposka.ui.filter.PersonalizedFilter;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;

/**
 * Created by gregor on 9/14/2015.
 */
public class MyScheduleActivity extends TimeTableBaseActivity {

    private static final String TAG = "MyScheduleActivity";

    private static long lastJournalUpdateMs;

    private boolean setCurrentDay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vViewPager.setOffscreenPageLimit(6);
        vViewPager.setPageMargin(0);
        if (Stuudium.hasUser())
            setTimetableFilter(new PersonalizedFilter(Stuudium.getUser().getIdentityId()), false, true);
        ViewCompat.setElevation(mToolbar, 0);
        vViewPager.setPageMargin(-1);

        // To refresh timetable
        createViewPagerAdapter();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        long ms = System.currentTimeMillis();
        if (ms - lastJournalUpdateMs > 10 * 60 * 60 * 1000) {
            onRefreshJournals();
            lastJournalUpdateMs = ms;
        }
        setCurrentDay = false;
    }

    private void onRefreshJournals() {
        if (Stuudium.hasUser()) {
            if (Stuudium.isLoginExpired()) {
                Stuudium.setLoginData(null);
            } else {
                User u = Stuudium.getUser();
                if (u != null) {
                    for (Person p : u.getStudents()) {
                        if (p == null)
                            continue;
                        Log.d(TAG, "Requesting journals...");
                        p.requestJournals();
                    }
                }
            }
        }
    }

    @Override
    public void onViewPagerCreated() {
        PagerAdapter adapter = vViewPager.getAdapter();
        if (adapter == null)
            return;
        if (!setCurrentDay) {
            int idx = getTimetable().getDayIndexIn(0);
            if (idx != -1 && adapter.getCount() != 0) {
                vViewPager.setCurrentItem(idx);
                setCurrentDay = true;
            }
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_my_schedule;
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_my_schedule;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean b = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_filterselector).setVisible(false);
        return b;
    }

    @Override
    public void onUserDataLoaded(User u, boolean manualUpdate) {
        super.onUserDataLoaded(u, manualUpdate);
        setTimetableFilter(new PersonalizedFilter(u.getIdentityId()), false, false);
    }

    @Override
    public void onJournalsLoaded(Person p, DataSet<Journal> journals) {
        super.onJournalsLoaded(p, journals);
        setTimetableFilter(new PersonalizedFilter(Stuudium.getUser().getIdentityId()), false, false);
    }

    @Override
    public void addTimesToLayout() {
        LinearLayout timeView = (LinearLayout) getLayoutInflater().inflate(R.layout.item_schedule_time_day, null);
        vTimes.addView(timeView);
        super.addTimesToLayout();
    }
}
