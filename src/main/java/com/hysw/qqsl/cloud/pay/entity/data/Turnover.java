package com.hysw.qqsl.cloud.pay.entity.data;

import com.hysw.qqsl.cloud.core.entity.data.BaseEntity;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="turnover")
@SequenceGenerator(name="sequenceGenerator", sequenceName="turnover_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Turnover extends BaseEntity{
    /** 流水号 */
    private String turboverNo;
    /** 订单号 */
    private String outTradeNo;
    /** 支付状态 */
    private Type type;
    /** 价格 */
    private double price;
    /** 余额 */
    private double balance;

    public Turnover() {
        this.balance = 0;
    }

    public Turnover(String turboverNo, String outTradeNo, Type type, double price, double balance) {
        this.turboverNo = turboverNo;
        this.outTradeNo = outTradeNo;
        this.type = type;
        this.price = price;
        if (type == Type.PAY) {
            this.balance = balance+price;
        }else{
            this.balance = balance-price;
        }
    }

    public enum Type{
        /** 支付 */
        PAY,
        /** 退款 */
        REFUND;
    }

    public String getTurboverNo() {
        return turboverNo;
    }

    public void setTurboverNo(String turboverNo) {
        this.turboverNo = turboverNo;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
