package com.hysw.qqsl.cloud.core.entity.build;

import com.hysw.qqsl.cloud.core.entity.data.Build;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 建筑物组实体类
 */
public class BuildGroup implements Serializable{

    private static final long serialVersionUID = 8172972686724467987L;
    /** 建筑物组名称*/
    private String name;
    /** 建筑物组别名*/
    private String alias;
    /** 建筑物组包含的建筑物*/
    private List<Build> builds;


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

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }

}
