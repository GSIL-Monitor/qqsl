package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Admin;
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
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by leinuo on 16-12-13.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AdminService adminService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private SessionDAO sessionDAO;

    /**
     * 缓存的刷新，包括info.xml;projectModel.xml;elementGroup.xml,
     * 认证缓存,用户缓存,套餐缓存,项目缓存,初始化千寻帐号,为绑定仪表加入
     * 缓存,一周的日志缓存
     * @return message响应消息OK:刷新成功,FIAL:刷新失败
     */
    @RequiresAuthentication
    @RequestMapping(value = "/refreshCache", method = RequestMethod.POST)
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    public
    @ResponseBody
    Message refreshCache() {
        Message message = null;
        try {
            message = projectService.refreshCache();
        } catch (Exception e) {
            message = MessageService.message(Message.Type.FAIL);
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 管理员登录
     * @param objectMap 包含用户名userName,以及动态密码verification
     * @param request 请求消息体
     * @param session 此次请求的session
     * @return message响应消息OK:登录成功,FIAL:信息不全或shiro认证不通过,EXIT:帐号不存在,INVALID:验证码过期,NO_ALLOW:验证码错误
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST,produces="application/json")
    public @ResponseBody Message login(@RequestBody Object objectMap, HttpServletRequest request,HttpSession session){
       /* if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }*/
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String,Object> map = (Map<String,Object>)message.getData();
        Admin admin = adminService.findByUserName(map.get("userName").toString());
        if (admin == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        // 测试状态
        Setting setting = SettingUtils.getInstance().getSetting();
        if(setting.getStatus().equals("dev")){
            return subjectLogin(admin);
        }
        Verification verification = (Verification) session
                .getAttribute("verification");
        String verifyCode = map.get("password").toString();
        message = userService.checkCode(verifyCode,verification);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        // 记录登录的ip
        String ip = request.getRemoteAddr();
        admin.setLoginIp(ip);
        admin.setLoginDate(new Date());
      return subjectLogin(admin);
    };


    /**
     * shiro的登录认证
     * @param admin 当前管理员对象
     * @return message响应消息OK:登录成功,FIAL:shiro认证不通过,EXIT:帐号不存在
     */
    private Message subjectLogin(Admin admin) {
        ShiroToken token = new ShiroToken();
        token.setAdmin(admin);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        } catch (IncorrectCredentialsException e) {
            return MessageService.message(Message.Type.FAIL);
        }
        logger.info(admin.getUserName() + ",管理员端登陆成功");
        admin.setLoginDate(new Date());
        adminService.save(admin);
        subject.getSession().setAttribute("token", subject.getPrincipals().getPrimaryPrincipal());
        JSONObject adminJson = adminService.makeAdminJson(admin);
        return MessageService.message(Message.Type.OK,adminJson);
    }


    /**
     * 管理员登录获取动态密码
     * @param userName 登录凭证userName
     * @param session  此次请求的session
     * @return message响应消息OK:获取成功,FIAL:参数不全,EXIT:帐号不存在,OTHER:未绑定手机
     */
    @RequestMapping(value = "/getPassword",method = RequestMethod.GET,produces="application/json")
    public @ResponseBody  Message getOTP(@RequestParam String userName, HttpSession session){
        if(!StringUtils.hasText(userName)){
            return MessageService.message(Message.Type.FAIL);
        }
        Admin admin = adminService.findByUserName(userName);
        if (admin == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!StringUtils.hasText(admin.getPhone())){
            return MessageService.message(Message.Type.FAIL);
        }
        Message message = noteService.isSend(admin.getPhone(),session);
        return message;
    }

    /**
     * 获取当前管理员对象
     * @return message消息体,包含admin的所有基本信息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getAdmin", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Message getAdmin() {
        Admin admin = authentService.getAdminFromSubject();
        JSONObject adminJson = adminService.makeAdminJson(admin);
        return MessageService.message(Message.Type.OK, adminJson);
    }

    /**
     * 获取所有用户
     * @return message消息体,包含所有用户的具体基本信息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUsers() {
        List<User> users = userService.findAll();
        List<JSONObject> userJsons = userService.makeUserJsons(users);
        return MessageService.message(Message.Type.OK, userJsons);
    }

    /**
     * 获取在线用户
     * @return message消息体,包含所有在线用户的基本信息
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getLandingNumber", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getLandingNumber() {
        Admin admin = authentService.getAdminFromSubject();
        Collection<Session> sessions = sessionDAO.getActiveSessions();
        List<User> users = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        User user;
        Account account;
        for(Session session:sessions){
            user = authentService.getUserFromSession(session);
            account = authentService.getAccountFromSession(session);
            if(user!=null){
                users.add(user);
            }
            if(account!=null){
                accounts.add(account);
            }
        }
        List<JSONObject> userJsons = userService.makeUserJsons(users);
        JSONObject adminJson = adminService.makeAdminJson(admin);
        userJsons.add(adminJson);
        return MessageService.message(Message.Type.OK, userJsons);
    }

    /**
     * 重置密码,管理员重置的密码均为123456
     *
     * @param objectMap 包含用户id
     * @return message消息体,OK:重置成功,EXIST:用户不存在
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message resetPassword(
            @RequestBody Map<String, Object> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        Integer userId = (Integer) map.get("id");
        User user = userService.find(Long.valueOf(userId.toString()));
        if (user == null) {
            // "用户不存在";
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        user.setPassword(DigestUtils.md5Hex("123456"));
        userService.save(user);
        return MessageService.message(Message.Type.OK);
    }

//    /**
//     * 角色分配
//     * @param roleMap
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"admin:simple"})
//    @RequestMapping(value = "/editRoles", method = RequestMethod.POST)
//    public @ResponseBody Message editRoles(@RequestBody Map<String,String> roleMap){
//        Long id = Long.valueOf(roleMap.get("id"));
//        User user = userService.find(id);
//        String roles = roleMap.get("roles");
//        //检查角色是否合法
//        if(roles != null&&StringUtils.hasText(roles.toString())){
//            List<String> realRoles = Arrays.asList(CommonAttributes.ROLES);
//            List<String> roleList = new ArrayList<>();
//            String rolesStr = roles.toString();
//            if(rolesStr.indexOf(",")==-1){
//                roleList.add(rolesStr);
//            }else{
//                roleList = Arrays.asList(rolesStr.split(","));
//            }
//            if(!realRoles.containsAll(roleList)){
//                return MessageService.message(Message.Type.OTHER);
//            }
//        }
//        return adminService.editRoles(user,roles);
//    }

    /**
     * 编辑用户是否禁用
     * @param objectMap 包含用户id,是否禁用的标志isLocked
     * @return message消息体,OK:编辑成功,FAIL:参数不全,EXIST:用户不存在
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/isLocked",method = RequestMethod.POST)
    public @ResponseBody Message Locked(@RequestBody  Map<String,Object> objectMap){
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String,Object> map = (Map<String, Object>) message.getData();
        Long userId = Long.valueOf(map.get("id").toString());
        String isLocked = map.get("isLocked").toString();
        User user = userService.find(userId);
        if(user == null){
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(isLocked.equals("true")){
            user.setLocked(true);
            user.setLockedDate(new Date());
        }else{
            user.setLocked(false);
            user.setLockedDate(null);
        }
        userService.save(user);
        List<User> users = new ArrayList<>();
        users.add(user);
        List<JSONObject> userJsons = userService.makeUserJsons(users);
        return MessageService.message(Message.Type.OK,userJsons.get(0));
    }

    /**
     * 获取平台所有项目
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getAllProjects", method = RequestMethod.GET)
    public @ResponseBody Message getAllProjects(){
        List<JSONObject> projectJsons = projectService.getProjectJsons();
        logger.info("平台项目总数为："+projectJsons.size());
        return MessageService.message(Message.Type.OK,projectJsons);
    }

//    /**
//     *获取当前项目的相关日志
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"admin:simple"})
//    @RequestMapping(value = "/getLogsByProject", method = RequestMethod.GET)
//    public @ResponseBody Message getLogsByProject(@RequestParam long id){
//        List<JSONObject> logJsons = logService.getLogJsonsByProject(id);
//        return MessageService.message(Message.Type.OK,logJsons);
//    }


    /**
     * 发布文章或重新编辑已发布的文章
     * @param map 包含文章类型type,文章内容content,标题title,文章id(id存在表示编辑,不存在表示首次发布)
     * @return message消息体,OK:操作成功,FAIL:参数不全,EXIST:当前编辑的文章不存在
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public
    @ResponseBody
    Message publishAriticle(
            @RequestBody Map<String, String> map) {
        if (!StringUtils.hasText(map.get("type")) ||
                !StringUtils.hasText(map.get("content")) ||
                !StringUtils.hasText(map.get("title"))) {
            return MessageService.message(Message.Type.FAIL);
        }
        String idStr = "";
        if (map.get("id") != null && !map.get("id").equals("null")) {
            idStr = map.get("id").toString();
        }
        int index = Integer.valueOf(map.get("type").toString());
        String content = map.get("content").toString();
        String title = map.get("title").toString();
        // content = articleService.replacePath(content);
        return articleService.save(idStr, title, content, index);
    }

    /**
     * 删除文章
     * @param id 文章标识id
     * @return message消息体,OK:删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteArticle/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Message deletetArticle(@PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        articleService.removeById(id);
        return MessageService.message(Message.Type.OK);

    }

    /**
     * 删除文章
     * @param id 文章标识id
     * @return message消息体,OK:删除成功
     */
    @RequestMapping(value = "/deleteArticle1/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message deletetArticle1(@PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        articleService.removeById(id);
        return MessageService.message(Message.Type.OK);

    }

}
