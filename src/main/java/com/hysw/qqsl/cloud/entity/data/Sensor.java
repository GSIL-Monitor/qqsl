package com.hysw.qqsl.cloud.entity.data;


import net.sf.json.JSONArray;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.List;

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

    /** 所属用户*/
    private Long userId;
    /** 唯一编码 */
    private String code;
    /** 类型 */
    private Type type;
    /** 安装地址 */
    private String address;
    /** 安装位置(坐标) */
    private String coordinate;
    /** 描述 */
    private String remark;
    /** 参数 */
    private String parameter;
    /**　是否激活 */
    private boolean activate;
    /** 是否修改过 */
    private boolean transform;
    /** 行政区 */
    private String area;
    /** 仪表分享 */
    private String shares;
    /** 摄像头 */
    private String video;

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
        THROW_WATER;
        public static Type valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }

    @JsonIgnore
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    @JsonIgnore
    public boolean isTransform() {
        return transform;
    }

    public void setTransform(boolean transform) {
        this.transform = transform;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
