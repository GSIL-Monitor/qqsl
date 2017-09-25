package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.SensorDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-4-6.
 */
@Service("sensorService")
public class SensorService extends BaseService<Sensor,Long>{
    @Autowired
    private SensorDao sensorDao;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMessageService userMessageService;
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    public void setBaseDao(SensorDao sensorDao) {
        super.setBaseDao(sensorDao);
    }

    /**
     * 只要保存就将change更改为true
     * @param sensor
     */
    @Override
    public void save(Sensor sensor){
//        sensor.setTransform(true);
        super.save(sensor);
    }

    public Sensor findByCode(String code){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("code", code));
        List<Sensor> sensors = sensorDao.findList(0, null, filters);
        if (sensors.size() == 1) {
            return sensors.get(0);
        }
        return null;
    }

    /**
     * 激活仪器
     * @return
     */
    public Message verify( Map<String,Object> map,Station station) {
        JSONObject infoJson = new JSONObject();
        JSONObject cameraJson;
        String code = map.get("code")==null?null:map.get("code").toString();
        Sensor sensor = new Sensor();
        if(map.get("type")!=null&&"camera".equals(map.get("type"))){
           sensor.setType(Sensor.Type.CAMERA);
           cameraJson = SettingUtils.convertMapToJson((Map<String,Object>)map.get("cameraUrl"));
           sensor.setCameraUrl(cameraJson.toString());
        }
        if(map.get("factory")!=null&&StringUtils.hasText(map.get("factory").toString())){
            infoJson.put("factory",map.get("factory").toString());
        }
        if(map.get("contact")!=null&&StringUtils.hasText(map.get("contact").toString())){
            infoJson.put("contact",map.get("contact").toString());
        }
        if(map.get("phone")!=null&&StringUtils.hasText(map.get("phone").toString())){
            if(!SettingUtils.phoneRegex(map.get("phone").toString())){
                return new Message(Message.Type.OTHER);
            }
            infoJson.put("phone",map.get("phone").toString());
        }
        if(map.get("settingHeight")!=null&&StringUtils.hasText(map.get("settingHeight").toString())){
            try{
                Double settingHeight = Double.valueOf(map.get("settingHeight").toString());
                sensor.setSettingHeight(Double.valueOf(map.get("settingHeight").toString()));
                if (settingHeight>100.0||settingHeight<0.0){
                    return new Message(Message.Type.FAIL);
                }
            }catch (NumberFormatException e){
                return new Message(Message.Type.FAIL);
            }
        }
        //当前测站为水位站时,只能绑定一个仪表,一个摄像头
        if (CommonEnum.StationType.WATER_LEVEL_STATION.equals(station.getType())) {
            Message message = isHydrologyStation(sensor,station);
            if (!message.getType().equals(Message.Type.OK)){
                return message;
            }
        }
        sensor.setCode(code);
        sensor.setActivate(false);
        sensor.setInfo(infoJson.isEmpty()?null:infoJson.toString());
        sensor.setStation(station);
        save(sensor);
        if(!Sensor.Type.CAMERA.equals(sensor.getType())){
            monitorService.add(code);
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 当前测站为水位站时,只能绑定一个仪表,一个摄像头
     * @param sensor
     * @param station
     * @return
     */
    private Message isHydrologyStation(Sensor sensor,Station station){
        List<Sensor> sensors = station.getSensors();
        if(sensors.size()==0){
            return new Message(Message.Type.OK);
        }
        for(int i = 0;i<sensors.size();i++){
            if(Sensor.Type.CAMERA.equals(sensors.get(i).getType())&&sensors.get(i).getType().equals(sensor.getType())){
                logger.info("测站Id:" + station.getId() + ",水位站已有仪表绑定");
                return new Message(Message.Type.EXIST);
            }
            if(!Sensor.Type.CAMERA.equals(sensor.getType())&&sensors.get(i).getType().equals(sensor.getType())){
                logger.info("测站Id:" + station.getId() + ",水位站已有仪表绑定");
                return new Message(Message.Type.EXIST);
            }
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 找出未绑定的仪表 =false
     * @return
     */
    public List<Sensor> findByActivate(){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("activate", false));
        List<Sensor> list = sensorDao.findList(0, null, filters);
        return list;
    }

    /**
     * 将未绑定的仪表加入缓存等待线程绑定
     */
    public void addCodeToCache(){
        List<Sensor> sensors = findByActivate();
        for (Sensor sensor : sensors) {
            monitorService.add(sensor.getCode());
        }
    }
    public List<Sensor> findByUserId(Long userId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userId", userId));
        List<Sensor> list = sensorDao.findList(0, null, filters);
        return list;
    }

    /**
     * 获取仪表列表Json数据
     * @param sensors
     * @return
     */
    public List<JSONObject> makeSensorJsons(List<Sensor> sensors) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject jsonObject;
        for(int i = 0;i<sensors.size();i++){
            jsonObject = makeSensorJson(sensors.get(i));
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }

    /**
     * 获取仪表Json数据
     * @param sensor
     * @return
     */
    public JSONObject makeSensorJson(Sensor sensor) {
        JSONObject jsonObject = new JSONObject();
        if(sensor==null){
            return jsonObject;
        }
        jsonObject.put("id",sensor.getId());
        jsonObject.put("createDate",sensor.getCreateDate());
        jsonObject.put("cameraUrl",sensor.getCameraUrl()==null?null:JSONObject.fromObject(sensor.getCameraUrl()));
        jsonObject.put("code",sensor.getCode());
        jsonObject.put("info",sensor.getInfo());
        jsonObject.put("modifyDate",sensor.getModifyDate());
        jsonObject.put("type",sensor.getType());
        jsonObject.put("settingHeight",sensor.getSettingHeight());
        jsonObject.put("activate",sensor.isActivate());
        jsonObject.put("station",sensor.getStation().getId());
        return jsonObject;
    }

    /**
     * 编辑仪表
     * @param map
     * @param sensor
     * @return
     */
    public Message editSensor(Map<String, Object> map, Sensor sensor) {
        JSONObject infoJson =  sensor.getInfo()==null?new JSONObject():JSONObject.fromObject(sensor.getInfo());
        if(map.get("phone")!=null&&StringUtils.hasText(map.get("phone").toString())){
            if(!SettingUtils.phoneRegex(map.get("phone").toString())){
                return new Message(Message.Type.FAIL);
            }
            infoJson.put("phone",map.get("phone").toString());
        }
        if(map.get("contact")!=null&&StringUtils.hasText(map.get("contact").toString())){
            infoJson.put("contact",map.get("contact").toString());
        }
        if(map.get("factory")!=null&&StringUtils.hasText(map.get("factory").toString())){
            infoJson.put("factory",map.get("factory").toString());
        }
        if(map.get("settingHeight")!=null&&StringUtils.hasText(map.get("settingHeight").toString())){
            try{
                Double settingHeight = Double.valueOf(map.get("settingHeight").toString());
                sensor.setSettingHeight(settingHeight);
                if (settingHeight>100.0||settingHeight<0.0){
                    return new Message(Message.Type.FAIL);
                }
            }catch (NumberFormatException e){
                return new Message(Message.Type.FAIL);
            }

        }
        sensor.setInfo(infoJson.isEmpty()?null:infoJson.toString());
        sensorDao.save(sensor);
        return new Message(Message.Type.OK);
    }

    /**
     * 摄像头编辑
     * @param cameraMap
     * @return
     */
    public Message editCamera(Map<String, Object> cameraMap,Sensor sensor) {
        if(!Sensor.Type.CAMERA.equals(sensor.getType())){
            return new Message(Message.Type.FAIL);
        }
        JSONObject infoJson = new JSONObject();
        if(cameraMap.get("phone")!=null&&StringUtils.hasText(cameraMap.get("phone").toString())){
            if(!SettingUtils.phoneRegex(cameraMap.get("phone").toString())){
                return new Message(Message.Type.FAIL);
            }
            infoJson.put("phone",cameraMap.get("phone").toString());
        }
        if(cameraMap.get("contact")!=null&&StringUtils.hasText(cameraMap.get("contact").toString())){
            infoJson.put("contact",cameraMap.get("contact").toString());
        }
        if(cameraMap.get("factory")!=null&&StringUtils.hasText(cameraMap.get("factory").toString())){
            infoJson.put("factory",cameraMap.get("factory").toString());
        }
        //rtmp://rtmp.open.ys7.com/openlive/ba4b2fde89ab43739e3d3e74d8b08f4a.hd
        if(cameraMap.get("cameraUrl")!=null&&StringUtils.hasText(cameraMap.get("cameraUrl").toString())){
            String cameraUrl = cameraMap.get("cameraUrl").toString();
            if(!SettingUtils.rtmpRegex(cameraUrl)){
                return new Message(Message.Type.FAIL);
            }
            sensor.setCameraUrl(cameraUrl);
        }
        sensorDao.save(sensor);
        return new Message(Message.Type.OK);
    }
}
