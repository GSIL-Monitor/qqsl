package com.hysw.qqsl.cloud.pay.service.wxPay;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付service层
 */
@Service("wXPayService")
public class WXPayService {
    @Autowired
    private WXPay wxPay;
    @Autowired
    private TradeService tradeService;

    /**
     * 扫码支付
     * @param trade 订单
     */
    public Message unifiedOrderPay(Trade trade){
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("body", tradeService.convertBuyType(trade)+"--"+tradeService.convertType(trade)+"--"+tradeService.convertBaseType(trade));
        data.put("out_trade_no", trade.getOutTradeNo());//商户支付的订单号由商户自定义生成，微信支付要求商户订单号保持唯一性（建议根据当前系统时间加随机序列来生成订单号）。重新发起一笔支付要使用原订单号，避免重复支付；已支付过或已调用关单、撤销（请见后文的API列表）的订单号不能重新发起支付
        data.put("device_info", "web");//自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
        data.put("fee_type", "CNY");//符合ISO 4217标准的三位字母代码，默认人民币：CNY
        data.put("total_fee", String.valueOf(Math.round(trade.getPrice() * 100)));//订单总金额，单位为分
        data.put("spbill_create_ip", "218.244.134.139");//APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP
        data.put("notify_url", SettingUtils.getInstance().getSetting().getNat123()+"/qqsl/wxPay/payNotice");//异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("trade_type", "NATIVE");//JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付，统一下单接口trade_type的传参可参考这里,MICROPAY--刷卡支付，刷卡支付有单独的支付接口，不调用统一下单接口
        data.put("product_id", String.valueOf(trade.getBaseType().ordinal()));//trade_type=NATIVE时（即扫码支付），此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
        // data.put("time_expire", "20170112104120");
        Map<String, String> r = null;
        try {
            r = wxPay.unifiedOrder(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        try {
            if (r.get("result_code").equals("SUCCESS") && r.get("return_code").equals("SUCCESS")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("qrCode",r.get("code_url"));
                return MessageService.message(Message.Type.OK,jsonObject);
            }
        } catch (NullPointerException e) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 关闭订单
     * @param trade
     */
    public void orderClose(Trade trade) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", trade.getOutTradeNo());
        try {
            Map<String, String> r = wxPay.closeOrder(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    /**
     * 查询订单
     * @param trade
     */
    public Message orderQuery(Trade trade) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", trade.getOutTradeNo());
//        data.put("transaction_id", "4008852001201608221962061594");
        Map<String, String> r = null;
        try {
            r = wxPay.orderQuery(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        try {
            JSONObject jsonObject=new JSONObject();
            if (r.get("result_code").equals("SUCCESS") && r.get("return_code").equals("SUCCESS")) {
                jsonObject.put("tradeState",r.get("trade_state"));
                return MessageService.message(Message.Type.OK,jsonObject);
            }else{
                jsonObject.put("tradeState",r.get("err_code"));
//                System.out.println(jsonObject);
                return MessageService.message(Message.Type.OK,jsonObject);
            }
        } catch (NullPointerException e) {
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 撤销订单
     * @param trade
     */
    public void orderReverse(Trade trade) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", trade.getOutTradeNo());
//        data.put("transaction_id", "4008852001201608221962061594");
        try {
            Map<String, String> r = wxPay.reverse(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    /**
     * 全额退款
     * @param trade
     */
    public Message refund(Trade trade) {
        if (trade.getType() != Trade.Type.GOODS) {
            return MessageService.message(Message.Type.FAIL);
        }
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", trade.getOutTradeNo());
        data.put("out_refund_no", trade.getOutTradeNo());
        data.put("total_fee", String.valueOf(Math.round(trade.getPrice() * 100)));
        data.put("refund_fee", String.valueOf(Math.round(trade.getPrice() * 100)));
        data.put("refund_fee_type", "CNY");
//        data.put("op_user_id", config.getMchID());
        Map<String, String> r = null;
        try {
            r = wxPay.refund(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        try {
            if (r.get("result_code").equals("SUCCESS") && r.get("return_code").equals("SUCCESS")) {
                return MessageService.message(Message.Type.OK);
            }
        } catch (NullPointerException e) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 非全额退款
     * @param trade
     */
    public void refund(Trade trade,int price) {
        if (trade.getType() != Trade.Type.GOODS) {
            return;
        }
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", trade.getOutTradeNo());
        data.put("out_refund_no", trade.getOutTradeNo());
        data.put("total_fee", String.valueOf(trade.getPrice()));
        data.put("refund_fee", String.valueOf(price));
        data.put("refund_fee_type", "CNY");
//        data.put("op_user_id", config.getMchID());
        try {
            Map<String, String> r = wxPay.refund(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    /**
     * 查村退款
     * @param trade
     */
    public void doRefundQuery(Trade trade){
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_refund_no", trade.getOutTradeNo());
        try {
            Map<String, String> r = wxPay.refundQuery(data);
//            System.out.println(r);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

}
