package com.mydeepsky.seventimer.stat.data;

public class WeatherStat extends StatData {
    long total_delay;

    long server_delay;

    int network;

    String uuid;

    String version;

    public WeatherStat(long total_delay, long server_delay, int network, String uuid, String version) {
        super();
        this.total_delay = total_delay;
        this.server_delay = server_delay;
        this.network = network;
        this.uuid = uuid;
        this.version = version;
    }

}
