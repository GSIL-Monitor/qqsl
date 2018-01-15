package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AccountService;
import com.hysw.qqsl.cloud.core.service.UserService;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Create by leinuo on 17-4-28 下午3:00
 *
 * qq:1321404703 https://github.com/leinuo2016
 */
public class AccountServiceTest extends BaseTest{

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Test
    public void getSimpleAccount() throws Exception {

    }

    @Test
    public void invite() throws Exception {

    }

    @Test
    public void register() throws Exception {

    }

    @Test
    public void update() throws Exception {

    }

    @Test
    public void updateInfo() throws Exception {

    }

    @Test
    public void updatePassword() throws Exception {

    }

    @Test
    public void changePhone() throws Exception {

    }

    @Test
    public void findByPhoneOrUserName() throws Exception {

    }

    @Test
    public void findByPhone() throws Exception {

    }

    @Test
    public void makeAccountJson() throws Exception {

    }

    @Test
    public void makeSimpleAccountJson() throws Exception {

    }

    @Test
    public void makeUserJsons() throws Exception {

    }

    @Test
    public void getUsersByAccountId() throws Exception {

    }

    @Test
    public void getAuthenticate() throws Exception {

    }

    @Test
    public void unbindUser() throws Exception {

    }

    @Test
    public void makeAccountJsons() throws Exception {

    }

//    @Test
    public void testRegistAccount() throws Exception{
        String phone = "18661925012";
        String password = DigestUtils.md5Hex("111111");
        User user = userService.findByUserName("qqsl");
        accountService.invite(phone,user);
        Account account = accountService.findByPhone(phone);
        assertNotNull(account.getUsers());
        assertTrue(account.getUsers().get(0).getUserName().equals("qqsl"));
        assertNotNull(account);
    }

    /**
     * 邀请已有子账号
     */
    @Test
    public void testInvite(){
        User user = userService.find(1l);
        Message message = accountService.invite("18661925010", user);
        Assert.assertTrue(message.getType()== Message.Type.EXIST);
    }

    /**
     * 邀请新的子账号
     */
    @Test
    public void testInvite1(){
        User user = userService.find(1l);
        Message message = accountService.invite("18661925011", user);
        Assert.assertTrue(message.getType()== Message.Type.OK);
    }

    /**
     * 注册成功
     */
    @Test
    public void testRegister() {
        String phone = "18661925012";
        String password = DigestUtils.md5Hex("111111");
        String name = "aaa";
        Message message = null;
        try {
            message = accountService.register(name, phone, password);
        } catch (QQSLException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(message.getType()== Message.Type.OK);
        boolean flag = false;
        phone = "186619250121";
        password = DigestUtils.md5Hex("111111");
        name = "aaa";
        try {
            accountService.register(name, phone, password);
        } catch (QQSLException e) {
            flag = true;
        }
        Assert.assertTrue(flag);
        flag = false;
        phone = "18661925012";
        password = DigestUtils.md5Hex("111111")+"1";
        name = "aaa";
        try {
            accountService.register(name, phone, password);
        } catch (QQSLException e) {
            flag = true;
        }
        Assert.assertTrue(flag);
        phone = "18661925010";
        password = DigestUtils.md5Hex("111111");
        name = "aaa";
        try {
            message = accountService.register(name, phone, password);
        } catch (QQSLException e) {
        }
        Assert.assertTrue(message.getType()== Message.Type.EXIST);

    }

    /**
     * 更新信息
     */
    @Test
    public void testUpdate(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name","aaa");
        map.put("email","123456789@qq.com");
        map.put("password",DigestUtils.md5Hex("111111"));
        Message message = accountService.update(map, 1l);
        Assert.assertTrue(message.getType()== Message.Type.OK);
    }

    /**
     * 修改name
     */
    @Test
    public void testUpdateInfo(){
        Message message = accountService.updateInfo("aaa", 1l);
        Assert.assertTrue(message.getType()== Message.Type.OK);
    }

    /**
     * 修改密码
     */
    @Test
    public void testUpdatePassword(){
        Message message = accountService.updatePassword(DigestUtils.md5Hex("111111"), 1l);
        Assert.assertTrue(message.getType()== Message.Type.OK);
        message = accountService.updatePassword(DigestUtils.md5Hex("111111")+"1", 1l);
        Assert.assertTrue(message.getType()== Message.Type.OTHER);
    }

    /**
     * 构建子账号信息
     */
    @Test
    public void testGetAuthenticate() {
        Account account = accountService.find(1l);
        JSONObject jsonObject = accountService.getAuthenticate(account);
        Assert.assertNotNull(jsonObject);
    }

}
