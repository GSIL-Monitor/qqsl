package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;
import java.util.List;

/**
 * Created by leinuo on 17-3-27.
 * 建筑物实体类
 */
@Entity
@Table(name="build1")
@SequenceGenerator(name="sequenceGenerator", sequenceName="build1_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Build1 extends BaseEntity{

    private static final long serialVersionUID = -7259757300916407016L;
    /**建筑物名称*/
    private String name;
    /**建筑物别名*/
    private String alias;
    /**建筑物类型*/
    private CommonEnum.CommonType type;
    /** 所属项目 */
    private Project project;
    /** 中心坐标 */
    private String centerCoor;
    /** 定位坐标 */
    private String positionCoor;
    /** 来源 */
    private Source source;
    /** 描述 */
    private String remark;
    /** 坐标id */
    private Long coordinateId;

    public enum Source{
        /** 设计 */
        DESIGN,
        /** 外业 */
        FIELD;
    }

    //非数据库对应
    private String mater;
    private String dimensions;
    private String hydraulics;
    private String geology;
    private String structure;
    /** 用于检索 */
    private String py;

    @Transient
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public CommonEnum.CommonType getType() {
        return type;
    }

    public void setType(CommonEnum.CommonType type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

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
    @JsonIgnore
    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCoordinateId() {
        return coordinateId;
    }

    public void setCoordinateId(Long coordinateId) {
        this.coordinateId = coordinateId;
    }

    @Transient
    public String getMater() {
        return mater;
    }

    public void setMater(String mater) {
        this.mater = mater;
    }
    @Transient
    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    @Transient
    public String getHydraulics() {
        return hydraulics;
    }

    public void setHydraulics(String hydraulics) {
        this.hydraulics = hydraulics;
    }
    @Transient
    public String getGeology() {
        return geology;
    }

    public void setGeology(String geology) {
        this.geology = geology;
    }
    @Transient
    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }
    @Transient
    public String getPy() {
        return py;
    }

    public void setPy(String py) {
        this.py = py;
    }

}
