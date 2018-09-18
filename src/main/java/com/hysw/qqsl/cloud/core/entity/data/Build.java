package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.builds.AttributeGroup;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private Project project;
    /** 中心坐标 */
    private String centerCoor;
    /** 中心坐标行号 */
    private Integer centerCoorNum;
    /** 定位坐标 */
    private String positionCoor;
    /** 定位坐标行号 */
    private Integer positionCoorNum;
    /** 设计标高 */
    private String designElevation;
    /** 设计标高行高 */
    private Integer designElevationNum;
    /** 来源 */
    private Source source;
    /** 描述 */
    private String remark;
    /** 描述行号 */
    private Integer remarkNum;
    /** 外业fieldWork或内业coordinate坐标id */
    private Long commonId;
    /** 错误标记 */
    private boolean errorMsg=false;
    /** 随机字符串 */
    private String noticeStr;
    /** 生成模板个数 */
    private int number;
    /** 错误信息 */
    private Map<Integer, String> errorMsgInfo = new LinkedHashMap<>();

    public enum ChildType {
        /**
         * 底流式消力池
         */
        DILSXLC(CommonEnum.CommonType.XIAOLC, "builds", "底流式消力池", "dlsxlc"),
        KAICSSZ(CommonEnum.CommonType.FSZ, "builds", "开敞式水闸", "kcssz"),
        ZHONGLSDQ(CommonEnum.CommonType.DANGQ, "builds", "重力式挡墙", "zlsdq"),
        FUBSDQ(CommonEnum.CommonType.DANGQ, "builds", "扶臂式挡墙", "fbsdq"),
        ANPSDQ(CommonEnum.CommonType.DANGQ, "builds", "岸坡式挡墙", "apsdq"),
        YUANXXSC(CommonEnum.CommonType.XSC, "builds", "圆形蓄水池", "yxxsc"),
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

    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE, CascadeType.MERGE},  fetch=FetchType.LAZY, mappedBy="build")
    @JsonIgnore
    public List<BuildAttribute> getBuildAttributes() {
        return buildAttributes;
    }

    public void setBuildAttributes(List<BuildAttribute> buildAttributes) {
        this.buildAttributes = buildAttributes;
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
    public Map<Integer, String> getErrorMsgInfo() {
        return errorMsgInfo;
    }

    public void setErrorMsgInfo(Integer key,String value) {
        this.errorMsgInfo.put(key, value);
    }

    @Transient
    public Integer getCenterCoorNum() {
        return centerCoorNum;
    }

    public void setCenterCoorNum(Integer centerCoorNum) {
        this.centerCoorNum = centerCoorNum;
    }

    @Transient
    public Integer getPositionCoorNum() {
        return positionCoorNum;
    }

    public void setPositionCoorNum(Integer positionCoorNum) {
        this.positionCoorNum = positionCoorNum;
    }

    @Transient
    public Integer getDesignElevationNum() {
        return designElevationNum;
    }

    public void setDesignElevationNum(Integer designElevationNum) {
        this.designElevationNum = designElevationNum;
    }

    @Transient
    public Integer getRemarkNum() {
        return remarkNum;
    }

    public void setRemarkNum(Integer remarkNum) {
        this.remarkNum = remarkNum;
    }

    @Transient
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getCommonId() {
        return commonId;
    }

    public void setCommonId(Long commonId) {
        this.commonId = commonId;
    }
}
