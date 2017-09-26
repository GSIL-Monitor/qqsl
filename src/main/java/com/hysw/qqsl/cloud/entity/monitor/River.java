package com.hysw.qqsl.cloud.entity.monitor;

import java.io.Serializable;

/**
 * Created by leinuo on 17-2-7.
 *
 * 爬取的重点河道对象
 */
public class River implements Serializable{
    /**
     * 站名
     */
    private String stationName;
    /**
     * 站址
     */
    private String station;
    /**
     * 河名
     */
    private String name;
    /**
     * 水位
     */
    private String waterLevel;
    /**
     * 流量
     */
    private String flow;
    /**
     * 时间
     */
    private String time;
    /**
     * 警戒水位
     */
    private String  warningLevel;

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

    public String getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(String waterLevel) {
        this.waterLevel = waterLevel;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }
}
