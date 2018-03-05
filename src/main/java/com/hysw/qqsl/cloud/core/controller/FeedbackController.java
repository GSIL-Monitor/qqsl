package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Feedback;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.FeedbackService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import net.sf.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 反馈控制层
 *
 * @since 10.0，2017-12-25
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
@Controller
@RequestMapping("/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private AuthentService authentService;

    private Log logger = LogFactory.getLog(getClass());

    /**
     * 管理员取得所有反馈列表
     *
     * @return Message 返回数据封装，有type，data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 取得成功，data属性是所有反馈的列表，列表中不需包含反馈内容(content)，
     * 回复字段(review)</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody
    Message getFeedbacks() {
        List<Feedback> feedbacks = feedbackService.findAll();
        JSONArray jsonArray=feedbackService.toJsons(feedbacks);
        return MessageService.message(Message.Type.OK,jsonArray);
    }

    /**
     * 管理员回复，回复成功后，保存回复内容，反馈状态改为已回复(CommonEnum.FeedbackStatus.REVIEWED),
     * 生成一条新的userMessage或accountMessage，内容是：您的"xx"反馈管理员已经回复，请前往查看
     *
     * @param objectMap 以map形式封装的参数，key是字符串，value是任意对象
     * <ol>
     * <li>feedbackId 反馈id，long</li>
     * <li>review 回复内容, String</li>
     * </ol>
     * @return Message 返回数据封装，有type,data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 回复成功</li>
     * <li>FAIL 参数有误</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/review", method = RequestMethod.POST)
    public @ResponseBody
    Message doAdminReview(@RequestBody Map<String, Object> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return feedbackService.doAdminReview(objectMap);
    }

    /**
     * 用户取得自己的反馈列表
     * @return Message 返回数据封装，有type，data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 取得成功，data属性是反馈的列表，列表中不需包含反馈内容(content)，
     * 回复字段(review)</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/user/lists", method = RequestMethod.GET)
    public @ResponseBody Message getUserFeedbacks() {
        User user = authentService.getUserFromSubject();
        List<Feedback> feedbacks = feedbackService.findByUserId(user.getId());
        return MessageService.message(Message.Type.OK,feedbackService.toJsons(feedbacks));
    }

    /**
     * 子账号取得自己的反馈列表
     * @return Message 返回数据封装，有type,data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 取得成功，data属性是反馈的列表，列表中不需包含反馈内容(content)，
     * 回复字段(review)</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/account/lists", method = RequestMethod.GET)
    public @ResponseBody Message getAccountFeedbacks() {
        Account account = authentService.getAccountFromSubject();
        List<Feedback> feedbacks=feedbackService.findByAccountId(account.getId());
        return MessageService.message(Message.Type.OK,feedbackService.toJsons(feedbacks));
    }

    /**
     * 用户提交反馈
     *
     * @param objectMap 以map形式封装的参数，key是字符串，value是任意对象
     * <ol>
     * <li>userId 用户id，long</li>
     * <li>title 标题，String</li>
     * <li>content 内容，String</li>
     * <li>type 类型，Feedback.Type</li>
     * </ol>
     * 新提交的反馈状态(status)是待处理状态(CommonEnum.FeedbackStatus.PENDING)
     * @return Message 返回数据封装，有type,data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 提交成功</li>
     * <li>FAIL 参数有误</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/user/submit", method = RequestMethod.POST)
    public @ResponseBody
    Message saveUserFeedback(@RequestBody Map<String, Object> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return feedbackService.saveUserFeedback(objectMap);
    }

    /**
     * 子账号提交反馈
     *
     * @param objectMap 以map形式封装的参数，key是字符串，value是任意对象
     * <ol>
     * <li>accountId 子账号id，long</li>
     * <li>title 标题，String</li>
     * <li>content 内容，String</li>
     * <li>type 类型，Feedback.Type</li>
     * </ol>
     * 新提交的反馈状态(status)是CommonEnum.Review PENDING状态
     * @return Message 返回数据封装，有type,data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 提交成功</li>
     * <li>FAIL 参数有误</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/account/submit", method = RequestMethod.POST)
    public @ResponseBody
    Message saveAccountFeedback(@RequestBody Map<String, Object> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return feedbackService.saveAccountFeedback(objectMap);
    }

    /**
     * 获取反馈详情
     * @param id 反馈id
     * @return Message 返回数据封装，有type,data属性。其中type属性是返回标识
     * <ol>
     * <li>OK 取得成功，data属性是反馈详情，反馈的所有属性</li>
     * <li>UNKNOWN 未知错误</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple","user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/feedback/{id}", method = RequestMethod.GET)
    public @ResponseBody Message get(@PathVariable("id") long id) {
        Feedback feedback;
        try {
            feedback = feedbackService.find(id);
        } catch (Exception e) {
            return MessageService.message(Message.Type.UNKNOWN);
        }
        return MessageService.message(Message.Type.OK,feedbackService.toJson(feedback));
    }

}

