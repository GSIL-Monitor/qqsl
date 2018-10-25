package com.hysw.qqsl.cloud.core.entity.buildModel;

import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import com.hysw.qqsl.cloud.core.entity.data.NewBuildAttribute;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 属性组，读取建筑物相关的xml文件
 */
public class NewAttributeGroup implements Serializable{

    private static final long serialVersionUID = -8550562251497482127L;
    /**属性组名称*/
    private String name;
    /**属性组别名*/
    private String alias;
    /**所包含的属性*/
    private List<NewBuildAttribute> newBuildAttributes;
    /**属性组父级*/
    private NewAttributeGroup parent;
    /**属性组子级*/
    private List<NewAttributeGroup> childs;

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

    public List<NewBuildAttribute> getNewBuildAttributes() {
        return newBuildAttributes;
    }

    public void setNewBuildAttributes(List<NewBuildAttribute> newBuildAttributes) {
        this.newBuildAttributes = newBuildAttributes;
    }

    public NewAttributeGroup getParent() {
        return parent;
    }

    public void setParent(NewAttributeGroup parent) {
        this.parent = parent;
    }

    public List<NewAttributeGroup> getChilds() {
        return childs;
    }

    public void setChilds(List<NewAttributeGroup> childs) {
        this.childs = childs;
    }

}