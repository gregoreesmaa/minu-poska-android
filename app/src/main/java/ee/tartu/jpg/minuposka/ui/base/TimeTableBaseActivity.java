package ee.tartu.jpg.minuposka.ui.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ee.tartu.jpg.minuposka.PoskaApplication;
import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.DataUpdateService;
import ee.tartu.jpg.minuposka.service.trigger.LessonNotificationScheduler;
import ee.tartu.jpg.minuposka.ui.adapter.ScheduleDayPagerAdapter;
import ee.tartu.jpg.minuposka.ui.filter.FilterListBuilder;
import ee.tartu.jpg.minuposka.ui.filter.TimetableListBuilder;
import ee.tartu.jpg.minuposka.ui.fragment.ScheduleDayFragment;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.LessonPeriod;
import ee.tartu.jpg.timetable.data.download.TimetableChangeListener;
import ee.tartu.jpg.timetable.utils.TimetableFilter;

/**
 * Provides Timetable methods along with Stuudium and other useful methods, also a drawer.
 */
public abstract class TimeTableBaseActivity extends StuudiumBaseActivity implements TimetableChangeListener {

    private static final String TAG = "TimeTableBaseActivity";

    private static SimpleDateFormat infoDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

    private SparseIntArray lessonNoMap = new SparseIntArray();
    protected ViewPager vViewPager;
    protected LinearLayout vTimes;
    private TextView vTimetableModifiedLabel;
    private TextView vTimetableCheckedLabel;
    private TextView vTimetableModified;

    private TextView vTimetableChecked;
    private Timetable timetable;

    private TimetableFilter timetableFilter;

    private boolean isTimetableSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vViewPager = (ViewPager) findViewById(R.id.viewpager);
        vTimes = (LinearLayout) findViewById(R.id.times_layout);
        vTimetableModifiedLabel = (TextView) findViewById(R.id.timetable_modify_date_label);
        vTimetableCheckedLabel = (TextView) findViewById(R.id.timetable_check_date_label);
        vTimetableModified = (TextView) findViewById(R.id.timetable_modify_date);
        vTimetableChecked = (TextView) findViewById(R.id.timetable_check_date);

        vTimetableModifiedLabel.setAlpha(0.33F);
        vTimetableCheckedLabel.setAlpha(0.33F);
        vTimetableModified.setAlpha(0.33F);
        vTimetableChecked.setAlpha(0.33F);

        TimetableUtils.attach(this);

        String selectedTtFilename = DataUtils.getTimetableSelection(this);
        Timetable selectedTt = PoskaApplication.timetables.getPeriod(selectedTtFilename);
        if (selectedTt == null) {
            if (!isTimetableSet) {
                for (String filename : PoskaApplication.timetables.timetables.keySet()) {
                    Timetable tt = PoskaApplication.timetables.getPeriod(filename);
                    setTimetable(tt, true, true);
                    isTimetableSet = tt != null;
                    break;
                }
            }
        } else {
            setTimetable(selectedTt, true, true);
            isTimetableSet = true;
        }
        if (!isTimetableSet) {
            DataUtils.setTimetableSelection(this, null);
        }
        TimetableFilter tf = DataUtils.getTimetableFilter(this);
        if (tf != null) setTimetableFilter(tf, false, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (timetable == null) {
            DataUpdateService.startTimetablesUpdate(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TimetableUtils.detach(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetable, menu);

        if (timetable != null)
            menu.findItem(R.id.action_timetableselector).setTitle(TextUtils.translateFromEstonian(this, timetable.getName()));
        if (timetableFilter != null) {
            String name = timetableFilter.getName();
            switch (timetableFilter.type) {
                case TimetableFilter.TYPE_ALL:
                    name = getString(R.string.filter_all);
                    break;
                case TimetableFilter.TYPE_SUBJECT:
                    name = TextUtils.translateFromEstonian(this, name);
                    break;
            }
            menu.findItem(R.id.action_filterselector).setTitle(name);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_timetableselector) {
            Log.i(TAG, "Timetable selected");
            TimetableListBuilder builder = new TimetableListBuilder(this);
            builder.show();
            return true;
        } else if (id == R.id.action_filterselector) {
            if (timetable != null) {
                Log.i(TAG, "Filter selected");
                FilterListBuilder builder = new FilterListBuilder(this);
                builder.show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTimetableFilter(TimetableFilter timetableFilter, boolean save, boolean initial) {
        Log.i(TAG, "Setting timetable filter");
        if (timetableFilter == null)
            return;
        this.timetableFilter = timetableFilter;

        if (!initial) {
            // Update menu text
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    createViewPagerAdapter();
                    invalidateOptionsMenu();
                }
            });
        }

        // Store selected filter
        if (save)
            DataUtils.setTimetableFilter(getApplicationContext(), timetableFilter);
    }

    public void setTimetable(final Timetable timetable, boolean save, final boolean initial) {
        Log.i(TAG, "Setting timetable");
        if (timetable == null)
            return;
        // Initialize this timetable
        timetable.init(PoskaApplication.timetables.db);

        this.timetable = timetable;
        resetTimesView();

        // Update menu text and last modified and checked times
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!initial) {
                    createViewPagerAdapter();
                    invalidateOptionsMenu();
                }
                vTimetableModified.setText(infoDateFormat.format(new Date(timetable.modified * 1000)));
                vTimetableChecked.setText(infoDateFormat.format(new Date(PoskaApplication.timetables.lastcheckSystem)));
            }
        });

        // Store selected timetable
        if (save)
            DataUtils.setTimetableSelection(this, timetable.getId());

        // Update possible lesson notification
        LessonNotificationScheduler.updateBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onTimetableAdded(Timetable timetable, boolean downloaded) {
        Log.i(TAG, "Timetable added");
        String selectedTtFilename = DataUtils.getTimetableSelection(this);
        if (selectedTtFilename == null) {
            if (!isTimetableSet) {
                setTimetable(timetable, true, false);
                isTimetableSet = true;
            }
        } else if (timetable.getId().equals(selectedTtFilename)) {
            setTimetable(timetable, true, false);
            isTimetableSet = true;
        }
    }

    @Override
    public void onTimetableUpdated(Timetable timetable, boolean downloaded) {
        Log.i(TAG, "Timetable updated");
    }

    @Override
    public void onTimetableRemoved(Timetable timetable, boolean downloaded) {
        Log.i(TAG, "Timetable removed");
        String selectedTtFilename = DataUtils.getTimetableSelection(this);
        if (selectedTtFilename != null && timetable.getId().equals(selectedTtFilename)) {
            // TODO Remove timetable when selected
        }
    }

    protected void createViewPagerAdapter() {
        if (timetable == null)
            return;
        ScheduleDayPagerAdapter adapter = new ScheduleDayPagerAdapter(this, getFragmentManager());
        List<Day> days = timetable.getAll(Day.class);
        for (Day day : days) {
            adapter.addFragment(ScheduleDayFragment.newInstance(day));
        }
        vViewPager.setAdapter(adapter);
        onViewPagerCreated();
    }

    protected abstract void onViewPagerCreated();

    private void resetTimesView() {
        Log.i(TAG, "Reseting schedule times view");
        lessonNoMap.clear();
        vTimes.removeAllViews();
        addTimesToLayout();
    }

    protected void addTimesToLayout() {
        Log.i(TAG, "Adding period times to layout");
        List<LessonPeriod> periods = timetable.getAll(LessonPeriod.class);
        for (int i = 0; i < periods.size(); i++) {
            LessonPeriod lessonPeriod = periods.get(i);
            lessonNoMap.put(lessonPeriod.period, i);

            LinearLayout timeView = (LinearLayout) getLayoutInflater().inflate(R.layout.item_schedule_time, null);
            TextView scheduleNumberView = (TextView) timeView.findViewById(R.id.scheduleNumberView);
            TextView scheduleStartTimeView = (TextView) timeView.findViewById(R.id.scheduleStartTimeView);
            TextView scheduleEndTimeView = (TextView) timeView.findViewById(R.id.scheduleEndTimeView);

            scheduleNumberView.setText(Integer.toString(lessonPeriod.period));
            scheduleStartTimeView.setText(lessonPeriod.starttime);
            scheduleEndTimeView.setText(lessonPeriod.endtime);

            vTimes.addView(timeView);
        }
    }

    public Timetable getTimetable() {
        return timetable;
    }

    public boolean hasTimetable() {
        return timetable != null;
    }

    public TimetableFilter getTimetableFilter() {
        return timetableFilter;
    }

    public int getListRow(int lessonPeriod) {
        int row = lessonNoMap.get(lessonPeriod, -1);
        if (row < 0) {
            Log.e(TAG, "Failed to get list row for: " + lessonPeriod);
            return 0;
        } else
            return row;
    }

}