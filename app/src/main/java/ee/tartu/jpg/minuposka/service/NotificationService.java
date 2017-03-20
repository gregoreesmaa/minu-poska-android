package ee.tartu.jpg.minuposka.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.AssignmentsActivity;
import ee.tartu.jpg.minuposka.ui.EventsActivity;
import ee.tartu.jpg.minuposka.ui.EventsAndAssignmentsActivity;
import ee.tartu.jpg.minuposka.ui.MyScheduleActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;

/**
 * Handles notifications and provides helper methods.
 */
public class NotificationService extends IntentService {

    private static final String TAG = "NotificationService";

    public static final String ACTION_SHOW_HOMEWORK = "ee.tartu.jpg.minuposka.service.action.show.homework";
    public static final String ACTION_HIDE_HOMEWORK = "ee.tartu.jpg.minuposka.service.action.hide.homework";
    public static final String ACTION_SHOW_LESSON = "ee.tartu.jpg.minuposka.service.action.show.lesson";
    public static final String ACTION_HIDE_LESSON = "ee.tartu.jpg.minuposka.service.action.hide.lesson";
    private static final String ACTION_NOTIFY_GRADE = "ee.tartu.jpg.minuposka.service.action.notify.grade";
    private static final String ACTION_DISMISS_GRADE_NOTIFICATIONS = "ee.tartu.jpg.minuposka.service.action.notify.grade.dismiss";

    public static final String HOMEWORK_SUBJECTS = "ee.tartu.jpg.minuposka.service.homework.subjects";
    public static final String LESSON_NAME = "ee.tartu.jpg.minuposka.service.lesson.name";
    public static final String LESSON_LOCATION = "ee.tartu.jpg.minuposka.service.lesson.location";
    public static final String LESSON_TEACHER = "ee.tartu.jpg.minuposka.service.lesson.teacher";
    public static final String LESSON_NUMBER = "ee.tartu.jpg.minuposka.service.lesson.number";
    private static final String GRADE_VALUE = "ee.tartu.jpg.minuposka.service.grade.value";
    private static final String GRADE_COMMENT = "ee.tartu.jpg.minuposka.service.grade.comment";
    private static final String GRADE_SUBJECT = "ee.tartu.jpg.minuposka.service.grade.subject";
    private static final String GRADE_DESCRIPTION = "ee.tartu.jpg.minuposka.service.grade.description";
    private static final String GRADE_ID = "ee.tartu.jpg.minuposka.service.grade.id";

    private static final String NOTIFICATION_SCHOOLINFO = "ee.tartu.jpg.minuposka.notification.schoolinfo";

    public NotificationService() {
        super(TAG);
    }

    /**
     * Starts this service to show dismissible grade notification with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void notifyGrade(Context context, String value, String comment, String subject, String description, int id) {
        if (!DataUtils.isNotificationEventsEnabled(context))
            return;
        Log.d(TAG, "Notifying of new grade");
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_NOTIFY_GRADE);
        intent.putExtra(GRADE_VALUE, value);
        intent.putExtra(GRADE_COMMENT, comment);
        intent.putExtra(GRADE_SUBJECT, subject);
        intent.putExtra(GRADE_DESCRIPTION, description);
        intent.putExtra(GRADE_ID, id);
        context.startService(intent);
    }


    /**
     * Starts this service to show dismissible grade notification with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void dismissGradeNotifications(Context context) {
        Log.d(TAG, "Dismissing grade notifications");
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_DISMISS_GRADE_NOTIFICATIONS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            TextUtils.updateLanguageInContext(this);
            final String action = intent.getAction();
            if (ACTION_SHOW_HOMEWORK.equals(action)) {
                final String[] subjects = intent.getStringArrayExtra(HOMEWORK_SUBJECTS);
                handleShowHomework(subjects);
            } else if (ACTION_HIDE_HOMEWORK.equals(action)) {
                handleHideHomework();
            } else if (ACTION_SHOW_LESSON.equals(action)) {
                final String name = intent.getStringExtra(LESSON_NAME);
                final String location = intent.getStringExtra(LESSON_LOCATION);
                final String teacher = intent.getStringExtra(LESSON_TEACHER);
                handleShowLesson(name, location, teacher);
            } else if (ACTION_HIDE_LESSON.equals(action)) {
                handleHideLesson();
            } else if (ACTION_NOTIFY_GRADE.equals(action)) {
                final String value = intent.getStringExtra(GRADE_VALUE);
                final String comment = intent.getStringExtra(GRADE_COMMENT);
                final String subject = intent.getStringExtra(GRADE_SUBJECT);
                final String description = intent.getStringExtra(GRADE_DESCRIPTION);
                final int id = intent.getIntExtra(GRADE_ID, 0);
                handleNotifyGrade(value, comment, subject, description, id);
            } else if (ACTION_DISMISS_GRADE_NOTIFICATIONS.equals(action)) {
                handleDismissGradeNotifications();
            }
        }
    }

    /**
     * Handle action to show homework notification in the provided background thread with the provided
     * parameters.
     */
    private void handleShowHomework(String[] subjects) {
        if (subjects == null)
            return;
        // This can be changed to show detailed homework, if needed
        // Prepare target activity
        Intent intent = new Intent(this, DataUtils.isTablet(this) ? EventsAndAssignmentsActivity.class : AssignmentsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        String subjectsstr = "";
        for (String subject : subjects) {
            if (!subjectsstr.isEmpty())
                subjectsstr += ", ";
            subjectsstr += subject;
        }
        Log.d(TAG, "Showing homework notification: " + subjectsstr);

        // Prepare lesson notification text
        String homeworkcontent;
        if (subjects.length == 1)
            homeworkcontent = getString(R.string.homework_notification_1, subjectsstr);
        else homeworkcontent = getString(R.string.homework_notification_n, subjectsstr);
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.homework_notification_title))
                .setContentText(homeworkcontent)
                .setSmallIcon(R.drawable.ic_assignment_black_24dp)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setGroupSummary(false)
                .setShowWhen(false)
                .setGroup(NOTIFICATION_SCHOOLINFO)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify("homework", 1, n);
    }

    /**
     * Handle action to hide homework notification in the provided background thread
     */
    private void handleHideHomework() {
        Log.d(TAG, "Hiding homework notification");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel("homework", 1);
    }

    /**
     * Handle action to show lesson notification in the provided background thread with the provided
     * parameters.
     */
    private void handleShowLesson(String name, String location, String teacher) {
        if (name == null)
            return;
        Log.d(TAG, "Showing lesson notification: " + name);
        // Prepare target activity
        Intent intent = new Intent(this, MyScheduleActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // Prepare lesson notification text
        Spannable homeworkcontent;
        if (location != null && !location.isEmpty())
            homeworkcontent = new SpannableString(getString(R.string.lesson_notification, name, location, teacher));
        else
            homeworkcontent = new SpannableString(getString(R.string.lesson_notification_nolocation, name, teacher));
        homeworkcontent.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.lesson_notification_title))
                .setContentText(homeworkcontent)
                .setSmallIcon(R.drawable.ic_schedule_black_24dp)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setGroupSummary(false)
                .setShowWhen(false)
                .setGroup(NOTIFICATION_SCHOOLINFO)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify("lesson", 0, n);
    }

    /**
     * Handle action to hide lesson notification in the provided background thread
     */
    private void handleHideLesson() {
        Log.d(TAG, "Hiding lesson notification");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel("lesson", 0);
    }

    /**
     * Handle action to notify about a new grade in the provided background thread with the provided
     * parameters.
     */
    private void handleNotifyGrade(String value, String comment, String subject, String description, int id) {
        Log.d(TAG, "Notifying of grade: " + value + " in " + subject);
        // Prepare target activity
        Intent intent = new Intent(this, DataUtils.isTablet(this) ? EventsAndAssignmentsActivity.class : EventsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // Prepare event notification text
        String eventtitle;
        String eventcontent;
        if (value != null) {
            if (comment != null) eventtitle = value + " / " + comment + " [" + subject + "]";
            else eventtitle = value + " [" + subject + "]";
        } else if (comment != null) eventtitle = comment + " [" + subject + "]";
        else eventtitle = getString(R.string.app_name);

        if (description != null) eventcontent = description;
        else eventcontent = null;

        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(eventtitle)
                .setContentText(eventcontent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setSmallIcon(R.drawable.ic_assessment_black_24dp)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                //.setGroupSummary(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify("event", id, n);
    }

    private void handleDismissGradeNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Hiding grade notifications");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                String tag = notification.getTag();
                if (tag != null && tag.equals("event")) {
                    notificationManager.cancel("event", notification.getId());
                }
            }
        } else {
            Log.d(TAG, "Hiding grade notifications (and all others for legacy reasons)");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancelAll();
        }
    }
}
