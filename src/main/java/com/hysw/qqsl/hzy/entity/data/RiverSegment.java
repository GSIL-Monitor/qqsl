package com.hysw.qqsl.hzy.entity.data;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * 河段
 *
 * 这是河长云核心对象，河长都是在河段上进行巡河的，
 * 包含河河段的起始坐标，用于确定河段线坐标
 *
 * @since 2018年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class RiverSegment extends BaseEntity {

    // 河段长度
    private float length;
    // 开始坐标
    private String beginCoor;
    // 结束坐标
    private String endCoor;
    // 开始位置
    private String beginStation;
    // 结束位置
    private String endStation;
    // 所属河流id
    private Long riverId;
    // 所属行政区id
    private Long regionId;
    // 责任河长
    private HzUser hzUser;

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public String getBeginCoor() {
        return beginCoor;
    }

    public void setBeginCoor(String beginCoor) {
        this.beginCoor = beginCoor;
    }

    public String getEndCoor() {
        return endCoor;
    }

    public void setEndCoor(String endCoor) {
        this.endCoor = endCoor;
    }

    public String getBeginStation() {
        return beginStation;
    }

    public void setBeginStation(String beginStation) {
        this.beginStation = beginStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public Long getRiverId() {
        return riverId;
    }

    public void setRiverId(Long riverId) {
        this.riverId = riverId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public HzUser getHzUser() {
        return hzUser;
    }

    public void setHzUser(HzUser hzUser) {
        this.hzUser = hzUser;
    }
}
