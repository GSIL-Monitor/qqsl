package com.hysw.qqsl.cloud.core.entity.data;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;

/**
 * 仪表实体类
 *
 * @since 2017年04月07日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name = "sensor")
@SequenceGenerator(name = "sequenceGenerator",sequenceName = "sensor_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Sensor extends BaseEntity {

    private static final long serialVersionUID = -3097083860247123817L;
    /** 唯一编码 */
    private String code;
    /** 类型 */
    private Type type;
    /**　是否激活 */
    private boolean activate;
    /** 厂家factroy，联系人contact，电话phone等Json */
    private String info;
    /** 摄像头 */
    private String cameraUrl;

    private Station station;
    /** 安装高度*/
    private Double settingHeight;
    public enum Type {
        // 宏电液位仪
        IRTU,
        // 超声波液位仪
        ULTRA_WATER,
        // 超声波液差计
        ULTRA_DIFF,
        // 明渠流量计
        CANAL_FLOW,
        // 多普勒流速计
        DOPPLER_FLOW,
        // 雷达物位计
        RADAR,
        // 投入式液位计
        THROW_WATER,
        // 摄像头
        CAMERA;
        public static Type valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getCameraUrl() {
        return cameraUrl;
    }

    public void setCameraUrl(String cameraUrl) {
        this.cameraUrl = cameraUrl;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
    public Double getSettingHeight() {
        return settingHeight;
    }

    public void setSettingHeight(Double settingHeight) {
        this.settingHeight = settingHeight;
    }

}
