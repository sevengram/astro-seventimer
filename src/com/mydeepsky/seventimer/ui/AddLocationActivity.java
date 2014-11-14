package com.mydeepsky.seventimer.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.mydeepsky.android.location.Locator;
import com.mydeepsky.android.location.Locator.LocationInfo;
import com.mydeepsky.android.location.LocatorActivity;
import com.mydeepsky.android.location.LocatorFactory;
import com.mydeepsky.android.location.LocatorFactory.LocatorType;
import com.mydeepsky.android.util.DeviceUtil;
import com.mydeepsky.seventimer.R;
import com.mydeepsky.seventimer.core.pref.SettingsProvider;
import com.mydeepsky.seventimer.core.pref.UserPrefLocation;
import com.mydeepsky.seventimer.core.pref.UserPrefs;
import com.mydeepsky.seventimer.stat.StatManager;
import com.mydeepsky.seventimer.ui.dialog.DialogManager;
import com.umeng.analytics.MobclickAgent;

public class AddLocationActivity extends LocatorActivity {

    private Dialog refreshDialog;

    private EditText longitudeEditText;
    private EditText latitudeEditText;
    private EditText locationNameEditText;

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int sdk = DeviceUtil.getAndroidSdkVersionCode();
        if (sdk >= 14) {
            setTheme(android.R.style.Theme_DeviceDefault_Dialog);
        } else if (sdk >= 11) {
            setTheme(android.R.style.Theme_Holo_Dialog);
        }
        setTitle(R.string.title_add_location);
        setContentView(R.layout.activity_addlocation);
        this.latitudeEditText = (EditText) findViewById(R.id.edittext_latitude);
        this.longitudeEditText = (EditText) findViewById(R.id.edittext_longitude);
        this.locationNameEditText = (EditText) findViewById(R.id.edittext_name);

        longitudeEditText.setOnFocusChangeListener(editLongitudeListener);
        latitudeEditText.setOnFocusChangeListener(editLatitudeListener);

        this.refreshDialog = DialogManager.createRefreshDialog(this);

        UserPrefs userPrefs = SettingsProvider.getInstance(this).getPrefs();
        this.locator = LocatorFactory.createLocator(this,
                userPrefs.isUseBaidu() ? LocatorType.BAIDU : LocatorType.GOOGLE);
    }

    private OnFocusChangeListener editLongitudeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                try {
                    double longitude = Double.parseDouble(longitudeEditText.getText().toString());
                    if (longitude > 180) {
                        longitudeEditText.setText("180.0");
                    } else if (longitude < -180) {
                        longitudeEditText.setText("-180.0");
                    }
                } catch (NumberFormatException e) {
                    longitudeEditText.setText("0.0");
                }
            }
        }
    };

    private void showRefreshDialog() {
        if (!isFinishing() && refreshDialog != null) {
            refreshDialog.show();
        }
    }

    private void dismissRefreshDialog() {
        if (!isFinishing() && refreshDialog != null) {
            refreshDialog.dismiss();
        }
    }

    private OnFocusChangeListener editLatitudeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                try {
                    double latitude = Double.parseDouble(latitudeEditText.getText().toString());
                    if (latitude > 89) {
                        latitudeEditText.setText("89.0");
                    } else if (latitude < -89) {
                        latitudeEditText.setText("-89.0");
                    }
                } catch (NumberFormatException e) {
                    latitudeEditText.setText("0.0");
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    };

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        refreshDialog = null;
        super.onDestroy();
    }

    public void onClickLocation(View v) {
        showRefreshDialog();
        startLocator();
    }

    public void onClickOK(View v) {
        String name = locationNameEditText.getText().toString();
        if (name.equals("")) {
            name = "Default";
        }
        String longitudeString = longitudeEditText.getText().toString();
        if (longitudeString.equals("")) {
            longitudeString = "0.0";
        }
        String latitudeString = latitudeEditText.getText().toString();
        if (latitudeString.equals("")) {
            latitudeString = "0.0";
        }
        double longitude;
        try {
            longitude = Double.parseDouble(longitudeString);
        } catch (NumberFormatException e) {
            longitude = 0.0;
        }
        if (longitude > 180) {
            longitude = 180;
        } else if (longitude < -180) {
            longitude = -180.0;
        }
        double latitude;
        try {
            latitude = Double.parseDouble(latitudeString);
        } catch (NumberFormatException e) {
            latitude = 0.0;
        }
        if (latitude > 89) {
            latitude = 89.0;
        } else if (latitude < -89) {
            latitude = -89.0;
        }
        Intent intent = new Intent();
        intent.putExtra("newLocation",
                new UserPrefLocation(latitude, longitude, name).toJsonString());
        setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    public void onLocationUpdate(LocationInfo location, long costTime, int locator) {
        if (location == null) {
            return;
        }
        dismissRefreshDialog();
        longitudeEditText.setText(location.getLongitude() + "");
        latitudeEditText.setText(location.getLatitude() + "");
        locationNameEditText.setText(new StringBuffer().append(location.getCity()).append(',')
                .append(location.getDistrict()));
        StatManager.getInstance().sendLocationStat(location, costTime, locator);
    }

    @Override
    public void onLocationError(boolean showMessage) {
        if (showMessage) {
            Toast.makeText(this, R.string.toast_location_fail, Toast.LENGTH_SHORT).show();
        }
        dismissRefreshDialog();
    }

    @Override
    public void onWithoutService(int error) {
        if (isFinishing()) {
            return;
        }

        DialogInterface.OnClickListener onNegativeClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onLocationError(false);
            }
        };
        if (error == Locator.ERR_NO_NETWORK) {
            Dialog dialog = DialogManager.createNetworkDialog(this, onNegativeClickListener);
            dialog.show();
        } else if (error == Locator.ERR_NO_LOCATION_SERVICE) {
            Dialog dialog = DialogManager.createLocationDialog(this, onNegativeClickListener);
            dialog.show();
        }
    }
}
