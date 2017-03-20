package ee.tartu.jpg.minuposka.service.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import ee.tartu.jpg.minuposka.PoskaApplication;
import ee.tartu.jpg.minuposka.service.NotificationService;
import ee.tartu.jpg.minuposka.ui.filter.PersonalizedFilter;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.timetable.Timetable;
import ee.tartu.jpg.timetable.TimetableUtils;
import ee.tartu.jpg.timetable.Timetables;
import ee.tartu.jpg.timetable.data.Day;
import ee.tartu.jpg.timetable.data.TimeTableSchedule;
import ee.tartu.jpg.timetable.utils.Filter;
import ee.tartu.jpg.timetable.utils.TimetableFilter;

/**
 * Schedules and triggers lesson notifications.
 */
public class LessonNotificationScheduler extends BroadcastReceiver {

    private static final String TAG = "LessonNotificationSched";

    private static final long NOTIFICATION_PRETIME = 45L * 60L * 1000L;
    private static final long NOTIFICATION_POSTTIME = 15L * 60L * 1000L;

    public static void startBroadcastReceiver(Context context) {
        Log.d(TAG, "Starting lesson update broadcast receiver");
        setNextAlarm(context, -1, 0);
    }

    public static void updateBroadcastReceiver(Context context) {
        Log.d(TAG, "Updating lesson update broadcast receiver");
        setNextAlarm(context, -1, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, NotificationService.class);
        // Schedule the next alarm at every show action
        int lessonNumber = intent.getIntExtra(NotificationService.LESSON_NUMBER, -1);
        if (intent.getAction().equals(NotificationService.ACTION_HIDE_LESSON))
            setNextAlarm(context, lessonNumber, 0);
        Log.d(TAG, "Forwarding notification for: " + intent.getAction());
        context.startService(intent);
    }

    private static void setNextAlarm(Context context, int currentLessonNumber, int dayinc) {
        if (!DataUtils.isNotificationLessonEnabled(context)) return;
        Log.d(TAG, "Setting next lesson alarm: " + currentLessonNumber + ", " + dayinc);
        if (dayinc == 7) {
            Log.d(TAG, "Tried the whole week, but no next lesson");
            return;
        }
        if (!Stuudium.isLoggedIn()) {
            Log.d(TAG, "Not logged in to Stuudium");
            return;
        }
        // Figure out next lesson
        String selectedTtFilename = DataUtils.getTimetableSelection(context);
        Timetable selectedTt = PoskaApplication.timetables.getPeriod(selectedTtFilename);
        if (selectedTt == null) {
            Log.d(TAG, "Selected timetable not found");
            return;
        }
        if (!selectedTt.isInitialised()) {
            if (PoskaApplication.timetables == null) {
                PoskaApplication.timetables = new Timetables(context, DataUtils.getLastTimetableCheckSystem(context));
                PoskaApplication.timetables.loadTimetablesFromBase();
            }
            selectedTt.init(PoskaApplication.timetables.db);
        }
        // Figure out current day
        Day today = selectedTt.getDayIn(dayinc);
        if (today == null) {
            Log.d(TAG, "Today not found");
            setNextAlarm(context, -1, dayinc + 1);
            return;
        }
        // Figure out correct filters and get all schedules
        TimetableFilter tf = new PersonalizedFilter(Stuudium.getUser().getIdentityId());
        Filter<TimeTableSchedule> df = tf.getFilter(today);
        List<TimeTableSchedule> todaysSchedules = selectedTt.getAll(TimeTableSchedule.class, df, TimetableUtils.lessonStartTimeComparator);
        // Set time, so that the notification would still be shown NOTIFICATION_POSTTIME milliseconds into the lesson.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, (int) -NOTIFICATION_POSTTIME);
        // Find first lesson that hasn't started and that we haven't displayed info about already
        TimeTableSchedule timeTableSchedule = null;
        Calendar nextLessonDate = null;
        for (TimeTableSchedule schedule : todaysSchedules) {
            if (schedule.getLessonNumber() == currentLessonNumber)
                continue;
            Calendar startDate = schedule.getStartDate();
            startDate.add(Calendar.DAY_OF_WEEK, dayinc);
            if (cal.before(startDate)) {
                timeTableSchedule = schedule;
                nextLessonDate = startDate;
                Log.d(TAG, "Next lesson time: " + startDate.getTime().toString());
                break;
            }
        }
        // 2017 spring holidays because there is no better way
        //noinspection WrongConstant
        if (nextLessonDate != null && nextLessonDate.get(Calendar.YEAR) == 2017 &&
                (nextLessonDate.get(Calendar.MONTH) == Calendar.MARCH && nextLessonDate.get(Calendar.DAY_OF_MONTH) >= 18 && nextLessonDate.get(Calendar.DAY_OF_MONTH) <= 26
                        || nextLessonDate.get(Calendar.MONTH) == Calendar.APRIL && nextLessonDate.get(Calendar.DAY_OF_MONTH) >= 29
                        || nextLessonDate.get(Calendar.MONTH) == Calendar.MAY && nextLessonDate.get(Calendar.DAY_OF_MONTH) <= 2
                        || nextLessonDate.get(Calendar.MONTH) == Calendar.JUNE && nextLessonDate.get(Calendar.DAY_OF_MONTH) >= 9
                        || nextLessonDate.get(Calendar.MONTH) == Calendar.JULY
                        || nextLessonDate.get(Calendar.MONTH) == Calendar.AUGUST)) {
            Log.d(TAG, "Cancelling notification due to holiday in 2017 spring");
            timeTableSchedule = null;
        }

        // If no more lessons today
        if (timeTableSchedule == null) {
            Log.d(TAG, "No more lessons today");
            setNextAlarm(context, -1, dayinc + 1);
            return;
        }

        long nextLessonMillis = nextLessonDate.getTimeInMillis();

        // Translate notification text
        String subjectname = TextUtils.translateFromEstonian(context, timeTableSchedule.getSubjectName());
        String classroomnumber = TextUtils.translateFromEstonian(context, timeTableSchedule.getClassroomNumber());
        String teachername = TextUtils.translateFromEstonian(context, timeTableSchedule.getTeacherName());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Setup alarm to show notification
        Intent showIntent = new Intent(context, LessonNotificationScheduler.class);
        showIntent.setAction(NotificationService.ACTION_SHOW_LESSON);
        showIntent.putExtra(NotificationService.LESSON_NAME, subjectname);
        showIntent.putExtra(NotificationService.LESSON_LOCATION, classroomnumber);
        showIntent.putExtra(NotificationService.LESSON_TEACHER, teachername);
        showIntent.putExtra(NotificationService.LESSON_NUMBER, timeTableSchedule.getLessonNumber());
        PendingIntent showPendingIntent = PendingIntent.getBroadcast(context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC, nextLessonMillis - NOTIFICATION_PRETIME, showPendingIntent);

        // Setup alarm to hide notification
        Intent hideIntent = new Intent(context, LessonNotificationScheduler.class);
        hideIntent.setAction(NotificationService.ACTION_HIDE_LESSON);
        PendingIntent hidePendingIntent = PendingIntent.getBroadcast(context, 1, hideIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC, nextLessonMillis + NOTIFICATION_POSTTIME, hidePendingIntent);
    }
}