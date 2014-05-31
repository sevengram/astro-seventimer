package com.mydeepsky.seventimer.update;

import com.google.gson.Gson;

public class UpdateInfo {
    String version;

    int rev;

    String url;

    String message;

    int level;

    public String getVersion() {
        return version;
    }

    public int getRev() {
        return rev;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
