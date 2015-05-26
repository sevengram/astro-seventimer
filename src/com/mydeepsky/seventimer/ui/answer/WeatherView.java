package com.mydeepsky.seventimer.ui.answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mydeepsky.android.util.GeoTimeUtil;
import com.mydeepsky.seventimer.R;

public class WeatherView implements IAnswerView {
    private JSONArray weatherRes;
    private JSONObject solarRes;

    private double lon;
    private double lat;

    private Date initTime;

    private boolean useCen;

    @Override
    public View getView(Context context) {
        LinearLayout layout = (LinearLayout) View.inflate(context, R.layout.answer_container, null);
        LinearLayout parentLayout = (LinearLayout) layout.findViewById(R.id.linearlayout_results);
        addChildView(context, parentLayout);
        return layout;
    }

    @Override
    public void setData(Object data) {
        try {
            this.weatherRes = ((JSONObject) data).getJSONArray("dataseries");
            this.solarRes = ((JSONObject) data).getJSONObject("solar");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public WeatherView(double lon, double lat, Date initTime, boolean useCen) {
        this.lat = lat;
        this.lon = lon;
        this.initTime = initTime;
        this.useCen = useCen;
    }

    @Override
    public void addChildView(final Context context, LinearLayout parentLayout) {
        Queue<Date> riseSetTimeQueue = new PriorityQueue<>();
        Queue<Date> twilightTimeQueue = new PriorityQueue<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        try {
            JSONArray riseSets = solarRes.getJSONArray("rise_set");
            JSONArray twilight = solarRes.getJSONArray("twilight");
            for (int i = 0; i < riseSets.length(); i++) {
                riseSetTimeQueue.add(df.parse(riseSets.getString(i)));
            }
            for (int i = 0; i < twilight.length(); i++) {
                twilightTimeQueue.add(df.parse(twilight.getString(i)));
            }
        } catch (JSONException | ParseException ignored) {
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(initTime);
        calendar.add(Calendar.MINUTE, 90);
        Date startTime = calendar.getTime();
        while (!riseSetTimeQueue.isEmpty() && riseSetTimeQueue.peek().before(startTime)) {
            riseSetTimeQueue.remove();
        }
        while (!twilightTimeQueue.isEmpty() && twilightTimeQueue.peek().before(startTime)) {
            twilightTimeQueue.remove();
        }
        try {
            for (int i = 0; i < weatherRes.length(); i++) {
                WeatherItemView itemView = new WeatherItemView(initTime, GeoTimeUtil.getZone(lon, lat),
                        useCen);
                itemView.setData(weatherRes.get(i));
                itemView.setTimeQueue(riseSetTimeQueue, twilightTimeQueue);
                parentLayout.addView(itemView.getView(context));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
