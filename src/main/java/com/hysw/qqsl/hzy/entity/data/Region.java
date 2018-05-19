package com.hysw.qqsl.hzy.entity.data;

import javax.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 行政区

 * 包含四级，市、县、乡、村
 *
 * @since 2018年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class Region extends BaseEntity {

    // 名称
    private String name;
    // 级别
    private RiginLevel level;
    // 描述
    private String remark;
    // 父级
    private Region parent;
    // 子级
    private List<Region> childs;
    // 行政区下的河长
    private List<HzUser> hzUsers;
    // 行政区下的河长办
    private HzbUser hzbUser;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RiginLevel getLevel() {
        return level;
    }

    public void setLevel(RiginLevel level) {
        this.level = level;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public Region getParent() {
        return parent;
    }

    public void setParent(Region parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy="region", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<Region> getChilds() {
        return childs;
    }

    public void setChilds(List<Region> childs) {
        this.childs = childs;
    }

    @OneToMany(mappedBy="region", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<HzUser> getHzUsers() {
        return hzUsers;
    }

    public void setHzUsers(List<HzUser> hzUsers) {
        this.hzUsers = hzUsers;
    }

    @OneToOne
    @JoinColumn(name = "id")
    public HzbUser getHzbUser() {
        return hzbUser;
    }

    public void setHzbUser(HzbUser hzbUser) {
        this.hzbUser = hzbUser;
    }
}
