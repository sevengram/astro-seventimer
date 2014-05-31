package com.mydeepsky.seventimer.stat.data;

public class DeviceStat extends StatData {
    String uuid;
    
    String version;

    String info;

    public DeviceStat(String uuid, String version, String info) {
        super();
        this.uuid = uuid;
        this.version = version;
        this.info = info;
    }
}
