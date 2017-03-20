package ee.tartu.jpg.minuposka.ui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.timetable.utils.TimetableFilter;

/**
 * Provides accessors to stored settings.
 */
public class DataUtils {

    private static final String TAG = "DataUtils";

    public static final String LANGUAGE_SELECTION = "language_selection";
    public static final String NOTIFICATION_EVENTS_ENABLED = "display_event_notifications";
    public static final String NOTIFICATION_HOMEWORK_ENABLED = "display_homework_notification";
    public static final String NOTIFICATION_LESSON_ENABLED = "display_lesson_notification";
    private static final String EULA_ACCEPTED = "eula_accepted";
    private static final String DRAWER_INTRODUCED = "drawer_introduced";
    private static final String LAST_MENU_NAVIGATION = "last_menu_navigation";
    private static final String TIMETABLE_SELECTION = "timetable_selection";
    private static final String TIMETABLE_LAST_CHECK_SYSTEM = "timetable_last_check_system";
    private static final String TIMETABLE_FILTER_NAME = "timetable_filter_name";
    private static final String TIMETABLE_FILTER_SHORTNAME = "timetable_filter_shortname";
    private static final String TIMETABLE_FILTER_TYPE = "timetable_filter_type";

    public static boolean isEulaAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar cal = Calendar.getInstance();
        //noinspection WrongConstant
        if (cal.get(Calendar.MONTH) == Calendar.APRIL && cal.get(Calendar.DAY_OF_MONTH) == 1) {
            Log.d(TAG, "Activating April Fools Joke for EULA!");
            // APRIL FOOLS!
            return sp.getBoolean(EULA_ACCEPTED + ".april" + cal.get(Calendar.YEAR), false);
        }

        return sp.getBoolean(EULA_ACCEPTED, false);
    }

    public static void markEulaAccepted(final Context context) {
        Log.d(TAG, "Marking EULA accepted.");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar cal = Calendar.getInstance();
        //noinspection WrongConstant
        if (cal.get(Calendar.MONTH) == Calendar.APRIL && cal.get(Calendar.DAY_OF_MONTH) == 1) {
            Log.d(TAG, "Accepting April Fools Joke for EULA!");
            // APRIL FOOLS!
            sp.edit().putBoolean(EULA_ACCEPTED + ".april" + cal.get(Calendar.YEAR), true).apply();
        }
        sp.edit().putBoolean(EULA_ACCEPTED, true).apply();
    }

    public static boolean isDrawerIntroduced(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(DRAWER_INTRODUCED, false);
    }

    public static void markDrawerIntroduced(final Context context) {
        Log.d(TAG, "Marking drawer introduced.");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(DRAWER_INTRODUCED, true).apply();
    }

    public static int getLastMenuNavigation(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int i = sp.getInt(LAST_MENU_NAVIGATION, R.id.drawer_login);
        if (i == R.id.drawer_settings) {
            i = R.id.drawer_schedules; // To avoid situation where only settings can be accessed
        }
        return i;
    }

    public static void setLastMenuNavigation(final Context context, int i) {
        Log.d(TAG, "Storing last menu navigation: " + i);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(LAST_MENU_NAVIGATION, i).apply();
    }

    public static String getLanguage(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sp.getString(LANGUAGE_SELECTION, "et");

        Calendar cal = Calendar.getInstance();
        //noinspection WrongConstant
        if (cal.get(Calendar.MONTH) == Calendar.APRIL && cal.get(Calendar.DAY_OF_MONTH) == 1) {
            Log.d(TAG, "Activating April Fools Joke for app language!");
            // APRIL FOOLS!
            lang = "wo";
            setLanguage(context, lang);
        }
        return lang;
    }

    public static void setLanguage(final Context context, String str) {
        Log.d(TAG, "Storing language: " + str);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(LANGUAGE_SELECTION, str).apply();
    }

    public static boolean isNotificationEventsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(NOTIFICATION_EVENTS_ENABLED, true);
    }

    public static void setNotificationEventsEnabled(final Context context, boolean b) {
        Log.d(TAG, "Storing events notifications enabled: " + b);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(LANGUAGE_SELECTION, b).apply();
    }

    public static boolean isNotificationHomeworkEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(NOTIFICATION_HOMEWORK_ENABLED, true);
    }

    public static void setNotificationHomeworkEnabled(final Context context, boolean b) {
        Log.d(TAG, "Storing homework notification enabled: " + b);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(NOTIFICATION_HOMEWORK_ENABLED, b).apply();
    }

    public static boolean isNotificationLessonEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(NOTIFICATION_LESSON_ENABLED, true);
    }

    public static void setNotificationLessonEnabled(final Context context, boolean b) {
        Log.d(TAG, "Storing lesson notification enabled: " + b);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(NOTIFICATION_LESSON_ENABLED, b).apply();
    }

    public static String getTimetableSelection(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(TIMETABLE_SELECTION, null);
    }

    public static void setTimetableSelection(final Context context, String str) {
        Log.d(TAG, "Storing timetable selection: " + str);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(TIMETABLE_SELECTION, str).apply();
    }

    public static long getLastTimetableCheckSystem(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(TIMETABLE_LAST_CHECK_SYSTEM, 0);
    }

    public static void setLastTimetableCheckSystem(final Context context, long lastTimetableCheckSystem) {
        Log.d(TAG, "Storing last timetable check time on system: " + lastTimetableCheckSystem);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(TIMETABLE_LAST_CHECK_SYSTEM, lastTimetableCheckSystem).apply();
    }

    public static TimetableFilter getTimetableFilter(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int type = sp.getInt(TIMETABLE_FILTER_TYPE, -1);
        if (type == -1) {
            return null;
        }
        String name = sp.getString(TIMETABLE_FILTER_NAME, null);
        String shortname = sp.getString(TIMETABLE_FILTER_SHORTNAME, null);
        return new TimetableFilter(type, name, shortname);
    }

    public static void setTimetableFilter(final Context context, TimetableFilter timetableFilter) {
        Log.d(TAG, "Storing timetable filter: " + timetableFilter);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spedit = sp.edit();
        if (timetableFilter == null) {
            spedit.putInt(TIMETABLE_FILTER_TYPE, -1);
        } else {
            spedit.putString(TIMETABLE_FILTER_NAME, timetableFilter.name)
                    .putString(TIMETABLE_FILTER_SHORTNAME, timetableFilter.shortname)
                    .putInt(TIMETABLE_FILTER_TYPE, timetableFilter.type);
        }
        spedit.apply();
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static boolean hideDrawer(Context context) {
        return context.getResources().getBoolean(R.bool.hideDrawer);
    }

    public static boolean isStoreVersion(Context context) {
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        return installer != null && !installer.trim().isEmpty();
    }
}
