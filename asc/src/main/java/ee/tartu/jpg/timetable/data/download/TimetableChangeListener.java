package ee.tartu.jpg.timetable.data.download;

import ee.tartu.jpg.timetable.Timetable;

public interface TimetableChangeListener {
    public void onTimetableAdded(Timetable timetable, boolean downloaded);

    public void onTimetableUpdated(Timetable timetable, boolean downloaded);

    public void onTimetableRemoved(Timetable timetable, boolean downloaded);
}
