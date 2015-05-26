package com.mydeepsky.seventimer.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.seventimer.R;

public class Iridium extends Satellite {

    private static final long serialVersionUID = -6667806567104913558L;

    private String distance;

    private double magCenter;

    private String sunAlt;

    private int fid;

    public Iridium(JSONObject data) {
        this.type = IRIDIUM;
        try {
            this.name = data.getString("satellite").replace("m", "m ");
            this.magnitude = data.getDouble("mag");
            this.highestAlt = data.getString("alt");
            this.highestAz = data.getString("az");
            this.sunAlt = data.getString("sunalt");
            this.distance = data.getString("distance");
            this.magCenter = data.getInt("centermag");
            this.longitude = data.getDouble("lng");
            this.latitude = data.getDouble("lat");
            this.fid = data.getInt("fid");

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            this.highestTime = df.parse(data.getString("time"));
            this.mjd = String.format(Locale.US, "%.6f", TimeMath.date2mjd(highestTime));
        } catch (JSONException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getStartInfo() {
        return "";
    }

    @Override
    public String getEndInfo() {
        return "";
    }

    public String getDistance() {
        return distance;
    }

    public double getMagCenter() {
        return magCenter;
    }

    public String getSunAlt() {
        return sunAlt;
    }

    public int getFid() {
        return fid;
    }

    @Override
    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return formatter.format(highestTime);
    }

    @Override
    public int getIcon() {
        return R.drawable.flare;
    }

    @Override
    public String getDateLong() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        return formatter.format(highestTime);
    }

    @Override
    public String getID() {
        return String.format(Locale.US, "iridium_%.3f_%.3f_%s", longitude, latitude, getMjd());
    }
}
