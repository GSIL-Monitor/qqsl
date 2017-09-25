package com.hysw.qqsl.cloud.core.entity;

import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;

import java.io.Serializable;

/**
 * Create by leinuo on 17-9-12 上午10:51
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 *
 *
 */
public class Camera implements Serializable{

    private static final long serialVersionUID = -3097083860247123817L;

    /** ID*/
    private Long id;

    /** 厂家factroy，联系人contact，电话phone等Json */
    private String info;
    /** 摄像头 */
    private String cameraUrl;

    private Station station;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCameraUrl() {
        return cameraUrl;
    }

    public void setCameraUrl(String cameraUrl) {
        this.cameraUrl = cameraUrl;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}
