package com.mydeepsky.seventimer.core.task;

import com.mydeepsky.android.task.Task;
import com.mydeepsky.seventimer.data.Satellite;

public abstract class ImageTask extends Task {

    public final static String KEY_SATINFO = "image_task_satinfo";

    public static ImageTask getTask(Satellite satellite) {
        switch (satellite.getType()) {
        case Satellite.ISS:
            return new ISSImageTask();
        case Satellite.IRIDIUM:
            return new IridiumImageTask();
        default:
            return null;
        }
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }
}
