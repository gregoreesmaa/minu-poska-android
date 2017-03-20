package ee.tartu.jpg.timetable.data.upper;

import java.io.Serializable;

import ee.tartu.jpg.timetable.Timetable;

public abstract class TimetableData implements Serializable {

    private Timetable timetable;

    protected TimetableData(Timetable timetable) {
        this.timetable = timetable;
    }

    protected TimetableData() {
    }

    protected String capitalize(String s) {
        if (s.length() == 0)
            return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    protected String capitalizeAll(String s) {
        if (s.length() == 0)
            return "";
        String capitalized = "";
        char[] cs = s.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            if (i == 0 || cs[i - 1] == ' ') {
                c = Character.toUpperCase(c);
            }
            capitalized += c;
        }
        return capitalized;
    }

    public abstract int getID();

    public abstract String getName();

    public abstract String getShortname();

    public Timetable getTimetable() {
        return timetable;
    }

    public boolean nameIsSame(TimetableData dt) {
        if (dt == null)
            return false;
        return nameIsSame(dt.getName());
    }

    public boolean nameIsSame(String s) {
        if (s == null)
            return false;
        String str1 = getName();
        String str2 = s;
        if (str1 == null || str2 == null)
            return false;
        str1 = str1.replace(".", "").replace(",", "").replace(" ", "");
        str2 = str2.replace(".", "").replace(",", "").replace(" ", "");
        return str1.equalsIgnoreCase(str2);
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    @Override
    public int hashCode() {
        return (getID() + "_" + getTimetable().getId()).hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
