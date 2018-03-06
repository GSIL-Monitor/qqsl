package com.hysw.qqsl.cloud.core.controller;

import net.sf.json.JSONObject;
import org.junit.Ignore;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Create by leinuo on 17-6-27 下午4:36
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 测试用的http请求工具类
 */
@Ignore
public class HttpUtils {

    /**
     * 用于发送post请求
     * @param mockMvc
     * @param paramMap 将请求消息体写入map,并将其转换为json字符串
     * @param url
     * @return
     * @throws Exception
     */
    public static JSONObject httpPost(MockMvc mockMvc, String url, String paramMap) throws Exception{
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        MvcResult result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(paramMap)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print()) //执行请求
                .andReturn();
        assertNotNull(result.getResponse().getBufferSize());
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        return  resultJson;
    }

    /**
     * 用于发送get请求不带任何参数,只有url
     * @param mockMvc
     * @param url
     * @return
     * @throws Exception
     */
    public static JSONObject httpGetUrl(MockMvc mockMvc,String url) throws Exception{
        MvcResult result = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();
        assertNotNull(result.getResponse().getBufferSize());
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        return  resultJson;
    }

    /**
     * 用于发送get请求可带参数
     * @param mockMvc
     * @param paramName
     * @param param
     * @param url
     * @return
     * @throws Exception
     */
    public static JSONObject httpGet(MockMvc mockMvc,String url,String paramName,Object param) throws Exception{
        boolean flag = paramName==null|| !StringUtils.hasText(paramName);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = flag ? get(url).contentType(MediaType.APPLICATION_JSON):get(url).param(paramName,param.toString()).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();
        assertNotNull(result.getResponse().getBufferSize());
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        return  resultJson;
    }

    /**
     * 用于发送delete请求可带参数
     * @param mockMvc
     * @param param
     * @param url
     * @return
     * @throws Exception
     */
    public static JSONObject httpDelete(MockMvc mockMvc,String url,Object param) throws Exception{
        boolean flag = param==null|| !StringUtils.hasText(param.toString());
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = flag ? delete(url).contentType(MediaType.APPLICATION_JSON):delete(url,param).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk()).andReturn();
        assertNotNull(result.getResponse().getBufferSize());
        String res = result.getResponse().getContentAsString();
        JSONObject resultJson= JSONObject.fromObject(res);
        return  resultJson;
    }

}
