package com.hysw.qqsl.cloud.core.entity.buildModel;

import com.hysw.qqsl.cloud.core.entity.data.Shape;
import com.hysw.qqsl.cloud.core.entity.data.ShapeCoordinate;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/20
 */
public class Elevation implements Serializable {
    private String ele;
    private String alias;
    private boolean errorMsg=false;
    private int errorCellNum;

    public Elevation(List<String> list, String cellProperty, int i, Shape shape, ShapeCoordinate shapeCoordinate) {
        try {
            this.ele = list.get(i);
            if (Float.valueOf(this.ele) < 0) {
                shape.setErrorMsg(true);
                errorMsg = true;
                shapeCoordinate.setErrorMsg(true);
                return;
            }
        } catch (Exception e) {
            shape.setErrorMsg(true);
            shapeCoordinate.setErrorMsg(true);
            this.ele = null;
        }
        String[] split = cellProperty.split(",");
        this.alias = split[i-1].split(":")[1];
    }

    public Elevation(String ele, String alias) {
        this.ele = ele;
        this.alias = alias;
    }

    public String getEle() {
        return ele;
    }

    public void setEle(String ele) {
        this.ele = ele;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(boolean errorMsg) {
        this.errorMsg = errorMsg;
    }
}
