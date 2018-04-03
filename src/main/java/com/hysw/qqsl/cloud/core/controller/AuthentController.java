package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Admin;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @return message消息体,NO_AUTHORIZE:没有访问权限
     */
    @RequestMapping(value = "/refuse", method = {RequestMethod.GET, RequestMethod.POST})
    public
    @ResponseBody
    Message refuse() {
        return MessageService.message(Message.Type.UNAUTHORIZED);
    }

    /**
     * session失效
     * @return message消息体,NO_SESSION:session过期或失效
     */
    @RequestMapping(value = "/session", method = {RequestMethod.GET, RequestMethod.POST})
    public
    @ResponseBody
    Message checkSession() {
        return MessageService.message(Message.Type.NO_SESSION);
    }


    /**
     * 对当前访问对象构建相应的角色,权限信息
     * @return JSONObject对象,包含角色role以及授权信息
     */
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
     * @return message消息体,OK:注销成功
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public
    @ResponseBody
    Message logout() {
        if (SecurityUtils.getSubject().getSession() == null) {
            return MessageService.message(Message.Type.OK);
        }
        SecurityUtils.getSubject().logout();
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 移动端获取版本号
     * @return message消息体,OK:获取成功,包含移动端版本号
     */
    @RequestMapping(value = "/mobile/version", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getVersion() {
        JSONObject version = mobileInfoService.findVersion();
        return MessageService.message(Message.Type.OK,version);
    }

    /**
     * 更新移动端版本号
     * @param map map包含要提交的移动端版本号
     * @return message消息体,FIAL:更新失败,OK:更新成功
     */
    @RequestMapping(value = "/mobile11/version", method = RequestMethod.POST)
    public
    @ResponseBody
    Message updateVersion(@RequestBody Map<String, Object> map) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Long version = Long.valueOf(map.get("version").toString());
        mobileInfoService.update(version);
        return MessageService.message(Message.Type.OK);
    }


}


