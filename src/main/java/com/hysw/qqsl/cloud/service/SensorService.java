package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.controller.Message;
import com.hysw.qqsl.cloud.dao.SensorDao;
import com.hysw.qqsl.cloud.entity.Filter;
import com.hysw.qqsl.cloud.entity.data.Sensor;
import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.entity.monitor.Share;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
        sensor.setTransform(true);
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
     * @param user
     * @param code
     * @param coordinate
     * @param remark
     * @param area
     * @return
     */
    public Message verify(User user, String code, Object coordinate, Object remark, Object area) {
        Message message = checkCoordinateIsInvalid(coordinate.toString());
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        JSONObject jsonObject = (JSONObject) message.getData();
        Sensor sensor1 = findByCode(code);
        if (sensor1!=null&&sensor1.getUserId() != null) {
            return new Message(Message.Type.EXIST);
        }
        Sensor sensor = new Sensor();
//            将用户及唯一码写入仪器表
        sensor.setUserId(user.getId());
        sensor.setCode(code);
        sensor.setCoordinate(jsonObject.toString());
        sensor.setActivate(false);
        sensor.setRemark(remark.toString());
        sensor.setArea(area.toString());
        save(sensor);
        monitorService.add(code);
        return new Message(Message.Type.OK);
    }

    /**
     * 检查坐标格式有效性ing转换为json格式
     * @return
     */
    private Message checkCoordinateIsInvalid(String coordinate){
        String[] coordinates = coordinate.split(",");
        if (coordinates.length != 3) {
            return new Message(Message.Type.FAIL);
        }
        if (Double.valueOf(coordinates[0]) > 180 || Double.valueOf(coordinates[0]) < 0) {
            return new Message(Message.Type.FAIL);
        }
        if (Double.valueOf(coordinates[1]) > 90 || Double.valueOf(coordinates[1]) < 0) {
            return new Message(Message.Type.FAIL);
        }
        if (Double.valueOf(coordinates[2]) < 0) {
            return new Message(Message.Type.FAIL);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("longitude", coordinates[0]);
        jsonObject.put("latitude", coordinates[1]);
        jsonObject.put("elevation", coordinates[2]);
        return new Message(Message.Type.OK, jsonObject);
    }

    /**
     * 编辑仪器参数
     * @param user
     * @param objectMap
     * @return
     */
    public Message editSensorParameter(User user,Map<String, Object> objectMap) {
        Object id = objectMap.get("id");
        if (id == null) {
            return new Message(Message.Type.FAIL);
        }
        Sensor sensor = find(Long.valueOf(id.toString()));
        if (sensor == null) {
            return new Message(Message.Type.EXIST);
        }
        if (!sensor.getUserId().toString().equals(user.getId().toString())) {
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        Object parameter = objectMap.get("parameter");
        if (parameter != null) {
            sensor.setParameter(String.valueOf(JSONObject.fromObject(parameter)));
        }
        save(sensor);
        return new Message(Message.Type.OK);
    }

    /**
     * 查询已被改变的仪表
     * @return
     */
    public JSONArray findByTransform(){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("transform", true));
        filters.add(Filter.eq("activate", true));
        List<Sensor> list = sensorDao.findList(0, null, filters);
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Sensor sensor : list) {
            jsonObject = new JSONObject();
            jsonObject.put("code", sensor.getCode());
            jsonObject.put("parameter", sensor.getParameter());
            jsonArray.add(jsonObject);
            sensor.setTransform(false);
            sensorDao.save(sensor);
        }
        return jsonArray;
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

    /**
     * 多人多仪表分享
     * @param sensorIds
     * @param userIds
     * @param own
     */
    public void shares(List<String> sensorIds, List<String> userIds, User own) {
        Share share;
        User user;
        Sensor sensor;
        for(int i = 0;i<sensorIds.size();i++){
            sensor = sensorDao.find(Long.valueOf(sensorIds.get(i)));
            if(sensor==null||!sensor.getUserId().equals(own.getId())){
                continue;
            }
            share = makeShare(sensor);
            for(int k = 0;k<userIds.size();k++){
                user = userService.find(Long.valueOf(userIds.get(k)));
                if(user.getId().equals(own.getId())){
                    continue;
                }
                    share.register(user);
            }
            saveShare(share);
        }
    }
    /**
     * 取消分享
     * @param sensor
     * @param userIds
     * @param own
     */
    public void unShare(Sensor sensor, List<String> userIds, User own) {
        Long userId;
        User user;
        boolean flag;
        Share share = makeShare(sensor);
        for(int i = 0;i<userIds.size();i++){
            userId = Long.valueOf(userIds.get(i));
            user = userService.find(userId);
            if(user==null){
                continue;
            }
            flag = share.unRegister(user);
            if(flag){
                saveShare(share,user,own);
            }
        }
    }
    /**
     * 构建仪表分享对象
     * @param sensor
     * @return
     */
    private Share makeShare(Sensor sensor){
        Share share = new Share(sensor);
        JSONArray jsonArray;
        if (StringUtils.hasText(sensor.getShares())) {
            jsonArray = JSONArray.fromObject(sensor.getShares());
        } else {
            jsonArray = new JSONArray();
        }
        if (jsonArray.isEmpty()) {
            return share;
        }
        User user;
        JSONObject jsonObject;
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            user = new User();
            user.setId(jsonObject.getLong("id"));
            user.setName(jsonObject.getString("name"));
            user.setPhone(jsonObject.getString("phone"));
            share.register(user);
        }
        return share;
    }

    private void saveShare(Share share,User user,User own){
        Sensor sensor = share.getSener();
        JSONArray shareJsons = share.toJson();
        sensor.setShares(shareJsons.isEmpty()?null:shareJsons.toString());
        sensorDao.save(sensor);
        //记录仪表分享消息
      //  userMessageService.sensorShareMessage(sensor,user,own,true);
    }

    private void saveShare(Share share){
        Sensor sensor = share.getSener();
        JSONArray shareJsons = share.toJson();
        sensor.setShares(shareJsons.isEmpty()?null:shareJsons.toString());
        sensorDao.save(sensor);
    }

    /**
     * 获取仪表包括自己的和别人分享的
     * @param user
     * @return
     */
    private List<Sensor> getSensors(User user) {
        List<Sensor> sensors = findAll();
        List<Sensor> sensorList = new ArrayList<>();
        if(sensors.size()==0){
            return sensors;
        }
        Sensor sensor;
        for(int i = 0;i<sensors.size();i++){
         sensor = sensors.get(i);
            if(isShare(user,sensor)){
                sensorList.add(sensor);
            }
        }
        return sensorList;
    }

    /**
     * 获取仪表包括自己的和别人分享的
     * @param user
     * @return
     */
    public JSONArray getSensorJsons(User user){
        List<Sensor> sensors = getSensors(user);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for(int i=0;i<sensors.size();i++){
            jsonObject = toJson(sensors.get(i));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * sensor json
     * @param sensor
     * @return
     */
    private JSONObject toJson(Sensor sensor){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",sensor.getId());
        jsonObject.put("userId",sensor.getUserId());
        jsonObject.put("code",sensor.getCode());
        jsonObject.put("type",sensor.getType());
        jsonObject.put("address",sensor.getAddress());
        jsonObject.put("coordinate",sensor.getCoordinate());
        jsonObject.put("remark",sensor.getRemark());
        jsonObject.put("parameter",sensor.getParameter());
        jsonObject.put("area",sensor.getArea());
        JSONArray jsonArray = sensor.getShares()==null?new JSONArray():JSONArray.fromObject(sensor.getShares());
        jsonObject.put("shares",jsonArray);
        if (sensor.getVideo() != null) {
            jsonObject.put("video", sensor.getVideo());
        }
        jsonObject.put("activate", sensor.isActivate());
        return jsonObject;
    }

    public List<Sensor> findByUserId(Long userId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userId", userId));
        List<Sensor> list = sensorDao.findList(0, null, filters);
        return list;
    }

    /**
     * 判断是否分享于该用户
     * @param user
     * @param sensor
     * @return
     */
    private boolean isShare(User user,Sensor sensor){
        if(user.getId().equals(sensor.getUserId())){
            return true;
        }
        Share share = makeShare(sensor);
        List<User> users = share.getShareUsers();
        for(int i = 0;i<users.size();i++){
            if(user.getId().equals(users.get(i).getId())){
                return true;
            }
        }
        return false;
    }

    public Message sensorToJson(Sensor sensor) {
        JSONObject jsonObject = toJson(sensor);
        return new Message(Message.Type.OK, jsonObject);
    }

    /**
     * 编辑仪表基本信息
     * @param user
     * @param objectMap
     * @return
     */
    public Message editSensor(User user, Map<String, Object> objectMap) {
        Object sensor1 = objectMap.get("sensor");
        if (sensor1 == null) {
            return new Message(Message.Type.FAIL);
        }
        Object coordinate = ((Map<String,Object>) sensor1).get("coordinate");
        Object remark = ((Map<String,Object>) sensor1).get("remark");
        Object area = ((Map<String,Object>) sensor1).get("area");
        Object id = objectMap.get("id");
        if (id == null || coordinate == null || remark == null || area == null) {
            return new Message(Message.Type.FAIL);
        }
        Sensor sensor = find(Long.valueOf(id.toString()));
        if (sensor == null) {
            return new Message(Message.Type.EXIST);
        }
        if (!user.getId().toString().equals(sensor.getUserId().toString())) {
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        Message message = checkCoordinateIsInvalid(coordinate.toString());
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        JSONObject jsonObject = (JSONObject) message.getData();
        sensor.setCoordinate(jsonObject.toString());
        sensor.setRemark(remark.toString());
        sensor.setArea(area.toString());
        save(sensor);
        return new Message(Message.Type.OK);
    }
}
