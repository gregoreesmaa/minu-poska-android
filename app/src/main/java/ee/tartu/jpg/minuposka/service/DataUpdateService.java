package ee.tartu.jpg.minuposka.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ee.tartu.jpg.minuposka.PoskaApplication;
import ee.tartu.jpg.minuposka.service.trigger.LessonNotificationScheduler;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.stuudium.ResponseHandler;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;
import ee.tartu.jpg.timetable.data.download.TimetableDownloadTask;

/**
 * Handles timetable and Stuudium data updates.
 */
public class DataUpdateService extends IntentService {

    private static final String TAG = "DataUpdateService";

    public static final String ACTION_UPDATE_STUUDIUM = "ee.tartu.jpg.minuposka.service.update.stuudium";
    private static final String ACTION_UPDATE_TIMETABLES = "ee.tartu.jpg.minuposka.service.update.timetables";

    public static final String MANUAL_UPDATE = "ee.tartu.jpg.minuposka.service.update.manual";
    public static final String UPDATE_STUUDIUM_USER = "ee.tartu.jpg.minuposka.service.update.stuudium.user";
    public static final String UPDATE_STUUDIUM_EVENTS = "ee.tartu.jpg.minuposka.service.update.stuudium.events";
    public static final String UPDATE_STUUDIUM_TODOS = "ee.tartu.jpg.minuposka.service.update.stuudium.todos";
    public static final String UPDATE_STUUDIUM_JOURNALS = "ee.tartu.jpg.minuposka.service.update.stuudium.journals";

    private static boolean updatingTimetables = false;

    public DataUpdateService() {
        super(TAG);
    }

    /**
     * Starts this service to perform Stuudium update (all data). If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startStuudiumUpdate(Context context, boolean manualUpdate) {
        Log.i(TAG, "Starting Stuudium update; manual: " + manualUpdate);
        Intent intent = new Intent(context, DataUpdateService.class);
        intent.setAction(ACTION_UPDATE_STUUDIUM);
        intent.putExtra(UPDATE_STUUDIUM_USER, true);
        intent.putExtra(UPDATE_STUUDIUM_EVENTS, true);
        intent.putExtra(UPDATE_STUUDIUM_TODOS, true);
        intent.putExtra(UPDATE_STUUDIUM_JOURNALS, true);
        intent.putExtra(MANUAL_UPDATE, manualUpdate);
        context.startService(intent);
    }

    /**
     * Starts this service to perform Stuudium events update. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startStuudiumEventsUpdate(Context context, boolean manualUpdate) {
        Log.i(TAG, "Starting Stuudium events update; manual: " + manualUpdate);
        Intent intent = new Intent(context, DataUpdateService.class);
        intent.setAction(ACTION_UPDATE_STUUDIUM);
        intent.putExtra(UPDATE_STUUDIUM_EVENTS, true);
        intent.putExtra(MANUAL_UPDATE, manualUpdate);
        context.startService(intent);
    }

    /**
     * Starts this service to perform Stuudium todos update. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startStuudiumTodosUpdate(Context context, boolean manualUpdate) {
        Log.i(TAG, "Starting Stuudium todos update; manual: " + manualUpdate);
        Intent intent = new Intent(context, DataUpdateService.class);
        intent.setAction(ACTION_UPDATE_STUUDIUM);
        intent.putExtra(UPDATE_STUUDIUM_TODOS, true);
        intent.putExtra(MANUAL_UPDATE, manualUpdate);
        context.startService(intent);
    }

    /**
     * Starts this service to perform Stuudium journals update. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startStuudiumJournalsUpdate(Context context) {
        Log.i(TAG, "Starting Stuudium journals update");
        Intent intent = new Intent(context, DataUpdateService.class);
        intent.setAction(ACTION_UPDATE_STUUDIUM);
        intent.putExtra(UPDATE_STUUDIUM_JOURNALS, true);
        context.startService(intent);
    }

    /**
     * Starts this service to perform Timetables update. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startTimetablesUpdate(Context context) {
        if (updatingTimetables) {
            Log.i(TAG, "Skipping timetables update, because already doing that");
            return;
        }
        Log.i(TAG, "Starting timetables update");
        updatingTimetables = true;
        Intent intent = new Intent(context, DataUpdateService.class);
        intent.setAction(ACTION_UPDATE_TIMETABLES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_UPDATE_STUUDIUM:
                    boolean user = intent.getBooleanExtra(MANUAL_UPDATE, false);
                    boolean events = intent.getBooleanExtra(MANUAL_UPDATE, false);
                    boolean todos = intent.getBooleanExtra(MANUAL_UPDATE, false);
                    boolean journals = intent.getBooleanExtra(MANUAL_UPDATE, false);
                    boolean manualUpdate = intent.getBooleanExtra(MANUAL_UPDATE, false);
                    handleStuudiumUpdate(user, events, todos, journals, manualUpdate);
                    break;
                case ACTION_UPDATE_TIMETABLES:
                    handleTimetablesUpdate();
                    break;
            }
        }
    }

    /**
     * Handle Stuudium update (for all data) in the provided background thread.
     */
    private void handleStuudiumUpdate(boolean user, final boolean events, final boolean todos, final boolean journals, final boolean manualUpdate) {
        Log.d(TAG, "Handling Stuudium update");
        if (!Stuudium.hasUser())
            return;
        if (Stuudium.isLoginExpired()) {
            Stuudium.setLoginData(null);
        } else {
            ResponseHandler<User> updateOther = null;
            if (events || journals || todos) {
                updateOther = new ResponseHandler<User>() {
                    @Override
                    public void handle(User u) {
                        if (u != null) for (Person p : u.getStudents()) {
                            if (p == null)
                                continue;
                            if (events) {
                                Log.d(TAG, "Requesting events for: " + p.getId());
                                p.requestEvents(manualUpdate);
                            }
                            if (todos) {
                                Log.d(TAG, "Requesting assignments for: " + p.getId());
                                p.requestAssignments(manualUpdate);
                            }
                            if (journals) {
                                Log.d(TAG, "Requesting journals for: " + p.getId());
                                p.requestJournals();
                            }
                        }
                    }
                };
            }
            // If also updating user, do other things after that.
            if (user) Stuudium.requestUserData(updateOther, manualUpdate, false);
            else if (updateOther != null) updateOther.handle(Stuudium.getUser());
        }
    }

    /**
     * Handle timetables update in the provided background thread.
     */
    private void handleTimetablesUpdate() {
        Log.d(TAG, "Handling timetables update");
        new TimetableDownloadTask(PoskaApplication.TIMETABLE_SCRIPT_URL, PoskaApplication.timetables, new Runnable() {
            public void run() {
                Log.d(TAG, "Updated timetables");
                updatingTimetables = false;
                DataUtils.setLastTimetableCheckSystem(getApplicationContext(), PoskaApplication.timetables.lastcheckSystem);
                LessonNotificationScheduler.updateBroadcastReceiver(getApplicationContext());
            }
        }).execute();
    }
}
