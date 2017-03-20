package ee.tartu.jpg.timetable;

import android.database.Cursor;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.tartu.jpg.timetable.data.Classroom;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.Form;
import ee.tartu.jpg.timetable.data.LessonPeriod;
import ee.tartu.jpg.timetable.data.Subject;
import ee.tartu.jpg.timetable.data.Teacher;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;
import ee.tartu.jpg.timetable.data.upper.TimetableData;
import ee.tartu.jpg.timetable.utils.Filter;
import ee.tartu.jpg.timetable.utils.TimetableDatabaseHelper;

@SuppressWarnings("WeakerAccess")
public class Timetable implements Serializable {

    private static final String TAG = "Timetable";

    private Map<Class<? extends TimetableData>, List<? extends TimetableData>> data = new HashMap<>();

    private String id;
    public long modified;
    private String name;

    public Timetable(Cursor c) {
        this.id = c.getString(0);
        this.modified = c.getLong(1);
    }

    public Timetable(String id, long modified) {
        this.id = id;
        this.modified = modified;
    }

    public boolean isInitialised() {
        return !data.isEmpty();
    }

    public void init(TimetableDatabaseHelper db) {
        if (data.isEmpty()) {
            Log.d(TAG, "Initializing Timetables");
            data.put(Day.class, db.requestDays(this));
            data.put(LessonPeriod.class, db.requestPeriods(this));
            data.put(Form.class, db.requestForms(this));
            data.put(Teacher.class, db.requestTeachers(this));
            data.put(Subject.class, db.requestSubjects(this));
            data.put(Classroom.class, db.requestClassrooms(this));
            data.put(TimeTableSchedule.class, db.requestSchedules(this));
        }
    }

    public <E extends TimetableData> E get(int index, Class<E> c) {
        List<E> list = getAll(c);
        if (list.size() <= index)
            return null;
        return list.get(index);
    }

    public <E extends TimetableData> List<E> getAll(Class<E> c) {
        List<E> all = new ArrayList<>();
        List<E> els = getData(c);
        for (E dt : els) {
            if (dt == null)
                continue;
            all.add(c.cast(dt));
        }
        return all;

		/*
         *
		 * Things that could be NULL:
		 * c - NOPE 
		 * all - NOPE
		 * dt - possibly
		 * els - possibly
		 */
    }

    public <E extends TimetableData> List<E> getAll(Class<E> c, Comparator<? super E> comparator) {
        List<E> all = new ArrayList<>();
        List<E> els = getData(c);
        for (E dt : els) {
            if (dt == null)
                continue;
            all.add(c.cast(dt));
        }
        Collections.sort(all, comparator);
        return all;
    }

    public <E extends TimetableData> List<E> getAll(Class<E> c, Filter<? super E> filter, Comparator<? super E> comparator) {
        List<E> all = new ArrayList<>();
        List<E> els = getData(c);
        for (E dt : els) {
            if (dt == null)
                continue;
            E t = c.cast(dt);
            if (filter.accept(t)) {
                all.add(t);
            }
        }
        Collections.sort(all, comparator);
        return all;
    }

    public <E extends TimetableData> E getByID(int ID, Class<E> c) {
        List<E> ts = getSortedByIDs(new int[]{ID}, c);
        return ts.size() == 0 ? null : ts.get(0);
    }

    public <E extends TimetableData> E getByName(String name, Class<E> c) {
        List<E> els = getData(c);
        for (E dt : els) {
            if (dt == null)
                continue;
            if (dt.getName().equals(name)) {
                return c.cast(dt);
            }
        }
        return null;
    }

    public <E extends TimetableData> E getByShortName(String shortname, Class<E> c) {
        List<E> els = getData(c);
        for (E dt : els) {
            if (dt == null)
                continue;
            if (dt.getShortname().equals(shortname)) {
                return c.cast(dt);
            }
        }
        return null;
    }

    public <E extends TimetableData> int getCount(Class<E> c) {
        List<E> els = getData(c);
        if (els == null)
            return 0;
        return els.size();
    }

    public Map<Class<? extends TimetableData>, List<? extends TimetableData>> getData() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public <E extends TimetableData> List<E> getData(Class<E> cls) {
        if (!data.containsKey(cls))
            return new ArrayList<E>();
        return (List<E>) data.get(cls);
    }

    public String getId() {
        return id;
    }

    public LessonPeriod getLessonPeriod(String period, int i) {
        return getByID(i, LessonPeriod.class);
    }

    public String getName() {
        if (name == null) {
            name = parseFilenameToName(id);
        }
        return name;
    }

    public <E extends TimetableData> List<E> getSortedByIDs(int[] IDs, Class<E> c) {
        List<E> all = new ArrayList<>();
        for (int ID : IDs) {
            List<E> els = getData(c);
            for (E dt : els) {
                if (dt.getID() == ID) {
                    all.add(c.cast(dt));
                }
            }
        }
        Collections.sort(all, TimetableUtils.idComparator);
        return all;
    }

    private String parseFilenameToName(String filename) {
        if (filename.endsWith(".xml")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        String name = "";
        char[] cs = filename.toCharArray();
        char prevChar = 0;
        for (char c : cs) {
            if (prevChar != 0 && ((Character.isDigit(c) && !Character.isDigit(prevChar)) || (!Character.isDigit(c) && Character.isDigit(prevChar)))) {
                // if not the first character; and either current or previous character is a digit. Then add space.
                name += ' ';
            } else if (prevChar == 0) {
                // if the first character. Then make it upper case.
                c = Character.toUpperCase(c);
            }
            name += c;
            prevChar = c;
        }
        return name;
    }

    private int getDayIndexIn(int daysAfterToday, List<Day> dayList) {
        // Calculate today's day of week
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, daysAfterToday);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        // Match it to a schoolday
        String dayStr = "";
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayStr = "e";
                break;
            case Calendar.TUESDAY:
                dayStr = "t";
                break;
            case Calendar.WEDNESDAY:
                dayStr = "k";
                break;
            case Calendar.THURSDAY:
                dayStr = "n";
                break;
            case Calendar.FRIDAY:
                dayStr = "r";
                break;
            case Calendar.SATURDAY:
                dayStr = "l";
                break;
            case Calendar.SUNDAY:
                dayStr = "p";
                break;
        }
        for (int i = 0; i < dayList.size(); i++)
            if (dayList.get(i).getName().toLowerCase().startsWith(dayStr))
                return i;
        return -1;
    }

    public int getDayIndexIn(int daysAfterToday) {
        List<Day> dayList = getAll(Day.class);
        return getDayIndexIn(daysAfterToday, dayList);
    }

    public Day getDayIn(int daysAfterToday) {
        List<Day> dayList = getAll(Day.class);
        int dayIdx = getDayIndexIn(daysAfterToday, dayList);
        return dayIdx >= 0 ? dayList.get(dayIdx) : null;
    }

}
