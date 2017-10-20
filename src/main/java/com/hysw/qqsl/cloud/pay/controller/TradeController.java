package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.annotation.util.*;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单controller
 */
@Controller
@RequestMapping("/trade")
public class TradeController {
    @Autowired
    private AuthentService authentService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private WXPayService wxPayService;

    /**
     * 生成订单--首购--套餐
     *
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify"}, logical = Logical.OR)
    @IsHaveTradeNoPay

    @RequestMapping(value = "/createPackage", method = RequestMethod.POST)
    public @ResponseBody
    Message createPackage(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return tradeService.createPackageTrade(objectMap, user);
    }

    /**
     * 生成订单--首购--测站
     *
     * @param objectMap
     * @return
     */
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/createStation", method = RequestMethod.POST)
    public @ResponseBody
    Message createStation(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return tradeService.createStationTrade(objectMap, user);
    }

    /**
     * 生成订单--首购--数据服务
     *
     * @param objectMap
     * @return
     */
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/createGoods", method = RequestMethod.POST)
    public @ResponseBody
    Message createGoods(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return tradeService.createGoodsTrade(objectMap, user);
    }

    /**
     * 套餐续费
     *
     * @param objectMap
     * @return
     */
    @IsExpire
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/renewPackage", method = RequestMethod.POST)
    public @ResponseBody
    Message renewPackage(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Object instanceId = objectMap.get("instanceId");
        if (instanceId == null) {
            return new Message(Message.Type.FAIL);
        }
        return tradeService.renewPackageTrade(instanceId.toString(), user);
    }

    /**
     * 测站续费
     *
     * @param objectMap
     * @return
     */
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/renewStation", method = RequestMethod.POST)
    public @ResponseBody Message renewStation(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Object instanceId = objectMap.get("instanceId");
        if (instanceId == null) {
            return new Message(Message.Type.FAIL);
        }
        return tradeService.renewStationTrade(instanceId.toString(), user);
    }

    /**
     * 套餐升级
     *
     * @param objectMap
     * @return
     */
    @IsExpire
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePackage", method = RequestMethod.POST)
    public @ResponseBody
    Message updatePackage(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return tradeService.updatePackageTrade(objectMap, user);
    }

    /**
     * 获取订单列表(不包含已删除订单)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public @ResponseBody Message getTradeList() {
        User user = authentService.getUserFromSubject();
        List<Trade> trades = tradeService.findByUser(user);
        return new Message(Message.Type.OK, tradeService.tradesToJson(trades));
    }

    /**
     * 获取订单详情(用户)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/trade/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message getTrade(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return new Message(Message.Type.FAIL);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.UNKNOWN);
        }
        return new Message(Message.Type.OK,tradeService.tradeToJson(trade));
    }

    /**
     * 获取订单详情(管理员)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/trade/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message getTradeAdmin(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return new Message(Message.Type.FAIL);
        }
        return new Message(Message.Type.OK,tradeService.tradeToJson(trade));
    }

    /**
     * 删除订单
     * @param outTradeNo
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/remove/{outTradeNo}", method = RequestMethod.POST)
    public @ResponseBody Message detele(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.UNKNOWN);
        }
        if (trade.getStatus() == Trade.Status.NOPAY) {
            return new Message(Message.Type.NO_ALLOW);
        }
        trade.setDeleteStatus(true);
        tradeService.save(trade);
        return new Message(Message.Type.OK);
    }

//    /**
//     * 计算套餐差价
//     * @param instanceId
//     * @param packageType
//     * @return
//     */
//    @RequestMapping(value = "/getPackageDiff", method = RequestMethod.GET)
//    public @ResponseBody Message getPackageDiff(@RequestParam("instanceId") Object instanceId,@RequestParam("packageType") Object packageType) {
//        return tradeService.getPackageDiff(instanceId,packageType);
//    }

    /**
     * 关闭订单
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public @ResponseBody Message close(@RequestBody Map<String, Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Object outTradeNo = objectMap.get("outTradeNo");
        if (outTradeNo == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo.toString());
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.UNKNOWN);
        }
        if (trade.getStatus() != Trade.Status.NOPAY) {
            return new Message(Message.Type.NO_ALLOW);
        }
        trade.setStatus(Trade.Status.CLOSE);
        tradeService.save(trade);
        wxPayService.orderClose(trade);
        return new Message(Message.Type.OK);
    }



//    ?套餐设定等级，升级套餐时验证套餐等级，降级购买时，验证是否符合降级要求


}
