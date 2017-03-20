package ee.tartu.jpg.stuudium.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import ee.tartu.jpg.stuudium.JSONResponseHandler;
import ee.tartu.jpg.stuudium.Request;
import ee.tartu.jpg.stuudium.Requests;
import ee.tartu.jpg.stuudium.ResponseHandler;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.StuudiumEventListener;
import ee.tartu.jpg.stuudium.data.upper.StuudiumData;
import ee.tartu.jpg.stuudium.data.upper.SubStuudiumData;
import ee.tartu.jpg.stuudium.utils.DateUtils;

/**
 * Stores data for an assignment and it's content.
 */
public class Assignment extends StuudiumData {

    private static final String TAG = "Assignment";

    public static final String TYPE_TEST = "test";
    public static final String TYPE_HOMEWORK = "homework";
    public static final String TYPE_CUSTOM = "todo";

    private static final String ID = "id";
    private static final String CONTENT = "content";

    private String person_id;

    private String id;

    private Content content;

    public Assignment(String person_id2, JSONObject obj) throws JSONException {
        person_id = person_id2;
        id = obj.getString(ID);
        content = new Content(obj.getJSONObject(CONTENT));
    }

    public String getId() {
        return id;
    }

    public Content getContent() {
        return content;
    }

    public class Content extends SubStuudiumData {

        static final String TYPE = "type";
        static final String DESCRIPTION = "description";
        static final String SUBJECT = "subject";
        static final String LABEL = "label";
        static final String LABEL_SHORT = "label_short";
        static final String COMPLETED = "completed";
        static final String READONLY = "readonly";
        static final String DEADLINE = "deadline";
        static final String CREATED_AT = "created_at";

        private String type;

        private String description;

        private String subject;

        private String label;

        private String label_short;

        private boolean completed;

        private boolean completed_unpushed;

        private long completedUpdatedTime;

        private boolean readonly;

        private Date deadline;

        private Date created_at;

        Content(JSONObject obj) throws JSONException {
            type = obj.getString(TYPE);
            description = obj.getString(DESCRIPTION);
            if (obj.has(SUBJECT))
                subject = obj.getString(SUBJECT);
            label = obj.getString(LABEL);
            label_short = obj.getString(LABEL_SHORT);
            completed = obj.getBoolean(COMPLETED);
            completed_unpushed = completed;
            readonly = obj.getBoolean(READONLY);
            try {
                deadline = dateFormat.parse(obj.getString(DEADLINE));
            } catch (ParseException e) {
                Log.e(TAG, "Failed to parse deadline", e);
            }
            if (obj.has(CREATED_AT)) try {
                created_at = dateTimeFormat.parse(obj.getString(CREATED_AT));
            } catch (ParseException e) {
                Log.e(TAG, "Failed to parse creation date", e);
            }
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getSubject() {
            return subject;
        }

        public String getLabel() {
            return label;
        }

        public String getLabelShort() {
            return label_short;
        }

        public boolean isCompleted() {
            return completed;
        }

        public boolean isCompletedUnpushed() {
            return completed_unpushed;
        }

        public boolean isReadonly() {
            return readonly;
        }

        public Date getDeadline() {
            return deadline;
        }

        public Date getCreatedAt() {
            return created_at;
        }

        public boolean isDueToday() {
            return DateUtils.isToday(getDeadline());
        }

        public boolean isDueTomorrow() {
            return DateUtils.isWithinDaysFuture(getDeadline(), 1);
        }

        public boolean isDueWithinDaysFuture(int d) {
            return DateUtils.isWithinDaysFuture(getDeadline(), d);
        }

        public boolean isDueTodayOrTomorrow() {
            return isDueToday() || isDueTomorrow();
        }

        public boolean isDueInFuture() {
            return DateUtils.isAfterToday(getDeadline());
        }

        public boolean isDueTodayOrInFuture() {
            return isDueToday() || isDueInFuture();
        }

        public boolean isDueInPast() {
            return DateUtils.isBeforeToday(getDeadline());
        }

        public boolean isDueTodayOrInPast() {
            return isDueToday() || isDueInPast();
        }

        public boolean isDueNextSchoolday() {
            Calendar today = Calendar.getInstance();
            switch (today.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.FRIDAY:
                    return isDueWithinDaysFuture(3);
                case Calendar.SATURDAY:
                    return isDueWithinDaysFuture(2);
                case Calendar.SUNDAY:
                    return isDueWithinDaysFuture(1);
                default:
                    return isDueTomorrow();
            }
        }

        public boolean isDueTodayOrNextSchoolday() {
            return isDueToday() || isDueNextSchoolday();
        }

        public boolean isDueNextMonday() {
            Calendar c = Calendar.getInstance();
            c.setTime(getDeadline());
            return DateUtils.isWithinDaysFuture(c, 7) && c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
        }

        public void pushCompleted(final boolean completed) {
            this.completed_unpushed = completed;
            final Request r = Stuudium.getRequest(Requests.ASSIGNMENTS, person_id, getId());
            r.setPostBody(String.format("{\"completed\":%s}", completed));
            r.setTimeout(10000);
            final long time = System.currentTimeMillis();
            r.setHandler(new JSONResponseHandler() {

                @Override
                public void handle(JSONObject obj) throws JSONException {
                    Stuudium.requestSucceeded(getId() + "_completed", r);
                    if (obj.getString("status").equalsIgnoreCase("ok")) {
                        if (completedUpdatedTime < time) {
                            Content.this.completed_unpushed = completed;
                            Content.this.completed = completed;
                            Content.this.completedUpdatedTime = time;
                            for (StuudiumEventListener sel : Stuudium.getEventListeners())
                                sel.onAssignmentPushCompleted(Stuudium.getPerson(person_id), Assignment.this);
                        }
                    }
                }

            });
            r.setOnFailHandler(new ResponseHandler<Exception>() {

                @Override
                public void handle(Exception obj) {
                    Stuudium.requestFailed(getId() + "_completed", r);
                }

            });
            r.send(true);
        }

        @Override
        public int hashCode() {
            return (id + "_content").hashCode();
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(StuudiumData sd) {
        if (!(sd instanceof Assignment))
            return 0;
        Content c1 = getContent();
        Content c2 = ((Assignment) sd).getContent();
        if (c1 == null || c2 == null)
            return 0;
        Date d1 = c1.getDeadline();
        Date d2 = c2.getDeadline();
        if (d1 == null || d2 == null)
            return 0;
        int compare1 = d1.compareTo(d2);
        if (compare1 != 0)
            return compare1;
        String type1 = c1.getType();
        String type2 = c2.getType();
        if (type1 == null || type2 == null) {
            return 0;
        }
        int t1 = getTypeIndex(type1);
        int t2 = getTypeIndex(type2);
        int compare2 = t1 > t2 ? 1 : (t1 < t2 ? -1 : 0);
        if (compare2 != 0)
            return compare2;
        String s1 = c1.getDescription();
        String s2 = c2.getDescription();
        return s1.compareTo(s2);

    }

    private int getTypeIndex(String type) {
        switch (type) {
            case "test":
                return 0;
            case "todo":
                return 2;
            default:
                return 1;
        }
    }

}
