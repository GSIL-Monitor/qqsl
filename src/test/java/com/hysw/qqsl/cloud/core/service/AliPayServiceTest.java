package com.hysw.qqsl.cloud.core.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import com.hysw.qqsl.cloud.CommonAttributes;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Create by leinuo on 17-7-24 下午5:21
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class AliPayServiceTest {
    private AlipayClient alipayClient;

    private static final String URL = "https://openapi.alipaydev.com/gateway.do";
    private static final String APP_ID = "2016080500170954";
    private static final String FORMAT = "json";
    private static final String CHARSET = "utf-8";
    private static final String SIGN_TYPE = "RSA2";
    //支付宝密钥
    private static final String ALIPAY_PUBLIC_KEY= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwU5HpNZRo+wfIJEHfqbO3YOyANjrZL8UyvXmC82g2O7rOzlGyTTjPNY/YhouYo71eRD/pkGOvOE6HLscwCUc1CC6GZxf2g/BOEwhDDnqIxOzHD6j0DoShXZ4f3GeM4pd8SRqHBCEFBw7ntK8rv9gnBbDg0j8YgVASmC89NW24G05Yc0q7OY6DDk542fHjeseMlLydAbZXTy8142U5+qtHTBKLSm/bNjFFPwGB4DZXDxwrqGyJtQZVrnEZmmg5cOKDVUACVuhyGV+dcsE1cvhw0Yf0kdwh4jTvzrzIJPsbwPGiJ4aBZ1HyvGTXQTwaAXh+UbEDQRkdzPhpCLAlyVywIDAQAB";
    //private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyqfdBou9u9BRZexcnQVqysoYdFy1dhtByf0k3e52R9h2TJb5LsiK8Lg7bdzSjLrpPNmJudLbVacDVXKhJrZY/tcPApuk77OgezD00Kx2KbPfTWZ0qNBL0ouiV65ZwB3QmRNN4A1e5EjI3Xjv95yBHYjG5weGTQ8F/fmw4bzdJRv8nHDPaYc1YJGrPVeGTDpnh88ehjwurdTI8pT7jY7RtbEpvTlWyFhHG1mXZRdqGci12Uefemh5vg+Jm/xgDgiQqRQiaQRjukouTvhZRhi6e2axdFfEOwiSAovAF8EPiYY00CK9F2xFrRNMcXGAh7nmNv56ALqKEFqRTXtvFJ8m7wIDAQAB";
    private static final String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDKp90Gi7270FFl7FydBWrKyhh0XLV2G0HJ/STd7nZH2HZMlvkuyIrwuDtt3NKMuuk82Ym50ttVpwNVcqEmtlj+1w8Cm6Tvs6B7MPTQrHYps99NZnSo0EvSi6JXrlnAHdCZE03gDV7kSMjdeO/3nIEdiMbnB4ZNDwX9+bDhvN0lG/yccM9phzVgkas9V4ZMOmeHzx6GPC6t1MjylPuNjtG1sSm9OVbIWEcbWZdlF2oZyLXZR596aHm+D4mb/GAOCJCpFCJpBGO6Si5O+FlGGLp7ZrF0V8Q7CJICi8AXwQ+JhjTQIr0XbEWtE0xxcYCHueY2/noAuooQWpFNe28UnybvAgMBAAECggEAP/pyyug/BBYmPHk8W84kAtV+lu3V0+2S/YPPqcjoypHJ9zAKhvyE8K4ZBPwb9JXloHJFCsdIu2e4o7dGrQQQYJPgh0A/9/TLi1jPUTnBLDU/IB5iYhEwfs3aeLfwWbiP7GOtyDgwZv2bfF/70j40fPB7auBzQ8ykZaP4dau8XURCp87NPRricSSLKMnHN3NLNRMve4aRQLmneCAOFQFtImo8mo4SxYjFoFhWDluvGncXHeJ3uw5EjJM24Beyrdb33Bm5O1cVnEUtM99vR0bt/CVov5/mB7CLvCM4A9EMLso+OBSzMEDKYpD6G5Pus294Z9kvk9hdZ5IpMVxNOepW0QKBgQDuw9kj6n/Eh2rEO2m6fTVqdfStG9ljfMSHlda76KmYyynMxTsi1wbe6kiQS8OWQ3M+tdgnSM3wfIxqwmJTYgKoOOBegUr99qGwfDSoy6OHrDxzL9Nk+aVJhi0KAps/79e5PrB72UBOObJlUks7sLSn7EfEm/CMjtkhmf9K0iI/dQKBgQDZSL/LnjUzxi8jno+i9k9qykHsyqXyBvvQAJ5lyitZ+qV3fKY2q2pckmTc8o2kMug9l2B0CtOIG2490O74Fru479+P4Bk0CEURTEkOtAouGBmHR/LkpJBNv5Gpcv9Bm7rxO4pqjqvr7UlWBwvPzEdLBRUyZiaZmyXqTMtSiFPEUwKBgCZZMmEAYvEPxugpmrunLJMiyt+a33mJKo+UU17u6X5u8xG+g9b+rk3TV0BFyu4xeysRTdxRZzI+7taezegSj9aw++hx37eWizWrXVHXEzbRRQxDHDLVneSHNmirLoBAZ2eLWBEsPZXS0oJPi2HU6c8mtggv+5y3vMwWzdgYlAOZAoGAbwz+cXvnZxG4T/UfJkPK7SJ4NSSRUbR+CJ34Vr/QDknLPdloPfK4Bp4PjNkuySf3iFsQwd4ypJKYcmGRcRx1TxzR3v/DAdPkMOYTRL+BoHNSwNBl9LOiyQnK0ZbjnM2R6u7qXHGUrpz06VHqmIaoPVBYuAx7V/BynWAoXoMshN8CgYA0a4iHa7M2tvkE7JBH6tYCDmk9ZbU1ChOMg5YgEJ5fDTNhkMJml+uvjOtl7286OXoRvMnyoGa8GrBUIu205OXaNymGfYC7ec2MO1B0Nf89CHvvo8x1ctxr4PbXVpD9waeoptdoVSUpErcuHHSVye35Cz51T0oGG04D140cxXQ4PQ==";
    private static final String RETURN_URL = "http://4107ce0a.all123.net/qqsl.web/tpls/user.html#/login";
    private static final String NOTIFY_URL = "http://4107ce0a.all123.net/qqsl.web/tpls/user.html#/login";
    public AliPayServiceTest(){
         alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
    }
    @Test
    public void testPay(){
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        AlipayTradePayModel model = new AlipayTradePayModel();
        request.setBizModel(model);
        model.setOutTradeNo(System.currentTimeMillis()+"");
        model.setSubject("Iphone6 16G");
        model.setTotalAmount("0.01");
        model.setAuthCode("289746530737274616");//沙箱钱包中的付款码
        model.setScene("bar_code");
        AlipayTradePayResponse response = null;
        try {
            response = alipayClient.execute(request);
            System.out.println(response.getBody());
            System.out.println(response.getTradeNo());
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void doPost() throws ServletException, IOException {
        HttpServletRequest httpRequest;
        HttpServletResponse httpResponse = new MockHttpServletResponse();
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl("http://4107ce0a.all123.net/qqsl.web/tpls/user.html#/login");
        alipayRequest.setNotifyUrl("http://5007c0d2.nat123.cc/qqsl/project/infos");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150320010101002\"," +
                " \"total_amount\":\"0.18\"," +
                " \"subject\":\"Iphone6 16G\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
}
