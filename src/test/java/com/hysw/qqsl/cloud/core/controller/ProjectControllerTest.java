package com.hysw.qqsl.cloud.core.controller;


import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Administrator on 2016/10/13.
 */
public class ProjectControllerTest extends BaseControllerTest {

    @Autowired
    private ProjectController projectController;
    @Autowired
    private UserController userController;
    @Before
    public void userLogin() throws Exception {
        Map<String, Object> loginMap = new HashedMap();
        loginMap.put("code", "18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        Message message = userController.login(loginMap);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
    }
    @Test
    public void testInfos() throws Exception {
        mockMvc.perform(get("/project/infos")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testLists() throws Exception {
         JSONObject jsonObject = HttpUtils.httpGet(mockMvc,"/project/lists","start","0");
         Assert.assertNotNull(jsonObject);
      /*   MvcResult result = mockMvc.perform(get("/project/lists").param("start","0")).
                 andExpect(status().isOk()).
                 andDo(MockMvcResultHandlers.print()).andReturn();*/
    }

    @Test
    public void testrefreshCache() throws Exception {
        mockMvc.perform(post("/project/refreshCache")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testLists1() throws Exception {
        Map<String,Object> loginMap = new HashMap<>();
        loginMap.put("userName","qqsl");
        loginMap.put("password", DigestUtils.md5Hex("abc"));
        loginMap.put("loginType", "web");
        MockHttpServletRequest req = new MockHttpServletRequest();
        Message message1 =  userController.login(loginMap);
        Assert.assertNotNull(message1);
        Message message = projectController.getProjects(0);
        Assert.assertNotNull(message);
    }

}
