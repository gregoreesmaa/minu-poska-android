package ee.tartu.jpg.minuposka.ui;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import ee.tartu.jpg.minuposka.R;
import ee.tartu.jpg.minuposka.ui.base.BaseActivity;
import ee.tartu.jpg.minuposka.ui.util.DataUtils;

public class EulaActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataUtils.markEulaAccepted(getBaseContext());
                Intent intent = new Intent(getBaseContext(), LaunchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.button_decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner spinner = ((Spinner) findViewById(R.id.language_spinner));

        String selectedLanguage = DataUtils.getLanguage(this);
        final TypedArray selectedValues = getResources().obtainTypedArray(R.array.languagesValues);
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (selectedValues.getString(i).equals(selectedLanguage)) {
                spinner.setSelection(i);
                break;
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = selectedValues.getString(position);
                DataUtils.setLanguage(getBaseContext(), selectedValue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_eula;
    }

}