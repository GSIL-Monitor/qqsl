package com.hysw.qqsl.cloud.core.entity.data;

import com.hysw.qqsl.cloud.core.entity.Review;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

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
    /** 坐标 */
    private String coor;
    /** 行政区 */
    private String region;
    /** 审核状态 */
    private Review status;
    /** 审核意见 */
    private String advice;
    /** 审核时间 */
    private Date reviewDate;
    /** 是否共享 */
    private Boolean isShare;
    /** 图片 */
    private String picture;
    /** 用户id */
    private Long userId;
    /** 拍摄时间 */
    private Date shootDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Review getStatus() {
        return status;
    }

    public void setStatus(Review status) {
        this.status = status;
    }

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

    public Boolean getShare() {
        return isShare;
    }

    public void setShare(Boolean share) {
        isShare = share;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getShootDate() {
        return shootDate;
    }

    public void setShootDate(Date shootDate) {
        this.shootDate = shootDate;
    }
}
