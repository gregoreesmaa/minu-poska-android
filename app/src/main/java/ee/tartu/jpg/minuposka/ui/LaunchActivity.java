package ee.tartu.jpg.minuposka.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ee.tartu.jpg.minuposka.PoskaApplication;
import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.DrawerBaseActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;
import ee.tartu.jpg.minuposka.ui.util.TextUtils;

/**
 * Created by gregor on 9/15/2015.
 */
public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextUtils.updateLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    protected void onStart() {
        TextUtils.updateLanguage(this);
        super.onStart();
        if (!DataUtils.isEulaAccepted(this)) {
            startActivity(new Intent(this, EulaActivity.class));
            finish();
        } else {
            DrawerBaseActivity.openActivity(this, DataUtils.getLastMenuNavigation(this));
            finish();
        }
    }
}
