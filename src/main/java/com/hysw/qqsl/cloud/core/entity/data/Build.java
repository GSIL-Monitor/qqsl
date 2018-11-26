package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;

import javax.persistence.*;
import java.util.List;

/**
 * Created by leinuo on 17-3-27.
 * 建筑物实体类
 */
@Entity
@Table(name="build")
@SequenceGenerator(name="sequenceGenerator", sequenceName="build_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Build extends BaseEntity{

    private static final long serialVersionUID = -7259757300916407016L;
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
    private ChildType childType;
    /**建筑物属性*/
    private List<BuildAttribute> buildAttributes;
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

    private ShapeCoordinate shapeCoordinate;

    public enum ChildType {
        /**
         * 底流式消力池
         */
        DILSXLC(CommonEnum.CommonType.XIAOLC, "buildModel", "底流式消力池", "dlsxlc"),
        KAICSSZ(CommonEnum.CommonType.FSZ, "buildModel", "开敞式水闸", "kcssz"),
        YUANXXSC(CommonEnum.CommonType.XSC, "buildModel", "圆形蓄水池", "yxxsc"),
        QUXSDHX(CommonEnum.CommonType.DHX, "buildModel", "曲线式倒虹吸", "qxsdhx"),
        ;
        //必须增加一个构造函数,变量,得到该变量的值\
        private CommonEnum.CommonType commonType;
        private String type;
        private String typeC;
        private String abbreviate;


        ChildType(CommonEnum.CommonType commonType, String type, String typeC, String abbreviate) {
            this.commonType = commonType;
            this.type = type;
            this.typeC = typeC;
            this.abbreviate = abbreviate;
        }

        public CommonEnum.CommonType getCommonType() {
            return commonType;
        }

        public String getType() {
            return type;
        }

        public String getTypeC() {
            return typeC;
        }

        public String getAbbreviate() {
            return abbreviate;
        }

        public static ChildType valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }

    }

    public enum Source{
        /** 设计 */
        DESIGN,
        /** 外业 */
        FIELD;
    }

    private AttributeGroup coordinate;
    private AttributeGroup waterResources;
    private AttributeGroup controlSize;
    private AttributeGroup groundStress;
    private AttributeGroup component;

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
    public List<BuildAttribute> getBuildAttributes() {
        return buildAttributes;
    }

    public void setBuildAttributes(List<BuildAttribute> buildAttributes) {
        this.buildAttributes = buildAttributes;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Transient
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public ChildType getChildType() {
        return childType;
    }

    public void setChildType(ChildType childType) {
        this.childType = childType;
    }

    public String getDesignElevation() {
        return designElevation;
    }

    public void setDesignElevation(String designElevation) {
        this.designElevation = designElevation;
    }

    @Transient
    public AttributeGroup getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(AttributeGroup coordinate) {
        this.coordinate = coordinate;
    }

    @Transient
    public AttributeGroup getWaterResources() {
        return waterResources;
    }

    public void setWaterResources(AttributeGroup waterResources) {
        this.waterResources = waterResources;
    }

    @Transient
    public AttributeGroup getControlSize() {
        return controlSize;
    }

    public void setControlSize(AttributeGroup controlSize) {
        this.controlSize = controlSize;
    }

    @Transient
    public AttributeGroup getGroundStress() {
        return groundStress;
    }

    public void setGroundStress(AttributeGroup groundStress) {
        this.groundStress = groundStress;
    }

    @Transient
    public AttributeGroup getComponent() {
        return component;
    }

    public void setComponent(AttributeGroup component) {
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

    @OneToOne(cascade={CascadeType.REMOVE,CascadeType.MERGE})
    @JsonIgnore
    public ShapeCoordinate getShapeCoordinate() {
        return shapeCoordinate;
    }

    public void setShapeCoordinate(ShapeCoordinate shapeCoordinate) {
        this.shapeCoordinate = shapeCoordinate;
    }

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
