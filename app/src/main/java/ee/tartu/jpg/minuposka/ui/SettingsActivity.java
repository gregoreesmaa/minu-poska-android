package ee.tartu.jpg.minuposka.ui;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.service.DataUpdateService;
import ee.tartu.jpg.minuposka.ui.base.StuudiumBaseActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.stuudium.Stuudium;

public class SettingsActivity extends StuudiumBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DataUtils.hideDrawer(this)) {
            mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, MAIN_CONTENT_FADEOUT_DURATION);
                    preFinish();
                }
            });
        }

        MainSettingsFragment mainSettingsFragment = new MainSettingsFragment();

        getFragmentManager().beginTransaction().replace(R.id.mainContent, mainSettingsFragment).commit();
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_settings;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_settings;
    }

    public static class MainSettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!DataUtils.isStoreVersion(getContext())) {
                    addPreferencesFromResource(R.xml.dev_actions);
                    final Preference stuudiumUpdateButton = findPreference("stuudium_events_update");
                    stuudiumUpdateButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            DataUpdateService.startStuudiumEventsUpdate(getContext(), false);
                            return true;
                        }
                    });
                }
            }

            final Preference logOutButton = findPreference("log_out_button");
            logOutButton.setEnabled(Stuudium.isLoggedIn());
            logOutButton.setShouldDisableView(!Stuudium.isLoggedIn());
            logOutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.preference_log_out)
                            .setMessage(R.string.preference_log_out_confirmation)
                            .setPositiveButton(R.string.preference_log_out, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Stuudium.unauthorize();
                                    logOutButton.setEnabled(false);
                                    logOutButton.setShouldDisableView(true);
                                }

                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    return true;
                }
            });

        }
    }
}