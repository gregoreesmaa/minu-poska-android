package ee.tartu.jpg.timetable.data;

import android.database.Cursor;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class Day extends TimetableData {

    private int day;
    private String name;
    private String shortname;

    @SuppressWarnings("unused")
    public Day() {
        // Required for serialization
    }

    public Day(Cursor c, Timetable t) {
        super(t);
        day = c.getInt(0);
        name = c.getString(1);
        shortname = c.getString(2);
    }

    @Override
    public int getID() {
        return day;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortname() {
        return shortname;
    }
}
