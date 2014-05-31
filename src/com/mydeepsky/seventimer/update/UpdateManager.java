package com.mydeepsky.seventimer.update;

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.mydeepsky.android.util.DirManager;
import com.mydeepsky.seventimer.R;

public class UpdateManager {
    static final String TAG = "_UpdateManager";

    private Context mAppContext;

    private DownloadManager mDownloadManager;

    private long mTaskId;

    private String mApkPath;

    private static UpdateManager sInstance;

    public static synchronized UpdateManager getInstance(Context context) {
        if (sInstance == null)
            sInstance = new UpdateManager(context.getApplicationContext());
        return sInstance;
    }

    private UpdateManager(Context context) {
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        mAppContext = context;
        mAppContext.registerReceiver(new DownloadCompleteReceiver(), new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void downloadApk(String url) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(mAppContext.getText(R.string.title_update));
        mApkPath = DirManager.getDownloadPath() + "/" + uri.getLastPathSegment();
        request.setDestinationUri(Uri.fromFile(new File(mApkPath)));
        mTaskId = mDownloadManager.enqueue(request);
    }

    class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downId == mTaskId) {
                    installApk(new File(mApkPath));
                }
            }
        }
    }

    private void installApk(File file) {
        if (file.toString().endsWith(".apk")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            mAppContext.startActivity(intent);
        } else {

        }
    }
}
