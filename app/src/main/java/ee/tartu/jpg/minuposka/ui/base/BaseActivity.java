package ee.tartu.jpg.minuposka.ui.base;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.NotificationService;
import ee.tartu.jpg.minuposka.service.trigger.HomeworkNotificationScheduler;
import ee.tartu.jpg.minuposka.service.trigger.LessonNotificationScheduler;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;

/**
 * Provides useful methods, is an underlying activity for all other activities.
 */
public abstract class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "BaseActivity";

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    protected static final long MAIN_CONTENT_FADEOUT_DURATION = 150;
    protected static final long MAIN_CONTENT_FADEIN_DURATION = 250;

    protected Toolbar mToolbar;
    View mMainContent;

    private boolean paused;
    private boolean restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextUtils.updateLanguage(this);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        SP.registerOnSharedPreferenceChangeListener(this);
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mMainContent = findViewById(R.id.mainContent);
        if (mToolbar != null) {
            refreshTitle();
            setSupportActionBar(mToolbar);
        }
    }

    public void refreshTitle() {
        String title = title();
        if (title != null) {
            mToolbar.setTitle(title);
        }
    }

    protected String title() {
        try {
            PackageManager pm = getPackageManager();
            ActivityInfo activityInfo = pm.getActivityInfo(getComponentName(), 0);
            if (activityInfo.labelRes != 0) {
                return getString(activityInfo.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to find package manager", e);
        }
        return getString(R.string.app_name);
    }

    @Override
    protected void onResume() {
        TextUtils.updateLanguage(this);
        super.onResume();
        if (restart) {
            finish();
            startActivity(getIntent());
        }
        paused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case DataUtils.LANGUAGE_SELECTION:
                String newlang = DataUtils.getLanguage(this);
                Log.i(TAG, "Language changed to: " + newlang);
                if (paused) restart = true;
                else TextUtils.updateLanguage(this);
                break;
            case DataUtils.NOTIFICATION_EVENTS_ENABLED:
                NotificationService.dismissGradeNotifications(this);
                break;
            case DataUtils.NOTIFICATION_HOMEWORK_ENABLED:
                HomeworkNotificationScheduler.updateBroadcastReceiver(this);
                break;
            case DataUtils.NOTIFICATION_LESSON_ENABLED:
                LessonNotificationScheduler.updateBroadcastReceiver(this);
                break;
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mMainContent != null) {
            mMainContent.setAlpha(0);
            mMainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
        if (mToolbar != null) {
            int[] attrs = {android.R.attr.textColorPrimary};
            TypedArray ta = obtainStyledAttributes(R.style.ToolbarNormalTheme, attrs);
            final int textColor = ta.getColor(0, Color.WHITE);

            ValueAnimator va = ValueAnimator.ofObject(new IntEvaluator(), 0, 255).setDuration(MAIN_CONTENT_FADEIN_DURATION);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mToolbar.setTitleTextColor(Color.argb((int) animation.getAnimatedValue(), Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
                }
            });
            va.start();
            ta.recycle();
        }
    }

    protected void preFinish() {
        View mainContent = findViewById(R.id.mainContent);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }
        if (mToolbar != null) {
            int[] attrs = {android.R.attr.textColorPrimary};
            TypedArray ta = obtainStyledAttributes(R.style.ToolbarNormalTheme, attrs);
            final int textColor = ta.getColor(0, Color.WHITE);

            ValueAnimator va = ValueAnimator.ofObject(new IntEvaluator(), 255, 0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mToolbar.setTitleTextColor(Color.argb((int) animation.getAnimatedValue(), Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
                }
            });
            va.start();
            ta.recycle();
        }
    }

    protected abstract int getLayout();
}

