package com.hysw.qqsl.cloud.pay.entity.data;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.BaseEntity;
import com.hysw.qqsl.cloud.core.entity.data.User;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.Date;

/**
 * 套餐实体类
 */
@Entity
@Table(name="package")
@SequenceGenerator(name="sequenceGenerator", sequenceName="package_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Package extends BaseEntity{
    /** 套餐类型 */
    private CommonEnum.PackageType type;
    /** 到期时间 */
    private Date expireDate;
    /** 当前总空间使用情况 */
    private long curSpaceNum;
    /** 当前流量使用情况 */
    private long curTrafficNum;
    /** 唯一标识 */
    private String instanceId;
    // 目前项目数
    private long curProjectNum;
    // 目前子账号数
    private long curAccountNum;

    private User user;

    public CommonEnum.PackageType getType() {
        return type;
    }

    public void setType(CommonEnum.PackageType type) {
        this.type = type;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public long getCurSpaceNum() {
        return curSpaceNum;
    }

    public void setCurSpaceNum(long curSpaceNum) {
        this.curSpaceNum = curSpaceNum;
    }

    public long getCurTrafficNum() {
        return curTrafficNum;
    }

    public void setCurTrafficNum(long curTrafficNum) {
        this.curTrafficNum = curTrafficNum;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @ManyToOne(fetch=FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Transient
    public long getCurProjectNum() {
        return curProjectNum;
    }

    public void setCurProjectNum(long curProjectNum) {
        this.curProjectNum = curProjectNum;
    }

    @Transient
    public long getCurAccountNum() {
        return curAccountNum;
    }

    public void setCurAccountNum(long curAccountNum) {
        this.curAccountNum = curAccountNum;
    }
}
