package ee.tartu.jpg.minuposka;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.Map;

import ee.tartu.jpg.minuposka.service.NotificationService;
import ee.tartu.jpg.minuposka.service.trigger.HomeworkNotificationScheduler;
import ee.tartu.jpg.minuposka.service.trigger.LessonNotificationScheduler;
import ee.tartu.jpg.minuposka.service.trigger.StuudiumUpdateScheduler;
import ee.tartu.jpg.minuposka.service.trigger.TimetablesUpdateScheduler;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.StuudiumEventListener;
import ee.tartu.jpg.stuudium.StuudiumSettings;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;
import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.Timetables;

/**
 * Created by gregor on 9/22/2015.
 */
public class PoskaApplication extends Application implements StuudiumEventListener {

    private static final String TAG = "PoskaApplication";

    private static String STUUDIUM_CLIENT_ID = ""; // TODO Your Stuudium API key here
    private static String STUUDIUM_SUBDOMAIN = "jpg"; // TODO Your xxx.ope.ee subdomain here

    public static final String TIMETABLE_SCRIPT_URL = "http://jpg.tartu.ee/tunniplaan/xml/"; // TODO Your timetable script address here

    public static StuudiumSettings stuudiumSettings;
    public static Timetables timetables;

    @Override
    public void onCreate() {
        Log.i(TAG, "Initializing Stuudium");
        Stuudium.init(getApplicationContext());

        stuudiumSettings = new JPGStuudiumSettings();
        Stuudium.setSettings(stuudiumSettings);
        Stuudium.attach(this);

        Log.i(TAG, "Initializing timetables");
        timetables = new Timetables(this, DataUtils.getLastTimetableCheckSystem(this));
        timetables.loadTimetablesFromBase();

        String selectedTtFilename = DataUtils.getTimetableSelection(this);
        Timetable selectedTt = timetables.getPeriod(selectedTtFilename);
        if (selectedTt != null)
            selectedTt.init(PoskaApplication.timetables.db);

        Log.i(TAG, "Starting schedulers");
        TimetablesUpdateScheduler.startBroadcastReceiver(getApplicationContext());
        StuudiumUpdateScheduler.startBroadcastReceiver(getApplicationContext());
        HomeworkNotificationScheduler.startBroadcastReceiver(getApplicationContext());
        LessonNotificationScheduler.startBroadcastReceiver(getApplicationContext());

        super.onCreate();
    }

    @Override
    public void onStuudiumLogin() {
        Log.i(TAG, "Logged in to Stuudium, requesting user data");
        Stuudium.requestUserData(true, true);
    }

    @Override
    public void onStuudiumLogout() {
        // Log.i(TAG, "Logged out of Stuudium");
    }

    @Override
    public void onAvatarLoaded(Person p, Bitmap bitmap, int size) {
        // Log.i(TAG, "Stuudium avatar loaded");
    }

    @Override
    public void onJournalsLoaded(Person p, DataSet<Journal> journals) {
        Log.d(TAG, "Stuudium journals loaded");
    }

    @Override
    public void onEventsLoaded(Person p, DataSet<Event> events) {
        Log.d(TAG, "Stuudium events loaded");
    }

    @Override
    public void onNewEvent(Person p, Event event, boolean manualUpdate) {
        Log.i(TAG, "Handling new Stuudium event for " + p.getFullName() + "; manual: " + manualUpdate);
        // Do not notify in case of a manual update
        if (manualUpdate)
            return;
        Event.Content content = event.getContent();
        Event.Grade grade = content.getGrade();

        // WE DO NOT WANT ABSENCES TO BE SHOWN, BUT OTHER DATA TO BE SHOWN WHETHER IT'S A GRADE OR A COMMENT
        if (grade == null) {
            Event.ExtraLabels extraLabels = content.getExtraLabels();
            if (extraLabels != null && extraLabels.getLabelCount() != 0) {
                return;
            }
        }
        String currentGradeValue = null;
        String gradeSubject = null;
        String gradeDescription = null;

        if (grade != null) {
            Event.Value gradeValue = grade.getValue();
            if (gradeValue != null) currentGradeValue = gradeValue.getCurrent();
        }
        String gradeComment = content.getComment();
        Event.Lesson lesson = content.getLesson();
        if (lesson != null) {
            gradeSubject = lesson.getSubject();
            gradeDescription = lesson.getDescription();
        }
        if (gradeDescription == null) gradeDescription = TextUtils.capitalize(content.getLabel());

        // Don't want meaningless notifications
        if (grade == null && gradeComment == null) return;

        NotificationService.notifyGrade(this, currentGradeValue, gradeComment, gradeSubject, gradeDescription, event.getId().hashCode());
    }

    @Override
    public void onAssignmentsLoaded(Person p, DataSet<Assignment> assignments, boolean manualUpdate) {
        Log.i(TAG, "Stuudium assignments loaded, refreshing possible notification");
        HomeworkNotificationScheduler.updateBroadcastReceiver(this);
    }

    @Override
    public void onAssignmentPushCompleted(Person p, Assignment assignment) {
        Log.i(TAG, "Stuudium assignment push completed, refreshing possible notification");
        HomeworkNotificationScheduler.updateBroadcastReceiver(this);
    }

    @Override
    public void onUserDataLoaded(User u, boolean manualUpdate) {
        Log.i(TAG, "Stuudium user data loaded, requesting events, assignments and journals");
        if (u == null) return;
        for (Person p : u.getStudents()) {
            if (p == null)
                continue;
            p.requestEvents(manualUpdate);
            p.requestAssignments(manualUpdate);
            p.requestJournals();
        }
    }

    @Override
    public void onPeopleChanged(Map<String, Person> people) {
        Log.d(TAG, "Stuudium people changed");
    }

    @Override
    public void onLoadingStarted() {
        Log.d(TAG, "Stuudium loading started");
    }

    @Override
    public void onLoadingFinished() {
        Log.d(TAG, "Stuudium loading finished");
    }

    @Override
    public void onError(String str) {
        Log.e(TAG, "Stuudium error: " + str);
    }

    private class JPGStuudiumSettings extends StuudiumSettings {

        @Override
        public String getSubdomain() {
            return STUUDIUM_SUBDOMAIN;
        }

        @Override
        public String getClientId() {
            return STUUDIUM_CLIENT_ID;
        }

        @Override
        public String getAuthRedirectUri() {
            return STUUDIUM_SUBDOMAIN + "app://stuudiumresponse";
        }

        @Override
        public String getUserAgent() {
            PackageInfo pi;
            try {
                pi = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                return String.format("%s v%s", pi.packageName, pi.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Failed to find package manager", e);
            }
            return "null";
        }

        @Override
        public Context getContext() {
            return PoskaApplication.this;
        }

    }
}
