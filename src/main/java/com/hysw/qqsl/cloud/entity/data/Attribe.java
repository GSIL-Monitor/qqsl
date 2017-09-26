package com.hysw.qqsl.cloud.entity.data;

import com.hysw.qqsl.cloud.entity.build.Config;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 建筑物的属性，包括材质属性、水利属性、结构尺寸属性、控制属性
 */
@Entity
@Table(name="attribe")
@SequenceGenerator(name="sequenceGenerator", sequenceName="attribe_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Attribe extends BaseEntity{

    private static final long serialVersionUID = -1469933766244270561L;
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
    /** 动态组序号 */
    private int code;
    /** 属性单位 */
    private String unit;
    /**属性类别 */
    private Config.Status status;
    /**所属建筑物*/
    private Build build;
    /**属性类型(status)*/
    private String genre;
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

    public Attribe() {
        this.code = 0;
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Transient
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Transient
    public Config.Status getStatus() {
        return status;
    }

    public void setStatus(Config.Status status) {
        this.status = status;
    }

    @Transient
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        if(genre!=null){
            if(genre.equals("dynamic")){
                this.setStatus(Config.Status.DYNAMIC);
            }else if(genre.equals("normal")){
                this.setStatus(Config.Status.NORMAL);
            }else if(genre.equals("hide")){
                this.setStatus(Config.Status.HIDE);
            }else{
                this.setStatus(Config.Status.NORMAL);
            }
        }else{
            this.setStatus(Config.Status.NORMAL);
        }
        this.genre = genre;
    }
}
