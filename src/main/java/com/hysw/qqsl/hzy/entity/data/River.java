package com.hysw.qqsl.hzy.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.hzy.CommonEnum;

import javax.persistence.*;

/**
 * 河流

 * 这是河长云核心对象，所有问题都是围绕它展开，包含流域面积(km²），河(沟)道长度km），河（沟）道坡度％），
 * 多年平均流量（m³/s），径流量（万m³）等信息，
 * 最重要的是包含河流的线坐标，用于显示河道起始段信息
 *
 * @since 2018年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
@Entity(name = "hzy.River")
@Table(name="hzy_river")
@SequenceGenerator(name="sequenceGenerator", sequenceName="hzy_river_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class River extends BaseEntity {

    // 流域面积(km²）
    private float area;
    // 河(沟)道长度km）
    private float length;
    // 河(沟)道坡度％
    private float slope;
    // 多年平均时间流量(m³/s)
    private float averTimeFlow;
    // 多年平均径流量(万m³)
    private float averFlow;
    // 上级河流
    private String superRiver;
    // 河流级别
    private CommonEnum.RiverLevel level;
    // 河道左岸线坐标
    private String leftCoor;
    // 河道右岸坐标
    private String rightCoor;

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getSlope() {
        return slope;
    }

    public void setSlope(float slope) {
        this.slope = slope;
    }

    public float getAverTimeFlow() {
        return averTimeFlow;
    }

    public void setAverTimeFlow(float averTimeFlow) {
        this.averTimeFlow = averTimeFlow;
    }

    public float getAverFlow() {
        return averFlow;
    }

    public void setAverFlow(float averFlow) {
        this.averFlow = averFlow;
    }

    public String getSuperRiver() {
        return superRiver;
    }

    public void setSuperRiver(String superRiver) {
        this.superRiver = superRiver;
    }

    public CommonEnum.RiverLevel getLevel() {
        return level;
    }

    public void setLevel(CommonEnum.RiverLevel level) {
        this.level = level;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getLeftCoor() {
        return leftCoor;
    }

    public void setLeftCoor(String leftCoor) {
        this.leftCoor = leftCoor;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getRightCoor() {
        return rightCoor;
    }

    public void setRightCoor(String rightCoor) {
        this.rightCoor = rightCoor;
    }
}
