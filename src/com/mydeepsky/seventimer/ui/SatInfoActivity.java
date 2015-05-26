package com.mydeepsky.seventimer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mydeepsky.android.util.DisplayUtil;
import com.mydeepsky.seventimer.R;
import com.mydeepsky.seventimer.data.ISS;
import com.mydeepsky.seventimer.data.Iridium;
import com.mydeepsky.seventimer.data.Satellite;

public class SatInfoActivity extends BaseActivity {

    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";

    public static final String EXTRA_SATINFO = "EXTRA_SATINFO";

    private Bitmap mSatImage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satinfo);

        byte[] data = getIntent().getByteArrayExtra(EXTRA_IMAGE);
        if (data != null) {
            mSatImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            ImageView imageView = (ImageView) findViewById(R.id.imageview_satellite);
            imageView.setImageBitmap(mSatImage);
            imageView.getLayoutParams().width = DisplayUtil.getWidthPx(this);
        }

        ViewGroup infoWrapper = (ViewGroup) findViewById(R.id.linearlayout_satellite);
        Satellite satellite = (Satellite) getIntent().getSerializableExtra(EXTRA_SATINFO);
        if (satellite != null) {
            infoWrapper.addView(getView(this, satellite));
        }
    }

    @Override
    protected void onDestroy() {
        if (mSatImage != null) {
            mSatImage.recycle();
        }
        super.onDestroy();
    }

    private View getView(Context context, Satellite satellite) {
        View infoView = null;
        if (satellite.getType() == Satellite.ISS) {
            ISS iss = (ISS) satellite;
            infoView = View.inflate(context, R.layout.linearlayout_iss, null);
            TextView issmagTextView = (TextView) infoView.findViewById(R.id.textview_iss_mag);
            TextView dateTextView = (TextView) infoView.findViewById(R.id.textview_iss_date);
            TextView starttimeTextView = (TextView) infoView
                .findViewById(R.id.textview_iss_starttime);
            TextView startposTextView = (TextView) infoView
                .findViewById(R.id.textview_iss_startpos);
            TextView maxtimeTextView = (TextView) infoView.findViewById(R.id.textview_iss_maxtime);
            TextView maxposTextView = (TextView) infoView.findViewById(R.id.textview_iss_maxpos);
            TextView endtimeTextView = (TextView) infoView.findViewById(R.id.textview_iss_endtime);
            TextView endposTextView = (TextView) infoView.findViewById(R.id.textview_iss_endpos);
            issmagTextView.setText(String.format("ISS (%.1f)", iss.getMagnitude()));
            dateTextView.setText(getString(R.string.text_satinfo_date, iss.getDate()));
            starttimeTextView.setText(getString(R.string.text_satinfo_start_time,
                iss.getStartTime()));
            startposTextView
                .setText(getString(R.string.text_satinfo_start_pos, iss.getStartAltAz()));
            maxtimeTextView.setText(getString(R.string.text_satinfo_highest_time,
                iss.getHighestTimeString()));
            maxposTextView.setText(getString(R.string.text_satinfo_highest_pos,
                iss.getHighestAltAz()));
            endtimeTextView.setText(getString(R.string.text_satinfo_end_time, iss.getEndTime()));
            endposTextView.setText(getString(R.string.text_satinfo_end_pos, iss.getEndAltAz()));
        } else if (satellite.getType() == Satellite.IRIDIUM) {
            Iridium iridium = (Iridium) satellite;
            infoView = View.inflate(context, R.layout.linearlayout_iridium, null);
            TextView ifmagTextView = (TextView) infoView.findViewById(R.id.textview_iridium_mag);
            TextView dateTextView = (TextView) infoView.findViewById(R.id.textview_iridium_date);
            TextView timeTextView = (TextView) infoView.findViewById(R.id.textview_iridium_time);
            TextView posTextView = (TextView) infoView.findViewById(R.id.textview_iridium_pos);
            TextView cenTextView = (TextView) infoView.findViewById(R.id.textview_iridium_center);
            TextView sunTextView = (TextView) infoView.findViewById(R.id.textview_iridium_sunalt);
            ifmagTextView.setText(String.format("%s (%.1f)", iridium.getName(),
                iridium.getMagnitude()));
            dateTextView.setText(getString(R.string.text_satinfo_date, iridium.getDate()));
            timeTextView.setText(getString(R.string.text_satinfo_time,
                iridium.getHighestTimeString()));
            posTextView.setText(getString(R.string.text_satinfo_pos, iridium.getHighestAltAz()));
            cenTextView.setText(getString(R.string.text_satinfo_flare_center,
                iridium.getMagCenter(), iridium.getDistance()));
            sunTextView.setText(getString(R.string.text_satinfo_sun_alt, iridium.getSunAlt()));
        }
        return infoView;
    }
}
