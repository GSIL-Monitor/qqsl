package com.hysw.qqsl.cloud.core.controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Create by leinuo on 17-9-11 下午5:20
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class StationControllerTest extends BaseControllerTest {
    @Autowired
    private UserController userController;
    private AuthentController authentController;
    @Before
    public void userLogin() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("code","18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        Message message =  userController.login(loginMap);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
    }

    @After
    public void userLogOut() throws Exception {
        Message message =  authentController.logout();
        Assert.assertNotNull(message);
    }

    @Test
    public void uploadRiverModel() throws Exception {

    }

    @Test
    public void uploadFlowModel() throws Exception {

    }

    @Test
    public void getStations() throws Exception {
        MvcResult result = HttpUtils.httpGet(mockMvc,"/station/lists");
        assertNotNull(result.getResponse().getBufferSize());
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        JSONArray jsonArray = JSONArray.fromObject(resultJson.get("data"));
        assertNotNull(jsonArray);
    }

    @Test
    public void editStation() throws Exception {

    }

    @Test
    public void addSensor() throws Exception {

    }

    @Test
    public void deleteSensor() throws Exception {

    }

    @Test
    public void editSensor() throws Exception {

    }

    @Test
    public void editParameter() throws Exception {

    }

    @Test
    public void unShare() throws Exception {

    }

    @Test
    public void shares() throws Exception {

    }

}