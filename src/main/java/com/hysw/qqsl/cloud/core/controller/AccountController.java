package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.data.UserMessage;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.core.shiro.ShiroToken;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Create by leinuo on 17-4-26 下午6:34
 *
 * qq:1321404703 https://github.com/leinuo2016
 */
@Controller
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private AccountMessageService accountMessageService;

    Log logger = LogFactory.getLog(this.getClass());


    /**
     * 子账号注册
     *
     * @param objectMap
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public
    @ResponseBody
    Message register(@RequestBody Map<String, String> objectMap,
                     HttpSession session) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        Verification verification = (Verification) session
                .getAttribute("verification");
        String name,phone,password,code;
        name = map.get("name").toString();
        password = map.get("password").toString();
        phone = map.get("phone").toString();
        code = map.get("code").toString();
        message = userService.checkCode(code,phone, verification);
        if (message.getType()!=Message.Type.OK) {
            return message;
        }
        try {
            message = accountService.register(name,phone,password);
        } catch (QQSLException e) {
            e.printStackTrace();
            return message;
        }
        return message;
    }
    /**
     * 子账号登录
     * @param map
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public @ResponseBody Message login(@RequestBody Map<String,Object> map){
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        String accountPhone = map.get("phone").toString();
        String password = map.get("password").toString();
        Account account = accountService.findByPhone(accountPhone);
        if(account==null){
            return new Message(Message.Type.EXIST);
        }
        if(!account.getPassword().equals(password)){
            return new Message(Message.Type.FAIL);
        }
        if (map.get("loginType") != null && StringUtils.hasText(map.get("loginType").toString()) && map.get("loginType").equals("phone")) {
            return subjectLogin(account,"phone");
        }
        if(map.get("cookie")==null||!StringUtils.hasText(map.get("cookie").toString())){
            return new Message(Message.Type.OTHER);
        }
        String cookie = map.get("cookie").toString();
        if(!cookie.equals(DigestUtils.md5Hex(account.getPassword()))){
            return new Message(Message.Type.OTHER);
        }else{
            return subjectLogin(account, "web");
        }
    }

    private Message subjectLogin( Account account, String loginType) {
        ShiroToken token = new ShiroToken();
        token.setAccount(account);
        token.setLoginType(loginType);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            return new Message(Message.Type.EXIST);
        } catch (IncorrectCredentialsException e) {
            return new Message(Message.Type.FAIL);
        }
        account.setLoginDate(new Date());
        accountService.save(account);
        if ("web".equals(loginType)) {
            account.setLoginType("web");
            logger.info("子账号web端登陆成功，手机号为"+account.getPhone() );
        } else {
            account.setLoginType("phone");
            logger.info("子账号移动端登陆成功，手机号为"+account.getPhone());
            subject.getSession().setTimeout(24 * 7 * 60 * 60 * 1000);
        }
        subject.getSession().setAttribute("token", subject.getPrincipals().getPrimaryPrincipal());
        JSONObject accountJson = accountService.makeAccountJson(account);
        return new Message(Message.Type.OK, accountJson);
    }


    /**
     * 子账号登陆时发现ip有变化发送验证码
     *　忘记密码时获取验证码
     *
     * @param phone
     * @param session
     * @return
     */
    @RequestMapping(value = "/getVerifyCode", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getVerifyCode(@RequestParam String phone,
                     HttpSession session) {
        Message message = Message.parametersCheck(phone);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Account account = accountService.findByPhoneOrUserName(phone);
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        if (account.getPhone() == null) {
            // 未绑定手机号码
            return new Message(Message.Type.UNKNOWN);
        }
        return noteService.isSend(account.getPhone(), session);
    }

    /**
     * ip有变化验证码验证
     *　　
     * @param session
     * @return
     */
    @RequestMapping(value = "/loginByVerifyCode", method = RequestMethod.POST)
    public
    @ResponseBody
    Message loginByVerifyCode(
            @RequestBody Map<String, String> objectMap, HttpSession session) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        String verifyIpCode = map.get("verifyIpCode").toString();
        Account account = accountService.findByPhone(map.get("phone").toString());
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if(account.getLocked()!=null&&account.getLocked()==true){
            return new Message(Message.Type.UNKNOWN);
        }
        Verification verification = (Verification) session
                .getAttribute("verification");
        message = userService.checkCode(verifyIpCode, account.getPhone(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        return subjectLogin(account,"web");
    }

    /**
     * 重置密码需要手机验证
     *
     * @return
     */
    @RequestMapping(value = "/getRegVerifyCode", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getRegVerifyCode(@RequestParam String phone, HttpSession session) {
        Message message = Message.parametersCheck(phone);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (phone.length() != 11) {
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhone(phone);
        if (account != null) {
            // 该手机号已被注册
            return new Message(Message.Type.EXIST);
        }
        return noteService.isSend(phone, session);
    }

    /**
     * 重置密码
     * @param map
     * @param session
     * @return
     */
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message changePassword(@RequestBody Map<String, Object> map,HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        map = (Map<String, Object>) message.getData();
        String code = map.get("code").toString();
        Account account = accountService.findByPhone(map.get("phone").toString());
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        String password = map.get("password").toString();
        Verification verification = (Verification) session
                .getAttribute("verification");
        message = userService.checkCode(code, account.getPhone(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        account.setPassword(password);
        accountService.save(account);
        return new Message(Message.Type.OK);
    }

    /**
     * 获取当前子账号
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/getAccount", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getAccount() {
        Account account = authentService.getAccountFromSubject();
        account = accountService.find(account.getId());
        JSONObject jsonObject = accountService.makeAccountJson(account);
       return new Message(Message.Type.OK,jsonObject);

    }

    /**
     * 完善或更新子账号信息
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public @ResponseBody Message update(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        message = accountService.update(map,account.getId());
        return message;
    }

    /**
     * 修改密码
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public @ResponseBody Message updatePassword(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        String oldPassword = map.get("oldPassword").toString();
        if(!account.getPassword().equals(oldPassword)){
            return new Message(Message.Type.UNKNOWN);
        }
        String newPassword = map.get("newPassword").toString();
        message = accountService.updatePassword(newPassword,account.getId());
        return message;
    }

    /**
     * 修改用户名和邮箱
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    public @ResponseBody Message updateInfo(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        String name,email;
        if(map.get("email")!=null&&StringUtils.hasText( map.get("email").toString())){
            email = map.get("email").toString();
        }else {
            email = account.getEmail();
        }
        if(map.get("name")!=null&&StringUtils.hasText( map.get("name").toString())){
            name = map.get("name").toString();
        }else {
            name = account.getName();
        }
        message = accountService.updateInfo(name,email,account.getId());
        return message;
    }
    /**
     * 更改手机号
     * @param map
     * @param session
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/changePhone", method = RequestMethod.POST)
    public @ResponseBody Message changePhone(@RequestBody Map<String,Object> map,HttpSession session){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        Verification verification = (Verification) session.getAttribute("verification");
        String code = map.get("code").toString();
        String phone = map.get("phone").toString();
        message = userService.checkCode(code,phone, verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        message = accountService.changePhone(phone,account.getId());
        return message;
    }


    /**
     * 子账号解绑企业
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/unbind/{id}", method = RequestMethod.GET)
    public @ResponseBody Message unbind(@PathVariable("id") Long id){
        User user = userService.find(id);
        return accountService.unbindUser(user);
    }

    /**
     * 更新accountMessage
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updateAccountMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message updateUserMessage() {
        Account account = authentService.getAccountFromSubject();
        List<AccountMessage> messages = accountMessageService.getMessage(account);
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getStatus()
                    .equals(UserMessage.Status.UNREAD)) {
                messages.get(i).setStatus(AccountMessage.Status.READED);
                accountMessageService.save(messages.get(i));
            }
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 上传子账号头像
     * @param avatar
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public
    @ResponseBody
    Message uploadAvatar(
            @RequestBody Map<String, String> avatar) {
        Message message = Message.parameterCheck(avatar);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        account.setAvatar(avatar.get("avatar"));
        accountService.save(account);
        return new Message(Message.Type.OK);
    }
}
