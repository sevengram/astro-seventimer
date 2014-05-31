package com.mydeepsky.seventimer.core.task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.mydeepsky.android.task.Task;
import com.mydeepsky.android.task.TaskContext;
import com.mydeepsky.android.task.TaskResult;
import com.mydeepsky.android.task.TaskResult.TaskStatus;
import com.mydeepsky.android.util.ConfigUtil;
import com.mydeepsky.android.util.HttpUtil;
import com.mydeepsky.android.util.Keys;
import com.mydeepsky.android.util.StringUtil;

public class SatelliteTask extends Task {
    final static String TAG = "_SatelliteTask";
    public final static String KEY_RESULT = "sat_task_result";
    public final static String KEY_LON = "sat_task_lon";
    public final static String KEY_LAT = "sat_task_lat";

    private final String ID = StringUtil.newGuid();
    private final String NAME = "Satellite";

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    protected TaskResult doInBackground(TaskContext... params) {
        if (params == null || params.length <= 0) {
            return new TaskResult(TaskStatus.Failed, "TaskContext is null");
        }

        TaskContext context = params[0];
        final double longitude = (Double) context.get(KEY_LON);
        final double latitude = (Double) context.get(KEY_LAT);

        final String issurl = String.format(ConfigUtil.getString(Keys.PRE_HEAVENS_ABOVE_ISS),
                latitude, longitude);
        final String ifurl = String.format(ConfigUtil.getString(Keys.PRE_HEAVENS_ABOVE_IRIDIUM),
                latitude, longitude);

        JSONObject results = new JSONObject();
        final JSONArray issResults = new JSONArray();
        final JSONArray ifResults = new JSONArray();

        final HttpClient client = HttpUtil.getHttpClient();
        Thread issThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, issurl);
                String html = HttpUtil.getHtmlByUrl(client, issurl);
                if (html == null || "".equals(html)) {
                    return;
                }

                Document document = Jsoup.parse(html);
                Elements trs = document.getElementsByClass("clickableRow");
                for (Element tr : trs) {
                    Elements tds = tr.getElementsByTag("td");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("mag", Double.parseDouble(tds.get(1).text()));
                    map.put("stime", tds.get(2).text());
                    map.put("salt", tds.get(3).text());
                    map.put("saz", tds.get(4).text());
                    map.put("htime", tds.get(5).text());
                    map.put("halt", tds.get(6).text());
                    map.put("haz", tds.get(7).text());
                    map.put("etime", tds.get(8).text());
                    map.put("ealt", tds.get(9).text());
                    map.put("eaz", tds.get(10).text());
                    map.put("lng", longitude);
                    map.put("lat", latitude);
                    map.put("mjd", tds.get(0).child(0).attr("href").split("&")[7].split("=")[1]);
                    issResults.put(new JSONObject(map));
                }
            }
        });

        Thread ifThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, ifurl);
                String html = HttpUtil.getHtmlByUrl(client, ifurl);
                if (html == null || "".equals(html)) {
                    return;
                }
                Document document = Jsoup.parse(html);
                SimpleDateFormat format = new SimpleDateFormat("HH:mm E, dd MMMM, yyyy", Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                try {
                    Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
                    cal1.setTime(format.parse(document.getElementById("ctl00_cph1_lblSearchStart")
                            .text()));
                    Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
                    cal2.setTime(format.parse(document.getElementById("ctl00_cph1_lblSearchEnd")
                            .text()));
                    boolean overyear = cal2.get(Calendar.YEAR) > cal1.get(Calendar.YEAR);
                    Elements trs = document.getElementsByClass("clickableRow");
                    int i = 0;
                    for (Element tr : trs) {
                        Elements tds = tr.getElementsByTag("td");
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("mag", Double.parseDouble(tds.get(1).text()));
                        map.put("alt", tds.get(2).text());
                        map.put("az", tds.get(3).text());
                        map.put("satellite", tds.get(4).text());
                        map.put("distance", tds.get(5).text());
                        map.put("centermag", Double.parseDouble(tds.get(6).text()));
                        map.put("sunalt", tds.get(7).text());
                        map.put("fid", i);
                        map.put("lng", longitude);
                        map.put("lat", latitude);
                        SimpleDateFormat df1 = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US);
                        df1.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                        Calendar cal3 = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
                        cal3.setTime(df1.parse(tds.get(0).text()));
                        if (overyear && cal3.get(Calendar.MONTH) == Calendar.JANUARY) {
                            cal3.set(Calendar.YEAR, cal2.get(Calendar.YEAR));
                        } else {
                            cal3.set(Calendar.YEAR, cal1.get(Calendar.YEAR));
                        }
                        SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                        df2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                        map.put("time", df2.format(cal3.getTime()));
                        ifResults.put(new JSONObject(map));
                        i++;
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        issThread.start();
        ifThread.start();
        try {
            issThread.join();
            ifThread.join();
            results.put("iss", issResults);
            results.put("iridium", ifResults);
        } catch (Exception e) {
            return new TaskResult(TaskStatus.Failed, "response error");
        }

        TaskResult taskResult = new TaskResult(TaskStatus.Finished);
        context.set(KEY_RESULT, results.toString());
        context.set(KEY_END_TIME, System.currentTimeMillis());
        taskResult.setContext(context);
        return taskResult;
    }
}
