package com.hysw.qqsl.cloud.pay.service.aliPay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Create by leinuo on 17-7-17 下午5:01
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 支付宝业务层
 */
@Service("aliPayService")
public class AliPayService {

    private AlipayClient alipayClient;
    private Setting setting = SettingUtils.getInstance().getSetting();
    public AliPayService(){
        alipayClient = new DefaultAlipayClient(CommonAttributes.OPEN_API, CommonAttributes.APP_ID, CommonAttributes.APP_PRIVATE_KEY,
                CommonAttributes.RESULT_TYPE, CommonAttributes.CHARSET, CommonAttributes.ALIPAY_PUBLIC_KEY, CommonAttributes.SIGN_TYPE);
    }

    public AlipayClient getAlipayClient(){
        return alipayClient;
    }

    /**
     * 支付成功之后,前台指定的跳转页面的地址
     * @return
     */
    public String getReturnUrl(){
        String returnUrl =  "http://"+setting.getAliPayIP()+"/tpls/productModule/aliPaySuccess.html";
        return returnUrl;
    }

    /**
     * 支付成功之后,向后台异步通知的接口地址
     * @return
     */
    public String getNotifyUrl(){
        String notifyUrl = "http://"+setting.getAliPayIP()+":8080/qqsl/aliPay/notify";
        return notifyUrl;
    }
    /**
     * 阿里订单查询
     * @param trade
     * @throws ServletException
     * @throws IOException
     * @throws AlipayApiException
     */
    public JSONObject queryTrade(Trade trade) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":" +trade.getOutTradeNo()+
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            String status;
            JSONObject jsonObject = new JSONObject();
            response = alipayClient.execute(request);
            if(response.isSuccess()){
//                System.out.println("调用成功");
                response.getSubCode();
                status = response.getTradeStatus();
                //   交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
                jsonObject.put("tradeState",status);
                return jsonObject;
            } else {
//                System.out.println("调用失败");
                status = response.getSubCode();
                // ACQ.SYSTEM_ERROR	系统错误	重新发起请求
                // ACQ.INVALID_PARAMETER	参数无效	检查请求参数，修改后重新发起请求
                // ACQ.TRADE_NOT_EXIST  查询的交易不存在  检查传入的交易号是否正确，修改后重新发起请求
                jsonObject.put("tradeState",status);
                return jsonObject;
            }
        } catch (AlipayApiException e) {
            return null;
        }
    }

    /**
     * 退款
     * @param trade
     * @return
     * @throws AlipayApiException
     */
    public void refund(Trade trade) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":" + trade.getOutTradeNo() + "," +
                //             "\"trade_no\":\"2014112611001004680073956707\"," +
                "\"refund_amount\":" + trade.getPrice() + "," +
                "\"refund_reason\":\"正常退款\"" +
               /* "\"out_request_no\":\"HZ01RF001\"," +
                "\"operator_id\":\"OP001\"," +
                "\"store_id\":\"NJ_S_001\"," +
                "\"terminal_id\":\"NJ_T_001\"" +*/
                "  }");
        AlipayTradeRefundResponse response = alipayClient.execute(request);
//        if (response.isSuccess()) {
//            System.out.println("调用成功");
//            return MessageService.message(Message.Type.OK);
//        } else {
//            System.out.println("调用失败");
//        }
//        return MessageService.message(Message.Type.OK);
        //{"alipay_trade_refund_response":{"code":"10000","msg":"Success","buyer_logon_id":"221***@qq.com","buyer_user_id":"2088512714636913","fund_change":"N","gmt_refund_pay":"2017-08-03 14:52:30","open_id":"20881028868410491303826532716191","out_trade_no":"20150320010101008","refund_fee":"0.01","send_back_fee":"0.00","trade_no":"2017080121001004910201344195"},"sign":"u/i7OVxMvyIzgQAIyR4YVmk3s5Zfrk21QwQ7Hq88O5MxA+xR9e5WLyuXrd75T53ykSL5j+AvaV/g8vttgZznO8j0rmuY2b339g7BMxHxIpy2TVLt0tfSCYaH7wmlMxNgr1dQvxkm5BIiWeHmZl/5hrRqqtaf4IINREhs6iGVLMgLzW7r8dkmdu852xIwST9pEiulpaOCj1FfAA1HDPy8ikijkmbH2/eqNMM3TIJg93Dc+l9GUsNfVGfUq4MhPv+SETvXCl1RoPqirPwi/ds5Hh0QUMwHv1K09BY3xyAoCiAjuP23wA7cAQO5+YklzaVj+Wi29PdLnJKZqHN42ktnOg=="}

    }

}
