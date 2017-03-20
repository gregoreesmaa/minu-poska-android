package ee.tartu.jpg.minuposka.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.LinearLayout;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.TimeTableBaseActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.timetable.Timetable;

/**
 * Created by gregor on 9/14/2015.
 */
public class SchedulesActivity extends TimeTableBaseActivity {

    private TabLayout vTabLayout;

    private boolean setCurrentDay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vViewPager.setPageMargin(0);
        if (!DataUtils.isTablet(this)) {
            vTabLayout = (TabLayout) findViewById(R.id.tablayout);
        } else {
            vViewPager.setOffscreenPageLimit(6);
            ViewCompat.setElevation(mToolbar, 0);
        }
        vViewPager.setPageMargin(-1);

        // To refresh timetable
        createViewPagerAdapter();
        invalidateOptionsMenu();
    }

    @Override
    public void onStart() {
        super.onStart();
        setCurrentDay = false;
    }

    @Override
    public void onViewPagerCreated() {
        PagerAdapter adapter = vViewPager.getAdapter();
        if (adapter == null)
            return;
        int idx = getTimetable().getDayIndexIn(0);
        if (!DataUtils.isTablet(this)) {
            if (vTabLayout != null) {
                vTabLayout.setupWithViewPager(vViewPager);
                if (idx != -1 && !setCurrentDay && vTabLayout.getTabCount() != 0) {
                    TabLayout.Tab t = vTabLayout.getTabAt(idx);
                    if (t != null)
                        t.select();
                    setCurrentDay = true;
                }
            }
        } else {
            if (idx != -1 && !setCurrentDay && adapter.getCount() != 0) {
                vViewPager.setCurrentItem(idx);
                setCurrentDay = true;
            }
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_schedules;
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_schedules;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View tabLayout = findViewById(R.id.tablayout);
        if (tabLayout != null) {
            tabLayout.setAlpha(0);
            tabLayout.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    @Override
    protected void preFinish() {
        View tabLayout = findViewById(R.id.tablayout);
        if (tabLayout != null)
            tabLayout.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        super.preFinish();
    }

    @Override
    public void setTimetable(Timetable timetable, boolean save, boolean initial) {
        super.setTimetable(timetable, save, initial);
        if (!DataUtils.isTablet(this) && vTabLayout != null && vViewPager != null && vViewPager.getAdapter() != null)
            vTabLayout.setupWithViewPager(vViewPager);
    }

    @Override
    public void addTimesToLayout() {
        if (DataUtils.isTablet(this)) {
            LinearLayout timeView = (LinearLayout) getLayoutInflater().inflate(R.layout.item_schedule_time_day, null);
            vTimes.addView(timeView);
        }
        super.addTimesToLayout();
    }
}
