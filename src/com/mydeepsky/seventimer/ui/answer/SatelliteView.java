package com.mydeepsky.seventimer.ui.answer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.mydeepsky.android.task.Task.OnTaskListener;
import com.mydeepsky.android.task.*;
import com.mydeepsky.android.util.ImageUtil;
import com.mydeepsky.android.util.NetworkManager;
import com.mydeepsky.seventimer.R;
import com.mydeepsky.seventimer.core.cache.CacheManager;
import com.mydeepsky.seventimer.core.task.ImageTask;
import com.mydeepsky.seventimer.data.ISS;
import com.mydeepsky.seventimer.data.Iridium;
import com.mydeepsky.seventimer.data.Satellite;
import com.mydeepsky.seventimer.ui.SatInfoActivity;
import com.mydeepsky.seventimer.ui.dialog.DialogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.*;

public class SatelliteView implements IAnswerView {
    private View root;

    private Context context;

    private List<Map<String, Object>> listItem = new ArrayList<>();

    private List<Satellite> satellites = new ArrayList<>();

    private Dialog refreshDialog;

    private CacheManager cacheManager;

    public SatelliteView(Context context, View root) {
        this.root = root;
        this.context = context;
        this.refreshDialog = DialogManager.createRefreshDialog(context);
        this.cacheManager = CacheManager.getInstance(context);
        ListView satelliteListView = (ListView) root.findViewById(R.id.listview_passes);
        SimpleAdapter listItemAdapter = new SimpleAdapter(context, listItem,
            R.layout.list_item_satellite, new String[] {Satellite.KEY_IMAGE,
            Satellite.KEY_MAG, Satellite.KEY_NAME, Satellite.KEY_DATE,
            Satellite.KEY_START, Satellite.KEY_HIGHEST, Satellite.KEY_END}, new int[] {
            R.id.imageview_satellite, R.id.textview_magnitude, R.id.textview_satname,
            R.id.textview_date, R.id.textview_startinfo, R.id.textview_highestinfo,
            R.id.textview_endinfo});
        satelliteListView.setAdapter(listItemAdapter);
        satelliteListView.setOnItemClickListener(listener);
    }

    @Override
    public View getView(Context context) {
        return root;
    }

    @Override
    public void setData(Object data) {
        try {
            JSONArray issRecords = ((JSONObject) data).getJSONArray("iss");
            JSONArray iridiumRecords = ((JSONObject) data).getJSONArray("iridium");

            for (int i = 0; i < issRecords.length(); i++) {
                ISS iss = new ISS((JSONObject) issRecords.get(i));
                if (iss.getHighestTime().after(new Date())) {
                    satellites.add(iss);
                }
            }

            for (int i = 0; i < iridiumRecords.length(); i++) {
                Iridium iridium = new Iridium((JSONObject) iridiumRecords.get(i));
                if (iridium.getHighestTime().after(new Date())) {
                    satellites.add(iridium);
                }
            }

            Collections.sort(satellites);
            for (Satellite satellite : satellites) {
                listItem.add(satellite.getMap());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addChildView(Context context, LinearLayout parentLayout) {
        // TODO Auto-generated method stub

    }

    private AdapterView.OnItemClickListener listener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Satellite satellite = satellites.get(arg2);
            Bitmap bitmap = cacheManager.getImageCache(satellite.getID());
            if (bitmap != null) {
                Intent intent = new Intent(context, SatInfoActivity.class);
                intent.putExtra(SatInfoActivity.EXTRA_IMAGE, ImageUtil.bitmap2bytes(bitmap));
                intent.putExtra(SatInfoActivity.EXTRA_SATINFO, satellite);
                context.startActivity(intent);
            } else {
                if (!NetworkManager.isNetworkAvailable()) {
                    Toast.makeText(context, R.string.toast_without_network, Toast.LENGTH_SHORT)
                        .show();
                }
                TaskContext taskContext = new TaskContext();
                taskContext.set(ImageTask.KEY_SATINFO, satellite);
                ImageTask task = ImageTask.getTask(satellite);
                if (task != null) {
                    refreshDialog.show();
                    task.addTaskListener(new WeakReference<>(taskListener));
                    task.execute(taskContext);
                }
            }
        }
    };

    private OnTaskListener taskListener = new OnTaskListener() {

        @Override
        public void onTimeout(Object sender, TaskTimeoutEvent event) {
            refreshDialog.dismiss();
            Toast.makeText(context, R.string.toast_timeout, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskFinished(Object sender, TaskFinishedEvent event) {
            Bitmap response = (Bitmap) event.getContext().get(ImageTask.KEY_RESULT);
            Satellite satellite = (Satellite) event.getContext().get(ImageTask.KEY_SATINFO);
            cacheManager.createImageCache(response, satellite.getID());
            Intent intent = new Intent(context, SatInfoActivity.class);
            intent.putExtra(SatInfoActivity.EXTRA_IMAGE, ImageUtil.bitmap2bytes(response));
            intent.putExtra(SatInfoActivity.EXTRA_SATINFO, satellite);
            context.startActivity(intent);
            refreshDialog.dismiss();
        }

        @Override
        public void onTaskFailed(Object sender, TaskFailedEvent event) {
            refreshDialog.dismiss();
            Toast.makeText(context, R.string.toast_image_failed, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTaskCancel(Object sender, TaskCancelEvent event) {
            // TODO Auto-generated method stub

        }
    };

}
