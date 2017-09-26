package com.hysw.qqsl.cloud.controller;

import static org.junit.Assert.*;

import com.hysw.qqsl.cloud.BaseTest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Factory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

/**
 * Created by Administrator on 2016/10/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(name = "parent",locations = {"classpath:applicationContext-test.xml","classpath*:/applicationContext-cache-test.xml"}),
        @ContextConfiguration(name = "child",locations = "classpath:applicationContext-shiro-test.xml"),
        @ContextConfiguration(name = "child",locations = "classpath:qqsl-servlet-test.xml")
})
public class BaseControllerTest{
    @Autowired
    protected WebApplicationContext wac;
    protected MockMvc mockMvc;
    @Resource
    org.apache.shiro.mgt.SecurityManager securityManager;

    @Before
    public void setUp() {
        System.out.println("start");
        ThreadContext.bind(securityManager);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    }

    @BeforeClass
    public static void testStart(){
        System.out.println("ControllerTest start!");
    }

    @Test
    public void test() {
        assertTrue(true);
    }
    @After
    public void tearDown() throws Exception {
        System.out.println("over");
    }
    @AfterClass
    public static void testOver(){
        System.out.println("ControllerTest over!");
    }

}
