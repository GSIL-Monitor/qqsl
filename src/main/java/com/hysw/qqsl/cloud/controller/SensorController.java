package com.hysw.qqsl.cloud.controller;

import com.hysw.qqsl.cloud.entity.data.Account;
import com.hysw.qqsl.cloud.entity.data.Sensor;
import com.hysw.qqsl.cloud.entity.data.User;
import com.hysw.qqsl.cloud.service.*;
import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-3-29.
 */
@Controller
@RequestMapping("/sensor")
public class SensorController {
    @Autowired
    private ApplicationTokenService applicationTokenService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private UserService userService;
    /**
     * 获取token
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public @ResponseBody Message getToken() {
//        User user = userService.getUserFormSubject();
//        if (user == null) {
//            return new Message(Message.Type.NO_SESSION);
//        }
        return new Message(Message.Type.OK, applicationTokenService.getToken());
    }

    /**
     * 激活并且绑定监测仪器
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public @ResponseBody Message verify(@RequestBody Map<String,Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        //唯一标识
        Object code = objectMap.get("code");
        //密文
        Object ciphertext = objectMap.get("ciphertext");
        Object coordinate = objectMap.get("coordinate");
        Object remark = objectMap.get("remark");
        Object area = objectMap.get("area");
        if (code == null || ciphertext == null || coordinate == null || remark == null || area == null||remark.toString().equals("")||area.toString().equals("")) {
            return new Message(Message.Type.FAIL);
        }
        //加密并验证密码是否一致
        boolean verify = monitorService.verify(code.toString(), ciphertext.toString());
        if (verify) {
//             注册成功
            return sensorService.verify(user,code.toString(),coordinate,remark,area);
        }
        return new Message(Message.Type.FAIL);

    }

    /**
     * 编辑传感器参数
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editParameter", method = RequestMethod.POST)
    public @ResponseBody Message editParameter(@RequestBody Map<String,Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.FAIL);
        }
        return sensorService.editSensorParameter(user,objectMap);
    }

    /**
     * 编辑传感器
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public @ResponseBody Message edit(@RequestBody Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.FAIL);
        }
        return sensorService.editSensor(user, objectMap);
    }

    /**
     * 获取当前用户的传感器列表
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public @ResponseBody Message getSensors() {
        Account account = authentService.getAccountFromSubject();
        if(account!=null){
            return new Message(Message.Type.OK, new ArrayList<>());
        }
        User user = authentService.getUserFromSubject();
       // List<Sensor> sensors = sensorService.findByUserId(user.getId());
        List<JSONObject> sensors = sensorService.getSensorJsons(user);
        return new Message(Message.Type.OK, sensors);
    }

    /**
     * 根据id查询仪表
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getSensor", method = RequestMethod.GET)
    public @ResponseBody Message getSensors(@RequestParam Long id) {
        Sensor sensor = null;
        try {
            sensor= sensorService.find(id);

        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if (sensor == null) {
            return new Message(Message.Type.FAIL);
        }
        return sensorService.sensorToJson(sensor);
    }

    /**
     * 返回已修改过的参数列表
     * @return
     */
    @RequestMapping(value = "/sensor", method = RequestMethod.GET)
    public @ResponseBody Message getSensorParameter(@RequestParam String token) {
        if (token.length() == 0) {
            return new Message(Message.Type.FAIL);
        }
        if (applicationTokenService.decrypt(token)) {
            return new Message(Message.Type.OK,sensorService.findByTransform());
        }
        return new Message(Message.Type.FAIL);
    }

    /**
     * 仪表取消分享
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles({"user:hydrology"})
    @RequestMapping(value = "/unShare", method = RequestMethod.POST)
    public @ResponseBody Message unShare(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(!message.getType().equals(Message.Type.OK)){
            return message;
        }
        String userIdsStr = map.get("userIds").toString();
        User own = authentService.getUserFromSubject();
        Sensor sensor = sensorService.find(Long.valueOf(map.get("sensorId").toString()));
        if(sensor==null||!sensor.getUserId().equals(own.getId())){
            return new Message(Message.Type.UNKNOWN);
        }
        List<String> userIds = Arrays.asList(userIdsStr.split(","));
        sensorService.unShare(sensor,userIds,own);
        return new Message(Message.Type.OK);
    }

    /**
     * 多人多仪表分享
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles({"user:hydrology"})
    @RequestMapping(value = "/share", method = RequestMethod.POST)
    public @ResponseBody Message shares(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(!message.getType().equals(Message.Type.OK)){
            return message;
        }
        String userIdsStr = map.get("userIds").toString();
        User own = authentService.getUserFromSubject();
        String seneorIdsStr = map.get("sensorIds").toString();
        List<String> userIds = Arrays.asList(userIdsStr.split(","));
        List<String> sensorIds = Arrays.asList(seneorIdsStr.split(","));
        sensorService.shares(sensorIds,userIds,own);
        return new Message(Message.Type.OK);
    }

}

