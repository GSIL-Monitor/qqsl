package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;
import java.util.List;

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
    private Project project;
    // 采集用户
    private long accountId;
    private String name;
    private String deviceMac;
    private List<FieldWorkPoint> fieldWorkPoints;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "fieldWork")
    @JsonIgnore
    public List<FieldWorkPoint> getFieldWorkPoints() {
        return fieldWorkPoints;
    }

    public void setFieldWorkPoints(List<FieldWorkPoint> fieldWorkPoints) {
        this.fieldWorkPoints = fieldWorkPoints;
    }
}
