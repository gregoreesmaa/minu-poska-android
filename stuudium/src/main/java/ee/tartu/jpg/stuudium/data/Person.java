package ee.tartu.jpg.stuudium.data;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import ee.tartu.jpg.stuudium.utils.DownloadAvatarTask;

public class Person extends StuudiumData {

    private static final String TAG = "Person";

    public static final String ID = "id";
    private static final String LANGUAGE = "language";
    private static final String NAME_FULL = "name_full";
    private static final String NAME_FIRST = "name_first";
    private static final String NAME_LAST = "name_last";
    private static final String NAME_SHORT = "name_short";
    private static final String LABEL = "label";
    private static final String AVATAR = "avatar";

    private String id;

    private String language;

    private String name_full;

    private String name_first;

    private String name_last;

    private String name_short;

    private String label;

    private Avatar avatar;

    public Person(JSONObject obj) throws JSONException {
        id = obj.getString(ID);
        if (obj.has(LANGUAGE))
            language = obj.getString(LANGUAGE);
        name_full = obj.getString(NAME_FULL);
        name_first = obj.getString(NAME_FIRST);
        name_last = obj.getString(NAME_LAST);
        name_short = obj.getString(NAME_SHORT);
        if (obj.has(LABEL))
            label = obj.getString(LABEL);
        avatar = new Avatar(obj.getJSONObject(AVATAR));
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getFullName() {
        return name_full;
    }

    public String getFirstName() {
        return name_first;
    }

    public String getLastName() {
        return name_last;
    }

    public String getShortName() {
        return name_short;
    }

    public String getLabel() {
        return label;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    private Person getPerson() {
        return this;
    }

    public void requestEvents(final boolean manualUpdate) {
        Log.d(TAG, "Requesting events");
        Stuudium.request(Requests.EVENTS, id, false, new JSONResponseHandler() {

            @Override
            public void handle(JSONObject obj) throws JSONException {
                Log.d(TAG, "Handling requested events");
                JSONArray arr = obj.getJSONArray("array");
                DataSet<Event> events = new DataSet<Event>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject json = arr.getJSONObject(i);
                    Event event = new Event(json);
                    events.add(event);
                    Stuudium.db.insertStuudiumEvent(event.getId(), getId(), json.toString());
                }
                setEvents(events, manualUpdate);
            }

        });
    }

    public DataSet<Event> getEvents() {
        return Stuudium.getAllEvents().get(getId());
    }

    private void setEvents(DataSet<Event> events, boolean manualUpdate) {
        Log.d(TAG, "Setting events");
        DataSet<Event> oldEvents = Stuudium.getAllEvents().get(getId());
        Stuudium.getAllEvents().put(getId(), events);
        for (StuudiumEventListener sel : Stuudium.getEventListeners())
            sel.onEventsLoaded(this, events);
        if (oldEvents != null && !oldEvents.isEmpty())
            for (Event newEvent : events)
                if (!oldEvents.contains(newEvent))
                    for (StuudiumEventListener sel : Stuudium.getEventListeners())
                        sel.onNewEvent(this, newEvent, manualUpdate);
    }

    public void requestJournals() {
        Log.d(TAG, "Requesting journals");
        Stuudium.request(Requests.JOURNALS, id, false, new JSONResponseHandler() {

            @Override
            public void handle(JSONObject obj) throws JSONException {
                Log.d(TAG, "Handling requested journals");
                JSONArray arr = obj.getJSONArray("array");
                DataSet<Journal> journals = new DataSet<Journal>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject json = arr.getJSONObject(i);
                    Journal journal = new Journal(json);
                    journals.add(journal);
                    Stuudium.db.insertStuudiumJournal(journal.getSubject(), getId(), json.toString());
                }
                setJournals(journals);
            }

        });
    }

    public DataSet<Journal> getJournals() {
        return Stuudium.getAllJournals().get(getId());
    }

    private void setJournals(DataSet<Journal> journals) {
        Log.d(TAG, "Setting journals");
        Stuudium.getAllJournals().put(getId(), journals);
        for (StuudiumEventListener sel : Stuudium.getEventListeners())
            sel.onJournalsLoaded(this, journals);
    }

    public void requestAssignments(Date since, Date until, JSONResponseHandler handler, boolean ignoreErrors, int timeout) {
        requestAssignments(since, until, handler, ignoreErrors, timeout, null);
    }

    public void requestAssignments(Date since, Date until, JSONResponseHandler handler, boolean ignoreErrors, int timeout, ResponseHandler<Exception> fhandler) {
        String sincestr = dateFormat.format(since);
        String untilstr = dateFormat.format(until);
        Log.d(TAG, "Requesting assignments between: " + sincestr + " - " + untilstr);
        Request r = Stuudium.getRequest(Requests.ASSIGNMENTS, id);
        r.setParameter("since", sincestr);
        r.setParameter("until", untilstr);
        r.setTimeout(timeout);
        r.setHandler(handler);
        if (fhandler != null)
            r.setOnFailHandler(fhandler);
        r.send(ignoreErrors);
    }

    public void requestAssignments(boolean manualUpdate) {
        requestAssignments(DateUtils.getDateToday(), DateUtils.getDateInDays(60), manualUpdate);
    }

    private void requestAssignments(Date since, Date until, boolean manualUpdate) {
        requestAssignments(since, until, false, -1, manualUpdate);
    }

    public void requestAssignments(Date since, Date until, ResponseHandler<Exception> fhandler, boolean manualUpdate) {
        requestAssignments(since, until, false, -1, fhandler, manualUpdate);
    }

    public void requestAssignments(Date since, Date until, int timeout, boolean manualUpdate) {
        requestAssignments(since, until, false, timeout, manualUpdate);
    }

    private void requestAssignments(Date since, Date until, boolean ignoreErrors, int timeout, final boolean manualUpdate) {
        requestAssignments(since, until, ignoreErrors, timeout, null, manualUpdate);
    }

    private void requestAssignments(Date since, Date until, boolean ignoreErrors, int timeout, ResponseHandler<Exception> fhandler, final boolean manualUpdate) {
        requestAssignments(since, until, new JSONResponseHandler() {

            @Override
            public void handle(JSONObject obj) throws JSONException {
                Log.d(TAG, "Handling requested assignments");
                JSONArray arr = obj.getJSONArray("array");
                DataSet<Assignment> assignments = new DataSet<Assignment>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject json = arr.getJSONObject(i);
                    Assignment assignment = new Assignment(getId(), json);
                    assignments.add(assignment);
                    Stuudium.db.insertStuudiumAssignment(assignment.getId(), getId(), json.toString());
                }
                setAssignments(assignments, manualUpdate);
            }

        }, ignoreErrors, timeout, fhandler);
    }

    public DataSet<Assignment> getAssignments() {
        return Stuudium.getAllAssignments().get(getId());
    }

    private void setAssignments(DataSet<Assignment> assignments, boolean manualUpdate) {
        Log.d(TAG, "Setting assignments");
        Stuudium.addAssignments(getId(), assignments);
        for (StuudiumEventListener sel : Stuudium.getEventListeners())
            sel.onAssignmentsLoaded(this, getAssignments(), manualUpdate);
    }

    public class Avatar extends SubStuudiumData {

        public static final String SIZE_35 = "35";
        public static final String SIZE_70 = "70";
        public static final String SIZE_100 = "100";

        private String size_35;

        private String size_70;

        private String size_100;

        private transient Bitmap b_35;

        private transient Bitmap b_70;

        private transient Bitmap b_100;

        private transient boolean loading_35;

        private transient boolean loading_70;

        private transient boolean loading_100;

        public Avatar(JSONObject obj) throws JSONException {
            size_35 = obj.getString(SIZE_35);
            size_70 = obj.getString(SIZE_70);
            size_100 = obj.getString(SIZE_100);
        }

        public static final int defaultsize = 100;

        public String getSize() {
            switch (defaultsize) {
                case 35:
                    return getSize_35();
                case 70:
                    return getSize_70();
                case 100:
                    return getSize_100();
            }
            return null;
        }

        public Bitmap getBitmap(boolean download) {
            switch (defaultsize) {
                case 35:
                    return !download ? b_35 : getBitmap_35();
                case 70:
                    return !download ? b_70 : getBitmap_70();
                case 100:
                    return !download ? b_100 : getBitmap_100();
            }
            return null;
        }

        public Drawable getDrawable(Drawable def, boolean download) {
            Bitmap bm = null;
            switch (defaultsize) {
                case 35:
                    bm = !download ? b_35 : getBitmap_35();
                    break;
                case 70:
                    bm = !download ? b_70 : getBitmap_70();
                    break;
                case 100:
                    bm = !download ? b_100 : getBitmap_100();
                    break;
            }
            return bm != null ? new BitmapDrawable(Stuudium.getSettings().getContext().getResources(), bm) : def;
        }


        public void setBitmap(Bitmap bm) {
            switch (defaultsize) {
                case 35:
                    b_35 = bm;
                    break;
                case 70:
                    b_70 = bm;
                    break;
                case 100:
                    b_100 = bm;
                    break;
            }
        }

        public void loadBitmap() {
            switch (defaultsize) {
                case 35:
                    loadBitmap_35();
                    break;
                case 70:
                    loadBitmap_70();
                    break;
                case 100:
                    loadBitmap_100();
                    break;
            }
        }

        public String getSize_35() {
            return size_35;
        }

        public String getSize_70() {
            return size_70;
        }

        public String getSize_100() {
            return size_100;
        }

        public Bitmap getBitmap_35() {
            if (b_35 == null) {
                loadBitmap_35();
                return null;
            }
            return b_35;
        }

        public Bitmap getBitmap_70() {
            if (b_70 == null) {
                loadBitmap_70();
                return null;
            }
            return b_70;
        }

        public Bitmap getBitmap_100() {
            if (b_100 == null) {
                loadBitmap_100();
                return null;
            }
            return b_100;
        }

        public void loadBitmap_35() {
            if (!loading_35) {
                loading_35 = true;
                new DownloadAvatarTask(new ResponseHandler<Bitmap>() {

                    @Override
                    public void handle(Bitmap obj) {
                        b_35 = obj;
                        for (StuudiumEventListener sel : Stuudium.getEventListeners())
                            sel.onAvatarLoaded(getPerson(), obj, 35);
                        loading_35 = false;
                    }

                }).execute(size_35);
            }
        }

        public void loadBitmap_70() {
            if (!loading_70) {
                loading_70 = true;
                new DownloadAvatarTask(new ResponseHandler<Bitmap>() {

                    @Override
                    public void handle(Bitmap obj) {
                        b_70 = obj;
                        for (StuudiumEventListener sel : Stuudium.getEventListeners())
                            sel.onAvatarLoaded(getPerson(), obj, 70);
                        loading_70 = false;
                    }

                }).execute(size_70);
            }
        }

        public void loadBitmap_100() {
            if (!loading_100) {
                loading_100 = true;
                new DownloadAvatarTask(new ResponseHandler<Bitmap>() {

                    @Override
                    public void handle(Bitmap obj) {
                        b_100 = obj;
                        for (StuudiumEventListener sel : Stuudium.getEventListeners())
                            sel.onAvatarLoaded(getPerson(), obj, 100);
                        loading_100 = false;
                    }

                }).execute(size_100);
            }
        }

        @Override
        public int hashCode() {
            return (id + "_avatar").hashCode();
        }

        public Drawable getDrawable_100(Drawable def) {
            Bitmap bm = getBitmap_100();
            return bm != null ? new BitmapDrawable(Stuudium.getSettings().getContext().getResources(), bm) : def;
        }

        public Drawable getDrawable_70(Drawable def) {
            Bitmap bm = getBitmap_70();
            return bm != null ? new BitmapDrawable(Stuudium.getSettings().getContext().getResources(), bm) : def;
        }

        public Drawable getDrawable_35(Drawable def) {
            Bitmap bm = getBitmap_35();
            return bm != null ? new BitmapDrawable(Stuudium.getSettings().getContext().getResources(), bm) : def;
        }
    }

    public boolean isStudent() {
        return Stuudium.getUser().hasStudentId(getId());
    }

    public boolean isTeacher() {
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(StuudiumData sd) {
        if (!(sd instanceof Person))
            return 0;
        String n1 = getFullName();
        String n2 = ((Person) sd).getFullName();
        if (n1 == null || n2 == null)
            return 0;
        return n1.compareTo(n2);
    }

}
