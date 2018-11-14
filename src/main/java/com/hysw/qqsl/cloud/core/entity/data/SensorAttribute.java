package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

/**
 * @author Administrator
 * @since 2018/11/14
 */
@Entity
@Table(name = "sensorAttribute")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "sensorAttribute_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class SensorAttribute extends BaseEntity {
    /** 名字 */
    private String name;
    /** 显示名字 */
    private String displayName;
    /** 值 */
    private String value;
    /** 类型 */
    private Type type;
    /** 值类型 */
    private ValueType valueType;

    private Sensor sensor;

    public enum Type {
        /** 系统 */
        SYSTEM,
        /** 自定义 */
        CUSTOM
    }

    private enum ValueType {
        /**
         * 数值
         */
        NUMBER,
        /**
         * 字符串
         */
        STRING,
        /**
         * 电话
         */
        PHONE
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    @ManyToOne(fetch= FetchType.LAZY)
    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }
}
