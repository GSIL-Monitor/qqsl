package com.hysw.qqsl.cloud.core.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 问题建议-反馈控制层
 *
 * @since 2017年12月25日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private Log logger = LogFactory.getLog(getClass());

    /**
     * 管理员取得所有反馈
     *
     * @return Message,有type,data属性。其中type属性是返回标识: </br>
     * <ol>
     * <li>
     * OK:取得成功，data属性是所有反馈的列表
     * </li>
     * <li>
     * UNKNOWN:未知错误
     * </li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message getFeedbacks() {
        return new Message(Message.Type.UNKNOWN);
    }

    /**
     * 用户取得自己的反馈列表
     *
     * @param userId:用户id
     * @return Message,有type,data属性。其中type属性是返回标识: </br>
     * <ol>
     * <li>
     * OK:取得成功，data属性是用户所有反馈的列表
     * </li>
     * <li>
     * UNKNOWN:未知错误
     * </li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/user/lists", method = RequestMethod.GET)
    public @ResponseBody Message getUserFeedbacks(long userId) {
        return new Message(Message.Type.UNKNOWN);
    }

    /**
     * 子账号取得自己的反馈列表
     *
     * @param accountId: 子账号id
     * @return Message,有type,data属性。其中type属性是返回标识: </br>
     * <ol>
     * <li>
     * OK:取得成功，data属性是子账号所有反馈的列表
     * </li>
     * <li>
     * UNKNOWN:未知错误
     * </li>
     * </ol>
     */
    public Message getAccountFeedbacks(long accountId) {
        return new Message(Message.Type.UNKNOWN);
    }
}

