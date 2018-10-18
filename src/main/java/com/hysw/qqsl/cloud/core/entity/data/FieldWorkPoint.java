package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;

/**
 * @author Administrator
 * @since 2018/10/18
 */
@Entity
@Table(name = "fieldWorkPoint")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "fieldWorkPoint_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class FieldWorkPoint extends BaseEntity {
    /** 中心坐标 */
    private String centerCoor;
    /** 定位坐标 */
    private String positionCoor;
    /**建筑物别名*/
    private String alias;
    /** 描述 */
    private String description;
    private CommonEnum.CommonType commonType;

    private FieldWork fieldWork;

    public String getCenterCoor() {
        return centerCoor;
    }

    public void setCenterCoor(String centerCoor) {
        this.centerCoor = centerCoor;
    }

    public String getPositionCoor() {
        return positionCoor;
    }

    public void setPositionCoor(String positionCoor) {
        this.positionCoor = positionCoor;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CommonEnum.CommonType getCommonType() {
        return commonType;
    }

    public void setCommonType(CommonEnum.CommonType commonType) {
        this.commonType = commonType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    public FieldWork getFieldWork() {
        return fieldWork;
    }

    public void setFieldWork(FieldWork fieldWork) {
        this.fieldWork = fieldWork;
    }
}
