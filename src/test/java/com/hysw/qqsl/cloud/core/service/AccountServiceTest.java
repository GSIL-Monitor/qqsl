package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
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
    @Autowired
    private AccountManager accountManager;

    @Test
    public void testRepeatCreate(){
        User user = userService.findByDao(90l);
        JSONObject jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(!jsonObject.isEmpty());
        jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(null == jsonObject || jsonObject.isEmpty());
        Account account = accountService.findByPhone("13007781310");
        Assert.assertTrue(account != null);
    }

    @Test
    public void testCreateAgree() {
        userService.flush();
        User user = userService.findByDao(90l);
        JSONObject jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(!jsonObject.isEmpty());
        Account account = accountService.findByPhone("13007781310");
        Assert.assertTrue(account.getStatus()==Account.Status.AWAITING);
        accountService.activateAccount(account);
        accountService.flush();
        account = accountService.findByPhoneConfirmed("13007781310");
        Assert.assertTrue(account.getStatus()==Account.Status.CONFIRMED);
        Assert.assertTrue(account.getName().equals("陈雷"));
        accountService.accountUpdate(account.getId(), "刘建斌", null, "无");
        accountService.flush();
        account = accountService.findByPhoneConfirmed("13007781310");
        Assert.assertTrue(account.getName().equals("刘建斌"));
        Assert.assertTrue(account.getRemark().equals("无"));
    }

    @Test
    public void testCreateRefused() {
        User user = userService.findByDao(90l);
        JSONObject jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(!jsonObject.isEmpty());
        Account account = accountService.findByPhone("13007781310");
        Assert.assertTrue(account.getStatus()==Account.Status.AWAITING);
        accountService.refusedAccount(account);
        accountService.flush();
        account = accountService.findByPhone("13007781310");
        Assert.assertTrue(account == null);
    }

    @Test
    public void changeExpired() {
        Account account = new Account();
        account.setId(1l);
        account.setPhone("13513997110");
        User user = new User();
        user.setId(1l);
        account.setUser(user);
        account.setCreateDate(new Date(12345678));
        accountManager.add(account);
        account = new Account();
        account.setId(2l);
        account.setPhone("13513997110");
        user.setId(1l);
        account.setUser(user);
        account.setCreateDate(new Date(12345678));
        accountManager.add(account);
        accountManager.changeExpiredAndDelete();
    }

}
