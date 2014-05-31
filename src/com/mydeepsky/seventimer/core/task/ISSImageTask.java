package com.mydeepsky.seventimer.core.task;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
import com.mydeepsky.seventimer.data.ISS;

public class ISSImageTask extends ImageTask {
    static final String TAG = "_ISSImageTask";

    private final String ID = StringUtil.newGuid();

    private final String NAME = "IssImage";

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
        if (params == null || params.length <= 0) {
            return new TaskResult(TaskStatus.Failed, "TaskContext is null");
        }

        TaskContext context = params[0];
        ISS iss = (ISS) context.get(KEY_SATINFO);
        double lat = iss.getLatitude();
        double lon = iss.getLongitude();
        String mjd = iss.getMjd();

        String preurl = String.format(ConfigUtil.getString(Keys.PRE_IMAGE_HEAVENS_ABOVE_ISS), lat,
                lon, mjd);

        HttpClient client = HttpUtil.getHttpClient();
        String html = HttpUtil.getHtmlByUrl(client, preurl);
        if (html == null || "".equals(html)) {
            return new TaskResult(TaskStatus.Failed, "execute error");
        }

        Document document = Jsoup.parse(html);
        String imageUrl = String.format("%s/%s", ConfigUtil.getString(Keys.URL_HEAVENS_ABOVE),
                document.getElementById("ctl00_cph1_imgViewFinder").attr("src"));

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
                if (bitmap == null) {
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
            result.setMessage("Task is canceled");
        }
        return result;
    }
}
