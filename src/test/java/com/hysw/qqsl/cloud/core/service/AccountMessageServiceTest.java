package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.AccountMessage;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @anthor Administrator
 * @since 16:08 2018/1/3
 */
public class AccountMessageServiceTest extends BaseTest {
    @Autowired
    private AccountMessageService accountMessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    @Test
    public void testBindMsessage(){
        User user = userService.findByDao(1l);
        Account account = user.getAccounts().get(0);
        accountMessageService.bindMsessage(user,account,false);
        Assert.assertNotNull(accountMessageService.findByAccount(account));
    }

    @Test
    public void testGetMessage(){
        User user = userService.findByDao(1l);
        Account account = user.getAccounts().get(0);
        Assert.assertNotNull(accountMessageService.getMessage(account));
    }

    @Test
    public void testViewMessage() {
        User user = userService.findByDao(1l);
        Account account = user.getAccounts().get(0);
        Project project = projectService.findByUser(user).get(0);
        accountMessageService.viewMessage(project,account,false);
        Assert.assertNotNull(accountMessageService.findByAccount(account));
    }

    @Test
    public void testCooperate(){
        User user = userService.findByDao(1l);
        Account account = user.getAccounts().get(0);
        Project project = projectService.findByUser(user).get(0);
        accountMessageService.cooperate(CooperateVisit.Type.VISIT_PREPARATION_ELEMENT,project,account,false);
        Assert.assertNotNull(accountMessageService.findByAccount(account));
    }
}
