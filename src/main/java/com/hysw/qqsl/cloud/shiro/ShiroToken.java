package com.hysw.qqsl.cloud.shiro;

import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.Admin;
import com.hysw.qqsl.cloud.entity.data.User;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * 登录令牌
 *
 * @since 2017年5月8日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class ShiroToken implements AuthenticationToken {

    private static final long serialVersionUID = 8458812223795221911L;
    // 真实姓名
    private String name;
    // 用户名
    private String userName;
    // 手机号
    private String phone;
    // 邮箱
    private String email;
    // 密码
    private String password;
    // 令牌类型
    private String type;
    // 登录类型
    private String loginType;
    // 登录实体
    private Object obj;

    public ShiroToken() {
        this.loginType = "web";
    }

    // 设置管理员登录令牌
    public void setAdmin(Admin admin) {
        this.name = admin.getName();
        this.userName = admin.getUserName();
        this.phone = admin.getPhone();
        this.email = admin.getEmail();
        this.password = admin.getPassword();
        this.type = Admin.class.getName();
        this.obj = admin;
    }

    // 设置企业用户登令牌
    public void setUser(User user) {
        this.name = user.getName();
        this.userName = user.getUserName();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.type = User.class.getName();
        this.obj = user;
    }

    // 设置子账号登录令牌
    public void setAccount(Account account) {
        this.name = account.getName();
        this.userName = account.getUserName();
        this.phone = account.getPhone();
        this.email = account.getEmail();
        this.password = account.getPassword();
        this.type = Account.class.getName();
        this.obj = account;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object getPrincipal() {
        return this.name;
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }
}
