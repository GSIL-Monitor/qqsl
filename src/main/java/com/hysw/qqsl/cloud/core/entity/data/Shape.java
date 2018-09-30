package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.buildModel.LineSectionPlaneModel;

import javax.persistence.*;
import java.util.List;

/**
 * 内业
 * @author Administrator
 * @since 2018/9/19
 */
@Entity
@Table(name = "shape")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "shape_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Shape extends BaseEntity {
    private Project project;
    private String remark;
    /**
     * 类型
     */
    private CommonEnum.CommonType commonType;
    /**
     * 剖面
     */
    private List<ShapeAttribute> shapeAttributes;
    /**
     * 坐标组
     */
    private List<ShapeCoordinate> shapeCoordinates;
    private LineSectionPlaneModel.Type childType;
    private boolean errorMsg;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public CommonEnum.CommonType getCommonType() {
        return commonType;
    }

    public void setCommonType(CommonEnum.CommonType commonType) {
        this.commonType = commonType;
    }
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "shape")
    @JsonIgnore
    public List<ShapeAttribute> getShapeAttributes() {
        return shapeAttributes;
    }

    public void setShapeAttributes(List<ShapeAttribute> shapeAttributes) {
        this.shapeAttributes = shapeAttributes;
    }
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "shape")
    @JsonIgnore
    public List<ShapeCoordinate> getShapeCoordinates() {
        return shapeCoordinates;
    }

    public void setShapeCoordinates(List<ShapeCoordinate> shapeCoordinates) {
        this.shapeCoordinates = shapeCoordinates;
    }

    public LineSectionPlaneModel.Type getChildType() {
        return childType;
    }

    public void setChildType(LineSectionPlaneModel.Type childType) {
        this.childType = childType;
    }

    @Transient
    public boolean isErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(boolean errorMsg) {
        this.errorMsg = errorMsg;
    }


}
