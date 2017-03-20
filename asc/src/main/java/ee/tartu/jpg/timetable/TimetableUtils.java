package ee.tartu.jpg.timetable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import ee.tartu.jpg.timetable.data.Teacher;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;
import ee.tartu.jpg.timetable.data.download.TimetableChangeListener;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class TimetableUtils {

    public static final Comparator<? super TimeTableSchedule> classroomComparator = new Comparator<TimeTableSchedule>() {

        @Override
        public int compare(TimeTableSchedule arg0, TimeTableSchedule arg1) {
            String id0 = arg0.getClassroomNumber();
            String id1 = arg1.getClassroomNumber();
            return id1.compareTo(id0);
        }
    };
    public static final Comparator<? super TimeTableSchedule> lessonNumberComparator = new Comparator<TimeTableSchedule>() {

        @Override
        public int compare(TimeTableSchedule arg0, TimeTableSchedule arg1) {
            int id0 = arg0.getLessonNumber();
            int id1 = arg1.getLessonNumber();
            return id1 > id0 ? -1 : ((id1 < id0) ? 1 : 0);
        }
    };
    public static final Comparator<? super TimeTableSchedule> lessonStartTimeComparator = new Comparator<TimeTableSchedule>() {

        @Override
        public int compare(TimeTableSchedule arg0, TimeTableSchedule arg1) {
            long id0 = arg0.getStartTime();
            long id1 = arg1.getStartTime();
            return id1 > id0 ? -1 : ((id1 < id0) ? 1 : 0);
        }
    };
    public static final Comparator<? super TimetableData> idComparator = new Comparator<TimetableData>() {

        @Override
        public int compare(TimetableData arg0, TimetableData arg1) {
            int i0 = arg0.getID();
            int i1 = arg1.getID();
            if (i0 == i1) {
                return 0;
            }
            return i0 < i1 ? -1 : (i0 > i1 ? 1 : 0);
        }
    };
    public static final Comparator<? super TimetableData> nameComparator = new Comparator<TimetableData>() {

        @Override
        public int compare(TimetableData arg0, TimetableData arg1) {
            String id0 = arg0.getName();
            String id1 = arg1.getName();
            if (arg0 instanceof Teacher && arg1 instanceof Teacher) {
                String lastName0 = id0.substring(id0.indexOf(" ") + 1);
                String lastName1 = id1.substring(id1.indexOf(" ") + 1);
                return lastName0.compareTo(lastName1);
            }
            return id0.compareTo(id1);
        }
    };
    public static Set<TimetableChangeListener> timetableListeners = new HashSet<TimetableChangeListener>();

    public static void attach(TimetableChangeListener listener) {
        timetableListeners.add(listener);
    }

    public static void detach(TimetableChangeListener listener) {
        timetableListeners.remove(listener);
    }

    public static boolean containsID(String field, String id) {
        String[] ss = field.split(",");
        for (String s : ss) {
            if (s.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsIDof(String field, TimetableData dt) {
        String[] ss = field.split(",");
        for (String s : ss) {
            if (s.equals(dt.getID())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSameTypeString(Timetable tt, int[] ids, String s, Class<? extends TimetableData> c) {
        for (int id : ids) {
            TimetableData check = tt.getByID(id, c);
            if (check != null && check.nameIsSame(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean acceptByFilter(Timetable tt, int[] ids, String name, Class cls) {
        for (int id : ids) {
            TimetableData check = tt.getByID(id, cls);
            if (check != null && check.nameIsSame(name)) {
                return true;
            }
        }
        return false;
    }
}
