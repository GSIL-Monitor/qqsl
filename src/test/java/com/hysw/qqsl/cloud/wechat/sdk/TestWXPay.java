package com.hysw.qqsl.cloud.wechat.sdk;

import java.util.*;

public class TestWXPay {

    private WXPay wxpay;
    private WXPayConfigImpl config;
    private String out_trade_no;
    private String total_fee;

    public TestWXPay() throws Exception {
        config = WXPayConfigImpl.getInstance();
        wxpay = new WXPay(config);
        total_fee = "1";
        // out_trade_no = "201701017496748980290321";
        out_trade_no = "201613091059590000003433-asd013";
    }

    /**
     * 扫码支付  下单
     */
    public void doUnifiedOrder() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("body", "青清水利在线服务-XX服务购买");
        data.put("out_trade_no", out_trade_no);//商户支付的订单号由商户自定义生成，微信支付要求商户订单号保持唯一性（建议根据当前系统时间加随机序列来生成订单号）。重新发起一笔支付要使用原订单号，避免重复支付；已支付过或已调用关单、撤销（请见后文的API列表）的订单号不能重新发起支付
        data.put("device_info", "web");//自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
        data.put("fee_type", "CNY");//符合ISO 4217标准的三位字母代码，默认人民币：CNY
        data.put("total_fee", "1");//订单总金额，单位为分
        data.put("spbill_create_ip", "218.244.134.139");//APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP
        data.put("notify_url", "http://5007c0d2.nat123.cc/qqsl/weChat/notice");//异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("trade_type", "NATIVE");//JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付，统一下单接口trade_type的传参可参考这里,MICROPAY--刷卡支付，刷卡支付有单独的支付接口，不调用统一下单接口
        data.put("product_id", "12");//trade_type=NATIVE时（即扫码支付），此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
        // data.put("time_expire", "20170112104120");

        try {
            Map<String, String> r = wxpay.unifiedOrder(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doOrderClose() {
        System.out.println("关闭订单");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
        try {
            Map<String, String> r = wxpay.closeOrder(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doOrderQuery() {
        System.out.println("查询订单");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
//        data.put("transaction_id", "4008852001201608221962061594");
        try {
            Map<String, String> r = wxpay.orderQuery(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doOrderReverse() {
        System.out.println("撤销");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
//        data.put("transaction_id", "4008852001201608221962061594");
        try {
            Map<String, String> r = wxpay.reverse(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 长链接转短链接
     * 测试成功
     */
    public void doShortUrl() {
        String long_url = "weixin://wxpay/bizpayurl?pr=etxB4DY";
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("long_url", long_url);
        try {
            Map<String, String> r = wxpay.shortUrl(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退款
     * 已测试
     */
    public void doRefund() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
        data.put("out_refund_no", out_trade_no);
        data.put("total_fee", total_fee);
        data.put("refund_fee", total_fee);
        data.put("refund_fee_type", "CNY");
        data.put("op_user_id", config.getMchID());

        try {
            Map<String, String> r = wxpay.refund(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询退款
     * 已经测试
     */
    public void doRefundQuery() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_refund_no", out_trade_no);
        try {
            Map<String, String> r = wxpay.refundQuery(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对账单下载
     * 已测试
     */
    public void doDownloadBill() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("bill_date", "20161102");
        data.put("bill_type", "ALL");
        try {
            Map<String, String> r = wxpay.downloadBill(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGetSandboxSignKey() throws Exception {
        WXPayConfigImpl config = WXPayConfigImpl.getInstance();
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("mch_id", config.getMchID());
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        String sign = WXPayUtil.generateSignature(data, config.getKey());
        data.put("sign", sign);
        WXPay wxPay = new WXPay(config);
        String result = wxPay.requestWithoutCert("https://api.mch.weixin.qq.com/sandbox/pay/getsignkey", data, 10000, 10000);
        System.out.println(result);
    }


//    public void doReport() {
//        HashMap<String, String> data = new HashMap<String, String>();
//        data.put("interface_url", "20160822");
//        data.put("bill_type", "ALL");
//    }

    /**
     * 小测试
     */
    public void test001() {
        String xmlStr = "<xml><appid><![CDATA[wx3b65c94d27a017aa]]></appid>\n" +
                "<bank_type><![CDATA[CFT]]></bank_type>\n" +
                "<cash_fee><![CDATA[1]]></cash_fee>\n" +
                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
                "<is_subscribe><![CDATA[Y]]></is_subscribe>\n" +
                "<mch_id><![CDATA[1448719602]]></mch_id>\n" +
                "<nonce_str><![CDATA[c1d7ac9cfc7843368a795d017d845f4a]]></nonce_str>\n" +
                "<openid><![CDATA[okXutuKnUikP571RFtolQFEy-rF4]]></openid>\n" +
                "<out_trade_no><![CDATA[201613091059590000003433-asd010]]></out_trade_no>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<sign><![CDATA[EBB7F4543058B13655E23786CDE8855D]]></sign>\n" +
                "<time_end><![CDATA[20170731163343]]></time_end>\n" +
                "<total_fee>1</total_fee>\n" +
                "<trade_type><![CDATA[NATIVE]]></trade_type>\n" +
                "<transaction_id><![CDATA[4003322001201707313666803950]]></transaction_id>\n" +
                "</xml>";
        try {
            System.out.println(xmlStr);
            System.out.println("+++++++++++++++++");
            System.out.println(WXPayUtil.isSignatureValid(xmlStr, config.getKey()));
            Map<String, String> hm = WXPayUtil.xmlToMap(xmlStr);
            System.out.println("+++++++++++++++++");
            System.out.println(hm);
            System.out.println(hm.get("attach").length());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testUnifiedOrderSpeed() throws Exception {
        TestWXPay dodo = new TestWXPay();

        for (int i = 0; i < 100; ++i) {
            long startTs = System.currentTimeMillis();
            out_trade_no = out_trade_no + i;
            dodo.doUnifiedOrder();
            long endTs = System.currentTimeMillis();
            System.out.println(endTs - startTs);
            Thread.sleep(1000);
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println("--------------->");
        TestWXPay dodo = new TestWXPay();
        // dodo.doGetSandboxSignKey();

        // dodo.doUnifiedOrder();
        // dodo.doOrderQuery();
        // dodo.doDownloadBill();
        // dodo.doShortUrl();
        // dodo.test001();
        // dodo.doOrderQuery();
        // dodo.doOrderClose();
        // dodo.doRefund();
        // dodo.doRefundQuery();
        // dodo.doOrderReverse();
        // dodo.test001();
        // dodo.testUnifiedOrderSpeed();

//        dodo.doOrderQuery();
//        dodo.doOrderReverse();
//        dodo.doOrderQuery();
//        dodo.doOrderReverse();
//        dodo.doOrderQuery();
//        dodo.doUnifiedOrder();
//        dodo.doOrderClose();
        dodo.doRefund();
        dodo.test002();
        System.out.println("<---------------"); // wx2016112510573077
    }

    public void test002() throws Exception {
        String result = "<xml><appid><![CDATA[wx3b65c94d27a017aa]]></appid>\n" +
                "<bank_type><![CDATA[CFT]]></bank_type>\n" +
                "<cash_fee><![CDATA[1]]></cash_fee>\n" +
                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
                "<is_subscribe><![CDATA[Y]]></is_subscribe>\n" +
                "<mch_id><![CDATA[1448719602]]></mch_id>\n" +
                "<nonce_str><![CDATA[4879307e8a5d4551a237e35f6e39ebe4]]></nonce_str>\n" +
                "<openid><![CDATA[okXutuKnUikP571RFtolQFEy-rF4]]></openid>\n" +
                "<out_trade_no><![CDATA[201613091059590000003433-asd012]]></out_trade_no>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<sign><![CDATA[83F153E08E0E6D42B81143BA4AD4C76E]]></sign>\n" +
                "<time_end><![CDATA[20170731170219]]></time_end>\n" +
                "<total_fee>1</total_fee>\n" +
                "<trade_type><![CDATA[NATIVE]]></trade_type>\n" +
                "<transaction_id><![CDATA[4003322001201707313667980549]]></transaction_id>\n" +
                "</xml>";
        Map<String, String> map = WXPayUtil.xmlToMap(result);
        map.put("total_fee", "1");
        String s = WXPayUtil.mapToXml(map);
        boolean b = WXPayUtil.isSignatureValid(s,config.getKey());
        System.out.println(b);
    }
}