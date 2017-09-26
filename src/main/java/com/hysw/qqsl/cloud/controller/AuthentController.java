package com.hysw.qqsl.cloud.controller;

import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.Admin;
import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.service.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Create by leinuo on 17-5-11 下午12:54
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 * <p>
 * 权限控制层
 */
@Controller
@RequestMapping("/authent")
public class AuthentController {
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private MobileInfoService mobileInfoService;


    /**
     * 拒绝访问
     *
     * @return
     */
    @RequestMapping(value = "/refuse", method = RequestMethod.GET)
    public
    @ResponseBody
    Message refuse() {
        return new Message(Message.Type.NO_AUTHORIZE);
    }

    /**
     * session失效
     *
     * @return
     */
    @RequestMapping(value = "/monitor", method = RequestMethod.GET)
    public
    @ResponseBody
    Message checkSession() {
        return new Message(Message.Type.NO_SESSION);
    }


    @RequiresAuthentication
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Object getAuthenticate() {
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        Admin admin = authentService.getAdminFromSubject();
        if (user == null && account == null && admin == null) {
            return null;
        }
        if (user != null) {
            return userService.getAuthenticate(user);
        }
        if (account != null) {
            return accountService.getAuthenticate(account);
        }
        if (admin != null) {
            return adminService.getAuthenticate(admin);
        }
        return null;
    }

    /**
     * 用户注销
     *
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public
    @ResponseBody
    Message logout() {
        if (SecurityUtils.getSubject().getSession() == null) {
            return new Message(Message.Type.OK);
        }
        SecurityUtils.getSubject().logout();
        return new Message(Message.Type.OK);
    }

    /**
     * 移动端获取版本号
     * @return
     */
    @RequestMapping(value = "/mobile/version", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getVersion() {
        return mobileInfoService.findVersion();
    }

    /**
     * 更新移动端版本号
     * @param map
     * @return
     */
    @RequestMapping(value = "/mobile/version", method = RequestMethod.POST)
    public
    @ResponseBody
    Message updateVersion(@RequestBody Map<String, Object> map) {
        Message message = Message.parameterCheck(map);
        if (!message.getType().equals(Message.Type.OK)) {
            return message;
        }
        Long version = Long.valueOf(map.get("version").toString());
        return mobileInfoService.update(version);
    }


}


