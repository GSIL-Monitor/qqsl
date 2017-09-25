package com.hysw.qqsl.cloud.core.entity.monitor;

import java.io.Serializable;

/**
 * Created by leinuo on 17-2-7.
 *
 * 爬取的重点水库对象
 */
public class Reservoir implements Serializable{
    /**
     * 站名
     */
    private String stationName;
    /**
     * 站地
     */
    private String station;
    /**
     * 河名
     */
    private String name;
    /**
     * 库水位
     */
    private String stationLevel;
    /**
     * 蓄水量
     */
    private String capacity;
    /**
     * 时间
     */
    private String time;
    /**
     * 汛限水位
     */
    private String  limitedWaterLevel ;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStationLevel() {
        return stationLevel;
    }

    public void setStationLevel(String stationLevel) {
        this.stationLevel = stationLevel;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLimitedWaterLevel() {
        return limitedWaterLevel;
    }

    public void setLimitedWaterLevel(String limitedWaterLevel) {
        this.limitedWaterLevel = limitedWaterLevel;
    }
}
