package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

/**
 * 管理员实体类
 *
 * @since 2016年12月13日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name="admin")
@SequenceGenerator(name="sequenceGenerator", sequenceName="admin_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Admin extends BaseEntity {

    private static final long serialVersionUID = 8665275752477990073L;

    /** 真实姓名 */
    private String name;
    /** 登陆名 */
    private String userName;
    /** 密码 */
    private String password;
    /** 联系电话 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 所属单位 */
    private String department;
    /** 拥有角色 */
    private String roles;
    /** 是否启用 */
    private Boolean isEnabled;
    /** 是否锁定 */
    private Boolean isLocked;
    /** 连续登录失败次数 */
    private Integer loginFailureCount;
    /** 锁定日期 */
    private Date lockedDate;
    /** 最后登录日期 */
    private Date loginDate;
    /** 最后登录IP */
    private String loginIp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public Integer getLoginFailureCount() {
        return loginFailureCount;
    }

    public void setLoginFailureCount(Integer loginFailureCount) {
        this.loginFailureCount = loginFailureCount;
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
}
