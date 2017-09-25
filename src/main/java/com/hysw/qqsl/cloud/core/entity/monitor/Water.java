package com.hysw.qqsl.cloud.core.entity.monitor;

import java.io.Serializable;
import java.util.Date;

/**
 * 水位
 *
 * @since 2016年9月1日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class Water implements Serializable{

    // 最小值
    private float minValue;
    // 最大值
    private float maxValue;
    // 采集时间
    private Date date;

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
