package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/13
 */
@Entity
@Table(name="buildAttribute")
@SequenceGenerator(name="sequenceGenerator", sequenceName="buildAttribute_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class BuildAttribute extends BaseEntity {
    /**属性名称*/
    private String name;
    /**属性别名*/
    private String alias;
    /**属性值的选项*/
    private String select;
    /**属性值的选项*/
    private List<String> selects;
    /** 属性值类别 */
    private Type type;
    /** 属性值 */
    private String value;
    /** 属性单位 */
    private String unit;
    /**所属建筑物*/
    private Build build;
    /** 单元格解除锁定 */
    private Boolean locked = false;
    /** excel计算公式 */
    private String fx;
    /** 模拟公式 */
    private String formula;
    /** excel行数 */
    private int row;
    private String fieldName;
    /**属性值类型*/
    public enum Type{
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
    }

    public BuildAttribute() {
    }

    @Transient
    public String getName() {
        return name;
    }

    public void setName(String neme) {
        this.name = neme;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Transient
    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        if(select!=null&&select.trim()!="") {
            if (select.indexOf(",") != -1) {
                this.setSelects(Arrays.asList(select.split(",")));
            } else {
                List<String> strings = new ArrayList<>();
                strings.add(select);
                this.setSelects(strings);
            }
        }
        this.select = select;
    }

    @Transient
    public List<String> getSelects() {
        return selects;
    }

    public void setSelects(List<String> selects) {
        this.selects = selects;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    @Transient
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Transient
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Transient
    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @Transient
    public String getFx() {
        return fx;
    }

    public void setFx(String fx) {
        this.fx = fx;
    }

    @Transient
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Transient
    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    @Transient
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
