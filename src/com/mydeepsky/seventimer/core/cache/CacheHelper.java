package com.mydeepsky.seventimer.core.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CacheHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cache.db";
    private static final int DATABASE_VERSION = 1;

    public CacheHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS satellite "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "updatetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "longitude FLOAT NOT NULL, latitude FLOAT NOT NULL, report TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS weather "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, updatetime DATETIME NOT NULL, "
                + "longitude FLOAT NOT NULL, latitude FLOAT NOT NULL, report TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}
