package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.ApplicationTokenService;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.StationService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Create by leinuo on 18-1-8 下午4:17
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class StationControllerTest extends BaseControllerTest{

    @Autowired
    private UserController userController;
    @Autowired
    private AuthentController authentController;
    @Autowired
    private StationController stationController;
    @Autowired
    private ApplicationTokenService applicationTokenService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private StationService stationService;
    @Before
    public void userLogin() throws Exception {
        Map<String,Object> loginMap = new HashedMap();
        loginMap.put("code","18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        Message message =  userController.login(loginMap);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
        User user = authentService.getUserFromSubject();
        //添加测站，添加仪表
        addTestStation(user);
    }

    private void addTestStation(User user) {
        Station station = stationService.find(1L);
        if(station==null){
            station = new Station();
            station.setId(1L);
            station.setType(CommonEnum.StationType.WATER_LEVEL_STATION);
            station.setUser(user);
            station.setName("Test");
            station.setCoor("{\"longitude\":\"101.67822819977684\",\"latitude\":\"36.65375645069668\",\"elevation\":\"0\"}");
            station.setAddress("青海");
            stationService.save(station);
        }
    }

    @After
    public void userLogOut() throws Exception {
        Message message = authentController.logout();
        Assert.assertNotNull(message);
    }

    @Test
    public void getToken() throws Exception {
        JSONObject resultJson = HttpUtils.httpGetUrl(mockMvc,"/station/token");
        Assert.assertNotNull(resultJson.get("data"));
        Assert.assertTrue(applicationTokenService.decrypt(resultJson.get("data").toString()));
    }

    @Test
    public void uploadModel() throws Exception {
        File testFile = new ClassPathResource("station.xlsx").getFile();
        FileInputStream fis = new FileInputStream(testFile);
        MockMultipartFile file = new MockMultipartFile("station.xlsx","station.xlsx","application/vnd.ms-excel",fis);
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest() ;
        request.addFile(file);
        request.setParameter("id","1");
        request.setParameter("fileName","station.xlsx");
        Message message = stationController.uploadModel(request);
        Assert.assertTrue(Message.Type.OK.equals(message.getType()));
        Station station = stationService.find(1L);
        assertNotNull(station.getFlowModel());
        assertNotNull(station.getRiverModel());
    }

    @Test
    public void downloadModel() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Message message = stationController.downloadModel(1L,mockHttpServletResponse);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
    }

    @Test
    public void getStations() throws Exception {
        JSONObject resultJson= HttpUtils.httpGetUrl(mockMvc,"/station/lists");
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
    public void addCamera() throws Exception {

    }

    @Test
    public void deleteSensor() throws Exception {
        JSONObject resultJson= HttpUtils.httpDelete(mockMvc,"/station/deleteSensor/{id}",1l);
        assertTrue("OK".equals(resultJson.getString("type")));
    }

    @Test
    public void deleteCamera() throws Exception {

    }

    @Test
    public void editSensor() throws Exception {

    }

    @Test
    public void editCamera() throws Exception {

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

    @Test
    public void getParameters() throws Exception {

    }

}