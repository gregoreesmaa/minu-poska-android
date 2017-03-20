package ee.tartu.jpg.minuposka.ui.filter;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import java.util.ArrayList;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.TimeTableBaseActivity;
import ee.tartu.jpg.timetable.data.Classroom;
import ee.tartu.jpg.timetable.data.Form;
import ee.tartu.jpg.timetable.data.Subject;
import ee.tartu.jpg.timetable.data.Teacher;
import ee.tartu.jpg.timetable.utils.TimetableFilter;

/**
 * Builds first order filter menu. Allows to choose between Form, Teacher, Classroom and Subject filtering.
 */
public class FilterListBuilder extends Builder {

    public FilterListBuilder(final TimeTableBaseActivity activity) {
        super(activity);
        setTitle(activity.getString(R.string.select_filter_title));

        final ArrayList<Object> filters = new ArrayList<Object>();
        filters.add(new FilterSubListBuilder(activity, activity.getString(R.string.filter_form), Form.class));
        filters.add(new FilterSubListBuilder(activity, activity.getString(R.string.filter_teacher), Teacher.class));
        filters.add(new FilterSubListBuilder(activity, activity.getString(R.string.filter_classroom), Classroom.class));
        filters.add(new FilterSubListBuilder(activity, activity.getString(R.string.filter_subject), Subject.class));
        //filters.add(activity.getString(R.string.filter_all)); removed all filtering because unnecessary
        final CharSequence[] titles = new CharSequence[filters.size()];
        for (int i = 0; i < titles.length; i++) {
            Object o = filters.get(i);
            if (o instanceof String) {
                titles[i] = (String) o;
            } else if (o instanceof FilterSubListBuilder) {
                titles[i] = ((FilterSubListBuilder) o).name;
            }
        }
        setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Object o = filters.get(i);
                if (o instanceof FilterSubListBuilder) {
                    ((FilterSubListBuilder) o).show();
                } else if (o instanceof String) {
                    activity.setTimetableFilter(new TimetableFilter(null), true, true);
                }
            }
        });
    }
}
