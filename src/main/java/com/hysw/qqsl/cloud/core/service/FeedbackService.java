package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.FeedbackDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Feedback;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 返回service
 * @anthor Administrator
 * @since 14:21 2018/1/2
 */
@Service("feedbackService")
public class FeedbackService extends BaseService<Feedback, Long> {
    @Autowired
    private FeedbackDao feedbackDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    public void setBaseDao(FeedbackDao feedbackDao) {
        super.setBaseDao(feedbackDao);
    }

    /**
     * 反馈列表转Json
     * @param feedbacks 反馈列表
     * @return json列表
     */
    public JSONArray toJsons(List<Feedback> feedbacks) {
        JSONArray jsonArray = new JSONArray();
        for (Feedback feedback : feedbacks) {
            jsonArray.add(toSimpleJson(feedback));
        }
        return jsonArray;
    }

    /**
     * 反馈对象转Json（部分）
     * @param feedback 反馈对象
     * @return json对象
     */
    public JSONObject toSimpleJson(Feedback feedback) {
        JSONObject jsonObject = new JSONObject();
        if (feedback.getAccountId() != 0) {
            Account account = accountService.find(feedback.getAccountId());
            jsonObject.put("account", accountService.makeSimpleAccountJson(account));
        }
//        jsonObject.put("content", feedback.getContent());
//        if (feedback.getReview() != null) {
//            jsonObject.put("review", feedback.getReview());
//        }
//        if (feedback.getReviewDate() != null) {
//            jsonObject.put("reviewDate", feedback.getReviewDate().getTime());
//        }
        jsonObject.put("status", feedback.getStatus());
        jsonObject.put("title", feedback.getTitle());
        jsonObject.put("type", feedback.getType());
        if (feedback.getUserId() != 0) {
            User user = userService.find(feedback.getUserId());
            jsonObject.put("user", userService.makeSimpleUserJson(user));
        }
        jsonObject.put("id", feedback.getId());
        jsonObject.put("createDate", feedback.getCreateDate().getTime());
        return jsonObject;
    }

    /**
     * 反馈对象转Json(全部)
     * @param feedback 反馈对象
     * @return json对象
     */
    public JSONObject toJson(Feedback feedback) {
        JSONObject jsonObject = new JSONObject();
        if (feedback.getAccountId() != 0) {
            Account account = accountService.find(feedback.getAccountId());
            jsonObject.put("account", accountService.makeSimpleAccountJson(account));
        }
        jsonObject.put("content", feedback.getContent());
        if (feedback.getReview() != null) {
            jsonObject.put("review", feedback.getReview());
        }
        if (feedback.getReviewDate() != null) {
            jsonObject.put("reviewDate", feedback.getReviewDate().getTime());
        }
        jsonObject.put("status", feedback.getStatus());
        jsonObject.put("title", feedback.getTitle());
        jsonObject.put("type", feedback.getType());
        if (feedback.getUserId() != 0) {
            User user = userService.find(feedback.getUserId());
            jsonObject.put("user", userService.makeSimpleUserJson(user));
        }
        jsonObject.put("id", feedback.getId());
        jsonObject.put("createDate", feedback.getCreateDate().getTime());
        return jsonObject;
    }

    /**
     * 保存子账户反馈
     * @param objectMap <br/>
     *                  <ol>
     * <li>accountId 子账号id，long</li>
     * <li>title 标题，String</li>
     * <li>content 内容，String</li>
     * <li>type 类型，Feedback.Type</li>
     * </ol>
     * @return <br/>
     * <ol>
     * <li>OK 提交成功</li>
     * <li>FAIL 参数有误</li>
     * </ol>
     */
    public Message saveAccountFeedback(Map<String, Object> objectMap) {
        Object accountId = objectMap.get("accountId");
        Object title = objectMap.get("title");
        Object content = objectMap.get("content");
        Object type = objectMap.get("type");
        if (accountId == null || title == null || content == null || type == null) {
            return new Message(Message.Type.FAIL);
        }
        Feedback feedback = new Feedback();
        feedback.setAccountId(Long.valueOf(accountId.toString()));
        feedback.setContent(content.toString());
        feedback.setTitle(title.toString());
        feedback.setType(Feedback.Type.valueOf(type.toString()));
        feedback.setStatus(CommonEnum.FeedbackStatus.PENDING);
        save(feedback);
        return new Message(Message.Type.OK);
    }

    /**
     * 保存用户反馈
     * @param objectMap <br/>
     *                  <ol>
     * <li>userId 用户id，long</li>
     * <li>title 标题，String</li>
     * <li>content 内容，String</li>
     * <li>type 类型，Feedback.Type</li>
     * </ol>
     * @return <br/>
     * <ol>
     * <li>OK 提交成功</li>
     * <li>FAIL 参数有误</li>
     * </ol>
     */
    public Message saveUserFeedback(Map<String, Object> objectMap) {
        Object userId = objectMap.get("userId");
        Object title = objectMap.get("title");
        Object content = objectMap.get("content");
        Object type = objectMap.get("type");
        if (userId == null || title == null || content == null || type == null) {
            return new Message(Message.Type.FAIL);
        }
        Feedback feedback = new Feedback();
        feedback.setUserId(Long.valueOf(userId.toString()));
        feedback.setContent(content.toString());
        feedback.setTitle(title.toString());
        feedback.setType(Feedback.Type.valueOf(type.toString()));
        feedback.setStatus(CommonEnum.FeedbackStatus.PENDING);
        save(feedback);
        return new Message(Message.Type.OK);
    }

    /**
     * 查询账户下所有列表
     * @param accountId 子账号id
     * @return 该账户下列表
     */
    public List<Feedback> findByAccountId(Long accountId) {
        List<Filter> filters = new LinkedList<>();
        filters.add(Filter.eq("accountId", accountId));
        return feedbackDao.findList(0, null, filters);
    }

    /**
     * 查询账户下所有列表
     * @param userId 用户账号id
     * @return 该账户下列表
     */
    public List<Feedback> findByUserId(Long userId) {
        List<Filter> filters = new LinkedList<>();
        filters.add(Filter.eq("userId", userId));
        return feedbackDao.findList(0, null, filters);
    }
}
