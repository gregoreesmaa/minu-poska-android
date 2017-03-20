package ee.tartu.jpg.stuudium.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.upper.StuudiumData;
import ee.tartu.jpg.stuudium.data.upper.SubStuudiumData;

public class Event extends StuudiumData {

    private static final String TAG = "Event";

    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String EVENT_ID = "event_id";
    private static final String CONTENT = "content";
    private static final String CREATED_AT = "created_at";
    private static final String CREATOR = "creator";

    private String type;

    private String id;

    private String event_id;

    private Content content;

    private Date created_at;

    private String creator_id;

    public Event(JSONObject obj) throws JSONException {
        type = obj.getString(TYPE);
        if (obj.has(ID)) {
            id = obj.getString(ID);
        }
        if (obj.has(EVENT_ID)) {
            event_id = obj.getString(EVENT_ID);
        }
        if (obj.has(CONTENT)) {
            content = new Content(obj.getJSONObject(CONTENT));
        }
        if (obj.has(CREATED_AT)) try {
            created_at = dateTimeFormat.parse(obj.getString(CREATED_AT));
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse creation date", e);
        }
        if (obj.has(CREATOR)) {
            creator_id = Stuudium.addPerson(obj.getJSONObject(CREATOR));
        }
    }

    public Content getContent() {
        return content;
    }

    private Date getCreatedAt() {
        return created_at;
    }

    public Date getCreatedAtDate() {
        try {
            return dateOnlyFormat.parse(dateOnlyFormat.format(created_at));
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse creation date", e);
            return created_at;
        }
    }

    public Date getCreatedAtTime() {
        try {
            return timeOnlyFormat.parse(timeOnlyFormat.format(created_at));
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse creation date", e);
            return created_at;
        }
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return event_id;
    }

    public boolean hasCreator() {
        return creator_id != null;
    }

    public String getCreatorId() {
        return creator_id;
    }

    public Person getCreator() {
        return Stuudium.getPerson(creator_id);
    }

    public class Content extends SubStuudiumData {

        static final String COMMENT = "comment";
        static final String EXTRA_LABELS = "extra_labels";
        static final String GRADE = "grade";
        static final String LABEL = "label";
        static final String LESSON = "lesson";
        static final String SUMMARY = "summary";

        private String comment;

        private ExtraLabels extra_labels;

        private Grade grade;

        private String label;

        private Lesson lesson;

        private String summary;

        Content(JSONObject jsonObject) throws JSONException {
            summary = jsonObject.getString(SUMMARY);
            label = jsonObject.getString(LABEL);
            if (jsonObject.has(COMMENT))
                comment = jsonObject.getString(COMMENT);
            lesson = new Lesson(jsonObject.getJSONObject(LESSON));
            if (jsonObject.has(GRADE))
                grade = new Grade(jsonObject.getJSONObject(GRADE));
            if (jsonObject.has(EXTRA_LABELS))
                extra_labels = new ExtraLabels(
                        jsonObject.getJSONObject(EXTRA_LABELS));
        }

        public String getComment() {
            return comment;
        }

        public ExtraLabels getExtraLabels() {
            return extra_labels;
        }

        public Grade getGrade() {
            return grade;
        }

        public String getLabel() {
            return label;
        }

        public Lesson getLesson() {
            return lesson;
        }

        public String getSummary() {
            return summary;
        }

        @Override
        public int hashCode() {
            return (id + "_content").hashCode();
        }

    }

    public class ExtraLabel extends SubStuudiumData {

        static final String LABEL = "label";
        static final String LABEL_SHORT = "label_short";

        private String label;

        private String label_name;

        private String label_short;

        ExtraLabel(String name, JSONObject jsonObject)
                throws JSONException {
            label_name = name;
            label_short = jsonObject.getString(LABEL_SHORT);
            label = jsonObject.getString(LABEL);
        }

        public String getLabel() {
            return label;
        }

        public String getLabelShort() {
            return label_short;
        }

        public String getLabelName() {
            return label_name;
        }

        @Override
        public int hashCode() {
            return (id + "_extralabel").hashCode();
        }
    }

    public class ExtraLabels extends SubStuudiumData {

        private ArrayList<ExtraLabel> labels = new ArrayList<>();

        ExtraLabels(JSONObject jsonObject) throws JSONException {
            Iterator<String> i = jsonObject.keys();
            String key;
            while (i.hasNext() && (key = i.next()) != null) {
                JSONObject labelObj = jsonObject.getJSONObject(key);
                labels.add(new ExtraLabel(key, labelObj));
            }
        }

        public ArrayList<ExtraLabel> getLabels() {
            return labels;
        }

        public int getLabelCount() {
            return labels.size();
        }

        @Override
        public int hashCode() {
            return (id + "_extralabels").hashCode();
        }
    }

    public class Grade extends SubStuudiumData {

        static final String IS_IMPOTANT = "is_important";
        static final String VALUE = "value";

        private boolean is_important;

        private Value value;

        Grade(JSONObject jsonObject) throws JSONException {
            value = new Value(jsonObject.getJSONObject(VALUE));
            is_important = jsonObject.getBoolean(IS_IMPOTANT);
        }

        public Value getValue() {
            return value;
        }

        public boolean isImportant() {
            return is_important;
        }

        @Override
        public int hashCode() {
            return (id + "_grade").hashCode();
        }
    }

    public class Lesson extends SubStuudiumData {

        static final String DESCRIPTION = "description";
        static final String SUBJECT = "subject";
        static final String TIME = "time";

        private String description;

        private String subject;

        private Date time; // YYYY-MM-DD

        Lesson(JSONObject jsonObject) throws JSONException {
            subject = jsonObject.getString(SUBJECT);
            if (jsonObject.has(DESCRIPTION))
                description = jsonObject.getString(DESCRIPTION);

            if (jsonObject.has(TIME)) try {
                time = dateFormat.parse(jsonObject.getString(TIME));
            } catch (ParseException e) {
                Log.e(TAG, "Failed to parse Lesson time", e);
            }
        }

        public String getDescription() {
            return description;
        }

        public String getSubject() {
            return subject;
        }

        public Date getTime() {
            return time;
        }

        @Override
        public int hashCode() {
            return (id + "_lesson").hashCode();
        }

    }

    public class Value extends SubStuudiumData {

        static final String CURRENT = "current";
        static final String PREVIOUS = "previous";

        private String current;

        private String previous;

        Value(JSONObject jsonObject) throws JSONException {
            current = jsonObject.getString(CURRENT);
            if (jsonObject.has(PREVIOUS))
                previous = jsonObject.getString(PREVIOUS);
        }

        public String getCurrent() {
            return current;
        }

        public String getPrevious() {
            return previous;
        }

        @Override
        public int hashCode() {
            return (id + "_value").hashCode();
        }

    }

    @Override
    public int compareTo(StuudiumData sd) {
        if (!(sd instanceof Event))
            return 0;
        Date d1 = getCreatedAt();
        Date d2 = ((Event) sd).getCreatedAt();
        if (d1 == null || d2 == null)
            return 0;
        int compare1 = d2.compareTo(d1);
        if (compare1 != 0)
            return compare1;
        String id1 = getId();
        String id2 = ((Event) sd).getId();
        if (id1 == null || id2 == null)
            return 0;
        return id1.compareTo(id2);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
