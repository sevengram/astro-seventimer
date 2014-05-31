package com.mydeepsky.seventimer.ui.answer;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mydeepsky.android.util.GeoTimeUtil;
import com.mydeepsky.android.util.GeoTimeUtil.GeoZone;
import com.mydeepsky.android.util.ImageUtil;
import com.mydeepsky.android.util.NetworkManager;
import com.mydeepsky.seventimer.R;
import com.mydeepsky.seventimer.ui.WebviewActivity;

public class WeatherItemView implements IAnswerItemView, View.OnClickListener {
    private LinearLayout dayDividerLinearLayout;

    private TextView dateTextView;
    private TextView hourTextView;

    private ImageView cloudCoverImageView;
    private ImageView seeingImageView;
    private ImageView transparencyImageView;
    private ImageView windImageView;
    private ImageView rainImageView;

    private TextView riseSetTextView;
    private TextView twilightTextView;

    private TextView humidityTextView;
    private TextView windSpeedTextView;
    private TextView temperatureTextView;

    private boolean userCen;
    private Date initTime;
    private GeoZone geoZone;
    private int timepoint;
    private JSONObject weatherObject;

    private Queue<Date> riseSetTimeQueue;
    private Queue<Date> twilightTimeQueue;

    private final static int DEFAULT_IMAGE = R.drawable.icon_unknown;

    public WeatherItemView(Date initTime, GeoZone geoZone, boolean useCen) {
        this.initTime = initTime;
        this.geoZone = geoZone;
        this.userCen = useCen;
    }

    @Override
    public void setData(Object weather) {
        this.weatherObject = (JSONObject) weather;
    }

    @Override
    public View getView(Context context) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.datapoint_compact, null);
        dateTextView = (TextView) layout.findViewById(R.id.txtDate);
        hourTextView = (TextView) layout.findViewById(R.id.txtHour);
        cloudCoverImageView = (ImageView) layout.findViewById(R.id.ivCloudCover);
        seeingImageView = (ImageView) layout.findViewById(R.id.ivSeeing);
        transparencyImageView = (ImageView) layout.findViewById(R.id.ivTransparency);
        windImageView = (ImageView) layout.findViewById(R.id.ivWind);
        humidityTextView = (TextView) layout.findViewById(R.id.txtHumidity);
        windSpeedTextView = (TextView) layout.findViewById(R.id.txtWindSpeed);
        temperatureTextView = (TextView) layout.findViewById(R.id.txtTemperature);
        rainImageView = (ImageView) layout.findViewById(R.id.ivRain);
        dayDividerLinearLayout = (LinearLayout) layout.findViewById(R.id.llDayDivider24);
        riseSetTextView = (TextView) layout.findViewById(R.id.txtRiseSetTime);
        twilightTextView = (TextView) layout.findViewById(R.id.astrolightTime);
        updateView();
        layout.setOnClickListener(this);
        return layout;
    }

    private void updateView() {
        try {
            this.timepoint = weatherObject.getInt("timepoint");
            Calendar cal = Calendar.getInstance();
            cal.setTime(initTime);
            cal.add(Calendar.HOUR_OF_DAY, timepoint);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            hourTextView.setText(String.format("%02d", hour));
            if (hour < 3 || timepoint <= 3) {
                dateTextView.setText(GeoTimeUtil.getWeekday(cal.getTime(), cal.getTimeZone()));
            }
            if (hour < 3 && timepoint > 3) {
                dayDividerLinearLayout.setVisibility(View.VISIBLE);
            }
            cal.add(Calendar.MINUTE, 90);
            Date tempTime = cal.getTime();
            if (!riseSetTimeQueue.isEmpty() && riseSetTimeQueue.peek().before(tempTime)) {
                setTimeTextView(riseSetTextView, riseSetTimeQueue.poll());
            }
            if (!twilightTimeQueue.isEmpty() && twilightTimeQueue.peek().before(tempTime)) {
                setTimeTextView(twilightTextView, twilightTimeQueue.poll());
            }

            Class<R.drawable> c = R.drawable.class;
            cloudCoverImageView.setImageResource(ImageUtil.getResId(c,
                    String.format("cloud_icon%d_8", weatherObject.getInt("cloudcover") - 1),
                    DEFAULT_IMAGE));
            seeingImageView.setImageResource(ImageUtil.getResId(c,
                    String.format("seeing_%d", weatherObject.getInt("seeing")), DEFAULT_IMAGE));
            transparencyImageView.setImageResource(ImageUtil.getResId(c,
                    String.format("transparency_%d", weatherObject.getInt("transparency")),
                    DEFAULT_IMAGE));
            windImageView.setImageResource(ImageUtil.getResId(
                    c,
                    String.format("wind_%s",
                            weatherObject.getJSONObject("wind10m").getString("direction")
                                    .toLowerCase(Locale.US)), DEFAULT_IMAGE));

            int windrate = weatherObject.getJSONObject("wind10m").getInt("speed");
            if (windrate >= 1 && windrate <= 3) {
                windSpeedTextView.setText(windrate + "");
            } else if (windrate >= 4 || windrate <= 5) {
                windSpeedTextView.setText((windrate + 1) + "");
            } else if (windrate >= 6 || windrate <= 8) {
                windSpeedTextView.setText((2 * windrate - 4) + "");
            } else {
                windSpeedTextView.setText(">13");
            }

            int temp = weatherObject.getInt("temp2m");
            if (userCen) {
                temperatureTextView.setText(String.valueOf(temp));
            } else {
                temperatureTextView.setText(String.valueOf(Math.round(GeoTimeUtil.cen2fah(temp))));
            }
            if (temp >= 30)
                temperatureTextView.setTextColor(0xffff4040);
            else if (temp >= 20)
                temperatureTextView.setTextColor(0xffff9c40);
            else if (temp >= 10)
                temperatureTextView.setTextColor(0xffffe740);
            else if (temp >= 0)
                temperatureTextView.setTextColor(0xffaaff40);
            else if (temp >= -10)
                temperatureTextView.setTextColor(0xff40ff49);
            else
                temperatureTextView.setTextColor(0xff40fff1);
            String precType = weatherObject.getString("prec_type");
            if (precType.equals("rain") || precType.equals("frzr")) {
                rainImageView.setImageResource(R.drawable.rain);
            } else if (precType.equals("snow") || precType.equals("icep")) {
                rainImageView.setImageResource(R.drawable.snow);
            }
            int hum = (weatherObject.getInt("rh2m") + 4) * 5;
            humidityTextView.setText(hum + "");
            if (hum >= 90)
                humidityTextView.setTextColor(0xff00970e);
            else if (hum >= 75)
                humidityTextView.setTextColor(0xff4fb611);
            else if (hum >= 50)
                humidityTextView.setTextColor(0xff8cc712);
            else if (hum >= 25)
                humidityTextView.setTextColor(0xffb5c914);
            else if (hum >= 10)
                humidityTextView.setTextColor(0xffdae512);
            else
                humidityTextView.setTextColor(0xfff59c12);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setTimeTextView(TextView textView, Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        textView.setText(String.format("%d:%02d", cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE)));
    }

    public void setTimeQueue(Queue<Date> q1, Queue<Date> q2) {
        this.riseSetTimeQueue = q1;
        this.twilightTimeQueue = q2;
    }

    @Override
    public void onClick(View v) {
        if (!NetworkManager.isNetworkAvailable()) {
            Toast.makeText(v.getContext(), R.string.toast_without_network, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Context context = v.getContext();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        calendar.setTime(initTime);
        Intent intent = new Intent(context, WebviewActivity.class);
        intent.putExtra(WebviewActivity.EXTRA_INITTIME, calendar.get(Calendar.HOUR_OF_DAY));
        intent.putExtra(WebviewActivity.EXTRA_TIMEPOINT, timepoint);
        intent.putExtra(WebviewActivity.EXTRA_GEOZONE, geoZone.toString());
        context.startActivity(intent);
    }
}
