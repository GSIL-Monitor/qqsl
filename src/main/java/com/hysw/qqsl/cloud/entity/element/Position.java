package com.hysw.qqsl.cloud.entity.element;

import java.io.Serializable;

/**
 * 千寻位置实体类
 * Created by chenl on 17-4-7.
 */
public class Position implements Serializable{
    private String userName;
    private String passWord;
    /** 心跳 */
    private long date;
    /** 过期时间*/
    private long timeout;

    private String mac;

    public Position(String userName, String passWord, long date, long timeout) {
        this.userName = userName;
        this.passWord = passWord;
        this.date = date;
        this.timeout = timeout;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
