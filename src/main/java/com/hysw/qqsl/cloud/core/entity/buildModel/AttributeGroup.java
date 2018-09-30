package com.hysw.qqsl.cloud.core.entity.buildModel;

import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 属性组，读取建筑物相关的xml文件
 */
public class AttributeGroup implements Serializable{

    private static final long serialVersionUID = -8550562251497482127L;
    /**属性组名称*/
    private String name;
    /**属性组别名*/
    private String alias;
    /**所包含的属性*/
    private List<BuildAttribute> buildAttributes;
    /**属性组父级*/
    private AttributeGroup parent;
    /**属性组子级*/
    private List<AttributeGroup> childs;

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

    public List<BuildAttribute> getBuildAttributes() {
        return buildAttributes;
    }

    public void setBuildAttributes(List<BuildAttribute> buildAttributes) {
        this.buildAttributes = buildAttributes;
    }

    public AttributeGroup getParent() {
        return parent;
    }

    public void setParent(AttributeGroup parent) {
        this.parent = parent;
    }

    public List<AttributeGroup> getChilds() {
        return childs;
    }

    public void setChilds(List<AttributeGroup> childs) {
        this.childs = childs;
    }

}
