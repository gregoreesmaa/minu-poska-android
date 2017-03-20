package ee.tartu.jpg.timetable.data;

import android.database.Cursor;

import java.util.Calendar;
import java.util.List;

import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.data.upper.TimetableData;

public class TimeTableSchedule extends TimetableData implements Cloneable {

    private int DayID;
    private int Period;
    public int SchoolRoomID;
    public int SubjectGradeID;
    public int[] ClassIDs;
    public int TeacherID;

    public TimeTableSchedule(Cursor c, Timetable t) {
        super(t);
        DayID = c.getInt(0);
        Period = c.getInt(1);
        SchoolRoomID = c.getInt(2);
        TeacherID = c.getInt(3);
        SubjectGradeID = c.getInt(4);

        String[] ClassIDsstr = c.getString(5).split(",");
        ClassIDs = new int[ClassIDsstr.length];
        for (int i = 0; i < ClassIDsstr.length; i++) {
            ClassIDs[i] = Integer.parseInt(ClassIDsstr[i].trim());
        }
    }

    public int getLessonNumber() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return p.period;
        }
        return 0;
    }

    public String getTeacherName() {
        Teacher t = getTeacher();
        if (t != null) {
            return t.getName().trim().replace("  ", " ");
        }
        return "";
    }

    public String getClassroomNumber() {
        Classroom c = getClassroom();
        if (c != null) {
            return c.getName();
        }
        return "";
    }

    public String getSubjectName() {
        Subject s = getSubject();
        if (s != null) {
            return s.getName();
        }
        return "";
    }

    public String getSubjectShortname() {
        Subject s = getSubject();
        if (s != null) {
            return s.getShortname();
        }
        return "";
    }

    public String getIncludedClasses(boolean allcompact) {
        List<Form> cs = getClazzes();
        if (cs != null) {
            String classes = "";
            int allcount = getTimetable().getCount(Form.class);
            if (allcompact && allcount == cs.size()) {
                classes = "kõik klassid";
            } else {
                String lastclassnumber = "";
                for (Form c : cs) {
                    String trimmed = c.getShortname().replace(".", "").trim();
                    String classnumber = "";
                    String letter = "";
                    char[] chars = trimmed.toCharArray();
                    for (char ch : chars) {
                        if (letter.length() == 0 && Character.isDigit(ch)) {
                            classnumber += ch;
                        } else {
                            letter += ch;
                        }
                    }
                    if (lastclassnumber.equals(classnumber)) {
                        classes += letter;
                    } else {
                        lastclassnumber = classnumber;
                        if (classes.length() != 0)
                            classes += ", ";
                        classes += classnumber + letter;
                    }
                }
            }
            return classes;
        }
        return "";
    }

    public boolean isHappening() {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();
        long start = getStartTime();
        long end = getEndTime();
        // HAVE TO CONSIDER WHICH DAY IT IS (AND MONTH AND YEAR)
        return false;
    }

    public Calendar getStartDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, (int) getStartTime());
        return c;
    }

    public Calendar getEndDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getEndTime());
        return c;
    }

    public long getStartTime() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return HHMMtoMS(p.starttime);
        }
        return 0;
    }

    public String getStartTimeStr() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return p.starttime;
        }
        return "";
    }

    private long getEndTime() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return HHMMtoMS(p.endtime);
        }
        return 0;
    }

    public String getEndTimeStr() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return p.endtime;
        }
        return "";
    }

    public int getStartHour() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return HHMMtoVAL(p.starttime, 0);
        }
        return 0;
    }

    public int getEndHour() {
        LessonPeriod p = getPeriod();
        if (p != null) {
            return HHMMtoVAL(p.endtime, 0);
        }
        return 0;
    }

    public int getTeacherColor() {
        Teacher t = getTeacher();
        if (t != null) {
            return t.color;
        }
        return 0xFFFFFF;
    }

    private long HHMMtoMS(String time) {
        String[] data = time.split(":");
        int hours = Integer.parseInt(data[0]);
        int mins = Integer.parseInt(data[1]);
        return hours * 3600000L + mins * 60000L;
    }

    private int HHMMtoVAL(String time, int val) {
        String[] data = time.split(":");
        return Integer.parseInt(data[val]);
    }

    public boolean isOnWeekdayId(int dayId) {
        return this.DayID == dayId;
    }

    public Day getDay() {
        return getTimetable().getByID(DayID, Day.class);
    }

    public Teacher getTeacher() {
        return getTimetable().getByID(TeacherID, Teacher.class);
    }

    private Classroom getClassroom() {
        return getTimetable().getByID(SchoolRoomID, Classroom.class);
    }

    private Subject getSubject() {
        return getTimetable().getByID(SubjectGradeID, Subject.class);
    }

    public LessonPeriod getPeriod() {
        return getTimetable().getByID(Period, LessonPeriod.class);
    }

    public List<Form> getClazzes() {
        return getTimetable().getSortedByIDs(ClassIDs, Form.class);
    }

    public int getClazzCount() {
        return ClassIDs.length;
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getShortname() {
        return "";
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
