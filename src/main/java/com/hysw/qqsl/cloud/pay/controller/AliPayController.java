package com.hysw.qqsl.cloud.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.pay.service.TurnoverService;
import com.hysw.qqsl.cloud.pay.service.aliPay.AliPayService;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Create by leinuo on 17-7-17 下午4:56
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 * <p>
 * 支付宝支付控制层
 */
@Controller
@RequestMapping("/aliPay")
public class AliPayController {
    @Autowired
    private AuthentService authentService;
    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private TurnoverService turnoverService;

    //private static final String RETURN_URL = "http://4107ce0a.all123.net/qqsl.web/tpls/productModule/paySuccess.html";
    private static final String RETURN_URL = "http://localhost:3000/tpls/productModule/paySuccess.html";
    private static final String NOTIFY_URL = "http://5007c0d2.nat123.cc/qqsl/aliPay/notify";


//    //手机网站支付
//    @RequestMapping(value = "/phonePay/{out_trade_no}", method = RequestMethod.GET)
//    public
//    @ResponseBody
//    void pay(@PathVariable("out_trade_no") String out_trade_no, HttpServletResponse httpResponse) throws ServletException, IOException {
//        AlipayClient alipayClient = aliPayService.getAlipayClient();
//        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        //支付完成跳转到指定浏览器页面,每个产品购买支付完成都有特定的跳转页面
//        alipayRequest.setReturnUrl(RETURN_URL);
//        //异步回调地址
//        alipayRequest.setNotifyUrl(NOTIFY_URL);//在公共参数中设置回跳和通知地址
//        Trade trade = tradeService.findByOutTradeNo(out_trade_no);
//        if (trade == null) {
//            return;
//        }
//        User user = authentService.getUserFromSubject();
//        if (!trade.getUser().getId().equals(user.getId())) {
//            return;
//        }
//        String type = tradeService.convertType(trade);
//        alipayRequest.setBizContent("{" +
//                " \"out_trade_no\":" + trade.getOutTradeNo() + "," +
//                " \"total_amount\":" + String.valueOf(Double.valueOf(trade.getPrice() / 100)) + "," +
//                " \"subject\":" + type + "," +
//                " \"product_code\":" + trade.getBaseType().ordinal() +
//                " }");//填充业务参数
//        String form = "";
//        try {
//            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        httpResponse.setContentType("text/html;charset=" + CommonAttributes.CHARSET);
//        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
//        httpResponse.getWriter().flush();
//        httpResponse.getWriter().close();
//    }

    /**
     * 支付结果异步回调
     *
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     * @throws AlipayApiException
     */
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public
    @ResponseBody
    String notify(HttpServletRequest request) throws ServletException, IOException, AlipayApiException {
        Map<String, String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        requestParams.get("trade_status");
        String tradeNo = request.getParameter("out_trade_no");
        Trade trade = tradeService.findByOutTradeNo(tradeNo);
        params.put("total_amount", String.valueOf(Double.valueOf(trade.getPrice() / 100)));
        String tradeStatus = request.getParameter("trade_status");
        boolean signVerified = AlipaySignature.rsaCheckV1(params, CommonAttributes.ALIPAY_PUBLIC_KEY, CommonAttributes.CHARSET, CommonAttributes.SIGN_TYPE); //调用SDK验证签名
        if (signVerified) {
            if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                trade.setPayDate(new Date());
                trade.setPayType(Trade.PayType.ALI);
                trade.setStatus(Trade.Status.PAY);
                tradeService.save(trade);
//                激活业务
                new Thread() {
                    public void run() {
                        turnoverService.writeTurnover(trade);
                        tradeService.activateServe(trade);
                    }
                }.start();
                return "success";
            } /*else if (){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            }*/

            //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
//            return "success";    //请不要修改或删除
            //////////////////////////////////////////////////////////////////////////////////////////
        } else {//验证失败
            return "failure";
        }
        return "failure";
    }

    /**
     * 电脑网站支付   20150320010101050
     *
     * @param out_trade_no
     * @param httpResponse
     * @throws ServletException
     * @throws IOException
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/pcPay/{out_trade_no}", method = RequestMethod.GET)
    public void doPost(@PathVariable("out_trade_no") String out_trade_no,
                       HttpServletResponse httpResponse) throws ServletException, IOException {
        AlipayClient alipayClient = aliPayService.getAlipayClient();
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);//在公共参数中设置回跳和通知地址
        Trade trade = tradeService.findByOutTradeNo(out_trade_no);
        if (trade == null) {
            return;
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return;
        }
        String type = tradeService.convertType(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no",trade.getOutTradeNo());
        jsonObject.put("product_code","FAST_INSTANT_TRADE_PAY");
        jsonObject.put("total_amount",trade.getPrice()/100.00);
        jsonObject.put("subject",type);
        alipayRequest.setBizContent(jsonObject.toString());//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CommonAttributes.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

//    /**
//     * 扫码支付
//     *
//     * @throws ServletException
//     * @throws IOException
//     * @throws AlipayApiException
//     */
//    @RequestMapping(value = "/QRPay/{out_trade_no}", method = RequestMethod.GET)
//    public Message QRPay(@PathVariable("out_trade_no") String out_trade_no) throws ServletException, IOException, AlipayApiException {
//        AlipayClient alipayClient = aliPayService.getAlipayClient();
//        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();//创建API对应的request类
//        request.setReturnUrl(RETURN_URL);
//        //异步回调地址
//        request.setNotifyUrl(NOTIFY_URL);//在公共参数中设置回跳和通知地址
//        Trade trade = tradeService.findByOutTradeNo(out_trade_no);
//        String type = tradeService.convertType(trade);
//        request.setBizContent("{" +
//                "\"out_trade_no\":" + out_trade_no + "," +
//                "    \"total_amount\":" + Double.valueOf(trade.getPrice()) / 100 + "," +
//                "    \"subject\":" + type + "," +
//                //       "    \"store_id\":\"NJ_001\"," +
//                "    \"timeout_express\":\"90m\"}");//设置业务参数
//        AlipayTradePrecreateResponse response = alipayClient.execute(request);
//        JSONObject jsonObject = JSONObject.fromObject(response.getBody());
//        JSONObject jsonObject1 = JSONObject.fromObject(jsonObject.get("alipay_trade_precreate_response"));
//        if (response.isSuccess()) {
//            System.out.println("调用成功");
//            return new Message(Message.Type.OK, jsonObject1);
//        } else {
//            System.out.println("调用失败");
//            return new Message(Message.Type.FAIL);
//        }
//    }


    /**
     * 申请退款
     *
     * @return
     * @throws ServletException
     * @throws IOException
     * @throws AlipayApiException
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/refund/{out_trade_no}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message refund(@PathVariable("out_trade_no") String out_trade_no) throws ServletException, IOException, AlipayApiException {
        if (out_trade_no == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(out_trade_no);
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.UNKNOWN);
        }
        trade.setStatus(Trade.Status.REFUND);
        trade.setRefundDate(new Date());
        tradeService.save(trade);
        turnoverService.writeTurnover(trade);
        return aliPayService.refund(trade);
}


    /**
     * 根据订单号查询订单支付情况(管理员)
     * @param out_trade_no
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/queryTrade/{out_trade_no}", method = RequestMethod.GET)
    public @ResponseBody Message queryTrade(@PathVariable("out_trade_no") String out_trade_no) throws ServletException, IOException, AlipayApiException {
        if (out_trade_no == null||out_trade_no.equals("")) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(out_trade_no);
        if (trade == null) {
            return new Message(Message.Type.EXIST);
        }
        return  aliPayService.queryTrade(trade);
    }

    /**
     * 订单状态变更
     * @param objectMap
     * @return
     * @throws AlipayApiException
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/refreshTrade", method = RequestMethod.POST)
    public @ResponseBody
    Message refreshOrder(@RequestBody Map<String, Object> objectMap) throws AlipayApiException {
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
        message = aliPayService.queryTrade(trade);
        JSONObject jsonObject = JSONObject.fromObject(message.getData());
        String tradeState = jsonObject.get("tradeState").toString();
        if (tradeState.equalsIgnoreCase("TRADE_SUCCESS")) {
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
                return aliPayService.refund(trade);
            }
        }
        return new Message(Message.Type.FAIL);
    }
}
