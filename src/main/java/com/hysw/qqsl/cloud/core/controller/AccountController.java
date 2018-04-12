package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.Polling;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.core.entity.data.User;
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
    @Autowired
    private PollingService pollingService;
    @Autowired
    private CommonController commonController;

    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * 发送验证码
     * @param  phone 手机号码
     * @return message响应消息,短信是否发送成功
     */
    private Message sendVerify(String phone, HttpSession session, boolean flag){
        Message message = CommonController.parametersCheck(phone);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.phoneRegex(phone)){
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhone(phone);
        if (flag) {
            if (account == null) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
        }else{
            if (account != null) {
                return MessageService.message(Message.Type.DATA_EXIST);
            }
        }
        if (noteService.isSend(phone, session)) {
            return MessageService.message(Message.Type.OK);
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 注册时发送手机验证码
     * @param  phone 手机号码
     * @return message响应消息,短信是否发送成功
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
     * @param  phone 手机号码
     * @return message响应消息,短信是否发送成功
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
     * @param phone 手机号码
     * @return message响应消息OK:发送成功,FIAL:手机号不合法，EXIST：账号不存在
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
     * @param code 登录凭证(手机号码或邮箱)
     * @return message响应消息OK:发送成功,FIAL:手机号或邮箱不合法，EXIST：账号不存在
     */
    @RequestMapping(value = "/login/getLoginVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getLoginVerify(@RequestParam String code,
                           HttpSession session) {
        Message message = CommonController.parametersCheck(code);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(SettingUtils.phoneRegex(code)){
            if (noteService.isSend(account.getPhone(), session)) {
                return MessageService.message(Message.Type.OK);
            }
        }else if(SettingUtils.emailRegex(code)){
            emailService.getVerifyCodeLogin(code,session);
        }
        return MessageService.message(Message.Type.FAIL);
    }


    /**
     * email绑定时发送验证码
     * @param email 要绑定的邮箱
     * @return message响应消息OK:发送成功,FIAL:手机号不合法，EXIST：账号不存在
     */
    @RequestMapping(value = "/email/getBindVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getBindVerify(@RequestParam String email, HttpSession session) {
        Message message = CommonController.parametersCheck(email);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(email)){
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByEmail(email);
        if (account != null) {
            // 该邮箱已被注册
            return MessageService.message(Message.Type.DATA_EXIST);
        }
        emailService.getVerifyCodeBinding(email,session);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * email找回密码时发送验证码
     * @param email 绑定的邮箱
     * @return message响应消息OK:发送成功,FIAL:邮箱不合法，EXIST：账号不存在
     */
    @RequestMapping(value = "/email/getGetbackVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getGetbackVerifyEmial(@RequestParam String email, HttpSession session) {
        Message message = CommonController.parametersCheck(email);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(email)){
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        emailService.getVerifyCoderesetPassword(email,session);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 子账号注册
     * @param objectMap 包含用户名name,密码password
     * @return message响应消息OK:注册成功,FIAL:信息不完整,INVALID:验证码过期,NO_ALLOW:验证码错误,EXIST:帐号已存在
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public
    @ResponseBody
    Message register(@RequestBody Map<String, String> objectMap,
                     HttpSession session) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        Verification verification = (Verification) session
                .getAttribute("verification");
        String name,password,code;
        name = map.get("name").toString();
        password = map.get("password").toString();
        code = map.get("verification").toString();
        message = commonController.checkCode(code,verification);
        if (message.getType()!=Message.Type.OK) {
            return message;
        }
        try {
            if(verification.getPhone().length()!=11|| SettingUtils.phoneRegex(verification.getPhone())==false){
                throw new QQSLException(verification.getPhone()+":电话号码异常！");
            }
            if(password.length()!=32){
                throw new QQSLException(password+":密码异常！");
            }
        } catch (QQSLException e) {
            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhone(verification.getPhone());
        // 用户已存在
        if (account!= null) {
            return MessageService.message(Message.Type.DATA_EXIST);
        }else{
            account = new Account();
        }
        account.setName(name);
        account.setPhone(verification.getPhone());
        account.setPassword(password);
        //默认新注册用户角色为account:simple
        account.setRoles(CommonAttributes.ROLES[4]);
        accountService.save(account);
        pollingService.addAccount(account);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 手机找回密码:忘记密码时找回密码
     * @param map 新密码password
     * @return message响应消息OK:注册成功,FIAL:信息不全或session过期,INVALID:验证码过期,NO_ALLOW:验证码错误
     */
    @RequestMapping(value = "/phone/getbackPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message getbackPassord(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null || map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return MessageService.message(Message.Type.CODE_NOEXIST);
        }
        message = commonController.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhone(verification.getPhone());
        String password = map.get("password").toString();
        account.setPassword(password);
        accountService.save(account);
        return message;
    }

    /**
     * 邮箱找回密码:忘记密码时找回密码
     * @param map 新密码password
     * @return message响应消息OK:注册成功,FIAL:信息不全或session过期,INVALID:验证码过期,NO_ALLOW:验证码错误
     */
    @RequestMapping(value = "/email/getbackPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message getbackPassordEmail(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null||map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return MessageService.message(Message.Type.CODE_NOEXIST);
        }
        message = commonController.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByEmail(verification.getEmail());
        String password = map.get("password").toString();
        account.setPassword(password);
        accountService.save(account);
        return message;
    }

    /**
     * 修改密码:在基本资料的修改密码处点击保存时调用
     * @param map 包含新密码newPassword,老密码oldPassword
     * @return 完整子帐号信息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public @ResponseBody Message updatePassword(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        if (map.get("oldPassword") == null || !StringUtils.hasText(map.get("oldPassword").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        String oldPassword = map.get("oldPassword").toString();
        if(!account.getPassword().equals(oldPassword)){
            return MessageService.message(Message.Type.PASSWORD_ERROR);
        }
        if (map.get("newPassword") == null || !StringUtils.hasText(map.get("newPassword").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        String newPassword = map.get("newPassword").toString();
        if(newPassword.length()!=32){
            return MessageService.message(Message.Type.PASSWORD_ERROR);
        }
        account.setPassword(newPassword);
        accountService.save(account);
        authentService.updateSession(account);
        return MessageService.message(Message.Type.OK,accountService.makeAccountJson(account));
    }

    /**
     * 修改手机号码:在基本资料的修改手机号码处点击保存时调用
     * @param map verification 验证码
     * @return 修改完手机号码的子帐号信息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePhone", method = RequestMethod.POST)
    public @ResponseBody
    Message updatePhone(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return MessageService.message(Message.Type.CODE_NOEXIST);
        }
        if (map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        message = commonController.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        account.setPhone(verification.getPhone());
        authentService.updateSession(account);
        accountService.save(account);
        return MessageService.message(Message.Type.OK, accountService.makeAccountJson(account));
    }

    /**
     * 绑定邮箱\修改绑定邮箱：在基本资料里的绑定邮箱处点击保存时调用
     * @param map verification 验证码
     * @return 修改完邮箱的子帐号信息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updateEmail", method = RequestMethod.POST)
    public @ResponseBody Message updateEmail(@RequestBody Map<String,Object> map,HttpSession session){
        Message message = CommonController.parameterCheck(map);
        if(message.getType()!=Message.Type.OK){
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return MessageService.message(Message.Type.CODE_NOEXIST);
        }
        if (map.get("verification") == null || !StringUtils.hasText(map.get("verification").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        message = commonController.checkCode(map.get("verification").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        account.setEmail(verification.getEmail());
        authentService.updateSession(account);
        accountService.save(account);
        return MessageService.message(Message.Type.OK, accountService.makeAccountJson(account));
    }


    /**
     * 获取子账户信息：在基本资料的基本信息处显示
     * @return 完整的子帐号消息,包含子帐号消息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getAccount", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getAccount() {
        Account account = authentService.getAccountFromSubject();
        JSONObject json = accountService.makeAccountJson(account);
        return MessageService.message(Message.Type.OK, json);
    }

    /**
     * web端登录
     * @param objectMap 包含登录凭证code(手机号码或邮箱),密码password,cookie,如果cookie为空,需要验证码登录
     * @return message响应消息OK:登录成功,FIAL:信息不全或code不合法或密码错误,EXIT:帐号不存在,OTHER:需要验证登录
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/web/login", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Message login(
            @RequestBody Map<String, Object> objectMap) {
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (map.get("code") == null || !StringUtils.hasText(map.get("code").toString())
                ||map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST) ;
        }
        if (!account.getPassword().equals(map.get("password").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(account, "web");
        }
        if(map.get("cookie")==null||!StringUtils.hasText(map.get("cookie").toString())){
            return MessageService.message(Message.Type.CODE_NEED);
        }
        //登录间隔时间过长需重新登录
        if(account.getLoginDate()==null){
            account.setLoginDate(new Date());
        }
        if(System.currentTimeMillis() - account.getLoginDate().getTime()>15*24*60*60*1000l){
            return MessageService.message(Message.Type.CODE_NEED);
        }
        String cookie = map.get("cookie").toString();
        if(!cookie.equals(DigestUtils.md5Hex(account.getPassword()))){
            return MessageService.message(Message.Type.CODE_NEED);
        }else{
            return subjectLogin(account, "web");
        }
    }

    /**
     * 移动端登录
     * @param objectMap 包含登录凭证code(手机号码或邮箱),密码password
     * @return message响应消息OK:登录成功,FIAL:信息不全或code不合法或密码错误,EXIT:帐号不存在
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/phone/login", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Message phoneLogin(
            @RequestBody Map<String, Object> objectMap) {
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (map.get("code") == null || !StringUtils.hasText(map.get("code").toString())
                ||map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST) ;
        }
        if (!account.getPassword().equals(map.get("password").toString())) {
            return MessageService.message(Message.Type.PASSWORD_ERROR);
        }
       /* if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(user, "web",null);
        }*/
        return subjectLogin(account, "phone");
    }

    /**
     * web端验证码登录
     * @param map verification验证码,密码password
     * @return message响应消息OK:登录成功,FIAL:信息不全或密码错误,EXIT:帐号不存在
     */
    @RequestMapping(value = "/web/loginByVerify", method = RequestMethod.POST)
    public
    @ResponseBody
    Message loginByVerify(
            @RequestBody Map<String, String> map, HttpSession session) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        if (verification == null) {
            return MessageService.message(Message.Type.CODE_NOEXIST);
        }
        String code = verification.getEmail()==null?verification.getPhone():verification.getEmail();
        Account account = accountService.findByPhoneOrEmial(code);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (map.get("verification") == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        String verifyCode = map.get("verification");
        if (map.get("password") == null || !StringUtils.hasText(map.get("password"))) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (!account.getPassword().equals(map.get("password"))) {
            return MessageService.message(Message.Type.PASSWORD_ERROR);
        }
        message = commonController.checkCode(verifyCode,verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        return subjectLogin(account,"web");
    }

    /**
     * 判断输入的用户密码是否正确
     * @param map 包含密码password
     * @return message响应消息OK:密码正确,FIAL:密码错误
     */
    @RequestMapping(value = "/checkPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message checkPassword(
            @RequestBody Map<String, String> map) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password"))) {
            return MessageService.message(Message.Type.FAIL);
        }
        Account account = authentService.getAccountFromSubject();
        if (account.getPassword().equals(map.get("password"))) {
            return MessageService.message(Message.Type.OK);
        }
        return MessageService.message(Message.Type.FAIL);
    }


    private Message subjectLogin( Account account, String loginType) {
        ShiroToken token = new ShiroToken();
        token.setAccount(account);
        token.setLoginType(loginType);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        } catch (IncorrectCredentialsException e) {
            return MessageService.message(Message.Type.FAIL);
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
        return MessageService.message(Message.Type.OK, accountJson);
    }

    /**
     * 子账号解绑企业
     * @param id 企业或用户的id
     * @return message响应消息OK:解帮成功,UNKNOWN:未知错误
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/unbind/{id}", method = RequestMethod.POST)
    public @ResponseBody Message unbind(@PathVariable("id") Long id){
        User user = userService.find(id);
        Account account = authentService.getAccountFromSubject();
        boolean flag = accountService.unbindUser(user, account);
        if (!flag) {
            return MessageService.message(Message.Type.FAIL);
        }else{
            return MessageService.message(Message.Type.OK);
        }
    }

    /**
     * 子账户对应user列表
     * @return message,包含所有与该子帐号绑定的user列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/getInviteCompany", method = RequestMethod.GET)
    public @ResponseBody Message userList(){
        Account account = authentService.getAccountFromSubject();
        List<User> users = accountService.getUsersByAccountId(account.getId());
        return MessageService.message(Message.Type.OK, userService.makeUserJsons(users));
    }

    /**
     * 更新accountMessage
     * @param ids 需要更新的accountMessage的id字符串
     * @return message响应消息OK:更新成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updateAccountMessage/{ids}", method = RequestMethod.POST)
    public
    @ResponseBody
    Message updateAccountMessage(@PathVariable("ids") String ids) {
        Message message = CommonController.parametersCheck(ids);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        List<AccountMessage> accountMessages = accountMessageService.findByAccount(account);
        String[] split = ids.split(",");
        for (String id : split) {
            for (AccountMessage accountMessage : accountMessages) {
                if (accountMessage.getId().toString().equals(id)) {
                    accountMessage.setStatus(CommonEnum.MessageStatus.READED);
                    accountMessageService.save(accountMessage);
                    break;
                }
            }
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除accountMessage
     * @param ids 需要更新的accountMessage的id字符串
     * @return message响应消息OK:更新成功,EXIST:消息不存在
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteAccountMessage/{ids}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Message deleteAccountMessage(@PathVariable("ids") String ids) {
        Message message = CommonController.parametersCheck(ids);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        AccountMessage accountMessage;
        String[] split = ids.split(",");
        for (String id : split) {
            accountMessage = accountMessageService.find(Long.valueOf(id));
            if (accountMessage==null){
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            accountMessageService.remove(accountMessage);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 修改name
     * @param map 包含子帐号名字name
     * @return message响应消息OK:更新成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    public @ResponseBody Message updateInfo(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        String name;
        if(map.get("name")!=null&&StringUtils.hasText( map.get("name").toString())){
            name = map.get("name").toString();
        }else {
            name = account.getName();
        }
        account.setName(name);
        accountService.save(account);
        authentService.updateSession(account);
        return MessageService.message(Message.Type.OK,accountService.makeAccountJson(account));
    }

    /**
     * 循环获取子帐号信息,包含AccountMessage
     * @return message响应消息OK:轮询响应成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"})
    @RequestMapping(value = "/polling", method = RequestMethod.GET)
    public @ResponseBody Message polling(){
        Account account = authentService.getAccountFromSubject();
        Polling polling=pollingService.findByAccount(account.getId());
        return MessageService.message(Message.Type.OK,pollingService.toJson(polling));
    }

}
