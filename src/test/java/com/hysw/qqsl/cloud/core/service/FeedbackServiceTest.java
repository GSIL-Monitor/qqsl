package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.controller.Message;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @anthor Administrator
 * @since 10:40 2018/1/3
 */
public class FeedbackServiceTest extends BaseTest {
    @Autowired
    private FeedbackService feedbackService;

    @Test
    public void testSaveAccountFeedback(){
        Map<String, Object> objectMap = new LinkedHashMap<>();
        objectMap.put("accountId", 17);
        objectMap.put("title", "aaaa");
        objectMap.put("content", "bbb");
        objectMap.put("type", "SUGGEST");
        Message message = feedbackService.saveAccountFeedback(objectMap);
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }

    @Test
    public void testSaveUserFeedback(){
        Map<String, Object> objectMap = new LinkedHashMap<>();
        objectMap.put("userId", 17);
        objectMap.put("title", "aaaa");
        objectMap.put("content", "bbb");
        objectMap.put("type", "SUGGEST");
        Message message = feedbackService.saveUserFeedback(objectMap);
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }
}
