package com.hysw.qqsl.cloud.core.entity.data;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 子账号实体类
 *
 * @since 2017年4月26日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name="account")
@SequenceGenerator(name="sequenceGenerator", sequenceName="account_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Account extends BaseEntity {

    private static final long serialVersionUID = 5684132177393981035L;

    /** 所属user */
    private User user;
    /** 真实姓名 */
    private String name;
    /** 密码 */
    private String password;
    /** 联系电话 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 所属部门 */
    private String department;
    /** 备注 */
    private String remark;
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
    /** 用户角色 */
    private String roles;
    /** 子账号消息 */
    private List<AccountMessage> accountMessages = new ArrayList<>();
    /** 子账号的登录方式 */
    private String loginType;

    @ManyToOne(fetch = FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @NotEmpty
    @Length(max=255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty
    @Length(max=255)
    @JsonIgnore
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

    public String getRoles() {
        return roles;
    }
    public void setRoles(String roles) {
        this.roles = roles;
    }

    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},  fetch=FetchType.LAZY, mappedBy="account")
    @JsonIgnore
    public List<AccountMessage> getAccountMessages() {
        return accountMessages;
    }

    public void setAccountMessages(List<AccountMessage> accountMessages) {
        this.accountMessages = accountMessages;
    }

    @Transient
    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
