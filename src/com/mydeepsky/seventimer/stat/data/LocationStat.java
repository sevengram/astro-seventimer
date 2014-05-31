package com.mydeepsky.seventimer.stat.data;

public class LocationStat extends StatData {
    long delay;

    int network;

    int provider;

    String uuid;

    String version;

    double longitude;

    double latitude;

    String location_info;

    public LocationStat(long delay, int network, int provider, String uuid, String version,
            double longitude, double latitude, String location_info) {
        super();
        this.delay = delay;
        this.network = network;
        this.provider = provider;
        this.uuid = uuid;
        this.version = version;
        this.longitude = longitude;
        this.latitude = latitude;
        this.location_info = location_info;
    }
}
