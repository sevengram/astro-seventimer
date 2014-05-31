package com.mydeepsky.seventimer.stat.data;

public class SatelliteStat extends StatData {
    long delay;

    int network;

    String uuid;

    String version;

    public SatelliteStat(long delay, int network, String uuid, String version) {
        super();
        this.delay = delay;
        this.network = network;
        this.uuid = uuid;
        this.version = version;
    }
}
