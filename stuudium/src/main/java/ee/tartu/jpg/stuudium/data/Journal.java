package ee.tartu.jpg.stuudium.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ee.tartu.jpg.stuudium.data.upper.StuudiumData;

public class Journal extends StuudiumData {

    private static final String FORMS = "forms";
    private static final String SUBJECT = "subject";
    private static final String TEACHERS = "teachers";

    private ArrayList<String> forms;

    private String subject;

    private ArrayList<String> teachers;

    public Journal(JSONObject obj) throws JSONException {
        forms = new ArrayList<>();
        JSONArray formsarr = obj.getJSONArray(FORMS);
        for (int i = 0; i < formsarr.length(); i++) {
            forms.add(formsarr.getString(i));
        }

        teachers = new ArrayList<>();
        JSONArray teachersarr = obj.getJSONArray(TEACHERS);
        for (int i = 0; i < teachersarr.length(); i++) {
            teachers.add(teachersarr.getString(i));
        }

        subject = obj.getString(SUBJECT);
    }

    public ArrayList<String> getForms() {
        return forms;
    }

    public String getSubject() {
        return subject;
    }

    public ArrayList<String> getTeachers() {
        return teachers;
    }

    @Override
    public int hashCode() {
        return subject.hashCode();
    }

    @Override
    public int compareTo(StuudiumData sd) {
        if (!(sd instanceof Journal))
            return 0;
        String s1 = getSubject();
        String s2 = ((Journal) sd).getSubject();
        if (s1 == null || s2 == null)
            return 0;
        int compare1 = s1.compareTo(s2);
        if (compare1 != 0)
            return compare1;
        return 1;
    }

}
