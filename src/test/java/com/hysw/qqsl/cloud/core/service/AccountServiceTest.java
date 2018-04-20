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

    @Test
    public void testRepeatCreate(){
        User user = userService.find(89l);
        JSONObject jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(!jsonObject.isEmpty());
        jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(null == jsonObject || jsonObject.isEmpty());
        List<Account> accounts = accountService.findByPhone("13007781310");
        Assert.assertTrue(accounts.size() == 1);
    }

    @Test
    public void testCreateAgree() {
        User user = userService.find(89l);
        JSONObject jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(!jsonObject.isEmpty());
        List<Account> accounts = accountService.findByPhone("13007781310");
        Assert.assertTrue(accounts.get(0).getStatus()==Account.Status.AWAITING);
        accountService.activateAccount(accounts,accounts.get(0).getId().toString());
        accountService.flush();
        Account account = accountService.findByPhoneConfirmed("13007781310");
        Assert.assertTrue(account.getStatus()==Account.Status.CONFIRMED);
        Assert.assertTrue(account.getName().equals("陈雷"));
        accountService.accountUpdate(account.getId(), "刘建斌", null, "无");
        accountService.flush();
        account = accountService.findByPhoneConfirmed("13007781310");
        Assert.assertTrue(account.getName().equals("刘建斌"));
    }

    @Test
    public void testCreateRefused() {
        User user = userService.find(89l);
        JSONObject jsonObject = accountService.create("13007781310", user, "陈雷", null, null);
        accountService.flush();
        Assert.assertTrue(!jsonObject.isEmpty());
        List<Account> accounts = accountService.findByPhone("13007781310");
        Assert.assertTrue(accounts.get(0).getStatus()==Account.Status.AWAITING);
        accountService.refusedAccount(accounts,accounts.get(0).getId().toString());
        accountService.flush();
        accounts = accountService.findByPhone("13007781310");
        Assert.assertTrue(accounts.size() == 0);
    }

}
