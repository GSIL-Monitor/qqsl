package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.core.shiro.ShiroToken;
import com.hysw.qqsl.cloud.annotation.util.IsExpire;
import com.hysw.qqsl.cloud.core.service.EmailService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.wechat.entity.data.WeChat;
import com.hysw.qqsl.cloud.wechat.service.GetAccessTokenService;
import com.hysw.qqsl.cloud.wechat.service.GetUserBaseMessage;
import com.hysw.qqsl.cloud.wechat.service.WeChatService;
import net.sf.json.JSONArray;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private GetAccessTokenService getAccessTokenService;
    @Autowired
    private WeChatService weChatService;
    @Autowired
    private GetUserBaseMessage getUserBaseMessage;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StorageLogService storageLogService;

    /**
     * 注册时发送手机验证码
     * 参数：phone:手机号
     * 返回：OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
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
     * 修改密保手机发送验证码：/user/phone/getUpdateVerify
     * 参数：phone:手机号
     * 返回：OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequestMapping(value = "/phone/getUpdateVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUpdateVeridy(@RequestParam String phone,
                            HttpSession session) {
        return sendVerify(phone, session,false);
    }


    /**
     * 手机找回密码时发送验证码：/user/phone/getGetbackVerify
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
     * web端登录发送验证码: /user/phone/getLoginVerify
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
        User user=userService.findByPhoneOrEmial(code);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        if(SettingUtils.phoneRegex(code)){
            message = noteService.isSend(user.getPhone(), session);
        }else if(SettingUtils.emailRegex(code)){
            return emailService.getVerifyCodeLogin(code,session);
        }
        return message;
    }


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
        User user = userService.findByPhone(phone);
        if (flag) {
            if (user == null) {
                return new Message(Message.Type.EXIST);
            }
        }else{
            if (user != null) {
                return new Message(Message.Type.EXIST);
            }
        }
        return noteService.isSend(phone, session);
    }


    /**
     * email绑定时发送验证码
     *
     * @return
     * OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequestMapping(value = "/email/getBindVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message sendBindVerify(@RequestParam String email, HttpSession session) {
        Message message = Message.parametersCheck(email);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(email)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByEmail(email);
        if (user != null) {
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
    Message sendGetbackVerifyEmail(@RequestParam String email, HttpSession session) {
        Message message = Message.parametersCheck(email);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(email)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByEmail(email);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return emailService.getVerifyCoderesetPassword(email,session);
    }

    /**
     * 用户注册
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
        if (verification == null) {
            return new Message(Message.Type.OTHER);
        }
        return userService.registerService(map, verification);
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
        User user = userService.findByPhone(verification.getPhone());
        if (user == null) {
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
        user.setPassword(password);
        userService.save(user);
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
        User user = userService.findByEmail(verification.getEmail());
        if (user == null) {
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
        user.setPassword(password);
        userService.save(user);
        return message;
    }

    /**
     * 修改密码:在基本资料的修改密码处点击保存时调用
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public @ResponseBody Message updatePassword(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (map.get("oldPassword") == null || !StringUtils.hasText(map.get("oldPassword").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String oldPassword = map.get("oldPassword").toString();
        if(!user.getPassword().equals(oldPassword)){
            return new Message(Message.Type.UNKNOWN);
        }
        if (map.get("newPassword") == null || !StringUtils.hasText(map.get("newPassword").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String newPassword = map.get("newPassword").toString();
        message = userService.updatePassword(newPassword,user.getId());
        return message;
    }

    /**
     * 修改手机号码:在基本资料的修改手机号码处点击保存时调用
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePhone", method = RequestMethod.POST)
    public @ResponseBody Message updatePhone(@RequestBody Map<String,Object> map,HttpSession session){
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
        User user = authentService.getUserFromSubject();
        user.setPhone(verification.getPhone());
        authentService.updateSession(user);
        userService.save(user);
        return new Message(Message.Type.OK, userService.makeUserJson(user));
    }

    /**
     * 绑定邮箱\修改绑定邮箱：在基本资料里的绑定邮箱处点击保存时调用
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
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
        User user = authentService.getUserFromSubject();
        user.setEmail(verification.getEmail());
        authentService.updateSession(user);
        userService.save(user);
        return new Message(Message.Type.OK, userService.makeUserJson(user));
    }

    /**
     * 获取用户信息：在基本资料的基本信息处显示
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUser() {
        User user = authentService.getUserFromSubject();
        JSONObject json = userService.makeUserJson(user);
        return new Message(Message.Type.OK, json);
    }

    /**
     * 获取用户列表(除自身)
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUsers() {
        User user = authentService.getUserFromSubject();
        List<User> users = userService.findUsersNeOwn(user);
        List<JSONObject> userJsons = userService.makeUserJsons(users);
        return new Message(Message.Type.OK, userJsons);
    }

    /**
     * 获取用户列表
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/getUsers", method = RequestMethod.GET)
    public
    @ResponseBody
    Message adminGetUsers() {
        List<User> users = userService.findUsers();
        List<JSONObject> userJsons = userService.makeUserJsons(users);
        return new Message(Message.Type.OK, userJsons);
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
        User user = userService.findByPhoneOrEmial(code);
        if (user == null) {
            return new Message(Message.Type.EXIST) ;
        }
        //判断是否被禁用
        if (user.getLocked() != null && user.getLocked()) {
            return new Message(Message.Type.UNKNOWN);
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!user.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(user, "web",null);
        }
        if(map.get("cookie")==null||!StringUtils.hasText(map.get("cookie").toString())){
            return new Message(Message.Type.OTHER);
        }
        //登录间隔时间过长需重新登录
        if(user.getLoginDate()==null){
            user.setLoginDate(new Date());
        }
        if(System.currentTimeMillis() - user.getLoginDate().getTime()>15*24*60*60*1000l){
            return new Message(Message.Type.OTHER);
        }
        String cookie = map.get("cookie").toString();
        if(!cookie.equals(DigestUtils.md5Hex(user.getPassword()))){
            return new Message(Message.Type.OTHER);
        }else{
            return subjectLogin(user, "web",null);
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
        User user = userService.findByPhoneOrEmial(code);
        if (user == null) {
            return new Message(Message.Type.EXIST) ;
        }
        //判断是否被禁用
        if (user.getLocked() != null && user.getLocked()) {
            return new Message(Message.Type.UNKNOWN);
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!user.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
       /* if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(user, "web",null);
        }*/
        return subjectLogin(user, "phone",null);
    }

    /**
     * 微信用户登录
     *
     * @param objectMap
     * @throws
     */
    @RequestMapping(value = "/wechat/login", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Message wechatLogin(
            @RequestBody Map<String, Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        Object openId = SecurityUtils.getSubject().getSession().getAttribute("openId");
        if (map.get("code") == null || !StringUtils.hasText(map.get("code").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        if (!(SettingUtils.phoneRegex(code)||SettingUtils.emailRegex(code))) {
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByPhoneOrEmial(code);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if (user.getLocked() != null && user.getLocked()) {
            return new Message(Message.Type.UNKNOWN);
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!user.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        return subjectLogin(user, "weChat",openId);
    }

    /**
     * 根据openid自动登录
     * @param objectMap
     * @return
     */

    @RequestMapping(value = "/weChat/autoLogin", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Message weChatAutoLogin(
            @RequestBody Map<String, Object> objectMap) {
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (map.get("code") == null||!StringUtils.hasText(map.get("code").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        Object openId = getAccessTokenService.getCodeOpenId(code);
        if (openId == null) {
            return new Message(Message.Type.FAIL);
        }
        SecurityUtils.getSubject().getSession().setAttribute("openId",openId);
        WeChat weChat = weChatService.findByOpenId(openId.toString());
        if (weChat == null) {
            return new Message(Message.Type.FAIL);
        }
        User user = userService.find(weChat.getUserId());
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if (user.getLocked() != null && user.getLocked()) {
            return new Message(Message.Type.UNKNOWN);
        }
        return subjectLogin(user, "weChat", null);
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
        User user = userService.findByPhoneOrEmial(code);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        String verifyCode = map.get("verification").toString();
        //判断是否被禁用
        if (user.getLocked() != null && user.getLocked()) {
            return new Message(Message.Type.UNKNOWN);
        }
        if (map.get("password") == null || !StringUtils.hasText(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (!user.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(verifyCode,verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        return subjectLogin(user,"web",null);
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
        User user = authentService.getUserFromSubject();
        if (user.getPassword().equals(map.get("password"))) {
            return new Message(Message.Type.OK);
        }
        return new Message(Message.Type.FAIL);
    }


//    =============================================================================================








    /**
     * 公众号与水利云账号是否绑定
     * @return
     */
    @RequestMapping(value = "/bindingRelationship", method = RequestMethod.GET)
    public @ResponseBody Message bindingRelationship() {
        Object openId = SecurityUtils.getSubject().getSession().getAttribute("openId");
        if (openId == null) {
            return new Message(Message.Type.FAIL);
        }
        WeChat weChat = weChatService.findByOpenId(openId.toString());
        if (weChat == null) {
            return new Message(Message.Type.EXIST);
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 解除公众号与水利云账号的绑定
     * @return
     */
    @RequestMapping(value = "/unbind", method = RequestMethod.POST)
    public @ResponseBody Message unbind() {
        Object openId = SecurityUtils.getSubject().getSession().getAttribute("openId");
        if (openId == null) {
            return new Message(Message.Type.FAIL);
        }
        WeChat weChat = weChatService.findByOpenId(openId.toString());
        if (weChat == null) {
            return new Message(Message.Type.EXIST);
        }
        weChatService.remove(weChat);
        return new Message(Message.Type.OK);
    }


    /**
     * 登陆
     *
     * @param user
     * @return
     */
    private Message subjectLogin(User user, String loginType, Object openId) {
        ShiroToken token = new ShiroToken();
        token.setUser(user);
        token.setLoginType(loginType);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            return new Message(Message.Type.EXIST);
        } catch (IncorrectCredentialsException e) {
            return new Message(Message.Type.FAIL);
        }
        if (loginType.equals("web")) {
            user.setLoginType("web");
            logger.info(user.getUserName() + ",web端登陆成功");
        } else if (loginType.equals("phone")) {
            user.setLoginType("phone");
            logger.info(user.getUserName() + ",移动端登陆成功");
            subject.getSession().setTimeout(24 * 7 * 60 * 60 * 1000);
        } else {
            user.setLoginType("weChat");
            logger.info(user.getUserName() + ",微信端登陆成功");
            subject.getSession().setTimeout(24 * 7 * 60 * 60 * 1000);
            WeChat weChat1 = weChatService.findByUserId(user.getId());
            if (openId != null&&weChat1==null) {
                WeChat weChat = new WeChat();
                weChat.setUserId(user.getId());
                weChat.setOpenId(openId.toString());
                weChat.setNickName(getUserBaseMessage.getNickname(openId.toString()));
                weChatService.save(weChat);
            }
        }
        user.setLoginDate(new Date());
        subject.getSession().setAttribute("token", subject.getPrincipals().getPrimaryPrincipal());
        JSONObject userJson = userService.makeUserJson(user);
        userService.save(user);
        return new Message(Message.Type.OK, userJson);
    }

    /**
     * 发布文章
     *
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:system"}, logical = Logical.OR)
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public
    @ResponseBody
    Message publishAriticle(
            @RequestBody Map<String, String> map) {
        if (!StringUtils.hasText(map.get("type")) ||
                !StringUtils.hasText(map.get("content")) ||
                !StringUtils.hasText(map.get("title"))) {
            return new Message(Message.Type.FAIL);
        }
        String idStr = "";
        if (map.get("id") != null && !map.get("id").equals("null")) {
            idStr = map.get("id").toString();
        }
        int index = Integer.valueOf(map.get("type").toString());
        String content = map.get("content").toString();
        String title = map.get("title").toString();
        content = articleService.replacePath(content);
        return articleService.save(idStr, title, content, index);
    }

    /**
     * 删除文章
     *
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:system"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteArticle/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Message deletetArticle(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        articleService.removeById(id);
        return new Message(Message.Type.OK);

    }
    /**
     * 更新userMessage
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updateUserMessage/{ids}", method = RequestMethod.POST)
    public
    @ResponseBody
    Message updateUserMessage(@PathVariable("ids") String ids) {
        Message message = Message.parametersCheck(ids);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        List<UserMessage> userMessages = userMessageService.findByUser(user);
        String[] split = ids.split(",");
        for (String id : split) {
            for (int i = 0; i < userMessages.size(); i++) {
                if (userMessages.get(i).getId().toString().equals(id)) {
                    userMessages.get(i).setStatus(CommonEnum.MessageStatus.READED);
                    userMessageService.save(userMessages.get(i));
                    break;
                }
            }
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 删除userMessage
     * @param ids
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteUserMessage/{ids}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Message deleteUserMessage(@PathVariable("ids") String ids) {
        Message message = Message.parametersCheck(ids);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        UserMessage userMessage;
        String[] split = ids.split(",");
        for (String id : split) {
            try {
                userMessage = userMessageService.find(Long.valueOf(id));
            } catch (Exception e) {
                return new Message(Message.Type.EXIST);
            }
            userMessageService.remove(userMessage);
        }
        return new Message(Message.Type.OK);

    }

    /**
     * 获取用户的所有通讯录列表
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getContacts", method = RequestMethod.GET)
    public @ResponseBody Message getContacts(){
        User user = authentService.getUserFromSubject();
        List<Contact> contacts = contactService.findByUser(user);
        for(int i=0;i<contacts.size();i++){
            contacts.get(i).setUser(null);
        }
        return new Message(Message.Type.OK,contacts);
    }

    //////////////////子账号//////////////////////////

    /**
     * 企业账号邀请子账号
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/account/invite",method = RequestMethod.POST)
    public @ResponseBody Message invite(@RequestBody Map<String, Object> map) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        if (map.get("phone") == null || !StringUtils.hasText(map.get("phone").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String phone = map.get("phone").toString();
        message = Message.parametersCheck(phone);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(!SettingUtils.phoneRegex(phone)){
            message = new Message(Message.Type.UNKNOWN);
            return message;
        }
        User user = authentService.getUserFromSubject();
        //        是否允许创建子账号
        if (userService.isAllowCreateAccount(user)) {
            return new Message(Message.Type.NO_ALLOW);
        }
        return  accountService.invite(phone,user);
    }

    /**
     * 企业解绑子账号
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/account/unbind", method = RequestMethod.POST)
    public @ResponseBody Message unbind(@RequestBody Map<String, Object> map){
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        if (map.get("id") == null || !StringUtils.hasText(map.get("id").toString())) {
            return new Message(Message.Type.FAIL);
        }
        Account account;
        try {
            account = accountService.find(Long.valueOf(map.get("id").toString()));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if(account==null){
            return new Message(Message.Type.EXIST);
        }
        return userService.unbindAccount(account);
    }

    /**
     * 用户获取当前套餐详情时,查看套餐剩余空间,每天文件上传下载的空间变化情况,以及下载流量的使用情况
     * 注:上传和下载流量最大是空间大小的10倍。
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/storageCountLog", method = RequestMethod.GET,produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message getStorageCountLog() {
        User user = authentService.getUserFromSubject();
        JSONArray jsonArray = storageLogService.getStorageCountLog(user);
        return new Message(Message.Type.OK,jsonArray);
    }
}

