package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Feedback;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
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
        feedbackService.saveAccountFeedback(17, "aaaa", "bbb", "SUGGEST");
        feedbackService.flush();
        List<Feedback> feedbacks = feedbackService.findByAccountId(17l);
        Assert.assertTrue(feedbacks.size() == 1);
    }

    @Test
    public void testSaveUserFeedback(){
        feedbackService.saveUserFeedback(1,"aaaa","bbb","SUGGEST");
        feedbackService.flush();
        List<Feedback> feedbacks = feedbackService.findByUserId(1l);
        Assert.assertTrue(feedbacks.size() != 0);
        List<Feedback> all = feedbackService.findAll();
        Assert.assertTrue(all.size() != 0);
        Feedback feedback = all.get(0);
        JSONObject jsonObject = feedbackService.toJson(feedback);
        Assert.assertTrue(!jsonObject.isEmpty());
        JSONArray jsonArray = feedbackService.toJsons(all);
        Assert.assertTrue(!jsonArray.isEmpty());
    }
}
