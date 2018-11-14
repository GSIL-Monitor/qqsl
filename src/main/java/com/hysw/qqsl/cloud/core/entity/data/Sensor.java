package com.hysw.qqsl.cloud.core.entity.data;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    /** 仪表名 */
    private String name;
    /** 唯一编码 */
    private String code;
    /** 描述 */
    private String description;
    /** 类型 */
    private Type type;
    /**　是否激活 */
    private boolean activate;
    /** 厂家 */
    private String factroy;
    /** 联系人 */
    private String contact;
    /** 电话 */
    private String phone;
    /** 安装高度 */
    private Double settingHeight;
    /** 安装海拔 */
    private Double settingElevation;
    /** 安装地址 */
    private String settingAddress;
    /** 测量范围 */
    private String measureRange;
    /** 测量上限 */
    private Double maxValue;
    /** 超过测量上限是否发送短息 */
    private boolean isMaxValueWaring;
    /** 测量下限 */
    private Double minValue;
    /** 低于测量下限是否发送短息 */
    private boolean isMinValueWaring;
    /** 是否编辑过仪表 */
    private boolean isChanged;
    /** 图片地址 */
    private String pictureurl;

    private Station station;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFactroy() {
        return factroy;
    }

    public void setFactroy(String factroy) {
        this.factroy = factroy;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getSettingElevation() {
        return settingElevation;
    }

    public void setSettingElevation(Double settingElevation) {
        this.settingElevation = settingElevation;
    }

    public String getSettingAddress() {
        return settingAddress;
    }

    public void setSettingAddress(String settingAddress) {
        this.settingAddress = settingAddress;
    }

    public String getMeasureRange() {
        return measureRange;
    }

    public void setMeasureRange(String measureRange) {
        this.measureRange = measureRange;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public boolean isMaxValueWaring() {
        return isMaxValueWaring;
    }

    public void setMaxValueWaring(boolean maxValueWaring) {
        isMaxValueWaring = maxValueWaring;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public boolean isMinValueWaring() {
        return isMinValueWaring;
    }

    public void setMinValueWaring(boolean minValueWaring) {
        isMinValueWaring = minValueWaring;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public String getPictureurl() {
        return pictureurl;
    }

    public void setPictureurl(String pictureurl) {
        this.pictureurl = pictureurl;
    }
}
