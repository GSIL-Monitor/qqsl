package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.service.SensorService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 仪表Service类
 *
 * @since 2017年6月26日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class SensorServiceTest extends BaseTest {

    @Autowired
    private SensorService sensorService;
    private Long uid = 24l;

    /**
     * 给24/13028710937账号准备仪表
     */
    @Test
    public void prepareSensors() {
        // 宏电的仪表
    /*    Sensor sensor = null;
        String code = "0010000002";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.IRTU);
            sensor.setActivate(true);
            sensor.setAddress("青海湟水河");
            sensor.setCode(code);
            sensor.setRemark("湟水河1＃");
            sensor.setArea("西宁市湟水河");
            sensor.setCoordinate("{\"longitude\":\"101.00\",\"latitude\":\"33.00\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "0020000002";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.IRTU);
            sensor.setActivate(true);
            sensor.setAddress("青海湟水河");
            sensor.setCode(code);
            sensor.setRemark("湟水河2＃");
            sensor.setArea("西宁市湟水河");
            sensor.setCoordinate("{\"longitude\":\"101.01\",\"latitude\":\"33.01\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "ABBBFE90AEBCD765BA";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.CANAL_FLOW);
            sensor.setActivate(true);
            sensor.setAddress("青海明渠流量计");
            sensor.setCode(code);
            sensor.setRemark("明渠流量计1＃");
            sensor.setArea("西宁市城东区");
            sensor.setCoordinate("{\"longitude\":\"101.02\",\"latitude\":\"33.02\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "BBBBFE90AEBCD765AA";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.DOPPLER_FLOW);
            sensor.setActivate(false);
            sensor.setAddress("青海多普勒流速计");
            sensor.setCode(code);
            sensor.setRemark("多普勒流速计1＃");
            sensor.setArea("西宁市城东区");
            sensor.setCoordinate("{\"longitude\":\"101.03\",\"latitude\":\"33.03\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "CCBBFE90AEBCD765BA";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.RADAR);
            sensor.setActivate(true);
            sensor.setAddress("青海雷达物位计");
            sensor.setCode(code);
            sensor.setRemark("雷达物位计1＃");
            sensor.setArea("西宁市城东区");
            sensor.setCoordinate("{\"longitude\":\"101.04\",\"latitude\":\"33.04\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "DDBBFE90AEBCD765BA";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.THROW_WATER);
            sensor.setActivate(true);
            sensor.setAddress("青海投入式液位计");
            sensor.setCode(code);
            sensor.setRemark("投入式液位计1＃");
            sensor.setArea("西宁市城东区");
            sensor.setCoordinate("{\"longitude\":\"101.05\",\"latitude\":\"33.05\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "EEBBFE90AEBCD765BA";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.ULTRA_DIFF);
            sensor.setActivate(true);
            sensor.setAddress("青海超声波液差计");
            sensor.setCode(code);
            sensor.setRemark("超声波液差计1＃");
            sensor.setArea("西宁市城东区");
            sensor.setCoordinate("{\"longitude\":\"101.06\",\"latitude\":\"33.06\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }
        code = "FFBBFE90AEBCD765BA";
        sensor = sensorService.findByCode(code);
        if (sensor==null) {
            sensor = new Sensor();
            sensor.setUserId(uid);
            sensor.setType(Sensor.Type.ULTRA_WATER);
            sensor.setActivate(true);
            sensor.setAddress("青海超声波液位仪");
            sensor.setCode(code);
            sensor.setRemark("超声波液位仪1＃");
            sensor.setArea("西宁市城东区");
            sensor.setCoordinate("{\"longitude\":\"101.07\",\"latitude\":\"33.07\", \"elevation\": \"0\"}");
            sensorService.save(sensor);
        }*/


    }
}
