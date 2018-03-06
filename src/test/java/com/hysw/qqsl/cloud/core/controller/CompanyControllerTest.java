package com.hysw.qqsl.cloud.core.controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Create by leinuo on 17-6-27 下午5:38
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Ignore
public class CompanyControllerTest extends BaseControllerTest {

    @Test
    public void testQueryArticles() throws Exception{
        JSONObject resultJson= HttpUtils.httpGetUrl(mockMvc,"/company/queryArticles");
        assertTrue("OK".equals(resultJson.getString("type")));
        assertNotNull(resultJson.get("data"));
        JSONArray jsonArray = JSONArray.fromObject(resultJson.get("data"));
        assertTrue(jsonArray.size()>0);
    }
}
