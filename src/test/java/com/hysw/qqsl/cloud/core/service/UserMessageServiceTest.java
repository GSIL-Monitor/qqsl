package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.Feedback;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.data.UserMessage;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @anthor Administrator
 * @since 10:44 2018/1/16
 */
public class UserMessageServiceTest extends BaseTest {
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private StationService stationService;
    @Autowired
    private CertifyService certifyService;

    /**
     * 测试企业解绑子账户
     */
    @Test
    public void testUnbindMessage(){
        userMessageService.unbindMessage(accountService.find(1l), userService.find(1l));
        User user = userService.find(1l);
        Assert.assertTrue(!user.getAccounts().contains(accountService.find(1l)));
    }

    @Test
    public void testShareMessage(){
        userMessageService.shareMessage(projectService.find(848l), userService.find(1l), true);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.SHARE_PROJECT);
    }

    @Test
    public void testStationShareMessage(){
        userMessageService.stationShareMessage(stationService.find(1l), userService.find(1l), true);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.SHARE_STATION);
    }

    @Test
    public void testPersonalCertifyFail(){
        Certify certify = certifyService.find(1l);
        userMessageService.personalCertifyFail(certify);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.CERTIFY);
    }

    @Test
    public void testCompanyCertifyFail(){
        Certify certify = certifyService.find(1l);
        userMessageService.companyCertifyFail(certify);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.CERTIFY);
    }

    @Test
    public void testPersonalCertifySuccess(){
        Certify certify = certifyService.find(1l);
        userMessageService.personalCertifySuccess(certify);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.CERTIFY);
    }

    @Test
    public void testCompanyCertifySuccess(){
        Certify certify = certifyService.find(1l);
        userMessageService.companyCertifySuccess(certify);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.CERTIFY);
    }

    @Test
    public void testBuyPackage(){
        userMessageService.buyPackage(userService.find(1l),"aaa");
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.BUY_PACKAGE);
    }

    @Test
    public void testBuyStation(){
        userMessageService.buyStation(userService.find(1l),"aaa");
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.BUY_STATION);
    }

    @Test
    public void testFeedbackMessage(){
        Feedback feedback = new Feedback();
        feedback.setTitle("aaa");
        feedback.setContent("bbb");
        feedback.setType(Feedback.Type.SUGGEST);
        feedback.setUserId(1l);
        userMessageService.feedbackMessage(feedback);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()==1);
        Assert.assertTrue(userMessages.get(0).getType()==UserMessage.Type.FEEDBACK);
    }
}
