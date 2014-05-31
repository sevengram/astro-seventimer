package com.mydeepsky.seventimer.core.pref;

import org.json.JSONException;
import org.json.JSONObject;

public class UserPrefLocation {
    private double latitude;
    private double longitude;
    private String name;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserPrefLocation() {
        super();
        this.latitude = 31.216;
        this.longitude = 121.472;
        this.name = "Shanghai";
    }

    public UserPrefLocation(double latitude, double longitude, String name) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public static UserPrefLocation parseFromJson(JSONObject jsonObject) throws JSONException {
        UserPrefLocation location = new UserPrefLocation();
        location.latitude = jsonObject.getDouble("lat");
        location.longitude = jsonObject.getDouble("lon");
        location.name = jsonObject.getString("name");
        return location;
    }

    public JSONObject toJsonObject() {
        JSONObject location = new JSONObject();
        try {
            location.put("name", name);
            location.put("lat", latitude);
            location.put("lon", longitude);
        } catch (JSONException e) {
        }
        return location;
    }

    public String toJsonString() {
        return this.toJsonObject().toString();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        return this.toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}