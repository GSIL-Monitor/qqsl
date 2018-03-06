package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Polling;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @anthor Administrator
 * @since 17:16 2018/1/17
 */
public class PollingServiceTest extends BaseTest {
    @Autowired
    private PollingService pollingService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;

    @Test
    public void testChangeMessageStatus(){
        User user = userService.find(1l);
        pollingService.changeMessageStatus(user, true);
        Polling polling = pollingService.findByUser(user.getId());
        Assert.assertTrue(polling.isMessageStatus());
        Account account = accountService.find(1l);
        pollingService.changeMessageStatus(account, true);
        polling = pollingService.findByUser(account.getId());
        Assert.assertTrue(polling.isMessageStatus());
    }

    @Test
    public void testChangeCooperateStatus(){
        User user = userService.find(1l);
        pollingService.changeCooperateStatus(user, true);
        Polling polling = pollingService.findByUser(user.getId());
        Assert.assertTrue(polling.isMessageStatus());
        Account account = accountService.find(1l);
        pollingService.changeCooperateStatus(account, true);
        polling = pollingService.findByUser(account.getId());
        Assert.assertTrue(polling.isMessageStatus());
    }

    @Test
    public void testChangeShareStatus(){
        User user = userService.find(1l);
        pollingService.changeShareStatus(user, true);
        Polling polling = pollingService.findByUser(user.getId());
        Assert.assertTrue(polling.isMessageStatus());
        Account account = accountService.find(1l);
        pollingService.changeShareStatus(account, true);
        polling = pollingService.findByUser(account.getId());
        Assert.assertTrue(polling.isMessageStatus());
    }

    @Test
    public void testChangeStationStatus(){
        User user = userService.find(1l);
        pollingService.changeStationStatus(user, true);
        Polling polling = pollingService.findByUser(user.getId());
        Assert.assertTrue(polling.isMessageStatus());
        Account account = accountService.find(1l);
        pollingService.changeStationStatus(account, true);
        polling = pollingService.findByUser(account.getId());
        Assert.assertTrue(polling.isMessageStatus());
    }


}
