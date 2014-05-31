package com.mydeepsky.seventimer.stat.data;

public class StartAppStat extends StatData {
    String uuid;

    String version;

    String channel;

    public StartAppStat(String uuid, String version, String channel) {
        super();
        this.uuid = uuid;
        this.version = version;
        this.channel = channel;
    }

}
