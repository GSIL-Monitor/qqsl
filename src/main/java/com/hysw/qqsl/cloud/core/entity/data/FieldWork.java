package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;

/**
 * 外业坐标实体类
 * @author Administrator
 * @since 2018/8/24
 */
@Entity
@Table(name = "fieldWork")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "fieldWork_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class FieldWork extends BaseEntity {
    private String coordinateStr;
    private Project project;
    private CommonEnum.CommonType commonType;
    // 采集用户
    private long accountId;
    private String name;
    private String deviceMac;

    @JsonIgnore
    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getCoordinateStr() {
        return coordinateStr;
    }

    public void setCoordinateStr(String coordinateStr) {
        this.coordinateStr = coordinateStr;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public CommonEnum.CommonType getCommonType() {
        return commonType;
    }

    public void setCommonType(CommonEnum.CommonType commonType) {
        this.commonType = commonType;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }
}
