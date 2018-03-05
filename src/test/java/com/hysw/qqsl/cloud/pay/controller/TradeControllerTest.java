package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.core.controller.BaseControllerTest;
import com.hysw.qqsl.cloud.core.controller.HttpUtils;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.controller.UserController;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

public class TradeControllerTest extends BaseControllerTest {
    private static final String httpUrl = "http://localhost:8080/qqsl/trade/";
    private static final String httpUrlLogin = "http://localhost:8080/qqsl/user/";
    @Autowired
    private UserController userController;
    @Before
    public void userLogin() throws Exception {
        Map<String, Object> loginMap = new HashedMap();
        loginMap.put("code", "18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        loginMap.put("cookie", DigestUtils.md5Hex(DigestUtils.md5Hex("111111")));
        Message message = userController.login(loginMap);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
    }

    @Test
    public void testCreatePackageTradeFirst() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("packageType", "TEST");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        JSONObject resultJson=  HttpUtils.httpPost(mockMvc,"/trade/createPackage",requestJson);
        Assert.assertTrue("NO_ALLOW".equals(resultJson.getString("type")));
    }

    @Test
    public void testCreatePackageTradeFirst1() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("type", "PACKAGE");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        JSONObject resultJson=  HttpUtils.httpPost(mockMvc,"/trade/createPackage",requestJson);
        Assert.assertTrue("NO_ALLOW".equals(resultJson.getString("type")));
    }

    @Test
    public void testCreatePackageTradeFirst2() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("packageType", "SUN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        JSONObject resultJson=  HttpUtils.httpPost(mockMvc,"/trade/createPackage",requestJson);
        Assert.assertTrue("NO_CERTIFY".equals(resultJson.getString("type")));
    }

    @Test
    public void testCreatePackageTradeFirst3() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        JSONObject resultJson=  HttpUtils.httpPost(mockMvc,"/trade/createPackage",requestJson);
        Assert.assertTrue("FAIL".equals(resultJson.getString("type")));
    }

    @Test
    public void testCreatePackageTradeFirst4() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("type", "aaa");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        JSONObject resultJson=  HttpUtils.httpPost(mockMvc,"/trade/createPackage",requestJson);
        Assert.assertTrue("NO_ALLOW".equals(resultJson.getString("type")));
    }


//    ******************************************************************************************************


}
