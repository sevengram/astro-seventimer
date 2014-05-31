package com.mydeepsky.seventimer.core.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mydeepsky.android.task.Task;
import com.mydeepsky.android.task.TaskContext;
import com.mydeepsky.android.task.TaskResult;
import com.mydeepsky.android.task.TaskResult.TaskStatus;
import com.mydeepsky.android.util.StringUtil;

public class StatTask extends Task {
    public final static String URL = "stat_task_url";
    public final static String REQUEST = "stat_task_request";

    private final static String ID = StringUtil.newGuid();
    private final static String TYPE = "Stat";

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    protected TaskResult doInBackground(TaskContext... params) {
        if (params == null || params.length <= 0) {
            return new TaskResult(TaskStatus.Failed);
        }

        TaskContext context = params[0];
        HttpClient client = new DefaultHttpClient();
        HttpPost postMethod = new HttpPost(context.getString(URL));
        HttpResponse response = null;
        postMethod.setEntity(new ByteArrayEntity(context.getString(REQUEST).getBytes()));
        try {
            response = client.execute(postMethod);
        } catch (IOException e) {
            return new TaskResult(TaskStatus.Failed);
        }
        if (response == null) {
            return new TaskResult(TaskStatus.Failed);
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
                return new TaskResult(TaskStatus.Failed);
            }
        } else {
            return new TaskResult(TaskStatus.Failed);
        }
        TaskResult taskResult = new TaskResult(TaskStatus.Finished);
        taskResult.setContext(context);
        return taskResult;
    }

}
