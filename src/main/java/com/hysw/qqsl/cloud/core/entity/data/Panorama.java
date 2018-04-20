package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 全景实体类
 *
 * @since 2016年12月13日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name="panorama")
@SequenceGenerator(name="sequenceGenerator", sequenceName="panorama_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Panorama extends  BaseEntity {

    private static final long serialVersionUID = 78201887721458724L;

    /** 名称 */
    private String name;
    /** 简介 */
    private String info;
    /** 唯一编码 */
    private String instanceId;
    /** 缩略图 */
    private String thumbUrl;
    /** 是否共享 */
    private Boolean isShare;
    /** 坐标 */
    private String coor;
    /** 行政区 */
    private String region;
    /** 审核状态 */
    private CommonEnum.Review status;
    /** 审核意见 */
    private String advice;
    /** 审核时间 */
    private Date reviewDate;
    /** 热点信息 */
    private String hotspot;
    /** 起始视角 */
    private String angleOfView;
    /** 场景顺序 */
    private String sceneGroup;
    /** 用户id */
    private Long userId;
    /** 子账户id */
    private Long accountId;
    List<Scene> scenes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2048)
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @NotEmpty
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public Boolean getShare() {
        return isShare;
    }

    public void setShare(Boolean share) {
        isShare = share;
    }

    public String getCoor() {
        return coor;
    }

    public void setCoor(String coor) {
        this.coor = coor;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public CommonEnum.Review getStatus() {
        return status;
    }

    public void setStatus(CommonEnum.Review status) {
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

    @Basic(fetch = FetchType.EAGER)
    @Column(length = 2048)
    public String getSceneGroup() {
        return sceneGroup;
    }

    public void setSceneGroup(String sceneGroup) {
        this.sceneGroup = sceneGroup;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @OneToMany(mappedBy="panorama", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }


}
