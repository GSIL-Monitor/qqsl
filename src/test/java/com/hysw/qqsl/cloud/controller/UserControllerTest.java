package com.hysw.qqsl.cloud.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.service.AuthentService;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/14.
 */
public class UserControllerTest extends BaseControllerTest{


    @Autowired
    private AuthentService authentService;

    /**
     * 测试准备,用户登录
     * @throws Exception
     */


    @Test
    public  void testLoginNoCookie() throws Exception{
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("phone","18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        MvcResult result = HttpUtils.httpPost(mockMvc,requestJson,"/user/login");
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        assertTrue("OTHER".equals(resultJson.getString("type")));
        User user = authentService.getUserFromSubject();
        assertNull(user);
    }

    @Test
    public void testLogin() throws Exception{
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("phone","18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        loginMap.put("cookie", DigestUtils.md5Hex(DigestUtils.md5Hex("111111")));
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        MvcResult result = HttpUtils.httpPost(mockMvc,requestJson,"/user/login");
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        assertTrue("OK".equals(resultJson.getString("type")));
        assertNotNull(resultJson.getJSONObject("data"));
        assertNotNull(resultJson.getJSONObject("data").get("phone").equals("18661925010"));
        User user = authentService.getUserFromSubject();
        assertNotNull(user);
    }

    @Test
    public void testChangePassword() throws Exception{
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("phone","18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        loginMap.put("code", DigestUtils.md5Hex(DigestUtils.md5Hex("123456")));
        String  requestJson = net.minidev.json.JSONObject.toJSONString(loginMap);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        MockHttpSession session = new MockHttpSession();
        MvcResult result = mockMvc.perform(post("/user/changePassword").contentType(MediaType.APPLICATION_JSON).content(requestJson).session(session)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print()) //执行请求
                .andReturn();
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        assertTrue(resultJson.getString("type")!=null);
    }


}
