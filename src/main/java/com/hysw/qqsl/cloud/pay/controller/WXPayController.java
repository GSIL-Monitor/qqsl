package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.core.controller.CommonController;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.CommonService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.pay.service.TurnoverService;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayConfigImpl;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayService;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayUtil;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * 微信支付控制层
 */
@Controller
@RequestMapping("/wxPay")
public class WXPayController {
    @Autowired
    private AuthentService authentService;
    @Autowired
    private WXPayConfigImpl wxPayConfig;
    @Autowired
    private WXPayService wxPayService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private TurnoverService turnoverService;
    @Autowired
    private CommonService commonService;

    /**
     * 扫码支付下单
     * @param outTradeNo 订单号
     * @return 二维码URl
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/unifiedOrderPay/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message unifiedOrderPay(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (System.currentTimeMillis()-trade.getCreateDate().getTime()>2*60*60*1000) {
            return MessageService.message(Message.Type.TRADE_EXPIRED);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        JSONObject jsonObject = wxPayService.unifiedOrderPay(trade);
        if (jsonObject == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, jsonObject);
    }

    /**
     * 退款（仅支持数据服务）
     * @param outTradeNo
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/refund/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message refund(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (trade.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        trade.setStatus(Trade.Status.REFUND);
        trade.setRefundDate(new Date());
        tradeService.save(trade);
        turnoverService.writeTurnover(trade);
        if (wxPayService.refund(trade)) {
            return MessageService.message(Message.Type.OK);
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 支付完成通知
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/payNotice", method = RequestMethod.POST)
    public @ResponseBody String payNotice(HttpServletRequest request) throws Exception {
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        String result = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> map = WXPayUtil.xmlToMap(result);
        try {
            if (map.get("result_code").equals("SUCCESS") && map.get("return_code").equals("SUCCESS")) {
                Trade trade = tradeService.findByOutTradeNo(map.get("out_trade_no"));
                if (trade.getStatus() == Trade.Status.PAY) {
                    return "<xml>\n" +
                            "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                            "</xml>";
                }
                map.put("total_fee", String.valueOf(Math.round(trade.getPrice() * 100)));
                if (WXPayUtil.isSignatureValid(WXPayUtil.mapToXml(map), wxPayConfig.getKey())) {
                    trade.setPayDate(new Date());
                    trade.setPayType(Trade.PayType.WX);
                    trade.setStatus(Trade.Status.PAY);
                    tradeService.save(trade);
//                激活业务
                    new Thread(){
                        public void run(){
                            turnoverService.writeTurnover(trade);
                            tradeService.activateServe(trade);
                            commonService.sendMessage(trade);
                        }
                    }.start();
                    return "<xml>\n" +
                            "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                            "</xml>";
                }else{
                    return "<xml>\n" +
                            "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                            "</xml>";
                }

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "<xml>\n" +
                    "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                    "</xml>";
        }
        return "<xml>\n" +
                "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                "</xml>";
    }

    /**
     * 根据订单号查询订单支付情况(管理员)
     * @param outTradeNo
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/queryTrade/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message orderQuery(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null||outTradeNo.equals("")) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = wxPayService.orderQuery(trade);
        if (jsonObject == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, jsonObject);
    }

    /**
     * 订单状态变更(管理员)
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/refreshTrade", method = RequestMethod.POST)
    public @ResponseBody
    Message refreshOrder(@RequestBody Map<String, Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object outTradeNo = objectMap.get("outTradeNo");
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo.toString());
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = wxPayService.orderQuery(trade);
        String tradeState = jsonObject.get("tradeState").toString();
        if (tradeState.equalsIgnoreCase("SUCCESS")) {
            if (trade.getStatus() == Trade.Status.NOPAY) {
                trade.setPayDate(new Date());
                trade.setPayType(Trade.PayType.WX);
                trade.setStatus(Trade.Status.PAY);
                tradeService.save(trade);
//                激活业务
                new Thread(){
                    public void run(){
                        turnoverService.writeTurnover(trade);
                        tradeService.activateServe(trade);
                    }
                }.start();
                return MessageService.message(Message.Type.OK);
            } else if (trade.getStatus() == Trade.Status.REFUND) {
                if (wxPayService.refund(trade)) {
                    return MessageService.message(Message.Type.OK);
                }
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.FAIL);
    }

//    /**
//     * 退款完成通知
//     * @param request
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping(value = "/refundNotice", method = RequestMethod.POST)
//    public @ResponseBody String refundNotice(HttpServletRequest request) throws Exception {
//        InputStream inStream = request.getInputStream();
//        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int len = 0;
//        while ((len = inStream.read(buffer)) != -1) {
//            outSteam.write(buffer, 0, len);
//        }
//        outSteam.close();
//        inStream.close();
//        String result = new String(outSteam.toByteArray(), "utf-8");
//        Map<String, String> map = WXPayUtil.xmlToMap(result);
//        if (map.get("return_code").equals("SUCCESS")) {
//            Map<String, String> decryptRefundTrade = tradeService.decryptRefundTrade(map.get("req_info"));
//            if (decryptRefundTrade.get("refund_status").equals("SUCCESS")) {
//                Trade trade = tradeService.findByOutTradeNo(decryptRefundTrade.get("out_trade_no"));
//                trade.setStatus(Trade.Status.REFUND);
//                trade.setRefundDate(new Date());
//                tradeService.save(trade);
//                final Trade trade1 = trade;
//                new Thread(){
//                    public void run(){
//                        turnoverService.writeTurnover(trade1);
//                    }
//                }.start();
//                return "<xml>\n" +
//                        "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
//                        "</xml>";
//            }
//        }
//        return "<xml>\n" +
//                "  <return_code><![CDATA[FAIL]]></return_code>\n" +
//                "</xml>";
//    }
}
