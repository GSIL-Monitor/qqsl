package com.hysw.qqsl.cloud.entity.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 水位监控
 *
 * @since 2016年9月1日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class WaterMonitor implements Serializable {

    // 在线状态
    private boolean exist;
    // 设备编号
    private String termSN;
    // 设备电压
    private float voltage;
    // 实时水位
    private List<Water> curWaters = new ArrayList<>();
    // 历史水位
//    private List<Water> hisWaters = new ArrayList<>();

    public WaterMonitor(String termSN){
        this.termSN = termSN;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public List<Water> getCurWaters() {
        return curWaters;
    }

    public void setCurWaters(List<Water> curWaters) {
        this.curWaters = curWaters;
    }

//    public List<Water> getHisWaters() {
//        return hisWaters;
//    }

//    public void setHisWaters(List<Water> hisWaters) {
//        this.hisWaters = hisWaters;
//    }
    public String getTermSN() {
        return termSN;
    }

    public void setTermSN(String termSN) {
        this.termSN = termSN;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }
}
