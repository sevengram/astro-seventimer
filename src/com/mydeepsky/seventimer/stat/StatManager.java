package com.mydeepsky.seventimer.stat;

import android.content.Context;
import android.text.TextUtils;
import com.mydeepsky.android.location.BaiduLocator;
import com.mydeepsky.android.location.GoogleLocator;
import com.mydeepsky.android.util.NetworkManager;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

public class StatManager {
    private Context mAppContext;

    private static StatManager sInstance;

    public static synchronized StatManager getInstance() {
        if (sInstance == null)
            sInstance = new StatManager();
        return sInstance;
    }

    public void init(Context context) {
        mAppContext = context.getApplicationContext();
    }

    private StatManager() {
    }

    public void sendWechatStat() {
        MobclickAgent.onEvent(mAppContext, "wechat");
    }

    public void sendSatelliteStat(long delay) {
        sendUmeng("satellite_delay", NetworkManager.getSimpleNetworkString(), delay);
    }

    public void sendWeatherStat(long totalDelay, long serverDelay) {
        sendUmeng("weather_total_delay", NetworkManager.getSimpleNetworkString(), totalDelay);
        sendUmeng("weather_server_delay", NetworkManager.getSimpleNetworkString(), serverDelay);
    }

    public void sendLocationStat(long costTime, int locator) {
        Map<String, String> map = new HashMap<>();
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
        Map<String, String> map = new HashMap<>();
        if (!TextUtils.isEmpty(network)) {
            map.put("network", network);
        }
        MobclickAgent.onEventValue(mAppContext, id, map, (int) delay);
    }
}
