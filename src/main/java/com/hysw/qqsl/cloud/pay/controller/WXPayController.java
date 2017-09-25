package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.Note;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.NoteCache;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.pay.service.TurnoverService;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayConfigImpl;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayService;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayUtil;
import com.hysw.qqsl.cloud.annotation.util.IsPersonalCertify;
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
    private NoteCache noteCache;

    /**
     * 扫码支付下单
     * @param outTradeNo 订单号
     * @return 二维码URl
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unifiedOrderPay/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message unifiedOrderPay(@PathVariable("outTradeNo") String outTradeNo) {
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
        return wxPayService.unifiedOrderPay(trade);
    }

    /**
     * 退款（仅支持数据服务）
     * @param outTradeNo
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @IsPersonalCertify
    @RequestMapping(value = "/refund/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message refund(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        User user = authentService.getUserFromSubject();
        if (trade.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.UNKNOWN);
        }
        trade.setStatus(Trade.Status.REFUND);
        trade.setRefundDate(new Date());
        tradeService.save(trade);
        turnoverService.writeTurnover(trade);
        return wxPayService.refund(trade);
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
                map.put("total_fee", String.valueOf(trade.getPrice()));
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
//                            //                    短信通知
//                            Note note = new Note(trade.getUser().getPhone(), CommonAttributes.BUY.replace("TYPE",tradeService.convertType(trade)).replace("BASE",tradeService.convertBaseType(trade)));
//                            noteCache.add(trade.getUser().getPhone(),note);
//                            //                    邮件通知
//
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
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        return wxPayService.orderQuery(trade);
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
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Object outTradeNo = objectMap.get("outTradeNo");
        if (outTradeNo == null) {
            return new Message(Message.Type.UNKNOWN);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo.toString());
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        message = wxPayService.orderQuery(trade);
        JSONObject jsonObject = JSONObject.fromObject(message.getData());
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
                return new Message(Message.Type.OK);
            } else if (trade.getStatus() == Trade.Status.REFUND) {
                return wxPayService.refund(trade);
            }
        }
        return new Message(Message.Type.FAIL);
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
