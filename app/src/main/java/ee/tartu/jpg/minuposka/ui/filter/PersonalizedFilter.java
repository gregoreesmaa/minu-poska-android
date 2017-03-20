package ee.tartu.jpg.minuposka.ui.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.data.Form;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;
import ee.tartu.jpg.timetable.utils.Filter;
import ee.tartu.jpg.timetable.utils.TimetableFilter;

/**
 * Provides personalised timetable filter, based on Stuudium journals and some clever matching.
 */
public class PersonalizedFilter extends TimetableFilter {
    private static final long serialVersionUID = 2073519482440898664L;

    private String personId;

    public PersonalizedFilter(String personId) {
        this.personId = personId;
    }

    private Person getPerson() {
        return Stuudium.getPerson(personId);
    }

    @Override
    public Filter<TimeTableSchedule> getFilter(final Day day) {
        return new Filter<TimeTableSchedule>() {

            @Override
            public boolean accept(TimeTableSchedule t) {
                if (!t.isOnWeekdayId(day.getID()))
                    return false;
                Person p = getPerson();
                if (p == null) {
                    return false;
                }
                if (p.isStudent()) {
                    if (acceptStudent(p, t)) {
                        return true;
                    }
                } else if (p.isTeacher()) {
                    if (acceptTeacher(p, t)) {
                        return true;
                    }
                }
                return false;
            }

            public boolean acceptTeacher(Person p, TimeTableSchedule t) {
                return t.getTeacher().nameIsSame(p.getFullName()); // WHY NOT?
            }

            public boolean acceptStudent(Person p, TimeTableSchedule t) {
                String clazz = p.getLabel();
                // kontrollin klassi
                if (!TimetableUtils.containsSameTypeString(t.getTimetable(), t.ClassIDs, clazz, Form.class)) {
                    return false;
                }
                // klassijuhataja tund
                if ((t.getSubjectName().toLowerCase().contains("klassi") && t.getSubjectName().toLowerCase().contains("tund")) || (t.getSubjectShortname().toLowerCase().contains("klassi") && t.getSubjectShortname().toLowerCase().contains("tund"))) {
                    return true;
                }
                DataSet<Journal> journals = getPerson().getJournals();
                if (journals == null) {
                    return true;
                }
                for (Journal j : journals) {
                    List<String> teachers = j.getTeachers();
                    for (String teacher : teachers) {
                        if (matchTeacherNames(teacher, t.getTeacherName())) {
                            // kui �petaja annab tundi t�pselt samadele
                            // klassidele
                            boolean classesMatch = matchClasses(j.getForms(), t.getClazzes());
                            if (classesMatch) {
                                return true;
                            }

                            // kui klassid ei klapi aga nimed klapivad
                            boolean namesMatch = matchNames(j.getSubject(), t.getSubjectName(), t.getSubjectShortname());
                            if (namesMatch) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

        };
    }

    @Override
    public String getName() {
        Person p = getPerson();
        if (p == null)
            return "Choose"; // TODO resourcify if needed
        if (p.equals(Stuudium.getUser().getIdentity())) {
            return "Minu";
        }
        return p.getShortName();
    }

    private boolean matchTeacherNames(String name1, String name2) {
        name1 = name1.replace(" ", "").replace(".", "").replace(",", "");
        name2 = name2.replace(" ", "").replace(".", "").replace(",", "");
        return name1.equalsIgnoreCase(name2);
    }

    private boolean matchSubjectNames(String name1, String name2, String name2short) {
        name1 = name1.replace(" ", "").replace(".", "").replace(",", "").toLowerCase();
        name2 = name2.replace(" ", "").replace(".", "").replace(",", "").toLowerCase();
        name2short = name2short.replace(" ", "").replace(".", "").replace(",", "").toLowerCase();
        return name1.contains(name2) || name1.contains(name2short);
    }

    private boolean matchClasses(List<String> classes1, List<Form> classes2) {
        if (classes1.size() != classes2.size()) {
            return false;
        }
        Set<String> mod1 = new HashSet<String>();
        Set<String> mod2 = new HashSet<String>();
        for (String cls : classes1) {
            mod1.add(cls.replace(" ", "").replace(".", "").replace(",", "").toLowerCase());
        }
        for (Form cls : classes2) {
            mod2.add(cls.getShortname().replace(" ", "").replace(".", "").replace(",", "").toLowerCase());
        }
        if (mod1.size() != mod2.size()) {
            return false;
        }
        a:
        for (String m1 : mod1) {
            for (String m2 : mod2) {
                if (m1.equalsIgnoreCase(m2)) {
                    continue a;
                }
            }
            return false;
        }
        return true;
    }

    private boolean matchNames(String a, String... bs) {
        a = a.toLowerCase().trim().replace(" ", "").replace(".", "");
        for (String b : bs) {
            b = b.toLowerCase().trim().replace(" ", "").replace(".", "");
            if (a.contains(b) || b.contains(a)) {
                return true;
            }
        }
        return false;
    }
}
