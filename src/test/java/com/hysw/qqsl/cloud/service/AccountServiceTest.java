package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.User;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


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
