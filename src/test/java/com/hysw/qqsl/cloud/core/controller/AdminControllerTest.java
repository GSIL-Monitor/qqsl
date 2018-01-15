package com.hysw.qqsl.cloud.core.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Create by leinuo on 18-1-12 下午6:23
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Ignore
public class AdminControllerTest extends BaseControllerTest{
    @Before
    public void setUp(){

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void refreshCache() throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/refreshCache")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void login() throws Exception {

    }

    @Test
    public void getOTP() throws Exception {

    }

    @Test
    public void getAdmin() throws Exception {

    }

    @Test
    public void getUsers() throws Exception {

    }

    @Test
    public void getLandingNumber() throws Exception {

    }

    @Test
    public void resetPassword() throws Exception {

    }

    @Test
    public void locked() throws Exception {

    }

    @Test
    public void getAllProjects() throws Exception {

    }

    @Test
    public void publishAriticle() throws Exception {

    }

    @Test
    public void deletetArticle() throws Exception {

    }

    @Test
    public void deletetArticle1() throws Exception {

    }

}