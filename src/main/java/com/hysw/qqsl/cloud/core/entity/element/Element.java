package com.hysw.qqsl.cloud.core.entity.element;

import com.hysw.qqsl.cloud.core.entity.data.ElementDataGroup;
import com.hysw.qqsl.cloud.core.entity.data.Project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Element implements Serializable {

    private static final long serialVersionUID = -8975886967699075893L;
    /**
     * id
     */
    private Long id;
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
     * 复合要素(非数据库对应)
     */
    private ElementGroup elementGroup;
    /**
     * 名称
     */
    private String name;
    /**
     * 类型
     */
    private Type type;
    /**
     * 要素选择值
     */
    private String select;
    /**
     * 要素数据选择值字符串
     */
    private String elementDataSelect;
    /**
     * 要素数据选择值
     */
    private List<String> elementDataSelects;
    /**
     * 要素数据别名
     */
    private String elementDataAlias;
    /** 要素数据类型*/
    private ElementDataGroup.DataType elementDataGroupType;
    /**
     * 描述
     */
    private String description;
    /**
     * 项目信息编号
     */
    private String infoOrder;
    /**
     * 计算方式
     */
    private CountType countType;
    /**
     * 父级
     */
    private Element elementParent;
    /**
     * 简介描述
     */
    private String introduceDescription;
    /**
     * 单位
     */
    private String unit;
    /**
     * 层级
     */
    private int grade;
    /**
     * 是否失效
     */
    private String alive;
    /** 是否为项目简介内容*/
    //private String sign;
    /**
     * 要素数据列表
     */
    private List<ElementDataGroup> elementDataGroups = new ArrayList<ElementDataGroup>();
    private List<String> selects = new ArrayList<String>();
    private List<Element> childs = new ArrayList<>();
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getElementDataStr() {
        return elementDataStr;
    }

    public void setElementDataStr(String elementDataStr) {
        this.elementDataStr = elementDataStr;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ElementGroup getElementGroup() {
        return elementGroup;
    }

    public void setElementGroup(ElementGroup elementGroup) {
        this.elementGroup = elementGroup;
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

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getElementDataSelect() {
        return elementDataSelect;
    }

    public void setElementDataSelect(String elementDataSelect) {
        this.elementDataSelect = elementDataSelect;
    }

    public List<String> getElementDataSelects() {
        return elementDataSelects;
    }

    public void setElementDataSelects(List<String> elementDataSelects) {
        this.elementDataSelects = elementDataSelects;
    }

    public String getElementDataAlias() {
        return elementDataAlias;
    }

    public void setElementDataAlias(String elementDataAlias) {
        this.elementDataAlias = elementDataAlias;
    }
    public ElementDataGroup.DataType getElementDataGroupType() {
        return elementDataGroupType;
    }

    public void setElementDataGroupType(ElementDataGroup.DataType elementDataGroupType) {
        this.elementDataGroupType = elementDataGroupType;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfoOrder() {
        return infoOrder;
    }

    public void setInfoOrder(String infoOrder) {
        this.infoOrder = infoOrder;
    }

    public CountType getCountType() {
        return countType;
    }

    public void setCountType(CountType countType) {
        this.countType = countType;
    }

    public Element getElementParent() {
        return elementParent;
    }

    public void setElementParent(Element elementParent) {
        this.elementParent = elementParent;
    }

    public String getIntroduceDescription() {
        return introduceDescription;
    }

    public void setIntroduceDescription(String introduceDescription) {
        this.introduceDescription = introduceDescription;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getAlive() {
        return alive;
    }

    public void setAlive(String alive) {
        this.alive = alive;
    }

    public List<ElementDataGroup> getElementDataGroups() {
        return elementDataGroups;
    }

    public void setElementDataGroups(List<ElementDataGroup> elementDataGroups) {
        this.elementDataGroups = elementDataGroups;
    }

    public List<String> getSelects() {
        return selects;
    }

    public void setSelects(List<String> selects) {
        this.selects = selects;
    }
    public List<Element> getChilds() {
        return childs;
    }

    public void setChilds(List<Element> childs) {
        this.childs = childs;
    }
    /**
     * 要素类型
     */
    public enum Type {
        /**
         * 文本
         */
        TEXT,
        /**
         * 数值
         */
        NUMBER,
        /**
         * 时间
         */
        DATE,
        /**
         * 地点
         */
        AREA,
        /**
         * 时间段
         */
        DATE_REGION,
        /**
         * 选择
         */
        SELECT,
        /**
         * 手机号码
         */
        TEL,
        /**
         * 邮箱
         */
        EMAIL,
        /**
         * 文本域
         */
        TEXT_AREA,
        /**
         * 链式结构
         */
        LABEL,
        /**
         * 选择+文本
         */
        SELECT_TEXT,
        /**
         * 点坐标
         */
        COORDINATE,
        /**
         * 坐标上传类型
         */
        COORDINATE_UPLOAD,
        /**
         * 文件上传类型
         */
        FILE_UPLOAD,
        /**
         * 流域
         */
        MAP,
        /**
         * 多选
         */
        CHECKBOX,
        DAQ,
        SECTION_UPLOAD,
        /**
         * 勘测
         */
        FIELD,
        /**
         * 工程布置
         */
        DESIGN,
    }

    /**
     * 计算方式
     */
    public enum CountType {
        /**
         * 求和
         */
        add,
        /**
         * 求平均值
         */
        average
    }

}
