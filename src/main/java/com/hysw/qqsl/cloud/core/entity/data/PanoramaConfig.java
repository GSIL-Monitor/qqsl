package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Create by leinuo on 18-3-27 下午5:03
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Entity
@Table(name = "panoramaConfig")
@SequenceGenerator(name = "sequenceGenerator",sequenceName = "panoramaConfig_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class PanoramaConfig extends BaseEntity {

    private static final long serialVersionUID = 4471107661461776780L;

    /**
     * 缩略图
     */
    private String thumbUrl;

    /**
     * 名称
     */
    private String name;

    /**
     * 简介
     */
    private String info;

    /**
     * 唯一编码
     */
    private String instanceId;

    /**
     * 公开私有
     */
    private boolean isShare;

    /**
     * 坐标
     */
    private String Coor;

    /**
     * 行政区
     */
    private String region;

    /**
     * 审核状态
     */
    private String status;

    /**
     * 审核意见
     */
    private String advice;

    /**
     * 审核时间
     */
    private Date reviewDate;

    /**
     *  热点
     */
    private String hotspot;

    /**
     * 起始视角
     */
    private String angleOfView;

    /**
     * 所属场景
     */
    private List<Scene> scenes;

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(fetch = FetchType.EAGER)
   // @Column(columnDefinition = "text")
    @Column(length = 2048)
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isShare() {
        return isShare;
    }

    public void setShare(boolean share) {
        isShare = share;
    }

    public String getCoor() {
        return Coor;
    }

    public void setCoor(String coor) {
        Coor = coor;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2048)
    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getHotspot() {
        return hotspot;
    }

    public void setHotspot(String hotspot) {
        this.hotspot = hotspot;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getAngleOfView() {
        return angleOfView;
    }

    public void setAngleOfView(String angleOfView) {
        this.angleOfView = angleOfView;
    }

    @OneToMany(mappedBy="panoramaConfig", fetch= FetchType.LAZY , cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }
}
