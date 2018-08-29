package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;


/**
 * 项目实体类
 *
 * @author leinuo
 * @date 2016年1月12日
 */
@Entity
@Table(name = "project")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "project_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Project extends BaseEntity {
    private static final long serialVersionUID = 4471107521461776780L;
    /**
     * 阿里云路径
     */
    private String treePath;
    /**
     * 项目编号
     */
    private String code;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目类型
     */
    private Type type;
    /**
     * 建设区域
     */
    private String buildArea;
    /**
     * 用户
     */
    private User user;
    /**
     * 开工时间
     */
    private Date startDate;
    /**
     * 竣工时间
     */
    private Date endDate;
    /**
     * 信息字符串
     */
    private String infoStr;
    /**
     * 规划
     */
    private Long planning;
    /**
     * 项目相关的要素
     */
    private List<ElementDB> elementDBs = new ArrayList<ElementDB>();
    /**
     * 坐标对象,当长度为1表示线或面坐标，不唯一表示点坐标
     */
    private List<Coordinate> coordinates = new ArrayList<Coordinate>();
    /**
     * 占用空间
     */
    private long curSpaceNum;
    /**
     * 建筑物
     */
    private List<Build> builds = new ArrayList<>();
    ///////////////////////////
    /**
     * 企业间查看
     */
    private String shares;
    /**
     * 子账号间协同
     */
    private String cooperate;
    /**
     * 子账号查看企业
     */
    private String views;

    /**
     * 项目图标类型
     */
    private IconType iconType;

    /**
     * 外业
     */
    private List<FieldWork> fieldWorks = new ArrayList<>();
    /**
     * 项目类型
     */
    public enum Type {
        /**
         * 人畜饮水工程
         */
        DRINGING_WATER,
        /**
         * 灌溉工程
         */
        AGRICULTURAL_IRRIGATION,
        /**
         * 防洪减灾工程
         */
        FLOOD_DEFENCES,
        /**
         * 水土保持工程
         */
        CONSERVATION,
        /**
         * 农村小水电工程
         */
        HYDROPOWER_ENGINEERING,
        /**
         * 供水保障工程
         */
        WATER_SUPPLY;

        public static Type valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }

    /**
     * 项目图标类型
     */
    public enum IconType {
        STYLE_0,
        STYLE_1,
        STYLE_2,
        STYLE_3,
        STYLE_4,
        STYLE_5
    }

    public Project() {
        this.iconType = IconType.STYLE_0;
    }

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getBuildArea() {
        return buildArea;
    }

    public void setBuildArea(String buildArea) {
        this.buildArea = buildArea;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @JsonIgnore
    public String getInfoStr() {
        return infoStr;
    }

    public void setInfoStr(String infoStr) {
        this.infoStr = infoStr;
    }

    public Long getPlanning() {
        return planning;
    }

    public void setPlanning(Long planning) {
        this.planning = planning;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "project")
    @JsonIgnore
    public List<ElementDB> getElementDBs() {
        return elementDBs;
    }

    public void setElementDBs(List<ElementDB> elementDBs) {
        this.elementDBs = elementDBs;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "project")
    @JsonIgnore
    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "project")
    @JsonIgnore
    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }

    ////////////////
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getCooperate() {
        return cooperate;
    }

    public void setCooperate(String cooperate) {
        this.cooperate = cooperate;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public long getCurSpaceNum() {
        return curSpaceNum;
    }

    public void setCurSpaceNum(long curSpaceNum) {
        this.curSpaceNum = curSpaceNum;
    }

    public IconType getIconType() {
        return iconType;
    }

    public void setIconType(IconType iconType) {
        this.iconType = iconType;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "project")
    @JsonIgnore
    public List<FieldWork> getFields() {
        return fieldWorks;
    }

    public void setFields(List<FieldWork> fieldWorks) {
        this.fieldWorks = fieldWorks;
    }
}
