package com.hysw.qqsl.cloud.core.entity.buildModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/27
 */
public class ShapeCache implements Serializable {
    private String name;
    private List<List<String>> list = new LinkedList<>();
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<String>> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list.add(list);
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
