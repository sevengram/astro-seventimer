package com.mydeepsky.seventimer.core.pref;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserPrefs {
    public enum TempUnit {
        CEL, FAH
    }

    private TempUnit mTempUnit;
    private UserPrefLocation mCurrentLocation;
    private List<UserPrefLocation> mSavedLocations;
    private boolean mUseBaidu;

    public UserPrefs() {
        this.mTempUnit = TempUnit.CEL;
        this.mCurrentLocation = new UserPrefLocation();
        this.mSavedLocations = new ArrayList<>();
        this.mUseBaidu = true;
    }

    public int getLocationIndex(String name) {
        for (int i = 0; i < this.mSavedLocations.size(); i++) {
            if (this.mSavedLocations.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isUseCen() {
        return mTempUnit == TempUnit.CEL;
    }

    public String getTempUnitString() {
        switch (this.mTempUnit) {
        case CEL:
            return "C";
        case FAH:
            return "F";
        default:
            return "C";
        }
    }

    public TempUnit getTempUnit() {
        return mTempUnit;
    }

    public UserPrefLocation getCurrentLocation() {
        return mCurrentLocation;
    }

    public String getCurrentLocationJsonString() {
        return mCurrentLocation.toJsonString();
    }

    public List<UserPrefLocation> getSavedLocations() {
        return mSavedLocations;
    }

    public String getSavedLocationsString() {
        JSONArray jsonArray = new JSONArray();
        for (UserPrefLocation location : mSavedLocations) {
            jsonArray.put(location.toJsonObject());
        }
        return jsonArray.toString();
    }

    public void setTempUnit(String tempUnit) {
        if (tempUnit.equals("F")) {
            this.mTempUnit = TempUnit.FAH;
        } else if (tempUnit.equals("C")) {
            this.mTempUnit = TempUnit.CEL;
        }
    }

    public void setCurrentLocation(String locationJsonObject) {
        try {
            this.mCurrentLocation = UserPrefLocation.parseFromJson(new JSONObject(
                    locationJsonObject));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setCurrentLocation(double lon, double lat, String name) {
        this.mCurrentLocation.setLatitude(lat);
        this.mCurrentLocation.setLongitude(lon);
        this.mCurrentLocation.setName(name);
    }

    public void setSavedLocations(String locationJsonArray) {
        try {
            JSONArray jsonArray = new JSONArray(locationJsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                UserPrefLocation location = UserPrefLocation.parseFromJson(jsonArray
                        .getJSONObject(i));
                this.mSavedLocations.add(location);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isUseBaidu() {
        return mUseBaidu;
    }

    public void setUseBaidu(boolean useBaidu) {
        this.mUseBaidu = useBaidu;
    }

}
