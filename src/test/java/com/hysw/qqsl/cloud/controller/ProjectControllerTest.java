package com.hysw.qqsl.cloud.controller;


import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.results.ResultMatchers;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

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
    @Test
    public void testInfos() throws Exception {
        mockMvc.perform(get("/project/infos")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testLists() throws Exception {
         MvcResult result = mockMvc.perform(get("/project/lists").param("start","0")).
                 andExpect(status().isOk()).
                 andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Test
    public void testrefreshCache() throws Exception {
        mockMvc.perform(post("/project/refreshCache")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testLists1() throws Exception {
        Map<String,String> loginMap = new HashMap<>();
        loginMap.put("userName","qqsl");
        loginMap.put("password", DigestUtils.md5Hex("abc"));
        loginMap.put("loginType", "web");
        MockHttpServletRequest req = new MockHttpServletRequest();
     //   Message message1 =  userController.login(loginMap,req);
       // Assert.assertNotNull(message1);
        Message message = projectController.getProjects(0);
        Assert.assertNotNull(message);
    }

}
