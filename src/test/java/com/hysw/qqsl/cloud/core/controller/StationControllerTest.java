package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.ApplicationTokenService;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.SensorService;
import com.hysw.qqsl.cloud.core.service.StationService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Create by leinuo on 18-1-8 下午4:17
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class StationControllerTest extends BaseControllerTest {

    Log logger = LogFactory.getLog(getClass());
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
    @Autowired
    private SensorService sensorService;

    @Before
    public void userLogin() throws Exception {
        Map<String, Object> loginMap = new HashedMap();
        loginMap.put("code", "18661925010");
        loginMap.put("password", DigestUtils.md5Hex("111111"));
        loginMap.put("loginType", "web");
        Message message = userController.login(loginMap);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
        //添加测站，添加仪表
        addTestStation();
    }

    private void addTestStation() {
        Station station = stationService.find(12L);
        if (station == null) {
           save();
        }
    }

    @After
    public void userLogOut() throws Exception {
        Message message = authentController.logout();
        Assert.assertNotNull(message);
    }

    @Test
    public void getToken() throws Exception {
        JSONObject resultJson = HttpUtils.httpGetUrl(mockMvc, "/station/token");
        Assert.assertNotNull(resultJson.get("data"));
        Assert.assertTrue(applicationTokenService.decrypt(resultJson.get("data").toString()));
    }

    @Test
    public void uploadModel() throws Exception {
        File testFile = new ClassPathResource("station.xlsx").getFile();
        FileInputStream fis = new FileInputStream(testFile);
        MockMultipartFile file = new MockMultipartFile("station.xlsx", "station.xlsx", "application/vnd.ms-excel", fis);
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.addFile(file);
        request.setParameter("id", "12");
        request.setParameter("fileName", "station.xlsx");
        Message message = stationController.uploadModel(request);
        Assert.assertTrue(Message.Type.OK.equals(message.getType()));
        Station station = stationService.find(12L);
        assertNotNull(station.getFlowModel());
        assertNotNull(station.getRiverModel());
    }

    @Test
    public void downloadModel() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Message message = stationController.downloadModel(12L, mockHttpServletResponse);
        Assert.assertTrue(message.getType().equals(Message.Type.OK));
    }

    @Test
    public void getStations() throws Exception {
        JSONObject resultJson = HttpUtils.httpGetUrl(mockMvc, "/station/lists");
        JSONArray jsonArray = JSONArray.fromObject(resultJson.get("data"));
        assertNotNull(jsonArray);
    }

    @Test
    public void editStation() throws Exception {
        Map<String, Object> stationMap = new HashMap<>();
        stationMap.put("id", "12");
        stationMap.put("type", "HYDROLOGIC_STATION");
        stationMap.put("name", "西宁");
        stationMap.put("description", "湟水河");
        stationMap.put("address", "西宁");
        stationMap.put("coor", "101.67822819977684,36.65375645069668,0");
        Map<String, Object> station = new HashMap<>();
        station.put("station", stationMap);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String requestJson = net.minidev.json.JSONObject.toJSONString(station);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc, "/station/edit", requestJson);
        Assert.assertTrue(Message.Type.OK.toString().equals(jsonObject.get("type").toString()));
        Station station1 = stationService.find(12l);
        assertTrue(station1.getDescription().equals("湟水河"));
    }

    @Test
    public void addSensor() throws Exception {
        Map<String, Object> sensorMap = new HashMap<>();
        sensorMap.put("code", "9930023");
        sensorMap.put("ciphertext", "02D7145AFB1");
        sensorMap.put("factory", "惠普");
        sensorMap.put("contact", "分布");
        sensorMap.put("phone", "18662905010");
        sensorMap.put("settingHeight", "3.5");
        Map<String, Object> station = new HashMap<>();
        station.put("sensor", sensorMap);
        station.put("id", "12");
        Sensor sensor = sensorService.findByCode("9930023");
        if(sensor!=null){
            sensorService.remove(sensor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String requestJson = net.minidev.json.JSONObject.toJSONString(station);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc, "/station/addSensor", requestJson);
        Assert.assertTrue("OK".equals(jsonObject.get("type").toString()));
    }

    @Test
    public void addCamera() throws Exception {
        Map<String, Object> sensorMap = new HashMap<>();
        sensorMap.put("factory", "惠普");
        sensorMap.put("contact", "分布");
        sensorMap.put("phone", "18662905010");
        sensorMap.put("cameraUrl", "45aa42920a9241a59d0374ecfcb64781");
        Map<String, Object> station = new HashMap<>();
        station.put("camera", sensorMap);
        station.put("id", "12");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String requestJson = net.minidev.json.JSONObject.toJSONString(station);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc, "/station/addCamera", requestJson);
        Assert.assertTrue(Message.Type.OK.toString().equals(jsonObject.get("type").toString()));
    }

    @Test
    public void deleteSensor() throws Exception {
        Sensor sensor = setUpData("sensor");
        Long id = sensor.getId();
        JSONObject resultJson = HttpUtils.httpDelete(mockMvc, "/station/deleteSensor/{id}", id);
        assertTrue("OK".equals(resultJson.getString("type")));
    }

    @Test
    public void deleteCamera() throws Exception {
        Sensor sensor = setUpData("camera");
        Long id = sensor.getId();
        JSONObject resultJson = HttpUtils.httpDelete(mockMvc, "/station/deleteCamera/{id}", id);
        assertTrue("OK".equals(resultJson.getString("type")));
    }

    @Test
    public void editSensor() throws Exception {
        Sensor sensor = setUpData("sensor");
        Map<String, Object> sensorMap = new HashMap<>();
        sensorMap.put("factory", "惠普update");
        sensorMap.put("contact", "分布");
        sensorMap.put("phone", "18662905010");
        sensorMap.put("settingHeight", "3.5");
        sensorMap.put("station",sensor.getStation().getId());
        sensorMap.put("id", sensor.getId());
        Map<String, Object> sensorMap1 = new HashMap<>();
        sensorMap1.put("sensor", sensorMap);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String requestJson = net.minidev.json.JSONObject.toJSONString(sensorMap1);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc, "/station/editSensor", requestJson);
        assertTrue("OK".equals(jsonObject.getString("type")));
    }

    @Test
    public void editCamera() throws Exception {
        Sensor sensor = setUpData("camera");
        Map<String, Object> sensorMap = new HashMap<>();
        sensorMap.put("factory", "摄像头");
        sensorMap.put("contact", "分布");
        sensorMap.put("phone", "18662905010");
        sensorMap.put("cameraUrl", "45aa42920a9241a59d0374ecfcb64781");
        sensorMap.put("station",sensor.getStation().getId());
        sensorMap.put("id", sensor.getId());
        Map<String, Object> sensorMap1 = new HashMap<>();
        sensorMap1.put("camera", sensorMap);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        String requestJson = net.minidev.json.JSONObject.toJSONString(sensorMap1);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc, "/station/editCamera", requestJson);
        assertTrue("OK".equals(jsonObject.getString("type")));
    }

    @Test
    public void editParameter() throws Exception {
        Map<String,Object> parameterMap = new HashedMap();
        parameterMap.put("maxValue","20");
        parameterMap.put("minValue","10");
        parameterMap.put("phone","18661905010");
        parameterMap.put("sendStatus","true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.getStatus();
        Map<String,Object> map = new HashedMap();
        map.put("id","12");
        map.put("parameter",parameterMap);
        String requestJson = net.minidev.json.JSONObject.toJSONString(map);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc,"/station/editParameter",requestJson);
        assertTrue("OK".equals(jsonObject.getString("type")));
    }

    @Test
    public void unShare() throws Exception {
        Map<String,Object> map = new HashedMap();
        map.put("userIds","3,24");
        map.put("stationId","1");
        String requestJson = net.minidev.json.JSONObject.toJSONString(map);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc,"/station/unShare",requestJson);
        assertTrue("OK".equals(jsonObject.getString("type")));
        map.put("stationId","12");
        requestJson = net.minidev.json.JSONObject.toJSONString(map);
        jsonObject = HttpUtils.httpPost(mockMvc,"/station/unShare",requestJson);
        assertTrue("OK".equals(jsonObject.getString("type")));
    }
    @Test
    public void shares() throws Exception {
        Map<String,Object> map = new HashedMap();
        map.put("userIds","3,24");
        User user = authentService.getUserFromSubject();
        List<Station> stations = stationService.findByUser(user);
        if(stations.size()==0){
            save();
            stations = stationService.findByUser(user);
        }
        String ids = "";
        for (int i=0;i<stations.size();i++){
            ids = ids+stations.get(i).getId()+",";
        }
        map.put("stationIds",ids);
        String requestJson = net.minidev.json.JSONObject.toJSONString(map);
        JSONObject jsonObject = HttpUtils.httpPost(mockMvc,"/station/shares",requestJson);
        assertTrue("OK".equals(jsonObject.getString("type")));
    }

    @Test
    public void getParameters() throws Exception {
        String token = applicationTokenService.getToken();
        JSONObject jsonObject = HttpUtils.httpGet(mockMvc,"/station/getParameters","token",token);
        assertTrue("OK".equals(jsonObject.getString("type")));
    }

    private void save(){
       User user = authentService.getUserFromSubject();
         Station station = new Station();
            station.setId(12L);
            station.setType(CommonEnum.StationType.WATER_LEVEL_STATION);
            station.setUser(user);
            station.setName("Test");
            station.setCoor("{\"longitude\":\"101.67822819977684\",\"latitude\":\"36.65375645069668\",\"elevation\":\"0\"}");
            station.setAddress("青海");
            Long time = System.currentTimeMillis()+200*60000;
            station.setExpireDate(new Date(time));
            stationService.save(station);
    }

    private Sensor setUpData(String type){
        Station station = stationService.find(12l);
        if(station==null){
            save();
        }
        List<Sensor> sensors = station.getSensors();
        Sensor sensor = null;
        for (int i = 0; i < sensors.size(); i++) {
            if("camera".equals(type)&&Sensor.Type.CAMERA.equals(sensors.get(i).getType())){
                sensor = sensors.get(i);
                break;
            }
            if (!"camera".equals(type)&&!Sensor.Type.CAMERA.equals(sensors.get(i).getType())) {
                sensor = sensors.get(i);
                break;
            }
        }
        if (sensor == null) {
            logger.info("暂无仪表,则添加仪表");
            sensor = new Sensor();
            if("camera".equals(type)){
                sensor.setType(Sensor.Type.CAMERA);
            }else {
                sensor.setType(Sensor.Type.CANAL_FLOW);
            }
            sensor.setCode("1213213");
            sensor.setStation(station);
            sensorService.save(sensor);
        }
        return sensor;
    }
}