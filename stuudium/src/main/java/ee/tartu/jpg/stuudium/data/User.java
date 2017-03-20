package ee.tartu.jpg.stuudium.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.upper.StuudiumData;

public class User extends StuudiumData {

    private static final String TAG = "User";

    private static final String IDENTITY = "identity";
    private static final String STUDENTS = "students";
    private static final String TOKEN_EXPIRES_AT = "token_expires_at";

    private String identity_id;

    private ArrayList<String> student_ids = new ArrayList<>();

    private Date token_expires_at;

    public User(JSONObject obj) throws JSONException {
        try {
            token_expires_at = dateTimeFormat.parse(obj
                    .getString(TOKEN_EXPIRES_AT));
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse expiration date", e);
        }

        identity_id = Stuudium.addPerson(obj.getJSONObject(IDENTITY));

        JSONArray students = obj.getJSONArray(STUDENTS);
        for (int i = 0; i < students.length(); i++) {
            student_ids.add(Stuudium.addPerson(students.getJSONObject(i)));
        }
    }

    public Person getIdentity() {
        return Stuudium.getPerson(identity_id);
    }

    public String getRoleString() {
        String str = "";
        Person identity = getIdentity();
        if (identity != null) {
            ArrayList<Person> students = getStudents();
            int otherStudents = students.size();
            if (students.contains(identity)) {
                str = identity.getLabel() + " õpilane";
                otherStudents--;
            } else {
                str += identity.getLabel();
            }
            if (otherStudents > 0) {
                if (!str.isEmpty()) {
                    str += "; ";
                }
                str += "lapsevanem";
            }
            if (students.size() == 0) {
                if (!str.isEmpty()) {
                    str += "; ";
                }
                str += "õpetaja";
            }

        }
        return str;
    }

    public ArrayList<Person> getStudents() {
        ArrayList<Person> students = new ArrayList<>();
        for (String id : student_ids) students.add(Stuudium.getPerson(id));
        return students;
    }

    public int getStudentCount() {
        return student_ids.size();
    }

    public String getIdentityId() {
        return identity_id;
    }

    public boolean hasStudentId(String student_id) {
        return student_ids.contains(student_id);
    }

    @Override
    public int hashCode() {
        return (identity_id + "_user").hashCode();
    }

    @Override
    public int compareTo(StuudiumData sd) {
        if (!(sd instanceof User))
            return 0;
        String id1 = identity_id;
        String id2 = ((User) sd).identity_id;
        if (id1 == null || id2 == null)
            return 0;
        return id1.compareTo(id2);
    }
}
