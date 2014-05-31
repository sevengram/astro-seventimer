package com.mydeepsky.seventimer.core.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.mydeepsky.android.util.GeoTimeUtil;

public class SettingsProvider {
    private static final String KEY_USE_BAIDU = "use_baidu";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_TEMP_UNIT = "temp_unit";
    private static final String KEY_CURRENT_LOCATION = "current_location";
    private static final String KEY_SAVED_LOCATION = "saved_location";
    private static final String KEY_IGNORE_UPDATE = "ignore_update";

    private static final String PREFS_USER = "com.mydeepsky.seventimer_preferences";
    private SharedPreferences mPreferences;
    private static SettingsProvider sInstance;

    public static synchronized SettingsProvider getInstance(Context context) {
        if (sInstance == null)
            sInstance = new SettingsProvider(context.getApplicationContext());
        return sInstance;
    }

    private SettingsProvider(Context context) {
        this.mPreferences = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
    }

    public UserPrefs getPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setTempUnit(mPreferences.getString(KEY_TEMP_UNIT, "C"));
        userPrefs.setUseBaidu(mPreferences.getBoolean(KEY_USE_BAIDU, GeoTimeUtil.isInChina()));
        userPrefs.setCurrentLocation(mPreferences.getString(KEY_CURRENT_LOCATION,
                new UserPrefLocation().toJsonString()));
        userPrefs.setSavedLocations(mPreferences.getString(KEY_SAVED_LOCATION,
                "[{\"lat\":31.216, \"lon\":121.472, \"name\":\"Shanghai\"}]"));
        return userPrefs;
    }

    public void savePrefs(UserPrefs userPrefs) {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putBoolean(KEY_USE_BAIDU, userPrefs.isUseBaidu());
        editor.putString(KEY_TEMP_UNIT, userPrefs.getTempUnitString());
        editor.putString(KEY_CURRENT_LOCATION, userPrefs.getCurrentLocationJsonString());
        editor.putString(KEY_SAVED_LOCATION, userPrefs.getSavedLocationsString());
        editor.commit();
    }

    public boolean isNewLaunch() {
        int result = mPreferences.getInt(KEY_FIRST_LAUNCH, 0);
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putInt(KEY_FIRST_LAUNCH, result + 1);
        editor.commit();
        return result < 2;
    }

    public void setIgnoreUpdate(int rev) {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putInt(KEY_IGNORE_UPDATE, rev);
        editor.commit();
    }

    public int getIgnoreUpdate() {
        return this.mPreferences.getInt(KEY_IGNORE_UPDATE, 0);
    }
}
