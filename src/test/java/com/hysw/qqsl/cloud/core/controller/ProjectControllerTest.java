package com.hysw.qqsl.cloud.core.controller;

import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.shiro.SecurityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

/**
 * Create by leinuo on 18-1-12 下午6:12
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class ProjectControllerTest extends BaseControllerTest{

    @Autowired
    private ProjectController projectController;
    @Autowired
    private UserController userController;
    @Before
    public void setUp(){
        if (SecurityUtils.getSubject().getSession() != null) {
            SecurityUtils.getSubject().logout();
        }
        Map<String, Object> loginMap = new HashedMap();
        loginMap.put("code", "18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        loginMap.put("cookie", DigestUtils.md5Hex(DigestUtils.md5Hex("111111")));
        Message message = userController.login(loginMap);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getProjects() throws Exception {
        JSONObject jsonObject = HttpUtils.httpGet(mockMvc, "/project/lists", "start", "0");
        Assert.assertNotNull(jsonObject);
    }

    @Test
    public void createProject() throws Exception {

    }

    @Test
    public void getSelects() throws Exception {

    }

    @Test
    public void getTreeJsons() throws Exception {

    }

    @Test
    public void updateProject() throws Exception {

    }

    @Test
    public void removeProjectByCode() throws Exception {

    }

    @Test
    public void getInfos() throws Exception {
        mockMvc.perform(get("/project/infos")).
                andExpect(status().isOk()).andReturn();
    }

    @Test
    public void getTemplateJsons() throws Exception {

    }

    @Test
    public void getUnit() throws Exception {

    }

    @Test
    public void getValues() throws Exception {

    }

    @Test
    public void getProject() throws Exception {

    }

    @Test
    public void getProjectIntroduce() throws Exception {

    }

    @Test
    public void sendMessage() throws Exception {

    }

    @Test
    public void uploadModel() throws Exception {

    }

    @Test
    public void addPanoramicUrl() throws Exception {

    }

    @Test
    public void share() throws Exception {

    }

    @Test
    public void unShare() throws Exception {

    }

    @Test
    public void viewCooperate() throws Exception {

    }

    @Test
    public void unViews() throws Exception {

    }

    @Test
    public void cooperateMult() throws Exception {

    }

    @Test
    public void cooperateSim() throws Exception {

    }

    @Test
    public void unCooperate() throws Exception {

    }

    @Test
    public void getInfos1() throws Exception {

    }

    @Test
    public void uploadFileSize() throws Exception {

    }

    @Test
    public void downloadFileSize() throws Exception {

    }

    @Test
    public void deleteFileSize() throws Exception {

    }

    @Test
    public void isAllowUpload() throws Exception {

    }

    @Test
    public void isAllowDownload() throws Exception {

    }

    @Test
    public void isAllowBim() throws Exception {

    }

    @Test
    public void iconTypeUpdate() throws Exception {

    }

    @Test
    public void elementLogWeek() throws Exception {

    }

    @Test
    public void elementLogMonth() throws Exception {

    }

    @Test
    public void elementLogThreeMonth() throws Exception {

    }

    @Test
    public void elementLogYear() throws Exception {

    }

}