package com.hysw.qqsl.cloud.core.shiro;

import com.hysw.qqsl.cloud.core.entity.data.Admin;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Create by leinuo on 17-5-11 下午3:22
 *
 * qq:1321404703 https://github.com/leinuo2016
 * shiro 管理员realm
 */
@Component("adminRealm")
public class AdminRealm extends AuthorizingRealm {
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AuthentService authentService;
    @Autowired
    private SessionDAO sessionDAO;

    public AdminRealm() {
        // 设置token为我们自定义的
        setAuthenticationTokenClass(ShiroToken.class);
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof ShiroToken;
    }

    // 认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {
        ShiroToken shiroToken = (ShiroToken) token;
        if (shiroToken.getType().equals(Admin.class.getName())==false) {
            return null;
        }
        String userName = shiroToken.getUserName();
        SimpleAuthenticationInfo simpleAuthenticationInfo;
        Collection<Session> sessions = sessionDAO.getActiveSessions();
        logger.info(sessions.size());
        Admin loginAdmin = (Admin)shiroToken.getObj();
        Admin admin;
        for (Session session : sessions) {
            admin = authentService.getAdminFromSession(session);
            if (admin == null) {
                continue;
            }
            if (userName.equals(admin.getUserName())) {
                session.stop();// 注销session，即将其踢出系统
                logger.info("踢出管理员为：" + userName + "，ip地址为：" + session.getHost());
            }
        }
        simpleAuthenticationInfo = new SimpleAuthenticationInfo(shiroToken,
                loginAdmin.getPassword(), this.getName());
        return simpleAuthenticationInfo;
    }

    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
        // 根据当前访问对象查出角色及权限
        Object subject = principals.getPrimaryPrincipal();
        List<String> permissions = new ArrayList<>();
        List<String> roles = new ArrayList<>();
        // 根据身份信息获取权限信息
        // 从数据库获取到权限数据
        ShiroToken token = (ShiroToken) subject;
        if (token.getType().equals(Admin.class.getName())==false) {
            return null;
        }
        Admin shiroAdmin = (Admin) token.getObj();
        String role = shiroAdmin.getRoles();
        if (role.indexOf(",") != -1) {
            roles = Arrays.asList(role.split(","));
        } else {
            roles.add(role);
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        //用户所有拥有的角色集合
        simpleAuthorizationInfo.addRoles(roles);
        //角色对应的权限集合
        simpleAuthorizationInfo.addStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }


}
