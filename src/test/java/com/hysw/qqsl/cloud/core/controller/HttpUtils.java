package com.hysw.qqsl.cloud.core.controller;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Create by leinuo on 17-6-27 下午4:36
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * http
 */
public class HttpUtils {

    public static MvcResult httpPost(MockMvc mockMvc, String param, String url) throws Exception{
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        MvcResult result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(param)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print()) //执行请求
                .andReturn();
        return  result;
    }

    public static MvcResult httpGet(MockMvc mockMvc,String url) throws Exception{
        MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
        return  result;
    }

    public static MvcResult httpGet(MockMvc mockMvc,String param,String url) throws Exception{
        MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
        return  result;
    }
}
