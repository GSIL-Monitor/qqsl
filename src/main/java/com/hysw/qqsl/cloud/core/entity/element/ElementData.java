package com.hysw.qqsl.cloud.core.entity.element;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/6/21.
 */
public class ElementData implements Serializable {

	private static final long serialVersionUID = 2522954496207886733L;
	/** 名称 */
    private String name;
    /** 值 */
    private String value;
    private String unit;
    private String description;
    /** 类型 */
    private Type type;
    /** 父级要素数据 */
    private ElementData parent;

    public enum Type {
        NUMBER,
        LABEL
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ElementData getParent() {
        return parent;
    }

    public void setParent(ElementData parent) {
        this.parent = parent;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
