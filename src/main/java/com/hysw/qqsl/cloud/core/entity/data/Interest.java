package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;
import java.util.Date;

/**
 * 兴趣点实体类
 *
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 * @since 2016年12月12日
 */
@Entity
@Table(name = "interest")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "interest_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Interest extends BaseEntity {

    private static final long serialVersionUID = 4188068036388900100L;

    /**
     * 名称
     */
    private String name;
    /**
     * 类别
     */
    private Type type;
    /**
     * 分类
     */
    private Category category;
    /**
     * 坐标
     */
    private String coordinate;
    /**
     * 行政区
     */
    private String region;
    /**
     * 联系方式
     */
    private String contact;
    /**
     * 描述
     */
    private String content;
    /** 客户评价 */
    private String evaluate;
    /**
     * 特色业务
     */
    private String business;
    /**
     * 等级
     */
    private String level;
    /**
     * 图片
     */
    private String pictures;
    /**
     * 审核状态
     */
    private CommonEnum.Review status;
    /**
     * 审核意见
     */
    private String advice;
    /**
     * 审核时间
     */
    private Date reviewDate;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 类别
     */
    public enum Type {
        // 基础兴趣点
        BASE,
        // 个性兴趣点
        PERSONAL;
        public static Type valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }

    /**
     * 分类
     */
    public enum Category {
        //招投标代理机构
        BIDDINGAGENT,
        // 设计单位
        DESIGN,
        // 监理单位
        SUPERVISE,
        // 施工单位
        EXECUTION,
        // 质检单位
        QUALITY,
        // 运营单位
        OPERATION,
        // 生态水利服务商
        ECOLOGY,
        // 节水灌溉服务商
        IRRIGATION,
        // 地理信息勘测服务商
        INVESTIGATION,
        // 水利信息化服务商
        INFORMATION,
        // 建筑工业化服务商
        INDUSTRIALIZATION,
        // 水位站
        STATION;
        public static Category valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    //@Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "text")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(String evaluate) {
        this.evaluate = evaluate;
    }

    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getPictures() {
        return pictures;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }

    public CommonEnum.Review getStatus() {
        return status;
    }

    public void setStatus(CommonEnum.Review status) {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
