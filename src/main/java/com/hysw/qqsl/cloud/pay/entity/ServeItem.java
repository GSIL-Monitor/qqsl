package com.hysw.qqsl.cloud.pay.entity;

import java.io.Serializable;

/**
 * 服务项
 */
public class ServeItem implements Serializable{
    /** 服务名 */
    private String name;
    /** 服务类型 */
    private Type type;
    /** 描述 */
    private String description;

    public enum Type{
        /** 空间 */
        SPACE,
        /** 子账户 */
        ACCOUNT,
        /** 项目 */
        PROJECT,
        /** 全景 */
        PANO,
        /** BIM 服务 */
        BIMSERVE,
        /** 千寻--亚米 */
        FINDYAMI,
        /** 千寻--厘米 */
        FINDCM,
        /** 千寻--毫米 */
        FINDMM;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
