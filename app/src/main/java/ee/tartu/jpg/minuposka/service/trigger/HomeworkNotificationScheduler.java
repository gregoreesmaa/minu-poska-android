package ee.tartu.jpg.minuposka.service.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ee.tartu.jpg.minuposka.service.NotificationService;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.utils.DateUtils;

/**
 * Schedules and triggers homework notifications.
 */
public class HomeworkNotificationScheduler extends BroadcastReceiver {

    private static final String TAG = "HomeworkNotificationSch";

    private static final int SHOW_NOTIFICATION_TIME = 17;
    private static final int HIDE_NOTIFICATION_TIME = 10;

    public static void startBroadcastReceiver(Context context) {
        Log.d(TAG, "Starting homework update broadcast receiver");
        setNextAlarm(context);
    }

    public static void updateBroadcastReceiver(Context context) {
        Log.d(TAG, "Updating homework update broadcast receiver");
        setNextAlarm(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, NotificationService.class);
        if (intent.getAction().equals(NotificationService.ACTION_SHOW_HOMEWORK)) {
            if (!Stuudium.isLoggedIn()) {
                Log.d(TAG, "Not logged in to Stuudium, thus can't show homework notification");
                return;
            }
            // Find out if tomorrow's homework should be shown
            Calendar cal = Calendar.getInstance();
            boolean forTomorrow = cal.get(Calendar.HOUR_OF_DAY) >= SHOW_NOTIFICATION_TIME;
            Person p = Stuudium.getUserIdentity();
            if (p == null) {
                Log.w(TAG, "User identity not found");
                return;
            }
            DataSet<Assignment> assignmentDataSet = p.getAssignments();
            Set<String> subjectSet = new HashSet<>();
            for (Assignment assignment : assignmentDataSet) {
                Assignment.Content content = assignment.getContent();
                if (content == null)
                    continue;
                if (content.isCompleted())
                    continue;
                Date deadline = content.getDeadline();
                if (deadline == null)
                    continue;
                if (forTomorrow) {
                    if (!DateUtils.isWithinDaysFuture(deadline, 1))
                        continue;
                } else if (!DateUtils.isToday(deadline))
                    continue;
                String subject = content.getSubject();
                if (subject == null)
                    continue;
                subjectSet.add(TextUtils.translateFromEstonian(context, subject));
            }
            String[] subjects = subjectSet.toArray(new String[subjectSet.size()]);
            if (subjects.length == 0) {
                intent.setAction(NotificationService.ACTION_HIDE_HOMEWORK);
            } else {
                intent.putExtra(NotificationService.HOMEWORK_SUBJECTS, subjects);
            }
        }
        Log.d(TAG, "Forwarding notification for: " + intent.getAction());
        context.startService(intent);
    }

    private static void setNextAlarm(Context context) {
        if (!DataUtils.isNotificationHomeworkEnabled(context)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent showIntent = new Intent(context, HomeworkNotificationScheduler.class);
            PendingIntent showPendingIntent = PendingIntent.getBroadcast(context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(showPendingIntent);
            Intent hideIntent = new Intent(context, HomeworkNotificationScheduler.class);
            PendingIntent hidePendingIntent = PendingIntent.getBroadcast(context, 1, hideIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(hidePendingIntent);
            return;
        }
        Log.d(TAG, "Setting next homework alarm");
        // Calculate time to display the notification
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) < HIDE_NOTIFICATION_TIME)
            // When notification should still be shown for today
            cal.add(Calendar.DAY_OF_WEEK, -1);
        cal.set(Calendar.HOUR_OF_DAY, SHOW_NOTIFICATION_TIME);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long showNotifMillis = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_WEEK, 1);
        cal.set(Calendar.HOUR_OF_DAY, HIDE_NOTIFICATION_TIME);
        long hideNotifMillis = cal.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Set notification show repeating time
        Intent showIntent = new Intent(context, HomeworkNotificationScheduler.class);
        showIntent.setAction(NotificationService.ACTION_SHOW_HOMEWORK);
        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent showPendingIntent = PendingIntent.getBroadcast(context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC, showNotifMillis, AlarmManager.INTERVAL_DAY, showPendingIntent);

        // Set notification hide repeating time
        Intent hideIntent = new Intent(context, HomeworkNotificationScheduler.class);
        hideIntent.setAction(NotificationService.ACTION_HIDE_HOMEWORK);
        hideIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent hidePendingIntent = PendingIntent.getBroadcast(context, 1, hideIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC, hideNotifMillis, AlarmManager.INTERVAL_DAY, hidePendingIntent);
    }

}