package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Admin;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by leinuo on 16-12-14.
 */
public class AdminServiceTest extends BaseTest{
    @Test
    public void findByUserName() throws Exception {

    }

    @Test
    public void makeAdminJson() throws Exception {

    }

    @Test
    public void getAuthenticate() throws Exception {

    }

    @Test
    public void getLandingUsers() throws Exception {

    }

    @Test
    public void editRoles() throws Exception {

    }

    @Test
    public void addDiffConnPoll() throws Exception {

    }

    @Test
    public void deteleDiffConnPoll() throws Exception {

    }

    @Test
    public void accountList() throws Exception {

    }

    @Autowired
    private AdminService adminService;
    @Test
    public void testStr(){
        String str = "web,mobile,system,admin";
        str = StringUtils.replaceOnce(str,"web","mobile");
       // org.apache.commons.lang.StringUtils;
        System.out.println(str);
    }

    //添加用户
    //@Test
    public void testSave(){
        Admin admin = new Admin();
        admin.setPhone("18661925010");
        admin.setDepartment("鸿源水务软件研发中心");
        admin.setEmail("123456@qq.com");
        admin.setEnabled(true);
        admin.setLocked(false);
        admin.setLoginDate(new Date());
        admin.setLoginIp("192.168.1.117");
        admin.setName("管理员");
        admin.setRoles("admin");
        admin.setUserName("admin");
        adminService.save(admin);
        Admin admin1 = adminService.find(admin.getId());
        Assert.assertNotNull(admin1);
    }

    @Test
    public void testInstance(){
       Object object = new User();
       Object objecs = new Admin();
       if(objecs instanceof Admin){
           logger.info("true---");
       }else{
           logger.info("false--");
       }
    }

    @Test
    public void testGetAuthenticate(){
        JSONObject jsonObject = adminService.getAuthenticate(adminService.find(1l));
        Assert.assertNotNull(jsonObject);
    }
}
