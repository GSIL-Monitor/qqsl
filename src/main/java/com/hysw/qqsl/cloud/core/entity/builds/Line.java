package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.CommonEnum;

import java.io.Serializable;

/**
 * @author Administrator
 * @since 2018/9/19
 */
public class Line implements Serializable {
    private String name;
    private CommonEnum.CommonType commonType;
    private String cellProperty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CommonEnum.CommonType getCommonType() {
        return commonType;
    }

    public void setCommonType(CommonEnum.CommonType commonType) {
        this.commonType = commonType;
    }

    public String getCellProperty() {
        return cellProperty;
    }

    public void setCellProperty(String cellProperty) {
        this.cellProperty = cellProperty;
    }
}
