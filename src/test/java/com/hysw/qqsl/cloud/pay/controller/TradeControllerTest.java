package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

public class TradeControllerTest{
    private static final String httpUrl = "http://localhost:8080/qqsl/trade/";
    private static final String httpUrlLogin = "http://localhost:8080/qqsl/user/";

    @Test
    public void testCreatePackageTradeFirst(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "TEST");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest(httpUrl + "createPackageTrade", "POST", jsonObject.toString());
        System.out.println(jsonObject1);
        Assert.assertTrue(jsonObject1.get("type").toString().equals("OK"));
    }

    @Test
    public void testCreatePackageTradeFirst1(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
//        jsonObject.put("packageType", "TEST");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest(httpUrl + "createPackageTrade", "POST", jsonObject.toString());
        Assert.assertTrue(jsonObject1.get("type").toString().equals("FAIL"));
    }

    @Test
    public void testCreatePackageTradeFirst2(){
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "TEST");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest(httpUrl + "createPackageTrade", "POST", jsonObject.toString());
        Assert.assertTrue(jsonObject1.get("type").toString().equals("FAIL"));
    }

    @Test
    public void testCreatePackageTradeFirst3(){
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("type", "PACKAGE");
//        jsonObject.put("packageType", "TEST");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest(httpUrl + "createPackageTrade", "POST", jsonObject.toString());
        Assert.assertTrue(jsonObject1.get("type").toString().equals("FAIL"));
    }

    @Test
    public void testCreatePackageTradeFirst4(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "aaa");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest(httpUrl + "createPackageTrade", "POST", jsonObject.toString());
        Assert.assertTrue(jsonObject1.get("type").toString().equals("FAIL"));
    }

    @Test
    public void testCreatePackageTradeFirst5(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "aaa");
        jsonObject.put("packageType", "TEST");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest(httpUrl + "createPackageTrade", "POST", jsonObject.toString());
        Assert.assertTrue(jsonObject1.get("type").toString().equals("FAIL"));
    }

//    ******************************************************************************************************


}
