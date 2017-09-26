package com.hysw.qqsl.cloud.entity.data;

import java.io.Serializable;

/**
 * Create by leinuo on 17-6-29 上午10:15
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 移动端qqsl版本号实体类
 */
public class Version extends BaseEntity{

    private static final long serialVersionUID = -3452664373259421059L;
    /** 版本号 */
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
