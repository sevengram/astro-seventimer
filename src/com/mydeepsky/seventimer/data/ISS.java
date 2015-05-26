package com.mydeepsky.seventimer.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.seventimer.R;

public class ISS extends Satellite {

    private static final long serialVersionUID = -1774946147686489081L;

    private Date startTime;

    private String startAlt;

    private String startAz;

    private Date endTime;

    private String endAlt;

    private String endAz;

    public ISS(JSONObject data) {
        this.type = ISS;
        this.name = "ISS";
        try {
            this.magnitude = data.getDouble("mag");
            this.startAlt = data.getString("salt");
            this.endAlt = data.getString("ealt");
            this.highestAlt = data.getString("halt");
            this.startAz = data.getString("saz");
            this.endAz = data.getString("eaz");
            this.highestAz = data.getString("haz");
            this.mjd = data.getString("mjd");
            this.longitude = data.getDouble("lng");
            this.latitude = data.getDouble("lat");

            Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
            cal1.setTime(TimeMath.mjd2date(Double.parseDouble(mjd)));
            this.highestTime = cal1.getTime();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
            cal2.setTime(df.parse(data.getString("stime")));
            cal2.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DATE));
            if (cal2.after(cal1)) {
                cal2.add(Calendar.DATE, -1);
            }
            this.startTime = cal2.getTime();

            cal2.setTime(df.parse(data.getString("etime")));
            cal2.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DATE));
            if (cal2.before(cal1)) {
                cal2.add(Calendar.DATE, 1);
            }
            this.endTime = cal2.getTime();
        } catch (JSONException | ParseException | NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getStartInfo() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return String.format(Locale.US, "%s %s/%s", formatter.format(startTime), startAlt, startAz);
    }

    public String getStartTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return formatter.format(startTime);
    }

    public String getStartAltAz() {
        return String.format(Locale.US, "%s/%s", startAlt, startAz);
    }

    @Override
    public String getEndInfo() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return String.format(Locale.US, "%s %s/%s", formatter.format(endTime), endAlt, endAz);
    }

    public String getEndTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return formatter.format(endTime);
    }

    public String getEndAltAz() {
        return String.format(Locale.US, "%s/%s", endAlt, endAz);
    }

    @Override
    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return formatter.format(startTime);
    }

    @Override
    public int getIcon() {
        return R.drawable.sat;
    }

    @Override
    public String getDateLong() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        return formatter.format(startTime);
    }

    @Override
    public String getID() {
        return String.format(Locale.US, "iss_%.3f_%.3f_%s", longitude, latitude, getMjd());
    }
}
