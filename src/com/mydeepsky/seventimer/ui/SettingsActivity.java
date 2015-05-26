package com.mydeepsky.seventimer.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.mydeepsky.seventimer.R;
import com.mydeepsky.seventimer.core.pref.SettingsProvider;
import com.mydeepsky.seventimer.core.pref.UserPrefLocation;
import com.mydeepsky.seventimer.core.pref.UserPrefs;
import com.mydeepsky.seventimer.core.pref.UserPrefs.TempUnit;

public class SettingsActivity extends BaseActivity {
    public final static String EXTRA_NEED_UPDATE = "needUpdated";

    private final static int LOCATION_REQUEST_CODE = 1;

    private Spinner locationSpinner;
    private UserPrefs userPrefs;
    private ArrayAdapter<UserPrefLocation> adapter;
    private String oldLocationJson;
    private TempUnit oldTempUnit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.locationSpinner = (Spinner) findViewById(R.id.spinner_location);

        this.userPrefs = SettingsProvider.getInstance(this).getPrefs();
        this.adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, this.userPrefs.getSavedLocations());
        this.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.locationSpinner.setAdapter(this.adapter);

        UserPrefLocation currentLocation = this.userPrefs.getCurrentLocation();
        this.oldLocationJson = currentLocation.toJsonString();
        this.oldTempUnit = userPrefs.getTempUnit();
        int index = this.userPrefs.getLocationIndex(currentLocation.toString());
        if (index != -1) {
            this.locationSpinner.setSelection(index);
        } else {
            currentLocation.setName(getText(R.string.text_current_location).toString());
            addToSpinner(currentLocation);
        }

        ((RadioGroup) findViewById(R.id.radiogroup_locator))
                .setOnCheckedChangeListener(onLocatorChangeListener);
        ((RadioGroup) findViewById(R.id.radiogroup_temp))
                .setOnCheckedChangeListener(onTempChangeListener);

        if (userPrefs.isUseBaidu()) {
            ((RadioButton) findViewById(R.id.btn_locator_baidu)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.btn_locator_google)).setChecked(true);
        }

        if (userPrefs.getTempUnit() == TempUnit.CEL) {
            ((RadioButton) findViewById(R.id.btn_temp_cen)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.btn_temp_fah)).setChecked(true);
        }
    }

    private void addToSpinner(UserPrefLocation location) {
        this.adapter.remove(location);
        this.adapter.add(location);
        this.locationSpinner.setSelection(this.adapter.getCount() - 1);
    }

    public void onClickAddLocation(View v) {
        Intent intent = new Intent(this, AddLocationActivity.class);
        startActivityForResult(intent, LOCATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                addToSpinner(UserPrefLocation.parseFromJson(new JSONObject(data
                        .getStringExtra("newLocation"))));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void onClickDeleteLocation(View v) {
        if (this.adapter.getCount() > 1) {
            this.adapter.remove((UserPrefLocation) this.locationSpinner.getSelectedItem());
        } else {
            Toast.makeText(this, R.string.toast_remove_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickOK(View v) {
        userPrefs.setCurrentLocation(((UserPrefLocation) this.locationSpinner
                .getSelectedItem()).toJsonString());
        String newLocationJson = userPrefs.getCurrentLocationJsonString();
        TempUnit newTempUnit = userPrefs.getTempUnit();
        SettingsProvider.getInstance(this).savePrefs(userPrefs);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NEED_UPDATE, !newLocationJson.equals(this.oldLocationJson)
                || newTempUnit != oldTempUnit);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    private RadioGroup.OnCheckedChangeListener onLocatorChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
            case R.id.btn_locator_baidu:
                userPrefs.setUseBaidu(true);
                break;
            case R.id.btn_locator_google:
                userPrefs.setUseBaidu(false);
                break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener onTempChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
            case R.id.btn_temp_cen:
                userPrefs.setTempUnit("C");
                break;
            case R.id.btn_temp_fah:
                userPrefs.setTempUnit("F");
                break;
            }
        }
    };
}
