package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.SensorDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Camera;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.SensorAttribute;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
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
    private SensorAttributeService sensorAttributeService;
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
            Sensor sensor = sensors.get(0);
            sensor.getStation().getType();
            return sensor;
        }
        return null;
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
        jsonObject.put("createDate",sensor.getCreateDate().getTime());
        jsonObject.put("phone",sensor.getPhone());
        jsonObject.put("contact",sensor.getContact());
        jsonObject.put("minValue",sensor.getMinValue());
        jsonObject.put("code",sensor.getCode());
        jsonObject.put("maxValue",sensor.getMaxValue());
        jsonObject.put("type",sensor.getType());
        jsonObject.put("settingHeight",sensor.getSettingHeight());
        jsonObject.put("description",sensor.getDescription());
        jsonObject.put("factory",sensor.getFactory());
        jsonObject.put("measureRange",sensor.getMeasureRange());
        jsonObject.put("name",sensor.getName());
        jsonObject.put("pictureUrl",sensor.getPictureUrl());
        jsonObject.put("activate",sensor.isActivate());
        jsonObject.put("isMaxValueWaring",sensor.isMaxValueWaring());
        jsonObject.put("isMinValueWaring",sensor.isMinValueWaring());
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1;
        for (SensorAttribute sensorAttribute : sensorAttributeService.findBySensor(sensor)) {
            jsonObject1 = new JSONObject();
            jsonObject1.put("id", sensorAttribute.getId());
            jsonObject1.put("value", sensorAttribute.getValue());
            jsonObject1.put("valueType", sensorAttribute.getValueType());
            jsonObject1.put("displayName", sensorAttribute.getDisplayName());
            jsonObject1.put("name", sensorAttribute.getName());
            jsonObject1.put("type", sensorAttribute.getType());
            jsonArray.add(jsonObject1);
        }
        jsonObject.put("sensorAttributes", jsonArray);
        jsonObject.put("settingAddress",sensor.getSettingAddress());
        jsonObject.put("settingElevation",sensor.getSettingElevation());
        return jsonObject;
    }

    /**
     * 编辑仪表
     * @param sensor
     * @param name
     * @param description
     * @param factory
     * @param contact
     * @param phone
     * @param settingHeight
     * @param settingElevation
     * @param settingAddress
     * @param measureRange
     * @param maxValue
     * @param isMaxValueWaring
     * @param minValue
     * @param isMinValueWaring
     * @return
     */
    public void editSensor(Sensor sensor, Object name, Object description, Object factory, Object contact, Object phone, Object settingHeight, Object settingElevation, Object settingAddress, Object measureRange, Object maxValue, Object isMaxValueWaring, Object minValue, Object isMinValueWaring) {
        if (name != null) {
            if (name.equals("")) {
                sensor.setName(null);
            } else {
                sensor.setName(name.toString());
            }
        }
        if (description != null) {
            if (description.equals("")) {
                sensor.setDescription(null);
            } else {
                sensor.setDescription(description.toString());
            }
        }
        if (factory != null) {
            if (factory.equals("")) {
                sensor.setFactory(null);
            } else {
                sensor.setFactory(factory.toString());
            }
        }
        if (contact != null) {
            if (contact.equals("")) {
                sensor.setContact(null);
            } else {
                sensor.setContact(contact.toString());
            }
        }
        if (phone != null) {
            if (phone.equals("")) {
                sensor.setPhone(null);
            } else {
                sensor.setPhone(phone.toString());
            }
        }
        if (settingHeight != null) {
            if (settingHeight.equals("")) {
                sensor.setSettingHeight(null);
            } else {
                sensor.setSettingHeight(Double.valueOf(settingHeight.toString()));
            }
        }
        if (settingElevation != null) {
            if (settingElevation.equals("")) {
                sensor.setSettingElevation(null);
            } else {
                sensor.setSettingElevation(Double.valueOf(settingElevation.toString()));
            }
        }
        if (settingAddress != null) {
            if (settingAddress.equals("")) {
                sensor.setSettingAddress(null);
            } else {
                sensor.setSettingAddress(settingAddress.toString());
            }
        }
        if (measureRange != null) {
            if (measureRange.equals("")) {
                sensor.setMeasureRange(null);
            } else {
                sensor.setMeasureRange(measureRange.toString());
            }
        }
        if (maxValue != null) {
            if (maxValue.equals("")) {
                sensor.setMaxValue(null);
            } else {
                sensor.setMaxValue(Double.valueOf(maxValue.toString()));
            }
        }
        if (isMaxValueWaring != null) {
            if (isMaxValueWaring.equals("")) {
                sensor.setMaxValueWaring(false);
            } else {
                sensor.setMaxValueWaring(Boolean.valueOf(isMaxValueWaring.toString()));
            }
        }
        if (minValue != null) {
            if (minValue.equals("")) {
                sensor.setMinValue(null);
            } else {
                sensor.setMinValue(Double.valueOf(minValue.toString()));
            }
        }
        if (isMinValueWaring != null) {
            if (isMinValueWaring.equals("")) {
                sensor.setMinValueWaring(false);
            } else {
                sensor.setMinValueWaring(Boolean.valueOf(isMinValueWaring.toString()));
            }
        }
        sensor.setChanged(true);
        save(sensor);
    }

    public List<Sensor> findByStation(Station station) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("station", station));
        return sensorDao.findList(0, null, filters);
    }

    /**
     * 获取仪表Json数据
     * @param sensor
     * @return
     */
    public JSONObject makeSensorSimpleJson(Sensor sensor) {
        JSONObject jsonObject = new JSONObject();
        if(sensor==null){
            return jsonObject;
        }
        jsonObject.put("id",sensor.getId());
        jsonObject.put("code",sensor.getCode());
        jsonObject.put("name",sensor.getName());
        return jsonObject;
    }
}
