package ee.tartu.jpg.minuposka.ui.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import ee.tartu.jpg.minuposka.ui.MyScheduleActivity;
import ee.tartu.jpg.minuposka.ui.fragment.ScheduleDayFragment;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;

/**
 * Adapter for Timetable/Schedule days.
 */
public class ScheduleDayPagerAdapter extends FragmentStatePagerAdapter {

    private List<ScheduleDayFragment> scheduleDayFragments = new ArrayList<>();
    private Activity activity;

    public ScheduleDayPagerAdapter(Activity activity, FragmentManager fm) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        return scheduleDayFragments.get(position);
    }

    @Override
    public int getCount() {
        return scheduleDayFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return scheduleDayFragments.get(position).getDayTitle(activity);
    }

    public void addFragment(ScheduleDayFragment scheduleDayFragment) {
        scheduleDayFragments.add(scheduleDayFragment);
        notifyDataSetChanged();
    }

    @Override
    public float getPageWidth(int position) {
        float f = 0;
        if (DataUtils.isTablet(activity)) {
            if (activity instanceof MyScheduleActivity) {
                f = 0.225F;
            } else {
                f = 0.2917F;
            }
        } else {
            f = 1F;
        }
        return f;
    }
}