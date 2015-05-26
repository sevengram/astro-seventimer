package com.mydeepsky.seventimer.data;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class Satellite implements Comparable<Satellite>, Serializable {

    private static final long serialVersionUID = 5262262503330643580L;

    public static final int ISS = 90000;

    public static final int IRIDIUM = 90001;

    public static final String KEY_IMAGE = "image";

    public static final String KEY_NAME = "name";

    public static final String KEY_MAG = "mag";

    public static final String KEY_DATE = "date";

    public static final String KEY_START = "sinfo";

    public static final String KEY_HIGHEST = "hinfo";

    public static final String KEY_END = "einfo";

    protected int type;

    protected String name;

    protected double magnitude;

    protected double longitude;

    protected double latitude;

    protected String mjd;

    protected Date highestTime;

    protected String highestAlt;

    protected String highestAz;

    public abstract String getID();

    public abstract String getDate();

    public abstract String getDateLong();

    public abstract String getStartInfo();

    public abstract String getEndInfo();

    public abstract int getIcon();

    public String getName() {
        return name;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getHighestInfo() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return String.format(Locale.US, "%s %s/%s", formatter.format(highestTime), highestAlt,
            highestAz);
    }

    public Date getHighestTime() {
        return highestTime;
    }

    public String getHighestTimeString() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return formatter.format(highestTime);
    }

    public String getHighestAltAz() {
        return String.format(Locale.US, "%s/%s", highestAlt, highestAz);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_IMAGE, getIcon());
        map.put(KEY_NAME, name);
        map.put(KEY_MAG, magnitude);
        map.put(KEY_DATE, getDate());
        map.put(KEY_START, getStartInfo());
        map.put(KEY_HIGHEST, getHighestInfo());
        map.put(KEY_END, getEndInfo());
        return map;
    }

    @Override
    public int compareTo(@NonNull Satellite another) {
        return this.highestTime.compareTo(another.highestTime);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getType() {
        return type;
    }

    public String getMjd() {
        return mjd;
    }
}
