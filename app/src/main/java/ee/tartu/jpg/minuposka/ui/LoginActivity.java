package ee.tartu.jpg.minuposka.ui;

import android.os.Build;
import android.os.Bundle;

import ee.tartu.jpg.minuposka.PoskaApplication;
import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.WebviewActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.stuudium.Stuudium;

/**
 * Created by gregor on 9/14/2015.
 */
public class LoginActivity extends WebviewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21)
            mToolbar.setElevation(0);
        if (Stuudium.isLoggedIn()) {
            onLogin();
        } else {
            Stuudium.authorizeIn(vWebView);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    public String getCssFile(String url) {
        return DataUtils.isTablet(this) ? "stuudium_auth_page_tablet.css" : "stuudium_auth_page.css";
    }

    @Override
    public boolean shouldOverrideUrlLoading(String url) {
        if (url.startsWith(PoskaApplication.stuudiumSettings.getAuthRedirectUri())) {
            Stuudium.onAuthorizationResponse(url);
            return true;
        }
        return false;
    }

    @Override
    protected int getMenuItem() {
        return R.id.drawer_login;
    }

    @Override
    public void onStuudiumLogin() {
        super.onStuudiumLogin();
        onLogin();
    }

    private void onLogin() {
        if (DataUtils.isTablet(this)) {
            navigate(R.id.drawer_events_and_assignments, MAIN_CONTENT_FADEOUT_DURATION);
        } else {
            navigate(R.id.drawer_events, MAIN_CONTENT_FADEOUT_DURATION);
        }
    }

}
