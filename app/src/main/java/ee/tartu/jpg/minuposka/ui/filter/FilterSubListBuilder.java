package ee.tartu.jpg.minuposka.ui.filter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import java.util.List;

import ee.tartu.jpg.minuposka.ui.base.TimeTableBaseActivity;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.data.Teacher;
import ee.tartu.jpg.timetable.data.upper.TimetableData;
import ee.tartu.jpg.timetable.utils.TimetableFilter;

/**
 * Builds second order Filter menu. For example, displays all teachers' filters.
 */
class FilterSubListBuilder extends Builder {

    private TimeTableBaseActivity activity;
    private Class<TimetableData> dt;
    public String name;

    @SuppressWarnings("unchecked")
    public FilterSubListBuilder(TimeTableBaseActivity activity, String name, Class<? extends TimetableData> dt) {
        super(activity);
        this.activity = activity;
        this.name = name;
        this.dt = (Class<TimetableData>) dt;
    }

    @Override
    public AlertDialog show() {

        setTitle(this.name);

        Timetable tt = activity.getTimetable();
        if (tt != null) {
            final List<? extends TimetableData> elements = tt.getAll(dt, TimetableUtils.nameComparator);
            final CharSequence[] titles = new CharSequence[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                if (!dt.equals(Teacher.class))
                    titles[i] = TextUtils.translateFromEstonian(activity, elements.get(i).getName());
                else titles[i] = elements.get(i).getName();
            }
            setItems(titles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    activity.setTimetableFilter(new TimetableFilter(elements.get(i)), true, false);
                }
            });
        }
        return super.show();
    }
}
