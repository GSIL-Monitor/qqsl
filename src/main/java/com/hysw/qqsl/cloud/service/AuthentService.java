package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.controller.Message;
import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.Admin;
import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.shiro.ShiroToken;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by leinuo on 17-5-15 上午10:36
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 认证service层
 */
@Service("authentService")
public class AuthentService {

    /**
     * 从访问主体获取user
     * @return
     */
    public User getUserFromSubject(){
        Subject subject = SecurityUtils.getSubject();
        ShiroToken token = (ShiroToken) subject.getSession().getAttribute("token");
        if (token==null) {
            return null;
        }
        if (token.getType().equals(User.class.getName())) {
            return  (User)token.getObj();
        }
        return null;
    }

    /**
     * 从访问主体获取Account
     * @return
     */
    public Account getAccountFromSubject(){
        Subject subject = SecurityUtils.getSubject();
        ShiroToken token = (ShiroToken) subject.getSession().getAttribute("token");
        if (token==null) {
            return null;
        }
        if (token.getType().equals(Account.class.getName())) {
            return  (Account) token.getObj();
        }
        return null;
    }

    /**
     * 从访问主体获取Admin
     * @return
     */
    public Admin getAdminFromSubject(){
        Subject subject = SecurityUtils.getSubject();
        ShiroToken token = (ShiroToken) subject.getSession().getAttribute("token");
        if (token==null) {
            return null;
        }
        if (token.getType().equals(Admin.class.getName())) {
            return  (Admin)token.getObj();
        }
        return null;
    }

    /**
     * 从访问主体获取Admin
     * @return
     */
    public Admin getAdminFromSession(Session session){
        Admin admin = null;
        ShiroToken token = (ShiroToken) session.getAttribute("token");
        if(token==null){
            return null;
        }
        if(token.getType().equals(Admin.class.getName())){
            admin = (Admin) token.getObj();
        }
        return admin;
    }


    public User getUserFromSession(Session session){
        User user = null;
        ShiroToken token = (ShiroToken) session.getAttribute("token");
        if(token==null){
            return null;
        }
        if(token.getType().equals(User.class.getName())){
            user = (User) token.getObj();
        }
        return user;
    }

    public Account getAccountFromSession(Session session){
        Account account = null;
        ShiroToken token = (ShiroToken) session.getAttribute("token");
        if(token==null){
            return null;
        }
        if(token.getType().equals(Account.class.getName())){
            account = (Account) token.getObj();
        }
        return account;
    }

    /**
     * 更新session数据
     * @param object
     */
    public void updateSession(Object object){
        Subject subject = SecurityUtils.getSubject();
        ShiroToken token = (ShiroToken) subject.getSession().getAttribute("token");
        if(object instanceof Account){
            token.setAccount((Account)object);
        }else if(object instanceof User){
            token.setUser((User)object);
        }else if (object instanceof Admin){
            token.setAdmin((Admin) object);
        }else{
            subject.logout();
        }
        subject.getSession().setAttribute("token",token);
    }
}
