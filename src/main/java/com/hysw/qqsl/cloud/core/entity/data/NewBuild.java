package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;
import com.hysw.qqsl.cloud.core.entity.buildModel.NewAttributeGroup;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/10/23
 */
@Entity
@Table(name = "newBuild")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "newBuild_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class NewBuild extends BaseEntity{
    /**建筑物名称*/
    private String name;
    /**建筑物别名*/
    private String alias;
    /**
     * 建筑物类型
     */
    private CommonEnum.CommonType type;
    /**
     * 建筑物子类型
     */
    private Build.ChildType childType;
    /**建筑物属性*/
    private List<NewBuildAttribute> newBuildAttributes;
    /** 所属项目 */
    private Long projectId;
    /** 中心坐标 */
    private String centerCoor;
    private int centerCoorNum;
    /** 定位坐标 */
    private String positionCoor;
    private int positionCoorNum;
    /** 设计标高 */
    private String designElevation;
    private int designElevationNum;
    /** 来源 */
    private Build.Source source;
    /** 描述 */
    private String remark;
    private int remarkNum;
//    /** 外业fieldWork或内业coordinate坐标id */
//    private Long commonId;
    /** 错误标记 */
    private boolean errorMsg=false;
    /** 随机字符串 */
    private String noticeStr;
    /** 生成模板个数 */
    private int number;

    public enum Source{
        /** 设计 */
        DESIGN,
        /** 外业 */
        FIELD;
    }

    private NewAttributeGroup coordinate;
    private NewAttributeGroup waterResource;
    private NewAttributeGroup controlSize;
    private NewAttributeGroup groundStress;
    private NewAttributeGroup component;

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

    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},  fetch=FetchType.LAZY, mappedBy="build")
    @JsonIgnore
    public List<NewBuildAttribute> getNewBuildAttributes() {
        return newBuildAttributes;
    }

    public void setNewBuildAttributes(List<NewBuildAttribute> newBuildAttributes) {
        this.newBuildAttributes = newBuildAttributes;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

//    @Transient
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
    public Build.Source getSource() {
        return source;
    }

    public void setSource(Build.Source source) {
        this.source = source;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Build.ChildType getChildType() {
        return childType;
    }

    public void setChildType(Build.ChildType childType) {
        this.childType = childType;
    }

    public String getDesignElevation() {
        return designElevation;
    }

    public void setDesignElevation(String designElevation) {
        this.designElevation = designElevation;
    }

    @Transient
    public NewAttributeGroup getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(NewAttributeGroup coordinate) {
        this.coordinate = coordinate;
    }

    @Transient
    public NewAttributeGroup getWaterResource() {
        return waterResource;
    }

    public void setWaterResource(NewAttributeGroup waterResource) {
        this.waterResource = waterResource;
    }

    @Transient
    public NewAttributeGroup getControlSize() {
        return controlSize;
    }

    public void setControlSize(NewAttributeGroup controlSize) {
        this.controlSize = controlSize;
    }

    @Transient
    public NewAttributeGroup getGroundStress() {
        return groundStress;
    }

    public void setGroundStress(NewAttributeGroup groundStress) {
        this.groundStress = groundStress;
    }

    @Transient
    public NewAttributeGroup getComponent() {
        return component;
    }

    public void setComponent(NewAttributeGroup component) {
        this.component = component;
    }

    @Transient
    public boolean isErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(boolean errorMsg) {
        this.errorMsg = errorMsg;
    }
    public void setErrorMsgTrue() {
        this.errorMsg = true;
    }

    @Transient
    public String getNoticeStr() {
        return noticeStr;
    }

    public void setNoticeStr(String noticeStr) {
        this.noticeStr = noticeStr;
    }

    @Transient
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

//    public Long getCommonId() {
//        return commonId;
//    }
//
//    public void setCommonId(Long commonId) {
//        this.commonId = commonId;
//    }

    @Transient
    public int getCenterCoorNum() {
        return centerCoorNum;
    }

    public void setCenterCoorNum(int centerCoorNum) {
        this.centerCoorNum = centerCoorNum;
    }

    @Transient
    public int getPositionCoorNum() {
        return positionCoorNum;
    }

    public void setPositionCoorNum(int positionCoorNum) {
        this.positionCoorNum = positionCoorNum;
    }

    @Transient
    public int getDesignElevationNum() {
        return designElevationNum;
    }

    public void setDesignElevationNum(int designElevationNum) {
        this.designElevationNum = designElevationNum;
    }

    @Transient
    public int getRemarkNum() {
        return remarkNum;
    }

    public void setRemarkNum(int remarkNum) {
        this.remarkNum = remarkNum;
    }
}
