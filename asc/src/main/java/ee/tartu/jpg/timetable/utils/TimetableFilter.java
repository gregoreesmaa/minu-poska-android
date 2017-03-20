package ee.tartu.jpg.timetable.utils;

import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.data.Classroom;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.Form;
import ee.tartu.jpg.timetable.data.Subject;
import ee.tartu.jpg.timetable.data.Teacher;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class TimetableFilter {

    public static final int TYPE_INVALID = 0;
    public static final int TYPE_FORM = 1;
    public static final int TYPE_TEACHER = 2;
    public static final int TYPE_CLASSROOM = 3;
    public static final int TYPE_SUBJECT = 4;
    public static final int TYPE_ALL = 5;

    public String name;
    public String shortname;
    public int type;
    private Class cls;

    public TimetableFilter(int type, String name, String shortname) {
        this.name = name;
        this.shortname = shortname;
        this.type = type;
        switch (type) {
            case TYPE_FORM:
                cls = Form.class;
                break;
            case TYPE_TEACHER:
                cls = Teacher.class;
                break;
            case TYPE_CLASSROOM:
                cls = Classroom.class;
                break;
            case TYPE_SUBJECT:
                cls = Subject.class;
                break;
            default:
                cls = null;
                break;
        }
    }

    public TimetableFilter(TimetableData dt) {
        if (dt == null) {
            type = TYPE_INVALID;
            return;
        }
        this.name = dt.getName();
        this.shortname = dt.getShortname();
        if (dt instanceof Form)
            type = TYPE_FORM;
        else if (dt instanceof Teacher)
            type = TYPE_TEACHER;
        else if (dt instanceof Classroom)
            type = TYPE_CLASSROOM;
        else if (dt instanceof Subject)
            type = TYPE_SUBJECT;
        else
            type = TYPE_ALL;
        this.cls = dt.getClass();
    }

    protected TimetableFilter() {
    }

    public Filter<TimeTableSchedule> getFilter(final Day day) {

        return new Filter<TimeTableSchedule>() {

            @Override
            public boolean accept(TimeTableSchedule t) {
                if (!t.isOnWeekdayId(day.getID()))
                    return false;
                int[] field;
                switch (type) {
                    case TYPE_FORM:
                        field = t.ClassIDs;
                        break;
                    case TYPE_TEACHER:
                        field = new int[]{t.TeacherID};
                        break;
                    case TYPE_CLASSROOM:
                        field = new int[]{t.SchoolRoomID};
                        break;
                    case TYPE_SUBJECT:
                        field = new int[]{t.SubjectGradeID};
                        break;
                    case TYPE_INVALID:
                        return true;
                    default:
                        return false;
                }
                return TimetableUtils.acceptByFilter(t.getTimetable(), field, name, cls);
            }

        };
    }

    public String getName() {
        return shortname == null ? "" : shortname;
    }

    @Override
    public String toString() {
        return "TimetableFilter{" +
                "name='" + name + '\'' +
                ", shortname='" + shortname + '\'' +
                ", type=" + type +
                ", cls=" + cls +
                '}';
    }
}
