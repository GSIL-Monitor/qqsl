package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AccountService;
import com.hysw.qqsl.cloud.core.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


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

    //@Test
    public void testRegistAccount() throws Exception{
        String phone = "18661925010";
        String password = DigestUtils.md5Hex("111111");
        User user = userService.findByUserName("qqsl");
        accountService.invite(phone,user);
        Account account = accountService.findByPhone(phone);
        assertNotNull(account.getUsers());
        assertTrue(account.getUsers().get(0).getUserName().equals("qqsl"));
        assertNotNull(account);
    }

}
