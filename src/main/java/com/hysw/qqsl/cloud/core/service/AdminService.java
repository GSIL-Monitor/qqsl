package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.AdminDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Admin;
import net.sf.json.JSONObject;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
