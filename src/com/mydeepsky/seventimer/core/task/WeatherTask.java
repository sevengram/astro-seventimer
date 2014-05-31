package com.mydeepsky.seventimer.core.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.mydeepsky.android.task.Task;
import com.mydeepsky.android.task.TaskContext;
import com.mydeepsky.android.task.TaskResult;
import com.mydeepsky.android.task.TaskResult.TaskStatus;
import com.mydeepsky.android.util.StringUtil;

public class WeatherTask extends Task {
    public final static String KEY_LON = "lon";
    public final static String KEY_LAT = "lat";
    public final static String KEY_URL = "url";

    private final String ID = StringUtil.newGuid();
    private final String NAME = "WeatherTask";

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public void pause() {
        // Do nothing
    }

    @Override
    public void resume() {
        // Do nothing
    }

    @Override
    protected TaskResult doInBackground(TaskContext... contexts) {
        if (contexts == null || contexts.length <= 0) {
            return new TaskResult(TaskStatus.Failed, "TaskContext is null");
        }
        if (isCancelled()) {
            return new TaskResult(TaskStatus.Cancel, "Cancelled");
        }
        TaskContext context = contexts[0];
        HttpClient client = new DefaultHttpClient();
        HttpPost postMethod = new HttpPost((String) context.get(KEY_URL));
        HttpResponse response = null;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(KEY_LON, context.getString(KEY_LON)));
        params.add(new BasicNameValuePair(KEY_LAT, context.getString(KEY_LAT)));
        try {
            postMethod.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            response = client.execute(postMethod);
        } catch (IOException e) {
            return new TaskResult(TaskStatus.Failed, "Request error");
        }
        if (response == null) {
            return new TaskResult(TaskStatus.Failed, "No response");
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent()));
                StringBuffer sb = new StringBuffer();
                String res = br.readLine();
                while (res != null) {
                    sb.append(res);
                    res = br.readLine();
                }
                context.set(KEY_RESULT, sb.toString());
            } catch (IOException e) {
                return new TaskResult(TaskStatus.Failed, "Content error!");
            }
        } else {
            return new TaskResult(TaskStatus.Failed, "Http status code " + statusCode);
        }

        if (isCancelled()) {
            return new TaskResult(TaskStatus.Cancel, "Cancelled");
        }
        TaskResult result = new TaskResult(TaskStatus.Finished);
        context.set(KEY_END_TIME, System.currentTimeMillis());
        result.setContext(context);
        return result;
    }
}
