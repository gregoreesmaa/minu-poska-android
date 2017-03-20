package ee.tartu.jpg.timetable.data;

import android.database.Cursor;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class Teacher extends TimetableData {

    private int id;
    private String name;
    private String shortname;
    public int color;

    public Teacher(Cursor c, Timetable t) {
        super(t);
        id = c.getInt(0);
        name = capitalizeAll(c.getString(1).replace("  ", " "));
        shortname = capitalizeAll(c.getString(2).replace("  ", " "));
        color = c.getInt(3);
    }

    @Override
    public int getID() {
        return id;
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
