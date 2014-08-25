package com.mydeepsky.seventimer.ui;

import java.lang.reflect.Field;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ZoomButtonsController;

import com.mydeepsky.android.util.ConfigUtil;
import com.mydeepsky.android.util.DeviceUtil;
import com.mydeepsky.android.util.Keys;
import com.mydeepsky.seventimer.R;

public class WebviewActivity extends BaseActivity {
    public static final String EXTRA_INITTIME = "initTime";

    public static final String EXTRA_GEOZONE = "geoZone";

    public static final String EXTRA_TIMEPOINT = "timepoint";

    protected WebView webview;

    private int timepoint;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        setContentView(R.layout.activity_webview);

        webview = (WebView) findViewById(R.id.image_webview);
        webview.setInitialScale(0);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setDefaultZoom(getZoomDensity());
        webview.getSettings().setBuiltInZoomControls(true);
        if (DeviceUtil.getAndroidSdkVersionCode() >= 11) {
            webview.getSettings().setDisplayZoomControls(false);
        } else {
            setZoomControlGone(webview);
        }
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.setWebChromeClient(new SimpleWebChromeClient());
        webview.setWebViewClient(new SimpleWebViewClient());

        Intent intent = getIntent();
        timepoint = intent.getIntExtra(EXTRA_TIMEPOINT, 0);
        String url = String.format(Locale.getDefault(),
                ConfigUtil.getString(Keys.CHART_7TIMER_WEATHER),
                intent.getIntExtra(EXTRA_INITTIME, 0), intent.getStringExtra(EXTRA_GEOZONE),
                timepoint);
        webview.loadDataWithBaseURL(null, String.format("<center><img src=%s></center>", url),
                "text/html", "utf-8", null);
    }

    protected class SimpleWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            setProgress(newProgress * 100);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            setTitle(title);
            super.onReceivedTitle(view, title);
        }
    }

    protected class SimpleWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private ZoomDensity getZoomDensity() {
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        ZoomDensity zoomDensity;
        switch (screenDensity) {
        case DisplayMetrics.DENSITY_LOW:
            zoomDensity = ZoomDensity.CLOSE;
            break;
        case DisplayMetrics.DENSITY_MEDIUM:
            zoomDensity = ZoomDensity.MEDIUM;
            break;
        case DisplayMetrics.DENSITY_HIGH:
            zoomDensity = ZoomDensity.FAR;
            break;
        case DisplayMetrics.DENSITY_XHIGH:
            zoomDensity = ZoomDensity.FAR;
            break;
        default:
            zoomDensity = ZoomDensity.MEDIUM;
            break;
        }
        return zoomDensity;
    }

    protected void setZoomControlGone(View view) {
        try {
            Class<WebView> classType = WebView.class;
            Field field = classType.getDeclaredField("mZoomButtonsController");
            field.setAccessible(true);
            ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(view);
            mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
            field.set(view, mZoomButtonsController);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void onClickNextImage(View v) {
        if (timepoint == 72) {
            return;
        }
        Intent intent = getIntent();
        timepoint += 3;
        String url = String.format(Locale.getDefault(),
                ConfigUtil.getString(Keys.CHART_7TIMER_WEATHER),
                intent.getIntExtra(EXTRA_INITTIME, 0), intent.getStringExtra(EXTRA_GEOZONE),
                timepoint);
        webview.loadDataWithBaseURL(null, String.format("<center><img src=%s></center>", url),
                "text/html", "utf-8", null);
    }

    public void onClickPreviousImage(View v) {
        if (timepoint == 0) {
            return;
        }
        Intent intent = getIntent();
        timepoint -= 3;
        String url = String.format(Locale.getDefault(),
                ConfigUtil.getString(Keys.CHART_7TIMER_WEATHER),
                intent.getIntExtra(EXTRA_INITTIME, 0), intent.getStringExtra(EXTRA_GEOZONE),
                timepoint);
        webview.loadDataWithBaseURL(null, String.format("<center><img src=%s></center>", url),
                "text/html", "utf-8", null);
    }
}
