package ee.tartu.jpg.timetable.data;

import android.database.Cursor;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class LessonPeriod extends TimetableData {

    public int period;
    public String starttime;
    public String endtime;

    public LessonPeriod(Cursor c, Timetable t) {
        super(t);
        period = c.getInt(0);
        starttime = c.getString(1);
        endtime = c.getString(2);
    }

    @Override
    public int getID() {
        return period;
    }

    @Override
    public String getName() {
        return period + " (" + starttime + " - " + endtime + ")";
    }

    @Override
    public String getShortname() {
        return getName();
    }
}
