package com.mydeepsky.seventimer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.*;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.mydeepsky.android.location.Locator;
import com.mydeepsky.android.location.Locator.LocationInfo;
import com.mydeepsky.android.location.LocatorActivity;
import com.mydeepsky.android.location.LocatorFactory;
import com.mydeepsky.android.location.LocatorFactory.LocatorType;
import com.mydeepsky.android.task.*;
import com.mydeepsky.android.task.Task.OnTaskListener;
import com.mydeepsky.android.ui.widget.CustomerViewPager;
import com.mydeepsky.android.util.ConfigUtil;
import com.mydeepsky.android.util.Keys;
import com.mydeepsky.android.util.NetworkManager;
import com.mydeepsky.android.util.astro.TimeMath;
import com.mydeepsky.seventimer.R;
import com.mydeepsky.seventimer.core.cache.CacheManager;
import com.mydeepsky.seventimer.core.pref.SettingsProvider;
import com.mydeepsky.seventimer.core.pref.UserPrefs;
import com.mydeepsky.seventimer.core.task.SatelliteTask;
import com.mydeepsky.seventimer.core.task.WeatherTask;
import com.mydeepsky.seventimer.data.ISS;
import com.mydeepsky.seventimer.data.Iridium;
import com.mydeepsky.seventimer.stat.StatManager;
import com.mydeepsky.seventimer.ui.answer.SatelliteView;
import com.mydeepsky.seventimer.ui.answer.WeatherView;
import com.mydeepsky.seventimer.ui.dialog.DialogManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends LocatorActivity {
    static final String TAG = "_MainActivity";

    private final static int SETTINGS_REQUEST_CODE = 1002;

    private final static int MENU_ABOUT_ID = 2001;

    private final static int MENU_PREFERENCE_ID = 2002;

    private final static int MODE_WEATHER = 3001;

    private final static int MODE_SATELLITE = 3002;

    private CacheManager cacheManager;

    private Dialog refreshDialog;

    private CustomerViewPager reportsViewPager;
    private View satelliteView;
    private TextView locationTextView;
    private TextView updatetimeTextView;
    private TextView templabelTextView;
    private ImageButton updateButton;
    private ImageButton settingsButton;
    private ImageButton locationButton;
    private ImageButton switchReportButton;
    private HorizontalScrollView forecastScrollView;

    private String oldLocation;
    private double latitude;
    private double longitude;
    private String locationName;
    private int reportMode;
    private UserPrefs userPrefs;

    private boolean weatherUpdated;
    private boolean satelliteUpdated;

    private List<View> reportsViewList;

    private Context context;

    private long exitTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;
        this.cacheManager = CacheManager.getInstance(context);
        this.reportMode = MODE_WEATHER;
        this.exitTime = 0L;

        this.reportsViewPager = (CustomerViewPager) findViewById(R.id.viewpager_reports);
        this.updateButton = (ImageButton) findViewById(R.id.button_update);
        this.locationButton = (ImageButton) findViewById(R.id.button_location);
        this.settingsButton = (ImageButton) findViewById(R.id.button_settings);
        this.switchReportButton = (ImageButton) findViewById(R.id.button_switch_report);
        this.updatetimeTextView = (TextView) findViewById(R.id.textview_updatetime);
        this.locationTextView = (TextView) findViewById(R.id.textview_location);

        LayoutInflater inflater = LayoutInflater.from(context);
        View weatherView = inflater.inflate(R.layout.linearlayout_weather, reportsViewPager, false);
        satelliteView = inflater.inflate(R.layout.linearlayout_satellite, reportsViewPager, false);
        this.forecastScrollView = (HorizontalScrollView) weatherView
            .findViewById(R.id.scrollview_forecast);
        this.templabelTextView = (TextView) weatherView.findViewById(R.id.textview_templabel);

        reportsViewList = new ArrayList<>();
        reportsViewList.add(weatherView);
        reportsViewList.add(satelliteView);

        reportsViewPager.setScrollable(false);
        reportsViewPager.setSwitchDuration(700);
        reportsViewPager.setAdapter(pagerAdapter);

        refreshDialog = DialogManager.createRefreshDialog(context);
        loadSettings();
        UmengUpdateAgent.update(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, R.string.toast_app_exit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setIconEnable(menu, true);
        menu.add(Menu.NONE, MENU_ABOUT_ID, 1, R.string.menu_about).setIcon(
            android.R.drawable.ic_menu_info_details);
        menu.add(Menu.NONE, MENU_PREFERENCE_ID, 2, R.string.menu_preferences).setIcon(
            android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ABOUT_ID:
                Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon)
                    .setTitle(R.string.app_name).setMessage(R.string.app_about)
                    .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create();
                dialog.show();
                break;
            case MENU_PREFERENCE_ID:
                settingsButton.performClick();
                break;
            default:
                break;
        }
        return false;
    }

    // enable为true时，菜单添加图标有效
    private void setIconEnable(Menu menu, boolean enable) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        this.userPrefs = SettingsProvider.getInstance(context).getPrefs();
        this.latitude = userPrefs.getCurrentLocation().getLatitude();
        this.longitude = userPrefs.getCurrentLocation().getLongitude();
        this.locationName = userPrefs.getCurrentLocation().getName();
        this.locator = LocatorFactory.createLocator(this,
            userPrefs.isUseBaidu() ? LocatorType.BAIDU : LocatorType.GOOGLE);
        this.locator.setOnLocationUpdateListener(this);
        if (!locationName.equals(getText(R.string.text_current_location))) {
            locationTextView.setText(String.format("%s (%.3f, %.3f)", this.locationName,
                this.longitude, this.latitude));
        }
        this.templabelTextView.setText(String.format(getText(R.string.format_temperature)
            .toString(), this.userPrefs.getTempUnitString()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            loadSettings();
            if (locationName.equals(getText(R.string.text_current_location))) {
                locationButton.performClick();
            } else if (data.getBooleanExtra(SettingsActivity.EXTRA_NEED_UPDATE, false)
                || forecastScrollView.getChildCount() == 0) {
                weatherUpdated = false;
                satelliteUpdated = false;
                updateButton.performClick();
            }
        }
    }

    public void onClickSettings(View v) {
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        SettingsProvider.getInstance(context).savePrefs(this.userPrefs);
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        refreshDialog = null;
        cacheManager.close();
        super.onDestroy();
    }

    public void onClickLocation(View v) {
        String str = locationTextView.getText().toString();
        if (!str.equals(getText(R.string.text_location_updating))) {
            this.oldLocation = str;
            locationTextView.setText(R.string.text_location_updating);
        }
        startLocator();
    }

    private void startWeatherTask() {
        JSONObject cache = cacheManager.getWeatherCache(longitude, latitude, 10.0 / 24.0);
        if (cache != null) {
            createWeatherView(cache, false);
        } else {
            if (!NetworkManager.isNetworkAvailable()) {
                Toast.makeText(this.context, R.string.toast_without_network, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            showRefreshDialog();
            TaskContext taskContext = new TaskContext();
            taskContext.set(WeatherTask.KEY_URL, ConfigUtil.getString(Keys.HTTP_REAL_SERVER));
            taskContext.set(WeatherTask.KEY_LON, longitude);
            taskContext.set(WeatherTask.KEY_LAT, latitude);
            taskContext.set(WeatherTask.KEY_START_TIME, System.currentTimeMillis());

            WeatherTask task = new WeatherTask();
            task.addTaskListener(new WeakReference<>(weatherTaskListener));
            task.execute(taskContext);
        }
    }

    private boolean satelliteCacheExpired(JSONObject data) {
        try {
            JSONArray issRecords = data.getJSONArray("iss");
            JSONArray iridiumRecords = data.getJSONArray("iridium");
            if (issRecords == null || iridiumRecords == null
                || (issRecords.length() == 0 && iridiumRecords.length() == 0)) {
                return true;
            }
            int expiredCount = 0;
            int futureCount = 0;
            if (iridiumRecords.length() != 0) {
                for (int i = 0; i < iridiumRecords.length(); i++) {
                    Date highestTime = new Iridium(iridiumRecords.getJSONObject(i))
                        .getHighestTime();
                    if (System.currentTimeMillis() - highestTime.getTime() > TimeMath.ONE_DAY_MS) {
                        return true;
                    } else if (System.currentTimeMillis() - highestTime.getTime() >= 0) {
                        expiredCount++;
                    } else {
                        futureCount++;
                    }
                }
            }
            if (issRecords.length() != 0) {
                Date highestTime = new ISS(issRecords.getJSONObject(0)).getHighestTime();
                if (System.currentTimeMillis() - highestTime.getTime() > TimeMath.ONE_DAY_MS) {
                    return true;
                } else if (System.currentTimeMillis() - highestTime.getTime() >= 0) {
                    expiredCount++;
                } else {
                    futureCount++;
                }
            }
            return expiredCount >= 2 || (expiredCount >= 1 && futureCount <= 3);
        } catch (JSONException e) {
            return true;
        }
    }

    private void showRefreshDialog() {
        if (!isFinishing() && refreshDialog != null) {
            refreshDialog.show();
        }
    }

    private void dismissRefreshDialog() {
        if (!isFinishing() && refreshDialog != null) {
            refreshDialog.dismiss();
        }
    }

    private void startSatelliteTask() {
        JSONObject data = cacheManager.getSatelliteCache(longitude, latitude, 0.5);
        if (data != null && !satelliteCacheExpired(data)) {
            SatelliteView sv = new SatelliteView(context, satelliteView);
            sv.setData(data);
            satelliteUpdated = true;
        } else {
            if (!NetworkManager.isNetworkAvailable()) {
                Toast.makeText(this.context, R.string.toast_without_network, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            showRefreshDialog();
            TaskContext taskContext = new TaskContext();
            SatelliteTask issTask = new SatelliteTask();
            issTask.addTaskListener(new WeakReference<>(satTaskListener));
            taskContext.set(SatelliteTask.KEY_LAT, latitude);
            taskContext.set(SatelliteTask.KEY_LON, longitude);
            taskContext.set(SatelliteTask.KEY_START_TIME, System.currentTimeMillis());
            issTask.execute(taskContext);
        }
    }

    public void onClickUpdate(View v) {
        if (reportMode == MODE_WEATHER) {
            weatherUpdated = false;
            startWeatherTask();
        } else if (reportMode == MODE_SATELLITE) {
            satelliteUpdated = false;
            startSatelliteTask();
        }
    }

    public void onClickShareWechat(View v) {
        if (!callWeChat()) {
            Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.wechat_icon)
                .setTitle(R.string.wechat_name).setMessage(R.string.wechat_info)
                .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                }).create();
            dialog.show();
        }
    }

    private boolean callWeChat() {
        Uri contentUrl = Uri.parse(ConfigUtil.getString(Keys.URL_WECHAT));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(contentUrl);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(intent, 0);
        Collections.sort(resInfo, new ResolveInfo.DisplayNameComparator(pm));
        List<Intent> targetedIntents = new ArrayList<>();
        for (ResolveInfo info : resInfo) {
            Intent targeted = new Intent(Intent.ACTION_VIEW);
            ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo.packageName.equals("com.tencent.mm")) {
                targeted.setData(contentUrl);
                targeted.setPackage(activityInfo.packageName);
                targeted.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                targetedIntents.add(targeted);
            }
        }
        if (targetedIntents.isEmpty()) {
            return false;
        }
        Intent chooser = Intent.createChooser(targetedIntents.remove(0),
            getString(R.string.title_choose_wechat));
        chooser.setClassName("android", "com.android.internal.app.ResolverActivity");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toArray(new Parcelable[targetedIntents.size()]));
        try {
            StatManager.getInstance().sendWechatStat();
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    public void onClickSwitchReport(View v) {
        switch (reportMode) {
            case MODE_WEATHER:
                reportMode = MODE_SATELLITE;
                reportsViewPager.setCurrentItem(1);
                switchReportButton.setImageResource(R.drawable.btn_switch_weather);
                if (!satelliteUpdated) {
                    startSatelliteTask();
                }
                break;
            case MODE_SATELLITE:
                reportMode = MODE_WEATHER;
                reportsViewPager.setCurrentItem(0);
                switchReportButton.setImageResource(R.drawable.btn_switch_sat);
                if (!weatherUpdated) {
                    startWeatherTask();
                }
                break;
        }
    }

    private OnTaskListener weatherTaskListener = new OnTaskListener() {
        @Override
        public void onTaskFinished(Object sender, TaskFinishedEvent event) {
            Log.d("deepsky", "Task finished");
            TaskContext taskContext = event.getContext();
            String response = (String) taskContext.get(WeatherTask.KEY_RESULT);
            long totalDelay = (Long) taskContext.get(Task.KEY_END_TIME)
                - (Long) taskContext.get(Task.KEY_START_TIME);
            long serverDelay = -1;
            try {
                JSONObject jsonObject = new JSONObject(response);
                serverDelay = jsonObject.getLong("delay");
                createWeatherView(jsonObject.getJSONObject("data"), true);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), R.string.toast_data_error,
                    Toast.LENGTH_SHORT).show();
            } finally {
                dismissRefreshDialog();
            }
            StatManager.getInstance().sendWeatherStat(totalDelay, serverDelay);
        }

        @Override
        public void onTimeout(Object sender, TaskTimeoutEvent event) {
            if (!isFinishing()) {
                dismissRefreshDialog();
                Toast.makeText(context, R.string.toast_timeout, Toast.LENGTH_SHORT).show();
                forecastScrollView.removeAllViews();
            }
        }

        @Override
        public void onTaskFailed(Object sender, TaskFailedEvent event) {
            if (!isFinishing()) {
                dismissRefreshDialog();
                Toast.makeText(context, R.string.toast_failed, Toast.LENGTH_SHORT).show();
                forecastScrollView.removeAllViews();
            }
        }

        @Override
        public void onTaskCancel(Object sender, TaskCancelEvent event) {
            // TODO Auto-generated method stub

        }
    };

    private OnTaskListener satTaskListener = new OnTaskListener() {

        @Override
        public void onTaskFinished(Object sender, TaskFinishedEvent event) {
            Log.d("deepsky", "Task finished");
            TaskContext taskContext = event.getContext();
            String response = (String) taskContext.get(SatelliteTask.KEY_RESULT);
            long delay = (Long) taskContext.get(Task.KEY_END_TIME)
                - (Long) taskContext.get(Task.KEY_START_TIME);
            cacheManager.createSatelliteCache(longitude, latitude, response);
            try {
                JSONObject data = new JSONObject(response);
                SatelliteView sv = new SatelliteView(context, satelliteView);
                sv.setData(data);
                satelliteUpdated = true;
            } catch (JSONException e) {
                Toast.makeText(context, R.string.toast_data_error, Toast.LENGTH_SHORT).show();
            } finally {
                dismissRefreshDialog();
            }
            StatManager.getInstance().sendSatelliteStat(delay);
        }

        @Override
        public void onTimeout(Object sender, TaskTimeoutEvent event) {
            if (!isFinishing()) {
                dismissRefreshDialog();
                Toast.makeText(context, R.string.toast_timeout, Toast.LENGTH_SHORT).show();
                forecastScrollView.removeAllViews();
            }
        }

        @Override
        public void onTaskFailed(Object sender, TaskFailedEvent event) {
            if (!isFinishing()) {
                dismissRefreshDialog();
                Toast.makeText(context, R.string.toast_failed, Toast.LENGTH_SHORT).show();
                forecastScrollView.removeAllViews();
            }
        }

        @Override
        public void onTaskCancel(Object sender, TaskCancelEvent event) {
            // TODO Auto-generated method stub

        }
    };

    public void createWeatherView(JSONObject data, boolean createCache) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            Date reportTime = df.parse(data.getString("init"));
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'Z", Locale.US);
            updatetimeTextView.setText(df2.format(reportTime));

            if (createCache) {
                cacheManager.createWeatherCache(longitude, latitude, reportTime, data.toString());
            }
            WeatherView wv = new WeatherView(longitude, latitude, reportTime, userPrefs.isUseCen());
            wv.setData(data);
            forecastScrollView.removeAllViews();
            forecastScrollView.addView(wv.getView(context));
            weatherUpdated = true;
            if (SettingsProvider.getInstance(context).isNewLaunch()) {
                Toast.makeText(getApplicationContext(), R.string.toast_cloud_info,
                    Toast.LENGTH_LONG).show();
            }
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationUpdate(LocationInfo location, long costTime, int locator) {
        if (location == null) {
            return;
        }
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationName = location.toString();
        userPrefs.setCurrentLocation(longitude, latitude, locationName);
        locationTextView.setText(String
            .format("%s (%.3f, %.3f)", locationName, longitude, latitude));
        weatherUpdated = false;
        satelliteUpdated = false;
        StatManager.getInstance().sendLocationStat(costTime, locator);
        updateButton.performClick();
    }

    @Override
    public void onLocationError(boolean showMessage) {
        if (showMessage) {
            Toast.makeText(context, R.string.toast_location_fail, Toast.LENGTH_SHORT).show();
        }
        if (oldLocation != null && !oldLocation.equals(getText(R.string.text_location_updating))) {
            locationTextView.setText(oldLocation);
        }
    }

    private PagerAdapter pagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            return reportsViewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(reportsViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(reportsViewList.get(position));
            return reportsViewList.get(position);
        }
    };

    @Override
    public void onWithoutService(int error) {
        if (isFinishing()) {
            return;
        }

        DialogInterface.OnClickListener onNegativeClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onLocationError(false);
            }
        };
        if (error == Locator.ERR_NO_NETWORK) {
            Dialog dialog = DialogManager.createNetworkDialog(context, onNegativeClickListener);
            dialog.show();
        } else if (error == Locator.ERR_NO_LOCATION_SERVICE) {
            Dialog dialog = DialogManager.createLocationDialog(context, onNegativeClickListener);
            dialog.show();
        }
    }

}
