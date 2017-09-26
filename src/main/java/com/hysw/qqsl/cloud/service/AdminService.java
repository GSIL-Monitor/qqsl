package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.controller.Message;
import com.hysw.qqsl.cloud.dao.AdminDao;
import com.hysw.qqsl.cloud.entity.Filter;
import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.Admin;
import com.hysw.qqsl.cloud.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.entity.element.Position;
import com.hysw.qqsl.cloud.shiro.ShiroToken;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 管理员业务层
 *
 * Created by leinuo on 16-12-13.
 */
@Service("adminService")
public class AdminService extends BaseService<Admin,Long>{
    @Autowired
    private AdminDao adminDao;
    @Autowired
    private SessionDAO sessionDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private DiffConnPollService diffConnPollService;
    @Autowired
    private AuthentService authentService;

    @Autowired
    public void setBaseDao(AdminDao adminDao) {
        super.setBaseDao(adminDao);
    }

    /**
     * 根据用户名查找管理员
     * @param userName
     * @return
     */
    public Admin findByUserName(String userName){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userName", userName));
        List<Admin> admins = adminDao.findList(0, null, filters);
        if (admins.size() == 1) {
            return admins.get(0);
        } else {
            return null;
        }
    }

    public JSONObject makeAdminJson(Admin admin){
        JSONObject adminJson = new JSONObject();
        adminJson.put("id",admin.getId());
        adminJson.put("email",admin.getEmail());
        adminJson.put("name",admin.getName());
        adminJson.put("phone",admin.getPhone());
        adminJson.put("userName",admin.getUserName());
        adminJson.put("roles",admin.getRoles());
        return adminJson;
    }


    /**
     * 为前台构建管理员授权信息
     * @param admin
     * @return
     */
    public JSONObject getAuthenticate(Admin admin) {
        JSONObject subjectJson = new JSONObject();
        JSONObject infoJson = new JSONObject();
        JSONObject authcJson = new JSONObject();
        JSONObject principalJson = new JSONObject();
        List<String> roles = new ArrayList<>();
        principalJson.put("name", admin.getPhone());
        principalJson.put("login", admin.getPhone());
        principalJson.put("email", admin.getEmail());
        roles.add(admin.getRoles());
        authcJson.put("principal", principalJson);
        authcJson.put("credentials", principalJson);
        JSONObject authzJson = new JSONObject();
        List<String> permissions = new ArrayList<>();
        authzJson.put("roles", roles);
        authzJson.put("permissions", permissions);
        infoJson.put("authc", authcJson);
        infoJson.put("authz", authzJson);
        subjectJson.put("info", infoJson);
        return subjectJson;
    }

    /**
     * 获取在线用户(管理员包含在内)
     * @return
     */
    public List<JSONObject> getLandingUsers() {
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
        JSONObject adminJson = makeAdminJson(authentService.getAdminFromSubject());
        userJsons.add(adminJson);
        return userJsons;
    }

    /**
     * 用户角色编辑
     * @param user
     * @param roles
     * @return
     */
    public Message editRoles(User user,Object roles){
        //roles为null或空字符串为取消角色
        if(roles == null||!StringUtils.hasText(roles.toString())){
           user.setRoles("user:simple");
        //不为空添加角色
        }else{
           String roleStr = "user:simple"+","+roles.toString();
           List<String> list1 = Arrays.asList(roleStr.split(","));
           List<String> list2 = new ArrayList<>();
           for(int i = 0;i<list1.size();i++){
               if(!list2.contains(list1.get(i))){
                   list2.add(list1.get(i));
               }
           }
           roleStr = list2.toString().substring(1,list2.toString().length()-1);
           String role = roleStr.replaceAll(" ", "");
           user.setRoles(role);
        }
        userService.save(user);
        return new Message(Message.Type.OK);
    }

    /**
     * 添加账户至差分表+缓存
     * @param object
     * @return
     */
    public Message addDiffConnPoll(Map<String, Object> object) {
        Object userName = object.get("userName");
        Object passWord = object.get("passWord");
        Object timeout = object.get("timeout");
        if (userName == null || passWord == null||timeout==null) {
            return new Message(Message.Type.FAIL);
        }
        try {
            Long.valueOf(timeout.toString());
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        DiffConnPoll diffConnPoll = new DiffConnPoll(userName.toString(),passWord.toString(),Long.valueOf(timeout.toString()));
        diffConnPollService.save(diffConnPoll);
        Position position = new Position(userName.toString(), passWord.toString(), System.currentTimeMillis(),Long.valueOf(timeout.toString()));
        positionService.unuseds.add(position);
        return new Message(Message.Type.OK);
    }

    /**
     * 删除差分账户及缓存数据
     * @param id
     * @return
     */
    public Message deteleDiffConnPoll(Long id) {
        DiffConnPoll diffConnPoll = diffConnPollService.find(id);
        positionService.deleteOneCache(diffConnPoll);
        diffConnPollService.remove(diffConnPoll);
        return new Message(Message.Type.OK);
    }

    /**
     * 显示差分账户列表
     * @return
     */
    public Message accountList() {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < positionService.unuseds.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("userName", positionService.unuseds.get(i).getUserName());
            jsonObject.put("timeout", positionService.unuseds.get(i).getTimeout());
            jsonObject.put("using", false);
            jsonArray.add(jsonArray);
        }
        for (int i = 0; i < positionService.useds.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("userName", positionService.useds.get(i).getUserName());
            jsonObject.put("timeout", positionService.useds.get(i).getTimeout());
            jsonObject.put("using", true);
            jsonArray.add(jsonObject);
        }
        for (int i = 0; i < positionService.timeout.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("userName", positionService.timeout.get(i).getUserName());
            jsonObject.put("timeout", positionService.timeout.get(i).getTimeout());
            jsonObject.put("using", false);
            jsonArray.add(jsonArray);
        }
        return new Message(Message.Type.OK,jsonArray);
    }

//    /**
//     * 更改缓存以及差分表的过期时间
//     * @param object
//     * @return
//     */
//    public Message editAccount(Map<String, Object> object) {
//        Object id = object.get("id");
//        Object timeout = object.get("timeout");
//        if (id == null || timeout == null) {
//            return new Message(Message.Type.FAIL);
//        }
//        DiffConnPoll diffConnPoll = diffConnPollService.find(Long.valueOf(id.toString()));
//        diffConnPoll.setTimeout(Long.valueOf(timeout.toString()));
//        diffConnPollService.save(diffConnPoll);
//        positionService.editPosition(diffConnPoll);
//        return new Message(Message.Type.OK);
//    }
}
