package com.hysw.qqsl.cloud.pay.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.core.entity.data.BaseEntity;
import com.hysw.qqsl.cloud.core.entity.data.User;

import javax.persistence.*;
import java.util.Date;

/**
 * 订单实体类
 */
@Entity
@Table(name="trade")
@SequenceGenerator(name="sequenceGenerator", sequenceName="trade_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Trade extends BaseEntity{
    /** 订单号 */
    private String outTradeNo;
    /** 支付日期 */
    private Date payDate;
    /** 支付类型（微信、支付宝） */
    private PayType payType;
    /** 服务类型 */
    private Type type;
    /** 购买类型（首次，续费）*/
    private BuyType buyType;
    /** 订单状态 */
    private Status status;
    /** 订单价格*/
    private double price;
    /** 服务唯一编码 */
    private String instanceId;
    /** 三类商品type整合 */
    private BaseType baseType;
//    /** 有效时间 */
//    private int validTime;
    /** 备注 */
    private String remark;
    /** Goods下载地址 */
    private String downloadUrl;
    /** Goods个数 */
    private int goodsNum;
    /** 退款日期 */
    private Date refundDate;


    // 删除状态
    private boolean deleteStatus;

    private User user;

    public Trade() {
        this.deleteStatus = false;
    }

    public enum BaseType{
        TEST("试用版"),
//        EXPERIENCE,
        YOUTH("青春版"),
        SUN("朝阳版"),
        SUNRISE("旭日版"),
        /** 水文站 */
        HYDROLOGIC_STATION("水文站"),
        /** 雨量站 */
        RAINFALL_STATION("雨量站"),
        /** 水位站 */
        WATER_LEVEL_STATION("水位站"),
        /** 水质站 */
        WATER_QUALITY_STATION("水质站"),
        PANORAMA("全景效果图"),
        AIRSURVEY("航测3D建模");
        //必须增加一个构造函数,变量,得到该变量的值
        private String  typeC;

        BaseType(String typeC) {
            this.typeC = typeC;
        }

        public String getTypeC() {
            return typeC;
        }
    }

    public enum PayType{
        /** 微信 */
        WX,
        /** 支付宝 */
        ALI
    }

    public enum BuyType{
        /** 购买 */
        BUY("购买"),
        /** 续费 */
        RENEW("续费"),
        /** 升级 */
        UPGRADE("升级");
        //必须增加一个构造函数,变量,得到该变量的值
        private String  typeC;

        BuyType(String typeC) {
            this.typeC = typeC;
        }

        public String getTypeC() {
            return typeC;
        }
    }

    public enum Status{
        /** 未支付 */
        NOPAY,
        /** 已支付 */
        PAY,
        /** 已关闭 */
        CLOSE,
        /** 已撤销 */
        REVERSE,
        /** 已退款 */
        REFUND,
        /** 已失效*/
        EXPIRE
    }

    public enum Type{
        /** 套餐 */
        PACKAGE("套餐服务"),
        /** 测站 */
        STATION("测站服务"),
        /** 数据服务 */
        GOODS("数据服务");
        //必须增加一个构造函数,变量,得到该变量的值
        private String  typeC;

        Type(String typeC) {
            this.typeC = typeC;
        }

        public String getTypeC() {
            return typeC;
        }
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public PayType getPayType() {
        return payType;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BuyType getBuyType() {
        return buyType;
    }

    public void setBuyType(BuyType buyType) {
        this.buyType = buyType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public BaseType getBaseType() {
        return baseType;
    }

    public void setBaseType(BaseType baseType) {
        this.baseType = baseType;
    }

    @ManyToOne(fetch=FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

//    public int getValidTime() {
//        return validTime;
//    }
//
//    public void setValidTime(int validTime) {
//        this.validTime = validTime;
//    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(int goodsNum) {
        this.goodsNum = goodsNum;
    }

    public Date getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(Date refundDate) {
        this.refundDate = refundDate;
    }

    public boolean isDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }
}
