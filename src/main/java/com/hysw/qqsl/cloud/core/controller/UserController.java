package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.annotation.util.IsAllowCreateAccount;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.core.shiro.ShiroToken;
import com.hysw.qqsl.cloud.annotation.util.IsExpire;
import com.hysw.qqsl.cloud.util.Email;
import com.hysw.qqsl.cloud.util.EmailCache;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.wechat.entity.data.WeChat;
import com.hysw.qqsl.cloud.wechat.service.GetAccessTokenService;
import com.hysw.qqsl.cloud.wechat.service.GetUserBaseMessage;
import com.hysw.qqsl.cloud.wechat.service.WeChatService;
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
    private EmailCache emailCache;

    /**
     * 注册时发送手机验证码
     *
     * @return
     * OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequestMapping(value = "/phone/sendRegistVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message sendRegistVerify(@RequestParam String phone, HttpSession session) {
        Message message = Message.parametersCheck(phone);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.phoneRegex(phone)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByPhone(phone);
        if (user != null) {
            // 该手机号已被注册
            return new Message(Message.Type.EXIST);
        }
        return noteService.isSend(phone, session);
    }

    /**
     * 修改手机号码发送验证码
     *
     * @param map
     * @param session
     * @return
     * OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/phone/sendUpdateVeridy", method = RequestMethod.POST)
    public
    @ResponseBody
    Message sendUpdateVeridy(@RequestBody Map<String,Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("phone") == null || !StringUtils.hasText(map.get("phone").toString())) {
            return new Message(Message.Type.FAIL);
        }
        String phone = map.get("phone").toString();
        if(!SettingUtils.phoneRegex(phone)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByPhone(phone);
        if (user != null) {
            // 该手机号已被注册
            return new Message(Message.Type.EXIST);
        }
        return noteService.isSend(phone, session);
    }

    /**
     * 手机找回密码时发送验证码
     *
     * @param phone
     * @param session
     * @return
     */
    @RequestMapping(value = "/phone/sendGetbackVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message sendGetbackVerify(@RequestParam String phone,
                          HttpSession session) {
        return sendVerify(phone, session);
    }

    /**
     *  web端登录发送验证码
     *
     * @param phone
     * @param session
     * @return
     */
    @RequestMapping(value = "/phone/sendLoginVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message sendLoginVerify(@RequestParam String phone,
                              HttpSession session) {
        return sendVerify(phone, session);
    }

    /**
     * 发送验证码
     *
     * @param phone
     * @param session
     * @return
     */
    private Message sendVerify(String phone, HttpSession session){
        Message message = Message.parametersCheck(phone);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        if(!SettingUtils.phoneRegex(phone)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByPhone(phone);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return noteService.isSend(user.getPhone(), session);
    }


    /**
     * email绑定时发送验证码
     *
     * @return
     * OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequestMapping(value = "/email/sendBindVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message sendBindVerify(@RequestParam String eamil, HttpSession session) {
        Message message = Message.parametersCheck(eamil);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(eamil)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByEmail(eamil);
        if (user != null) {
            // 该邮箱已被注册
            return new Message(Message.Type.EXIST);
        }
        Verification verification = new Verification();
        String code = noteService.createRandomVcode();
        verification.setEmail(eamil);
        verification.setCode(code);
        session.setAttribute("verification", verification);
        emailCache.add(Email.getVerifyCodeBinding(eamil, code));
        return new Message(Message.Type.OK);
    }

    /**
     * email找回密码时发送验证码
     * @param eamil
     * @param session
     * @return
     */
    @RequestMapping(value = "/email/sendGetbackVerify", method = RequestMethod.GET)
    public
    @ResponseBody
    Message sendGetbackVerifyEmail(@RequestParam String eamil, HttpSession session) {
        Message message = Message.parametersCheck(eamil);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!SettingUtils.emailRegex(eamil)){
            return new Message(Message.Type.FAIL);
        }
        User user = userService.findByEmail(eamil);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        Verification verification = new Verification();
        String code = noteService.createRandomVcode();
        verification.setEmail(eamil);
        verification.setCode(code);
        session.setAttribute("verification", verification);
        emailCache.add(Email.getVerifyCoderesetPassword(user,code));
        return new Message(Message.Type.OK);
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
        return userService.registerService(map, verification);
    }

    /**
     * 手机找回密码:忘记密码时找回密码
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/phone/getbackPassord", method = RequestMethod.POST)
    public
    @ResponseBody
    Message getbackPassord(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        User user = userService.findByPhone(verification.getPhone());
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        if (map.get("code") == null) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("code").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("password") == null) {
            return new Message(Message.Type.FAIL);
        }
        String password = map.get("password").toString();
        user.setPassword(password);
        userService.save(user);
        return message;
    }

    /**
     * 手机找回密码:忘记密码时找回密码
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/email/getbackPassord", method = RequestMethod.POST)
    public
    @ResponseBody
    Message getbackPassordEmail(@RequestBody Map<String, Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Verification verification = (Verification) session.getAttribute("verification");
        User user = userService.findByEmail(verification.getEmail());
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        if (map.get("code") == null || !StringUtils.hasText(map.get("code").toString())) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("code").toString(), verification);
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
     * 修改密码:在基本资料的修改密码处点击保存时调用
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
        if (map.get("code") == null) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("code").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        user.setPhone(verification.getPhone());
        authentService.updateSession(user);
        return new Message(Message.Type.OK, userService.makeUserJson(user));
    }

    /**
     * 绑定邮箱：在基本资料里的绑定邮箱处点击保存时调用
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
        if (map.get("code") == null) {
            return new Message(Message.Type.FAIL);
        }
        message = userService.checkCode(map.get("code").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        user.setEmail(verification.getEmail());
        authentService.updateSession(user);
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
        List<JSONObject> userJsons;
        List<User> users = userService.findUsersNeOwn(user);
        userJsons = userService.makeUserJsons(users);
        return new Message(Message.Type.OK, userJsons);
    }

    /**
     * 获取用户列表(除自身)
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
        Object code = map.get("code");
        if (code == null || !StringUtils.hasText(code.toString())) {
            return new Message(Message.Type.FAIL);
        }
        User user = null;
        if (SettingUtils.phoneRegex(code.toString())) {
            user = userService.findByPhone(code.toString());
        } else if (SettingUtils.emailRegex(code.toString())) {
            user = userService.findByEmail(code.toString());
        }
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if (user.getLocked() != null && user.getLocked()) {
            return new Message(Message.Type.UNKNOWN);
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
        Object code = map.get("code");
        if (code == null || StringUtils.hasText(code.toString())) {
            return new Message(Message.Type.FAIL);
        }
        User user = null;
        if (SettingUtils.phoneRegex(code.toString())) {
            user = userService.findByPhone(code.toString());
        } else if (SettingUtils.emailRegex(code.toString())) {
            user = userService.findByEmail(code.toString());
        }
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if(user.getLocked()!=null&&user.getLocked()==true){
            return new Message(Message.Type.UNKNOWN);
        }
        if (!user.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (map.get("loginType") != null && StringUtils.hasText(map.get("loginType").toString()) && map.get("loginType").equals("phone")) {
            return subjectLogin(user, "phone",null);
        }
        if("dev".equals(SettingUtils.getInstance().getSetting().getStatus())){
            return subjectLogin(user, "web",null);
        }
        return new Message(Message.Type.FAIL);
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
        Object code = map.get("code");
        if (code == null || StringUtils.hasText(code.toString())) {
            return new Message(Message.Type.FAIL);
        }
        User user = null;
        if (SettingUtils.phoneRegex(code.toString())) {
            user = userService.findByPhone(code.toString());
        } else if (SettingUtils.emailRegex(code.toString())) {
            user = userService.findByEmail(code.toString());
        }
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if(user.getLocked()!=null&&user.getLocked()==true){
            return new Message(Message.Type.UNKNOWN);
        }
        if (!user.getPassword().equals(map.get("password").toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (map.get("loginType") != null && StringUtils.hasText(map.get("loginType").toString()) && map.get("loginType").equals("weChat")) {
            return subjectLogin(user, "weChat",openId);
        }
        return new Message(Message.Type.FAIL);
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
        if (map.get("code") == null || map.get("loginType") == null) {
            return new Message(Message.Type.FAIL);
        }
        String code = map.get("code").toString();
        String loginType = map.get("loginType").toString();
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
        if(user.getLocked()!=null&&user.getLocked()==true){
            return new Message(Message.Type.UNKNOWN);
        }
        return subjectLogin(user, loginType, null);
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
        User user = null;
        if (verification.getEmail() == null && verification.getPhone() == null) {
            return new Message(Message.Type.FAIL);
        } else if (verification.getEmail() == null) {
            user = userService.findByPhone(verification.getPhone());
        } else if (verification.getPhone() == null) {
            user = userService.findByEmail(verification.getEmail());
        }

        String verifyIpCode = map.get("verifyIpCode").toString();
        //判断是否被禁用
        if(user.getLocked()!=null&&user.getLocked()){
            return new Message(Message.Type.UNKNOWN);
        }
        message = userService.checkCode(verifyIpCode,verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        return subjectLogin(user,"web",null);
    }

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
        return sendVerify(phone, session);
    }

    /**
     * 修改密保手机发送验证码：/user/phone/getUpdateVeridy
     * 参数：phone:手机号
     * 返回：OK:发送成功,FIAL:手机号不合法，EXIST：手机号已被使用
     */
    @RequestMapping(value = "/phone/getUpdateVeridy", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUpdateVeridy(@RequestParam String phone,
                            HttpSession session) {
        return sendVerify(phone, session);
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
        return sendVerify(phone, session);
    }

    /**
     * web端登录发送验证码: /user/phone/getLoginVerify
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
        if(!SettingUtils.phoneRegex(code)||!SettingUtils.emailRegex(code)){
            return new Message(Message.Type.FAIL);
        }
        User user=userService.findByPhoneOrEmial(code);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        if(SettingUtils.phoneRegex(code)){
          message = noteService.isSend(user.getPhone(), session);
        }else if(SettingUtils.emailRegex(code)){
            emailCache.add(Email.getVerifyCodeLogin(user.getEmail(),session));
            return new Message(Message.Type.OK);
        }
        return message;
    }

   /* 5. email绑定时发送验证码:/user/email/getBindVerify

    参数：email:手机号
    返回：OK:发送成功,FAIL:邮箱不合法，EXIST：邮箱已存在
    */

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
     * 解除公众号与水利云账号的绑定qq
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
     * 忘记密码时修改密码
     *
     * @param object
     * @param session
     * @return
     */
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message changePassword(@RequestBody Object object, HttpSession session) {
        Message message = Message.parameterCheck(object);
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = userService.findByPhone(map.get("phone").toString());
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        Verification verification = (Verification) session
                .getAttribute("verification");
        message = userService.checkCode(map.get("code").toString(), verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        String password = map.get("password").toString();
        user.setPassword(password);
        userService.save(user);
        return message;
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
     * 登陆时发现ip有变化或忘记密码时发送验证码
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
        User user = userService.findByPhone(phone);
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        if (user.getPhone() == null) {
            // 未绑定手机号码
            return new Message(Message.Type.UNKNOWN);
        }
        return noteService.isSend(user.getPhone(), session);
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
        User user = userService.findByPhone(map.get("phone").toString());
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        //判断是否被禁用
        if(user.getLocked()!=null&&user.getLocked()==true){
            return new Message(Message.Type.UNKNOWN);
        }
        Verification verification = (Verification) session
                .getAttribute("verification");
        message = userService.checkCode(verifyIpCode,verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        return subjectLogin(user,"web",null);
    }

    /**
     * 检查该用户名是否被注册
     *
     * @param userName
     * @return
     */
    @RequestMapping(value = "/checkUserName/{userName}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message checkUserName(
            @PathVariable("userName") String userName) {
        Message message = Message.parametersCheck(userName);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = userService.findByUserName(userName);
        if (user == null) {
            return new Message(Message.Type.OK);
        }
        return new Message(Message.Type.EXIST);
    }

    /**
     * 更改手机号码或注册时，需要手机验证
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
        User user = userService.findByPhone(phone);
        if (user != null) {
            // 该手机号已被注册
            return new Message(Message.Type.EXIST);
        }
        //user.setPhone(phone);
        return noteService.isSend(phone, session);
    }

//    /**
//     * 用户注册
//     *
//     * @param objectMap
//     * @return
//     */
//    @RequestMapping(value = "/register", method = RequestMethod.POST)
//    public
//    @ResponseBody
//    Message register(@RequestBody Map<String, String> objectMap,
//                     HttpSession session) {
//        Message message = Message.parameterCheck(objectMap);
//        if (message.getType() == Message.Type.FAIL) {
//            return message;
//        }
//        Map<String, Object> map = (Map<String, Object>) message.getData();
//        Verification verification = (Verification) session
//                .getAttribute("verification");
//        message = userService.registerService(map, verification);
//        return message;
//    }

    /**
     * 修改用户信息
     *
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public
    @ResponseBody
    Message update(@RequestBody Object object) {
        Message message = Message.parameterCheck(object);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        Map<String, Object> map = (Map<String, Object>) message.getData();
        if (!Long.valueOf(map.get("id").toString()).equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        try {
            message = userService.update(map.get("userName").toString(), map.get("name").toString(),
                    map.get("email").toString(), map.get("password").toString());
        } catch (QQSLException e) {
            logger.info(e.getMessage());
            return new Message(Message.Type.FAIL);
        }
        return message;

    }

//    /**
//     * 修改密码
//     * @param map
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
//    public @ResponseBody Message updatePassword(@RequestBody Map<String,Object> map){
//        Message message = Message.parameterCheck(map);
//        if(message.getType().equals(Message.Type.FAIL)){
//            return message;
//        }
//        User user = authentService.getUserFromSubject();
//        String oldPassword = map.get("oldPassword").toString();
//        if(!user.getPassword().equals(oldPassword)){
//            return new Message(Message.Type.UNKNOWN);
//        }
//        String newPassword = map.get("newPassword").toString();
//        message = userService.updatePassword(newPassword,user.getId());
//        return message;
//    }

    /**
     * 修改用户名和邮箱
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    public @ResponseBody Message updateInfo(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        User user = authentService.getUserFromSubject();
        String email,name;
        if(map.get("email")!=null&&StringUtils.hasText( map.get("email").toString())){
            email = map.get("email").toString();
        }else {
            email = user.getEmail();
        }
        if(map.get("name")!=null&&StringUtils.hasText( map.get("name").toString())){
            name = map.get("name").toString();
        }else {
            name = user.getName();
        }
        message = userService.updateInfo(name,email,user.getId());
        return message;
    }

    /**
     * 上传用户头像
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/avatar", method = RequestMethod.POST)
    public
    @ResponseBody
    Message uploadAvatar(
            @RequestBody Map<String, String> avatar) {
        Message message = Message.parameterCheck(avatar);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        User user = authentService.getUserFromSubject();
        user.setAvatar(map.get("avatar").toString());
        userService.save(user);
        return new Message(Message.Type.OK);
    }

    /**
     * 获取用户
     *
     * @return
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
//    //@RequiresPermissions(value = { "webUser:user:query","clientUser:user:query","mobileUser:user:query" }, logical = Logical.OR)
//    public
//    @ResponseBody
//    Message getUser() {
//        User user = authentService.getUserFromSubject();
//        JSONObject json = userService.makeUserJson(user);
//            return new Message(Message.Type.OK, json);
//    }

//    /**
//     * 获取所有用户
//     *
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
//    public
//    @ResponseBody
//    Message getUsers() {
//        User user = authentService.getUserFromSubject();
//        List<JSONObject> userJsons;
//        List<User> users = userService.findUsers(user);
//        userJsons = userService.makeUserJsons(users);
//        return new Message(Message.Type.OK, userJsons);
//    }

    /**
     * 修改手机号码验证短信
     *
     * @param map
     * @param session
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/changePhone", method = RequestMethod.POST)
    public
    @ResponseBody
    Message checkPhoneCode(@RequestBody Map<String,Object> map, HttpSession session) {
        Message message = Message.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        Verification verification = (Verification) session
                .getAttribute("verification");
        String code = map.get("code").toString();
        String newPhone = map.get("phone").toString();
        message = userService.checkCode(code, verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        user.setPhone(newPhone);
        userService.save(user);
        authentService.updateSession(user);
        return message;
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
    @RequestMapping(value = "/updateUserMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message updateUserMessage() {
        User user = authentService.getUserFromSubject();
        List<UserMessage> messages = userMessageService.findByUser(user);
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getStatus().toString()
                    .equals(UserMessage.Status.UNREAD.toString())) {
                messages.get(i).setStatus(UserMessage.Status.READED);
                userMessageService.save(messages.get(i));
            }
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
    @IsAllowCreateAccount
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/invite",method = RequestMethod.GET)
    public @ResponseBody Message invite(@RequestParam String phone) {
        Message message = Message.parametersCheck(phone);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(!SettingUtils.phoneRegex(phone)){
            message = new Message(Message.Type.UNKNOWN);
            return message;
        }
        User user = authentService.getUserFromSubject();
        return  accountService.invite(phone,user);
    }

    /**
     * 企业解绑子账号
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unbind/{id}", method = RequestMethod.GET)
    public @ResponseBody Message unbind(@PathVariable("id") Long id){
        Account account = accountService.find(id);
        if(account==null){
            return new Message(Message.Type.EXIST);
        }
        return userService.unbindAccount(account);
    }
//    使用user缓存，加载用户时，将已认证用户真实姓名从认证表写入用户
//    ?企业认证限制




}

