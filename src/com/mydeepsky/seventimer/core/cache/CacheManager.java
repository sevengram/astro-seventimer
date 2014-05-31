package com.mydeepsky.seventimer.core.cache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mydeepsky.android.util.DirManager;
import com.mydeepsky.android.util.FileUtil;
import com.mydeepsky.android.util.ImageUtil;
import com.mydeepsky.android.util.astro.TimeMath;

public class CacheManager {
    private SQLiteDatabase mCache;

    private CacheHelper mCacheHelper;

    private String mImageCachePath;

    private static CacheManager instance;

    public static synchronized CacheManager getInstance(Context context) {
        if (instance == null)
            instance = new CacheManager(context.getApplicationContext());
        return instance;
    }

    private CacheManager(Context context) {
        mCacheHelper = new CacheHelper(context);
        mCache = mCacheHelper.getWritableDatabase();
        mImageCachePath = DirManager.getImageCachePath();
    }

    public void createImageCache(Bitmap bitmap, String filename) {
        ImageUtil.bitmap2File(bitmap, String.format("%s/%s", mImageCachePath, filename));
    }

    public Bitmap getImageCache(String filename) {
        return BitmapFactory.decodeFile(String.format("%s/%s", mImageCachePath, filename));
    }

    public void clearImageCache() {
        FileUtil.deleteAllFiles(new File(mImageCachePath));
    }

    public void cleanImageCache() {
        File files[] = new File(mImageCachePath).listFiles();
        for (File subfile : files) {
            String[] tmps = subfile.getName().split("_");
            if (TimeMath.date2mjd(new Date()) > Double.parseDouble(tmps[tmps.length - 1])) {
                subfile.delete();
            }
        }
    }

    public boolean checkCache() {
        if (mCache != null && mCache.isOpen()) {
            return true;
        } else {
            try {
                mCache = mCacheHelper.getWritableDatabase();
                return mCache != null;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void createWeatherCache(double longitude, double latitude, Date reportTime, String report) {
        if (!checkCache()) {
            return;
        }
        mCache.beginTransaction();
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            mCache.execSQL(
                    "INSERT INTO weather (updatetime, longitude, latitude, report) VALUES(datetime(?), ?, ?, ?)",
                    new Object[] { df.format(reportTime), longitude, latitude, report });
            mCache.setTransactionSuccessful();
        } finally {
            mCache.endTransaction();
        }
    }

    public void createSatelliteCache(double longitude, double latitude, String report) {
        if (!checkCache()) {
            return;
        }
        mCache.beginTransaction();
        try {
            mCache.execSQL("INSERT INTO satellite (longitude, latitude, report) VALUES(?, ?, ?)",
                    new Object[] { longitude, latitude, report });
            mCache.setTransactionSuccessful();
        } finally {
            mCache.endTransaction();
        }
    }

    public JSONObject getSatelliteCache(double longitude, double latitude, double expiredtime) {
        if (!checkCache()) {
            return null;
        }
        Cursor c = mCache
                .rawQuery(
                        "SELECT report FROM satellite WHERE abs(longitude - ?) < 0.001 AND abs(latitude - ?) < 0.001 AND julianday() < julianday(updatetime)+?  ORDER BY updatetime DESC",
                        new String[] { String.valueOf(longitude), String.valueOf(latitude),
                                String.valueOf(expiredtime) });
        if (c.moveToFirst()) {
            try {
                return new JSONObject(c.getString(c.getColumnIndex("report")));
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public JSONObject getWeatherCache(double longitude, double latitude, double expiredtime) {
        if (!checkCache()) {
            return null;
        }
        Cursor c = mCache
                .rawQuery(
                        "SELECT report FROM weather WHERE abs(longitude - ?) < 0.1 AND abs(latitude - ?) < 0.1 AND julianday() < julianday(updatetime)+? ORDER BY updatetime DESC",
                        new String[] { String.valueOf(longitude), String.valueOf(latitude),
                                String.valueOf(expiredtime) });
        if (c.moveToFirst()) {
            try {
                return new JSONObject(c.getString(c.getColumnIndex("report")));
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void cleanCache(double expiredtime) {
        if (!checkCache()) {
            return;
        }
        mCache.delete("satellite", "julianday(updatetime) < julianday() - ?",
                new String[] { String.valueOf(expiredtime) });
        mCache.delete("weather", "julianday(updatetime) < julianday() - ?",
                new String[] { String.valueOf(expiredtime) });
    }

    public void clearSatelliteCache() {
        if (!checkCache()) {
            return;
        }
        mCache.delete("satellite", null, null);
        mCache.delete("weather", null, null);
    }

    public void close() {
        cleanCache(0.5);
        cleanImageCache();
        mCacheHelper.close();
        instance = null;
    }

}
