package com.mydeepsky.seventimer.stat;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.mydeepsky.android.location.BaiduLocator;
import com.mydeepsky.android.location.GoogleLocator;
import com.mydeepsky.android.location.Locator.LocationInfo;
import com.mydeepsky.android.task.Task;
import com.mydeepsky.android.task.Task.OnTaskFinishListener;
import com.mydeepsky.android.task.TaskContext;
import com.mydeepsky.android.util.AppUtil;
import com.mydeepsky.android.util.ConfigUtil;
import com.mydeepsky.android.util.DeviceUtil;
import com.mydeepsky.android.util.Keys;
import com.mydeepsky.android.util.NetworkManager;
import com.mydeepsky.seventimer.core.task.StatTask;
import com.mydeepsky.seventimer.stat.data.DeviceStat;
import com.mydeepsky.seventimer.stat.data.LocationStat;
import com.mydeepsky.seventimer.stat.data.SatelliteStat;
import com.mydeepsky.seventimer.stat.data.StartAppStat;
import com.mydeepsky.seventimer.stat.data.WeatherStat;
import com.umeng.analytics.MobclickAgent;

public class StatManager {
    private Context mAppContext;

    private String mUUID;

    private String mVersion;

    private String mUrl;

    private String mChannel;

    private static StatManager sInstance;

    public static synchronized StatManager getInstance() {
        if (sInstance == null)
            sInstance = new StatManager();
        return sInstance;
    }

    public void init(Context context) {
        mAppContext = context.getApplicationContext();
        mUUID = DeviceUtil.getID(context);
        mVersion = AppUtil.getVersionName(context);
        mChannel = AppUtil.getChannel(context);
        mUrl = ConfigUtil.getString(Keys.HTTP_STAT_SERVER);
    }

    private StatManager() {
    }

    private void sendReport(StatReport report) {
        sendReport(report, null);
    }

    private void sendReport(StatReport report, WeakReference<OnTaskFinishListener> listener) {
        Task statTask = new StatTask();
        TaskContext taskContext = new TaskContext();
        taskContext.set(StatTask.URL, mUrl);
        taskContext.set(StatTask.REQUEST, report.toString());
        statTask.addTaskFinishListener(listener);
        statTask.execute(taskContext);
    }

    public void sendStartApp(WeakReference<OnTaskFinishListener> listener) {
        sendReport(new StatReport("start_app", new StartAppStat(mUUID, mVersion, mChannel)),
                listener);
    }

    public void sendDeviceStat() {
        sendReport(new StatReport("add_device", new DeviceStat(mUUID, mVersion,
                DeviceUtil.getDeviceModel())));
    }

    public void sendWechatStat() {
        MobclickAgent.onEvent(mAppContext, "wechat");
    }

    public void sendSatelliteStat(long delay) {
        sendReport(new StatReport("satellite_stat", new SatelliteStat(delay,
                NetworkManager.getNetworkType(), mUUID, mVersion)));
        sendUmeng("satellite_delay", NetworkManager.getSimpleNetworkString(), delay);
    }

    public void sendWeatherStat(long totalDelay, long serverDelay) {
        sendReport(new StatReport("weather_stat", new WeatherStat(totalDelay, serverDelay,
                NetworkManager.getNetworkType(), mUUID, mVersion)));
        sendUmeng("weather_total_delay", NetworkManager.getSimpleNetworkString(), totalDelay);
        sendUmeng("weather_server_delay", NetworkManager.getSimpleNetworkString(), serverDelay);
    }

    public void sendLocationStat(LocationInfo info, long costTime, int locator) {
        sendReport(new StatReport("location_stat", new LocationStat(costTime,
                NetworkManager.getNetworkType(), locator, mUUID, mVersion, info.getLongitude(),
                info.getLatitude(), info.getDetail())));
        Map<String, String> map = new HashMap<String, String>();
        switch (locator) {
        case GoogleLocator.ID:
            map.put("locator", "google");
            break;
        case BaiduLocator.ID:
            map.put("locator", "baidu");
            break;
        }
        MobclickAgent.onEventValue(mAppContext, "location_delay", map, (int) costTime);
    }

    private void sendUmeng(String id, String network, long delay) {
        Map<String, String> map = new HashMap<String, String>();
        if (!TextUtils.isEmpty(network)) {
            map.put("network", network);
        }
        MobclickAgent.onEventValue(mAppContext, id, map, (int) delay);
    }
}
