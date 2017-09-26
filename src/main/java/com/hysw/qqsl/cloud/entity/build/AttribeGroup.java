package com.hysw.qqsl.cloud.entity.build;

import com.hysw.qqsl.cloud.entity.data.Attribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leinuo on 17-3-28.
 * 属性组，读取建筑物相关的xml文件
 */
public class AttribeGroup implements Serializable{

    private static final long serialVersionUID = -8550562251497482127L;
    /**属性组名称*/
    private String name;
    /**属性组别名*/
    private String alias;
    /** 动态组序号 */
    private int code;
    /**所包含的属性*/
    private List<Attribe> attribes;
    /**属性组父级*/
    private AttribeGroup parent;
    /**属性组子级*/
    private List<AttribeGroup> childs;
    /**属性组类型*/
    private String genre;
/*    *//**选择值*//*
    private String select;
    *//**选择值*//*
    private List<String> selects;
    *//**属性组类型*/
    private Config.Status status;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Attribe> getAttribes() {
        return attribes;
    }

    public void setAttribes(List<Attribe> attribes) {
        this.attribes = attribes;
    }

    public AttribeGroup getParent() {
        return parent;
    }

    public void setParent(AttribeGroup parent) {
        this.parent = parent;
    }

    public List<AttribeGroup> getChilds() {
        return childs;
    }

    public void setChilds(List<AttribeGroup> childs) {
        this.childs = childs;
    }

    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        if(genre!=null){
            if(genre.equals("select")){
                this.setStatus(Config.Status.SELECT);
            }else if(genre.equals("dynamic")){
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
    public Config.Status getStatus() {
        return status;
    }

    public void setStatus(Config.Status status) {
        this.status = status;
    }

}
