package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/17
 */
public class LineSectionPlaneModel implements Serializable {
    private Type type;
    private List<ShapeAttribute> shapeAttribute;
    private String alias;
    private String name;
    private int number;


    private LineAttributeGroup remark;
    private LineAttributeGroup lineWaterResources;
    private LineAttributeGroup lineControlSize;
    private LineAttributeGroup lineGroundStress;
    private LineAttributeGroup lineComponent;

    public enum Type{
        YANGXSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","仰斜式挡墙","yxsdq"),
        BINGWXSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","宾格网箱式挡墙","bgwxsdq"),
        HENGZSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","衡重式挡墙","hdsdq"),
        FUXSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","俯斜式挡墙","fxsdq"),
        FUBSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","扶臂式挡墙","fbsdq"),
        ANPSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","岸坡式挡墙","apsdq"),
        ;
        //必须增加一个构造函数,变量,得到该变量的值
        private CommonEnum.CommonType commonType;
        private String type = "";
        private String typeC = "";
        private String abbreviate = "";

        Type(CommonEnum.CommonType commonType, String type, String typeC, String abbreviate) {
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
    }

    public List<ShapeAttribute> getShapeAttribute() {
        return shapeAttribute;
    }

    public void setShapeAttribute(List<ShapeAttribute> shapeAttribute) {
        this.shapeAttribute = shapeAttribute;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public LineAttributeGroup getRemark() {
        return remark;
    }

    public void setRemark(LineAttributeGroup remark) {
        this.remark = remark;
    }

    public LineAttributeGroup getLineWaterResources() {
        return lineWaterResources;
    }

    public void setLineWaterResources(LineAttributeGroup lineWaterResources) {
        this.lineWaterResources = lineWaterResources;
    }

    public LineAttributeGroup getLineControlSize() {
        return lineControlSize;
    }

    public void setLineControlSize(LineAttributeGroup lineControlSize) {
        this.lineControlSize = lineControlSize;
    }

    public LineAttributeGroup getLineGroundStress() {
        return lineGroundStress;
    }

    public void setLineGroundStress(LineAttributeGroup lineGroundStress) {
        this.lineGroundStress = lineGroundStress;
    }

    public LineAttributeGroup getLineComponent() {
        return lineComponent;
    }

    public void setLineComponent(LineAttributeGroup lineComponent) {
        this.lineComponent = lineComponent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
