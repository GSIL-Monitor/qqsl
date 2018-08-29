package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 属性组，读取建筑物相关的xml文件
 */
public class AttribeGroup implements Serializable{

    private static final long serialVersionUID = -8550562251497482127L;
    /**属性组名称*/
    private String name;
    /**属性组别名*/
    private String alias;
    /**所包含的属性*/
    private List<Attribe> attribes;
    /**属性组父级*/
    private AttribeGroup parent;
    /**属性组子级*/
    private List<AttribeGroup> childs;

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

    public List<Attribe> getAttribes() {
        return attribes;
    }

    public void setAttribes(List<Attribe> attribes) {
        this.attribes = attribes;
    }

    public AttribeGroup getParent() {
        return parent;
    }

    public void setParent(AttribeGroup parent) {
        this.parent = parent;
    }

    public List<AttribeGroup> getChilds() {
        return childs;
    }

    public void setChilds(List<AttribeGroup> childs) {
        this.childs = childs;
    }

}
