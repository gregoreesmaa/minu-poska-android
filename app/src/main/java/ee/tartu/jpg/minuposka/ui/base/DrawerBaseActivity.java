package ee.tartu.jpg.minuposka.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.AssignmentsActivity;
import ee.tartu.jpg.minuposka.ui.EventsActivity;
import ee.tartu.jpg.minuposka.ui.EventsAndAssignmentsActivity;
import ee.tartu.jpg.minuposka.ui.LoginActivity;
import ee.tartu.jpg.minuposka.ui.MessagesActivity;
import ee.tartu.jpg.minuposka.ui.MyScheduleActivity;
import ee.tartu.jpg.minuposka.ui.SchedulesActivity;
import ee.tartu.jpg.minuposka.ui.SettingsActivity;
import ee.tartu.jpg.minuposka.ui.TeraActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;

/**
 * Provides useful methods along with a drawer. Should be used for activities that require a drawer.
 */
public abstract class DrawerBaseActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DrawerBaseActivity";

    private static final int DRAWER_CLOSE_DELAY_MS = 250;

    private final Handler mDrawerActionHandler = new Handler();
    private ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // listen for navigation events
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);

            // select the correct nav menu item
            MenuItem menuItem = mNavigationView.getMenu().findItem(getMenuItem());
            if (menuItem != null)
                menuItem.setChecked(true);
            if (DataUtils.hideDrawer(this)) {
                // set up the hamburger icon to open and close the drawer_logged_in
                mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close) {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        super.onDrawerSlide(drawerView, 0);
                    }
                };
                mDrawerLayout.setDrawerListener(mDrawerToggle);
                mDrawerToggle.syncState();
            } else {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                mDrawerLayout.setScrimColor(0x00000000);
                mDrawerLayout.requestDisallowInterceptTouchEvent(true);
                if (mToolbar != null) {
                    RelativeLayout.LayoutParams mToolbarLayoutParams = ((RelativeLayout.LayoutParams) mToolbar.getLayoutParams());
                    mToolbarLayoutParams.setMargins(mToolbarLayoutParams.leftMargin + getResources().getDimensionPixelSize(R.dimen.drawer_size), mToolbarLayoutParams.topMargin, mToolbarLayoutParams.rightMargin, mToolbarLayoutParams.bottomMargin);
                }
                if (mMainContent != null) {
                    RelativeLayout.LayoutParams mMainContentLayoutParams = ((RelativeLayout.LayoutParams) mMainContent.getLayoutParams());
                    mMainContentLayoutParams.setMargins(mMainContentLayoutParams.leftMargin + getResources().getDimensionPixelSize(R.dimen.drawer_size), mMainContentLayoutParams.topMargin, mMainContentLayoutParams.rightMargin, mMainContentLayoutParams.bottomMargin);
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mNavigationView != null && !DataUtils.isDrawerIntroduced(this)) {
            Log.i(TAG, "Introducing drawer");
            mDrawerLayout.openDrawer(GravityCompat.START);
            DataUtils.markDrawerIntroduced(this);
        }
    }

    protected abstract int getMenuItem();

    public static void openActivity(Context context, int item) {
        Log.i(TAG, "Opening activity on menu item: " + item);
        Intent intent;
        String screenName;
        switch (item) {
            case R.id.drawer_login:
                intent = new Intent(context, LoginActivity.class);
                screenName = "Login";
                break;
            case R.id.drawer_events:
                intent = new Intent(context, EventsActivity.class);
                screenName = "Events";
                break;
            case R.id.drawer_assignments:
                intent = new Intent(context, AssignmentsActivity.class);
                screenName = "Assignments";
                break;
            case R.id.drawer_events_and_assignments:
                intent = new Intent(context, EventsAndAssignmentsActivity.class);
                screenName = "Events and Assignments (tablet)";
                break;
            case R.id.drawer_messages:
                intent = new Intent(context, MessagesActivity.class);
                screenName = "Messages";
                break;
            case R.id.drawer_tera:
                intent = new Intent(context, TeraActivity.class);
                screenName = "Tera";
                break;
            case R.id.drawer_my_schedule:
                intent = new Intent(context, MyScheduleActivity.class);
                screenName = "My Schedule";
                break;
            case R.id.drawer_schedules:
                intent = new Intent(context, SchedulesActivity.class);
                screenName = "Schedules";
                break;
            case R.id.drawer_settings:
                intent = new Intent(context, SettingsActivity.class);
                screenName = "Settings";
                break;
            default:
                intent = new Intent(context, SchedulesActivity.class);
                screenName = "Schedules";
                break;
        }
        context.startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // update highlighted item in the navigation menu
        menuItem.setChecked(true);

        // allow some time after closing the drawer_logged_in before performing real navigation
        // so the user can see what is happening

        if (mNavigationView != null && DataUtils.hideDrawer(this))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        navigate(menuItem.getItemId());
        return true;
    }

    protected void navigate(final int id) {
        if (id == getMenuItem()) {
            Log.i(TAG, "Not navigating to current menu item");
            return;
        }
        long delay = Math.max(DataUtils.hideDrawer(this) ? DRAWER_CLOSE_DELAY_MS : 0, MAIN_CONTENT_FADEOUT_DURATION);
        navigate(id, delay);
    }

    protected void navigate(final int id, long delay) {
        if (id == getMenuItem()) {
            Log.i(TAG, "Not navigating to current menu item with delay");
            return;
        }
        Log.i(TAG, "Navigating after a delay of " + delay + " ms");
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateStraight(id);
            }
        }, delay);
        // Stop activity if didn't click on settings
        if (id != R.id.drawer_settings) mDrawerActionHandler.post(new Runnable() {
            @Override
            public void run() {
                preFinish();
            }
        });
    }

    private void navigateStraight(int item) {
        if (item == getMenuItem()) {
            Log.i(TAG, "Not navigating to current menu item straight");
            return;
        }
        Log.i(TAG, "Navigating to menu item: " + item);
        openActivity(this, item);

        if (item != R.id.drawer_settings) {
            DataUtils.setLastMenuNavigation(this, item);
            finish();
        }
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home && mDrawerToggle != null)
            return mDrawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (DataUtils.hideDrawer(this) && mDrawerLayout != null && mNavigationView != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "Closing drawer on back button press");
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
