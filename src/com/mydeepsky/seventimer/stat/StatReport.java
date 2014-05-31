package com.mydeepsky.seventimer.stat;

import com.google.gson.Gson;
import com.mydeepsky.seventimer.stat.data.StatData;

public class StatReport {
    String type;

    StatData data;

    public StatReport(String type, StatData data) {
        super();
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
