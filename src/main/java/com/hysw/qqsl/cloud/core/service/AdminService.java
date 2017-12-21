package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.AdminDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Admin;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.element.Position;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
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

//    /**
//     * 用户角色编辑
//     * @param user
//     * @param roles
//     * @return
//     */
//    public Message editRoles(User user,Object roles){
//        //roles为null或空字符串为取消角色
//        if(roles == null||!StringUtils.hasText(roles.toString())){
//           user.setRoles("user:simple");
//        //不为空添加角色
//        }else{
//           String roleStr = "user:simple"+","+roles.toString();
//           List<String> list1 = Arrays.asList(roleStr.split(","));
//           List<String> list2 = new ArrayList<>();
//           for(int i = 0;i<list1.size();i++){
//               if(!list2.contains(list1.get(i))){
//                   list2.add(list1.get(i));
//               }
//           }
//           roleStr = list2.toString().substring(1,list2.toString().length()-1);
//           String role = roleStr.replaceAll(" ", "");
//           user.setRoles(role);
//        }
//        userService.save(user);
//        return new Message(Message.Type.OK);
//    }
//
}
