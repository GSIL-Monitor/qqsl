package com.hysw.qqsl.cloud.pay.entity;

import java.io.Serializable;

public class PackageItem implements Serializable{
    /** 服务项目 */
    private ServeItem serveItem;
    /** 限制数 */
    private Long limitNum;

    public ServeItem getServeItem() {
        return serveItem;
    }

    public void setServeItem(ServeItem serveItem) {
        this.serveItem = serveItem;
    }

    public Long getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(Long limitNum) {
        this.limitNum = limitNum;
    }
}
