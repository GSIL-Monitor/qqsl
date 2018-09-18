package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;

import java.io.Serializable;

/**
 * @author Administrator
 * @since 2018/9/17
 */
public class lineSectionPlaneModel implements Serializable {
    private Type type;
    private ShapeAttribute shapeAttribute;

    public enum Type{
        YANGXSDQ(CommonEnum.CommonType.DANGQ,"sectionPlane","仰斜式挡墙","yxsdq"),
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

    public ShapeAttribute getShapeAttribute() {
        return shapeAttribute;
    }

    public void setShapeAttribute(ShapeAttribute shapeAttribute) {
        this.shapeAttribute = shapeAttribute;
    }
}
