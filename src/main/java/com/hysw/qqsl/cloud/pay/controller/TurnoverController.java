package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.pay.service.TurnoverService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * 流水控制层
 *
 * @author chenl
 * @create 2017-08-21 下午5:20
 */
@Controller
@RequestMapping("/turnover")
public class TurnoverController {
    @Autowired
    private TurnoverService turnoverService;

    /**
     * 获取流水(管理员)
     * @param begin
     * @param end
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody
    Message lists(@RequestParam("begin") long begin, @RequestParam("end") long end) {
        try {
            Date beginDate = new Date(begin);
            Date endDate = new Date(end);
            return turnoverService.getTurnoverListBetweenDate(beginDate, endDate);
        } catch (Exception e) {
            return MessageService.message(Message.Type.FAIL);
        }
    }

}
