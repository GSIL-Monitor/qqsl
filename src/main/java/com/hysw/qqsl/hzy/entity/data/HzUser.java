package com.hysw.qqsl.hzy.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.hzy.CommonEnum.HzType;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

/**
 * 河长用户
 * 河长云主要用户，职责是巡河，生成巡河记录，处理事件，报告事件
 *
 * @since 2018年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
@Entity(name = "hzy.HzUser")
@Table(name="hzy_hz_user")
@SequenceGenerator(name="sequenceGenerator", sequenceName="hzy_hz_user_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class HzUser extends BaseEntity {

    // 姓名
    private String name;
    // 联系电话
    private String phone;
    // 描述
    private String remark;
    // 类型
    private HzType type;
    // 行政区
    private Region region;
    // 管理的河道
    private List<RiverSegment> riverSegments;

    /** 密码 */
    private String password;
    /** 邮箱 */
    private String email;
    /** 是否锁定 */
    private Boolean isLocked;
    /** 锁定日期 */
    private Date lockedDate;
    /** 最后登录日期 */
    private Date loginDate;
    /** 最后登录IP */
    private String loginIp;
    /** 连续登录失败次数 */
    private Integer loginFailureCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public HzType getType() {
        return type;
    }

    public void setType(HzType type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    @OneToMany(mappedBy="hzUser", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<RiverSegment> getRiverSegments() {
        return riverSegments;
    }

    public void setRiverSegments(List<RiverSegment> riverSegments) {
        this.riverSegments = riverSegments;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public Date getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(Date lockedDate) {
        this.lockedDate = lockedDate;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Integer getLoginFailureCount() {
        return loginFailureCount;
    }

    public void setLoginFailureCount(Integer loginFailureCount) {
        this.loginFailureCount = loginFailureCount;
    }
}
