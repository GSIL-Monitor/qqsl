package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 属性组，读取建筑物相关的xml文件
 */
public class LineAttributeGroup implements Serializable{

    private static final long serialVersionUID = -8550562251497482127L;
    /**属性组名称*/
    private String name;
    /**属性组别名*/
    private String alias;
    /**所包含的属性*/
    private List<ShapeAttribute> shapeAttributes;
    /**属性组父级*/
    private LineAttributeGroup parent;
    /**属性组子级*/
    private List<LineAttributeGroup> childs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<ShapeAttribute> getShapeAttributes() {
        return shapeAttributes;
    }

    public void setShapeAttributes(List<ShapeAttribute> shapeAttributes) {
        this.shapeAttributes = shapeAttributes;
    }

    public LineAttributeGroup getParent() {
        return parent;
    }

    public void setParent(LineAttributeGroup parent) {
        this.parent = parent;
    }

    public List<LineAttributeGroup> getChilds() {
        return childs;
    }

    public void setChilds(List<LineAttributeGroup> childs) {
        this.childs = childs;
    }

}
