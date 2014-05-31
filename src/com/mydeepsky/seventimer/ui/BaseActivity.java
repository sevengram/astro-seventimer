package com.mydeepsky.seventimer.ui;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

}
