package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * 要素实体类
 *
 * @author leinuo
 * @date 2016年1月12日
 */
@Entity
@Table(name = "elementDB")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "elementDB_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class ElementDB extends BaseEntity {

    private static final long serialVersionUID = -2838140853150926698L;
    /**
     * 要素值
     */
    private String value;
    /**
     * 别名
     */
    private String alias;
    /**
     * 要素数据合计字段，用于要素输出 多个要素数据合计为一句话
     */
    private String elementDataStr;
    /**
     * 项目
     */
    private Project project;
    /**
     * 要素数据列表
     */
    private List<ElementDataGroup> elementDataGroups = new ArrayList<ElementDataGroup>();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Transient
    public String getElementDataStr() {
        return elementDataStr;
    }

    public void setElementDataStr(String elementDataStr) {
        this.elementDataStr = elementDataStr;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }


    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "elementDB")
    @JsonIgnore
    public List<ElementDataGroup> getElementDataGroups() {
        return elementDataGroups;
    }

    public void setElementDataGroups(List<ElementDataGroup> elementDataGroups) {
        this.elementDataGroups = elementDataGroups;
    }
}
