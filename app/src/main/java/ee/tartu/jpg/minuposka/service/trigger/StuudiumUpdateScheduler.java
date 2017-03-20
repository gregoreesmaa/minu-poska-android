package ee.tartu.jpg.minuposka.service.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ee.tartu.jpg.minuposka.service.DataUpdateService;

/**
 * Schedules and triggers Stuudium updates for the specified interval.
 */
public class StuudiumUpdateScheduler extends BroadcastReceiver {

    private static final String TAG = "StuudiumUpdateScheduler";

    private static final long OTHER_INTERVAL = AlarmManager.INTERVAL_DAY / 2;
    private static final long EVENT_INTERVAL = AlarmManager.INTERVAL_DAY / 48;

    public static void startBroadcastReceiver(Context context) {
        Log.d(TAG, "Starting Stuudium update schedulers");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent updateEvents = new Intent(context, StuudiumUpdateScheduler.class);
        updateEvents.setAction(DataUpdateService.ACTION_UPDATE_STUUDIUM);
        updateEvents.putExtra(DataUpdateService.UPDATE_STUUDIUM_EVENTS, true);
        PendingIntent updateEventsPendingIntent = PendingIntent.getBroadcast(context, 0, updateEvents, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), EVENT_INTERVAL, updateEventsPendingIntent);

        Intent updateOther = new Intent(context, StuudiumUpdateScheduler.class);
        updateOther.setAction(DataUpdateService.ACTION_UPDATE_STUUDIUM);
        updateOther.putExtra(DataUpdateService.UPDATE_STUUDIUM_USER, true);
        updateOther.putExtra(DataUpdateService.UPDATE_STUUDIUM_TODOS, true);
        updateOther.putExtra(DataUpdateService.UPDATE_STUUDIUM_JOURNALS, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, updateOther, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), OTHER_INTERVAL, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Forwarding Stuudium update trigger for: " + intent.getAction());
        intent.setClass(context, DataUpdateService.class);
        intent.putExtra(DataUpdateService.MANUAL_UPDATE, false);
        context.startService(intent);
    }
}
