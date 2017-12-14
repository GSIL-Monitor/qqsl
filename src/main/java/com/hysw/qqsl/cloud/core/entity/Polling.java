package com.hysw.qqsl.cloud.core.entity;

/**
 * @anthor Administrator
 * @since 10:20 2017/12/14
 */
public class Polling {
    /** 分享轮询状态 */
    private boolean shareStatus;
    /** 消息轮询状态 */
    private boolean messageStatus;
    /** 协同轮询状态 */
    private boolean cooperateStatus;
    /** 测站轮询状态 */
    private boolean stationStatus;

    public Polling() {
        this.shareStatus = false;
        this.messageStatus = false;
        this.cooperateStatus = false;
        this.stationStatus = false;
    }

    public boolean isShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(boolean shareStatus) {
        this.shareStatus = shareStatus;
    }

    public boolean isMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(boolean messageStatus) {
        this.messageStatus = messageStatus;
    }

    public boolean isCooperateStatus() {
        return cooperateStatus;
    }

    public void setCooperateStatus(boolean cooperateStatus) {
        this.cooperateStatus = cooperateStatus;
    }

    public boolean isStationStatus() {
        return stationStatus;
    }

    public void setStationStatus(boolean stationStatus) {
        this.stationStatus = stationStatus;
    }
}
