package com.mydeepsky.seventimer;

import android.app.Application;

import com.mydeepsky.android.util.DirManager;
import com.mydeepsky.android.util.NetworkManager;
import com.mydeepsky.android.util.SysUtil;
import com.mydeepsky.seventimer.stat.StatManager;

public class SeventimerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DirManager.init(getApplicationContext());
        NetworkManager.init(getApplicationContext());
        StatManager.getInstance().init(getApplicationContext());
        initPaths();

        StatManager.getInstance().sendDeviceStat();
    }

    private void initPaths() {
        SysUtil.checkPath(DirManager.getPrivateCachePath());
        SysUtil.checkPath(DirManager.getPrivateFilesPath());
        SysUtil.checkPath(DirManager.getCachePath());
        SysUtil.checkPath(DirManager.getFilesPath());
        SysUtil.checkPath(DirManager.getAppPath());
        SysUtil.checkPath(DirManager.getDownloadPath());
        SysUtil.checkPath(DirManager.getImageCachePath());
    }
}
