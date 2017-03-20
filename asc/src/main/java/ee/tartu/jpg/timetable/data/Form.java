package ee.tartu.jpg.timetable.data;

import android.database.Cursor;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class Form extends TimetableData {

    private int id;
    private String name;
    private String shortname;
    private String teacherid;

    public Form(Cursor c, Timetable t) {
        super(t);
        id = c.getInt(0);
        name = c.getString(1);
        shortname = c.getString(2);
        teacherid = c.getString(3);
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
