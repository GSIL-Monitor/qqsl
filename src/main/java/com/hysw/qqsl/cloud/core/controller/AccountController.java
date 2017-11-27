package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.data.UserMessage;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.core.shiro.ShiroToken;
import com.hysw.qqsl.cloud.util.SettingUtils;
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
    @Autowired
    private EmailService emailService;

    Log logger = LogFactory.getLog(this.getClass());

    /**
     * 发送验证码
     *
     * @param phone
     * @param session
     * @return
     */
    private Message sendVerify(String phone, HttpSession session,boolean flag){
        Message message = Message.parametersCheck(phone);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        if(!SettingUtils.phoneRegex(phone)){
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhone(phone);
        if (flag) {
            if (account == null) {
                return new Message(Message.Type.EXIST);
            }
        }else{
            if (account != null) {
                return new Message(Message.Type.EXIST);
            }
        }
        return noteService.isSend(phone, session);
    }

    /**
     * 注册时发送手机验证码
     * @param phone
     * @param session
     * @return
     */
    @RequestMapping(value = "/phone/getRegistVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getRegistVerify(@RequestParam String phone,
                            HttpSession session) {
        return sendVerify(phone,session,false);
    }

    /**
     * 修改密保手机发送验证码
     * @param phone
     * @param session
     * @return
     */
    @RequestMapping(value = "/phone/getUpdateVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUpdateVerify(@RequestParam String phone,
                            HttpSession session) {
        return sendVerify(phone,session,false);
    }

    /**
     * 手机找回密码时发送验证码：
     * 参数：phone:手机号
     * 返回：OK:发送成功,FIAL:手机号不合法，EXIST：账号不存在
     */
    @RequestMapping(value = "/phone/getGetbackVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getGetbackVerify(@RequestParam String phone,
                             HttpSession session) {
        return sendVerify(phone, session,true);
    }

    /**
     * web端登录发送验证码:
     *
     */
    @RequestMapping(value = "/login/getLoginVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getLoginVerify(@RequestParam String code,
                           HttpSession session) {
        Message message = Message.parametersCheck(code);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        if(SettingUtils.phoneRegex(code)){
            message = noteService.isSend(account.getPhone(), session);
        }else if(SettingUtils.emailRegex(code)){
            return emailService.getVerifyCodeLogin(code,session);
        }
        return message;
    }

    /**
     * email绑定时发送验证码
     * @return
     * OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequestMapping(value = "/email/getBindVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getBindVerify(@RequestParam String email, HttpSession session) {
        Message message = Message.parametersCheck(email);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(email)){
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByEmail(email);
        if (account != null) {
            // 该邮箱已被注册
            return new Message(Message.Type.EXIST);
        }
        emailService.getVerifyCodeBinding(email,session);
        return new Message(Message.Type.OK);
    }

    /**
     * email找回密码时发送验证码
     * @param email
     * @param session
     * @return
     */
    @RequestMapping(value = "/email/getGetbackVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getGetbackVerifyEmial(@RequestParam String email, HttpSession session) {
        Message message = Message.parametersCheck(email);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(email)){
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        return emailService.getVerifyCoderesetPassword(email,session);
    }

    /**
     * 子账号注册
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
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        String name,password,code;
        name = map.get("name").toString();
        password = map.get("password").toString();
        code = map.get("verification").toString();
        message = userService.checkCode(code,verification);
        if (message.getType()!=Message.Type.OK) {
            return message;
        }
        try {
            message = accountService.register(name,verification.getPhone(),password);
        } catch (QQSLException e) {
            e.printStackTrace();
            return message;
        }
        return message;
    }

    /**
     * 手机找回密码:忘记密码时找回密码
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/phone/getbackPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message getbackPassord(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        Account account = accountService.findByPhone(verification.getPhone());
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        if (map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String password = map.get("password").toString();
        account.setPassword(password);
        accountService.save(account);
        return message;
    }

    /**
     * 邮箱找回密码:忘记密码时找回密码
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/email/getbackPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message getbackPassordEmail(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        Account account = accountService.findByEmail(verification.getEmail());
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        if (map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String password = map.get("password").toString();
        account.setPassword(password);
        accountService.save(account);
        return message;
    }

    /**
     * 修改密码:在基本资料的修改密码处点击保存时调用
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public @ResponseBody Message updatePassword(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        if (map.get("oldPassword") == null || !StringUtils.hasText(map.get("oldPassword").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String oldPassword = map.get("oldPassword").toString();
        if(!account.getPassword().equals(oldPassword)){
            return new Message(Message.Type.UNKNOWN);
        }
        if (map.get("newPassword") == null || !StringUtils.hasText(map.get("newPassword").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String newPassword = map.get("newPassword").toString();
        message = accountService.updatePassword(newPassword,account.getId());
        return message;
    }

    /**
     * 修改手机号码:在基本资料的修改手机号码处点击保存时调用
     *
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/changePhone ", method = RequestMethod.POST)
    public @ResponseBody
    Message changePhone(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType().equals(Message.Type.FAIL)) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        if (map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        account.setPhone(verification.getPhone());
        authentService.updateSession(account);
        accountService.save(account);
        return new Message(Message.Type.OK, accountService.makeAccountJson(account));
    }

    /**
     * 绑定邮箱\修改绑定邮箱：在基本资料里的绑定邮箱处点击保存时调用
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updateEmail", method = RequestMethod.POST)
    public @ResponseBody Message updateEmail(@RequestBody Map<String,Object> map,HttpSession session){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        if (map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        account.setEmail(verification.getEmail());
        authentService.updateSession(account);
        accountService.save(account);
        return new Message(Message.Type.OK, accountService.makeAccountJson(account));
    }


    /**
     * 获取子账户信息：在基本资料的基本信息处显示
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getAccount", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getAccount() {
        Account account = authentService.getAccountFromSubject();
        JSONObject json = accountService.makeAccountJson(account);
        return new Message(Message.Type.OK, json);
    }

    /**
     * web端登录
     *
     * @param objectMap
     * @throws
     */
    @RequestMapping(value = "/web/login", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Message login(
            @RequestBody Map<String, Object> objectMap) {
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (map.get("code") == null || !StringUtils.hasText(map.get("code").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return new Message(Message.Type.EXIST) ;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!account.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(account, "web");
        }
        if(map.get("cookie")==null||!StringUtils.hasText(map.get("cookie").toString())){
            return new Message(Message.Type.OTHER);
        }
        //登录间隔时间过长需重新登录
        if(account.getLoginDate()==null){
            account.setLoginDate(new Date());
        }
        if(System.currentTimeMillis() - account.getLoginDate().getTime()>15*24*60*60*1000l){
            return new Message(Message.Type.OTHER);
        }
        String cookie = map.get("cookie").toString();
        if(!cookie.equals(DigestUtils.md5Hex(account.getPassword()))){
            return new Message(Message.Type.OTHER);
        }else{
            return subjectLogin(account, "web");
        }
    }

    /**
     * 移动端登录
     *
     * @param objectMap
     * @throws
     */
    @RequestMapping(value = "/phone/login", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Message phoneLogin(
            @RequestBody Map<String, Object> objectMap) {
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (map.get("code") == null || !StringUtils.hasText(map.get("code").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return new Message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return new Message(Message.Type.EXIST) ;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!account.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
       /* if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(user, "web",null);
        }*/
        return subjectLogin(account, "phone");
    }

    /**
     * web端验证码登录
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/web/loginByVerify", method = RequestMethod.POST)
    public
    @ResponseBody
    Message loginByVerify(
            @RequestBody Map<String, String> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        String code = verification.getEmail()==null?verification.getPhone():verification.getEmail();
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return new Message(Message.Type.EXIST);
        }
        String verifyCode = map.get("verification").toString();
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!account.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(verifyCode,verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        return subjectLogin(account,"web");
    }

    /**
     * 判断输入的用户密码是否正确
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/checkPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message checkPassword(
            @RequestBody Map<String, String> map) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        Account account = authentService.getAccountFromSubject();
        if (account.getPassword().equals(map.get("password"))) {
            return new Message(Message.Type.OK);
        }
        return new Message(Message.Type.FAIL);
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
/////////////////////////////////////////////////////////////////////////////////////////////////////

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

}
