package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.service.MonitorService;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Administrator on 2016/9/5.
 */
public class MonitorServiceTest extends BaseTest{
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private StationService stationService;

    @Test
    public void testVerify(){
        String code = "11111111";
        String s = null;
        try {
            s = RSACoderUtil.encryptAES(code, CommonAttributes.appliactionKey, CommonAttributes.appliactionIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer simple = new StringBuffer("");
        for (int i = 0; i < s.length(); i += 3) {
            simple.append(s.substring(i, i + 1));
        }
        Assert.assertTrue(monitorService.verify(code, simple.toString()));
        Assert.assertTrue(!monitorService.verify("11", simple.toString()));
    }

    @Test
    public void testGetApplicationList(){
        JSONArray applicationList = monitorService.getApplicationList();
        Assert.assertNotNull(applicationList);
    }

    @Test
    public void testIsActivation(){
        JSONArray applicationList = monitorService.getApplicationList();
        JSONObject jsonObject= (JSONObject) applicationList.get(0);
        Sensor sensor = new Sensor();
        Station station = new Station();
        stationService.save(station);
        sensor.setCode(jsonObject.get("code").toString());
        sensor.setStation(station);
        monitorService.add(sensor.getCode());
        sensorService.save(sensor);
        sensorService.flush();
        stationService.flush();
        monitorService.isActivation();
        sensorService.flush();
        List<Sensor> sensors = sensorService.findAll();
        Assert.assertTrue(sensors.size() == 1);
        Assert.assertTrue(sensors.get(0).isActivate());
    }

}
