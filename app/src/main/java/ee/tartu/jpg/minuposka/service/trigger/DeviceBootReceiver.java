package ee.tartu.jpg.minuposka.service.trigger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Restarts schedulers on system boot
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    private static final String TAG = "DeviceBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(TAG, "Starting schedulers");
            HomeworkNotificationScheduler.startBroadcastReceiver(context);
            LessonNotificationScheduler.startBroadcastReceiver(context);
        }
    }
}