package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 反馈实体类
 *
 * @since 10.0，2017-12-25
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
@Entity
@Table(name = "feedback")
@SequenceGenerator(name = "sequenceGenerator",sequenceName = "feedback_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Feedback extends BaseEntity {

    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 类型 */
    private Type type;
    /** 回复内容 */
    private String review;
    /** 回复时间 */
    private Date reviewDate;
    /** 反馈状态 */
    private CommonEnum.FeedbackStatus status;
    /** 用户id */
    private long userId;
    /** 子账号id */
    private long accountId;

    /**
     * 反馈类型
     */
    public enum Type {
        /** 功能建议 */
        SUGGEST,
        /** 产品缺陷 */
        DEFECT,
        /** 产品需求 */
        REQUIREMENT
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CommonEnum.FeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(CommonEnum.FeedbackStatus status) {
        this.status = status;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }
}
