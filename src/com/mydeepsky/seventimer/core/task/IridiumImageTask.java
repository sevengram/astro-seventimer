package com.mydeepsky.seventimer.core.task;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mydeepsky.android.task.TaskContext;
import com.mydeepsky.android.task.TaskResult;
import com.mydeepsky.android.task.TaskResult.TaskStatus;
import com.mydeepsky.android.util.ConfigUtil;
import com.mydeepsky.android.util.HttpUtil;
import com.mydeepsky.android.util.Keys;
import com.mydeepsky.android.util.StringUtil;
import com.mydeepsky.seventimer.data.Iridium;

public class IridiumImageTask extends ImageTask {
    static final String TAG = "_IridiumImageTask";

    private final String ID = StringUtil.newGuid();

    private final String NAME = "IridiumImage";

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    protected TaskResult doInBackground(TaskContext... params) {
        if (params.length <= 0) {
            return new TaskResult(TaskStatus.Failed, "TaskContext is null");
        }

        TaskContext context = params[0];

        Iridium iridium = (Iridium) context.get(KEY_SATINFO);
        double lat = iridium.getLatitude();
        double lng = iridium.getLongitude();
        int fid = iridium.getFid();

        String imageUrl = String.format(ConfigUtil.getString(Keys.IMAGE_HEAVENS_ABOVE_IRIDIUM),
                lat, lng, fid);
        String preurl = String.format(ConfigUtil.getString(Keys.PRE_HEAVENS_ABOVE_IRIDIUM), lat,
                lng);

        HttpClient client = HttpUtil.getHttpClient();
        String html = HttpUtil.getHtmlByUrl(client, preurl);
        if (html == null || "".equals(html)) {
            return new TaskResult(TaskStatus.Failed, "execute error");
        }

        HttpGet request = HttpUtil.getHttpGetRequest(imageUrl, "image/png");
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            return new TaskResult(TaskStatus.Failed, "execute error");
        }

        TaskResult result = new TaskResult(TaskStatus.Failed);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(response.getEntity().getContent());
                if (bitmap == null){
                    return new TaskResult(TaskStatus.Failed, "empty image");
                }
                context.set(KEY_RESULT, bitmap);
                result.setStatus(TaskStatus.Finished);
                result.setContext(context);
            } catch (IOException e) {
                return new TaskResult(TaskStatus.Failed, "execute error");
            }
        } else {
            Log.d(TAG, response.getStatusLine().getStatusCode() + "");
        }

        if (isCancelled()) {
            result.setStatus(TaskStatus.Cancel);
            result.setMessage("Task is cancel");
        }
        return result;
    }

}
