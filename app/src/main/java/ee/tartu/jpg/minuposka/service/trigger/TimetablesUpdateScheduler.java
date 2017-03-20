package ee.tartu.jpg.minuposka.service.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ee.tartu.jpg.minuposka.service.DataUpdateService;

/**
 * Schedules and triggers timetables update
 */
public class TimetablesUpdateScheduler extends BroadcastReceiver {

    private static final String TAG = "TimetablesUpdateSchedul";

    private static final long INTERVAL = AlarmManager.INTERVAL_DAY;

    public static void startBroadcastReceiver(Context context) {
        Log.d(TAG, "Starting Timetable update scheduler");
        Intent broadcastReceiver = new Intent(context, TimetablesUpdateScheduler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Forwarding timetables update trigger.");
        DataUpdateService.startTimetablesUpdate(context);
    }
}
