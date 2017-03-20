package ee.tartu.jpg.minuposka.ui.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;
import ee.tartu.jpg.stuudium.Stuudium;
import ee.tartu.jpg.stuudium.StuudiumEventListener;
import ee.tartu.jpg.stuudium.data.Assignment;
import ee.tartu.jpg.stuudium.data.DataSet;
import ee.tartu.jpg.stuudium.data.Event;
import ee.tartu.jpg.stuudium.data.Journal;
import ee.tartu.jpg.stuudium.data.Person;
import ee.tartu.jpg.stuudium.data.User;

/**
 * Provides Stuudium methods along with other useful methods and a drawer.
 */
public abstract class StuudiumBaseActivity extends DrawerBaseActivity implements StuudiumEventListener {

    private static final String TAG = "StuudiumBaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stuudium.attach(this);

        refreshAccountProfileOnThread();
        refreshDrawerItems();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Stuudium.detach(this);
    }

    private void refreshAccountProfile() {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                refreshAccountProfileOnThread();
            }
        });
    }

    private void refreshAccountProfileOnThread() {
        // If UI not initialised yet, skip
        if (mDrawerLayout == null) return;

        Log.d(TAG, "Refreshing Stuudium account profile picture");
        View headerLayout = mNavigationView.getHeaderView(0);
        TextView userTitle = (TextView) headerLayout.findViewById(R.id.user_title);
        TextView userSubtitle = (TextView) headerLayout.findViewById(R.id.user_subtitle);
        ImageView userAvatar = (ImageView) headerLayout.findViewById(R.id.user_avatar);
        User u = Stuudium.getUser();
        if (u != null) {
            Person p = u.getIdentity();
            if (p != null) {
                userTitle.setText(p.getFullName());
                userAvatar.setImageDrawable(p.getAvatar().getDrawable(getResources().getDrawable(R.drawable.ic_face_white_64dp), true));
                userAvatar.setVisibility(View.VISIBLE);
            }
            userSubtitle.setText(TextUtils.translateFromEstonian(this, u.getRoleString()));
        } else {
            userTitle.setText("");
            userSubtitle.setText("");
            // userAvatar.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshDrawerItems() {
        new Handler(getMainLooper()).post(new Runnable() {
            public void run() {
                refreshDrawerItemsOnThread();
            }
        });
    }

    private void refreshDrawerItemsOnThread() {
        if (mNavigationView == null)
            return;
        mNavigationView.getMenu().setGroupVisible(R.id.groupLogin, !Stuudium.isLoggedIn());
        mNavigationView.getMenu().setGroupVisible(R.id.groupStuudium, Stuudium.isLoggedIn());
        mNavigationView.getMenu().findItem(R.id.drawer_my_schedule).setVisible(Stuudium.isLoggedIn());
    }


    @Override
    public void onStuudiumLogin() {
        Log.i(TAG, "Logged in to Stuudium, refreshing drawer");
        refreshDrawerItems();
    }

    @Override
    public void onStuudiumLogout() {
        Log.i(TAG, "Logged out of Stuudium, refreshing drawer");
        refreshDrawerItems();
        navigate(R.id.drawer_login);
    }

    @Override
    public void onAvatarLoaded(Person p, Bitmap bitmap, int size) {
        Log.d(TAG, "Stuudium avatar loaded, refreshing account info in drawer");
        Person identity = Stuudium.getUserIdentity();
        if (identity != null && identity.equals(p)) refreshAccountProfile();
    }

    @Override
    public void onEventsLoaded(Person p, DataSet<Event> events) {
        //Log.d(TAG, "Stuudium events loaded");
    }

    @Override
    public void onNewEvent(Person p, Event event, boolean manualUpdate) {
        //Log.d(TAG, "New Stuudium event");
    }

    @Override
    public void onAssignmentsLoaded(Person p, DataSet<Assignment> assignments, boolean manualUpdate) {
        //Log.d(TAG, "Stuudium assignments loaded");
    }

    @Override
    public void onAssignmentPushCompleted(Person p, Assignment assignment) {
        //Log.d(TAG, "Stuudium assignment push completed");
    }

    @Override
    public void onJournalsLoaded(Person p, DataSet<Journal> journals) {
        //Log.d(TAG, "Stuudium journals loaded");
    }

    @Override
    public void onUserDataLoaded(User u, boolean manualUpdate) {
        Log.d(TAG, "Stuudium user data loaded, refreshing account info in drawer");
        refreshAccountProfile();
    }

    @Override
    public void onPeopleChanged(Map<String, Person> people) {
        //Log.d(TAG, "Stuudium people changed");
    }

    @Override
    public void onError(String str) {
        //Log.w(TAG, "Stuudium error: " + str);
    }

    @Override
    public void onLoadingStarted() {
        //Log.d(TAG, "Stuudium loading started");
    }

    @Override
    public void onLoadingFinished() {
        //Log.d(TAG, "Stuudium loading finished");
    }
}
