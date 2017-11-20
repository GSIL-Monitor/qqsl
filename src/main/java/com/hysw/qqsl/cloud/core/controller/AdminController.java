package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.Verification;
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
    private LogService logService;
    @Autowired
    private AuthentService authentService;

    /**
     * 缓存的刷新，包括info.xml;projectModel.xml;elementGroup.xml
     *
     * @return
     */
    @RequiresAuthentication
    @RequestMapping(value = "/refreshCache", method = RequestMethod.POST)
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    public
    @ResponseBody
    Message refreshCache() {
        Message message = projectService.refreshCache();
        return message;
    }

    /**
     * 管理员登录
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST,produces="application/json")
    public @ResponseBody Object login(@RequestBody Object objectMap, HttpServletRequest request,HttpSession session){
       /* if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }*/
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map = (Map<String,Object>)message.getData();
        Admin admin = adminService.findByUserName(map.get("userName").toString());
        if (admin == null) {
            return new Message(Message.Type.EXIST);
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
        if (message.getType()!=Message.Type.OK) {
            return message;
        }
        // 记录登录的ip
        String ip = request.getRemoteAddr();
        admin.setLoginIp(ip);
        admin.setLoginDate(new Date());
      return subjectLogin(admin);
    };

    /**
     * 登陆
     *
     * @param admin
     * @return
     */
    private Object subjectLogin(Admin admin) {
        ShiroToken token = new ShiroToken();
        token.setAdmin(admin);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            return new Message(Message.Type.EXIST);
        } catch (IncorrectCredentialsException e) {
            return new Message(Message.Type.FAIL);
        }
        logger.info(admin.getUserName() + ",管理员端登陆成功");
        admin.setLoginDate(new Date());
        adminService.save(admin);
        subject.getSession().setAttribute("token", subject.getPrincipals().getPrimaryPrincipal());
        JSONObject adminJson = adminService.makeAdminJson(admin);
        return new Message(Message.Type.OK,adminJson);
    }

    /**
     * 管理员登录获取动态密码
     * @param session
     * @return
     */
    @RequestMapping(value = "/getPassword",method = RequestMethod.GET,produces="application/json")
    public @ResponseBody  Message getOTP(@RequestParam String userName, HttpSession session){
        if(!StringUtils.hasText(userName)){
            return new Message(Message.Type.FAIL);
        }
        Admin admin = adminService.findByUserName(userName);
        if(admin==null){
            return new Message(Message.Type.EXIST);
        }
        if(!StringUtils.hasText(admin.getPhone())){
            return new Message(Message.Type.OTHER);
        }
        Message message = noteService.isSend(admin.getPhone(),session);
        return message;
    }

    /**
     * 获取当前管理员对象
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getAdmin", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Message getAdmin() {
        Admin admin = authentService.getAdminFromSubject();
        JSONObject adminJson = adminService.makeAdminJson(admin);
        return new Message(Message.Type.OK, adminJson);
    }

    /**
     * 获取所有用户
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUsers() {
        List<User> users = userService.findAll();
        List<JSONObject> userJsons = userService.makeUserJsons(users);
        return new Message(Message.Type.OK, userJsons);
    }

    /**
     * 获取在线用户
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getLandingNumber", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getLandingNumber() {
        List<JSONObject> userJsons = adminService.getLandingUsers();
        return new Message(Message.Type.OK, userJsons);
    }

    /**
     * 重置密码
     *
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Message resetPassword(
            @RequestBody Map<String, Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        Integer userId = (Integer) map.get("id");
        User user = userService.find(Long.valueOf(userId.toString()));
        if (user == null) {
            // "用户不存在";
            return new Message(Message.Type.EXIST);
        }
        user.setPassword(DigestUtils.md5Hex("123456"));
        userService.save(user);
        return new Message(Message.Type.OK);
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
//                return new Message(Message.Type.OTHER);
//            }
//        }
//        return adminService.editRoles(user,roles);
//    }

    /**
     * 编辑用户是否禁用
     * @param object
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/isLocked",method = RequestMethod.POST)
    public @ResponseBody Message Locked(@RequestBody  Map<String,Object> object){
        Message message = Message.parameterCheck(object);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map = (Map<String, Object>) message.getData();
        Long userId = Long.valueOf(map.get("id").toString());
        String isLocked = map.get("isLocked").toString();
        User user = userService.find(userId);
        if(user == null){
            return new Message(Message.Type.EXIST);
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
        return new Message(Message.Type.OK,userJsons.get(0));
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
        return new Message(Message.Type.OK,projectJsons);
    }

    /**
     *获取当前项目的相关日志
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/getLogsByProject", method = RequestMethod.GET)
    public @ResponseBody Message getLogsByProject(@RequestParam long id){
        List<JSONObject> logJsons = logService.getLogJsonsByProject(id);
        return new Message(Message.Type.OK,logJsons);
    }

}
