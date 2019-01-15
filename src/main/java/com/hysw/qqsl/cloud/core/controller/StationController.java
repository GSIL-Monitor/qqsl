package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.annotation.util.StationIsExpire;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.util.TradeUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import sun.misc.resources.Messages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Create by leinuo on 17-8-17 上午10:52
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 测站控制层
 */
@Controller
@RequestMapping("/station")
public class StationController {
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private StationService stationService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private ApplicationTokenService applicationTokenService;
    @Autowired
    private PollingService pollingService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private CameraService cameraService;
    @Autowired
    private SensorAttributeService sensorAttributeService;
    /**
     * 获取token
     * @return message消息体,附带token令牌
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public @ResponseBody
    Message getToken() {
//        User user = authentService.getUserFromSubject();
//        if (user == null) {
//            user = authentService.getAccountFromSubject()==null?null:authentService.getAccountFromSubject().getUser();
//        }
//        if (user == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        user = userService.find(16l);
        return MessageService.message(Message.Type.OK, applicationTokenService.makeIntendedEffectToken());
    }

    /**
     * 获取测站列表包括分享的测站
     * @return message消息体,OK:获取成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/zTree/list",method = RequestMethod.GET)
    public @ResponseBody Message getStations(){
        User user = authentService.getUserFromSubject();
        List<JSONObject> jsonObjectList = stationService.getStations(user);
//        pollingService.changeStationStatus(user, false);
        return MessageService.message(Message.Type.OK,jsonObjectList);
    }

    /**
     * 获取测站列表包括分享的测站
     * @return message消息体,OK:获取成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public @ResponseBody Message getLists(){
        User user = authentService.getUserFromSubject();
        List<JSONObject> jsonObjectList = stationService.getList(user);
        pollingService.changeStationStatus(user, false);
        return MessageService.message(Message.Type.OK,jsonObjectList);
    }

    /**
     * 取得站点详情
     * @param id 测站id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/details/{id}",method = RequestMethod.GET)
    public @ResponseBody Message detailsId(@PathVariable("id") Long id){
        Station station = stationService.find(id);
        if (station == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
//        User user = authentService.getUserFromSubject();
//        if (!user.getId().equals(station.getUser().getId())) {
//            return MessageService.message(Message.Type.DATA_REFUSE);
//        }
        JSONObject jsonObject = stationService.toJSON(station);
        return MessageService.message(Message.Type.OK, jsonObject);
    }


    /**
     * 取得仪表详情
     * @param id 仪表id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/sensor/details/{id}",method = RequestMethod.GET)
    public @ResponseBody Message sensorDetailsId(@PathVariable("id") Long id){
        Sensor sensor = sensorService.find(id);
        if (sensor == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
//        User user = authentService.getUserFromSubject();
//        if (!user.getId().equals(sensor.getStation().getUser().getId())) {
//            return MessageService.message(Message.Type.DATA_REFUSE);
//        }
        JSONObject jsonObject = sensorService.makeSensorJson(sensor);
        return MessageService.message(Message.Type.OK, jsonObject);
    }


    /**
     * 取得摄像头详情
     * @param id 摄像头id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/camera/details/{id}",method = RequestMethod.GET)
    public @ResponseBody Message cameraDetailsId(@PathVariable("id") Long id){
        Camera camera = cameraService.find(id);
        if (camera == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
//        User user = authentService.getUserFromSubject();
//        if (!user.getId().equals(camera.getStation().getUser().getId())) {
//            return MessageService.message(Message.Type.DATA_REFUSE);
//        }
        JSONObject jsonObject = cameraService.makeCameraJson(camera);
        return MessageService.message(Message.Type.OK, jsonObject);
    }




    /**
     * 安布雷拉水文用户新建站点
     *
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody
    Message abllCreate(@RequestBody Map<String, Object> map) {
        User user = authentService.getUserFromSubject();
        Object name = map.get("name");
        Object type = map.get("type");
        Object description = map.get("description");
        if (name == null || type == null || description == null) {
            return MessageService.message(Message.Type.FAIL);
        }

        List<Station> byUser = stationService.findByUser(user);
        if (byUser.size() > 1000) {
            return MessageService.message(Message.Type.DATA_LOCK);
        }
        JSONObject jsonObject = stationService.abllCreateStation(user, name, type, description);
        return MessageService.message(Message.Type.OK,jsonObject);
    }


    /**
     * 测站编辑
     * @param map 包含测站id,以及测站类型type,测站名字name,测站描述description,测站地址address,测站坐标coor
     * @return message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
     */
    @StationIsExpire("station")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/edit",method = RequestMethod.POST)
    public @ResponseBody Message editStation(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(map.get("id")==null){
            return MessageService.message(Message.Type.FAIL);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (stationService.edit(map, station)) {
            return MessageService.message(Message.Type.OK);
        }
        return MessageService.message(Message.Type.FAIL);
    }

    @RequiresAuthentication
    @RequiresRoles(value = {"user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message delete(@PathVariable("id") Long id){
        Station station = stationService.find(id);
        if (station == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }

        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(station.getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        List<Camera> cameras = cameraService.findByStation(station);
        List<Sensor> sensors = sensorService.findByStation(station);
        if (sensors.size() != 0 || cameras.size() != 0) {
            return MessageService.message(Message.Type.DATA_LOCK);
        }
        stationService.remove(station);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 仪表添加
     * @param map 包含测站id,以及仪表参数factory,contact,phone,settingHeight,ciphertext,code
     * @return message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:添加成功,FAIL:添加失败
     */
    @StationIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/addSensor",method = RequestMethod.POST)
    public @ResponseBody Message addSensor(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object id = map.get("id");
        Object name = map.get("name");
        Object code = map.get("code");
        Object ciphertext = map.get("ciphertext");
        if (id == null || name == null || code == null || ciphertext == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Station station = stationService.find(Long.valueOf(id.toString()));
        if (station == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (monitorService.verify(code.toString(),ciphertext.toString())) {
            Sensor sensor = sensorService.findByCode(code.toString());
            if (sensor != null) {
                return MessageService.message(Message.Type.DATA_EXIST);
            }
            //注册成功
            sensor = new Sensor();
            sensor.setName(name.toString());
            sensor.setCode(code.toString());
            sensor.setStation(station);
            sensorService.save(sensor);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", sensor.getId());
            jsonObject.put("name", sensor.getName());
            return MessageService.message(Message.Type.OK, jsonObject);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 添加摄像头
     * @param map 包含测站id,以及摄像头参数factory,contact,phone,settingHeight,cameraUrl
     * @return  message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:添加成功,FAIL:添加失败
     */
    @StationIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/addCamera",method = RequestMethod.POST)
    public @ResponseBody Message addCamera(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object id = map.get("id");
        Object name = map.get("name");
        Object code = map.get("code");
//        Object password = map.get("password");
        if (id == null || name == null || code == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Station station = stationService.find(Long.valueOf(id.toString()));
        if(!isOperate(station)){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        Camera camera = cameraService.findByCode(code.toString());
        if (camera != null) {
            return MessageService.message(Message.Type.DATA_EXIST);
        }
        camera = new Camera();
        camera.setName(name.toString());
        camera.setStation(station);
        camera.setCode(code.toString());
//        camera.setPassword(password.toString());
        cameraService.save(camera);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", camera.getId());
        jsonObject.put("name", camera.getName());
        return MessageService.message(Message.Type.OK, jsonObject);
    }


    /**
     * 编辑仪表
     * @param map 包含测站station,以及仪表参数id,factory,contact,phone,settingHeight
     * @return message消息体,EXIST:仪表或测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
     */
    @StationIsExpire("sensor")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/editSensor",method = RequestMethod.POST)
    public @ResponseBody Message editSensor(@RequestBody Map<String,Object> map){
        Object id = map.get("id");
        Object name = map.get("name");
        Object description = map.get("description");
        Object factory = map.get("factory");
        Object contact = map.get("contact");
        Object phone = map.get("phone");
        Object settingHeight = map.get("settingHeight");
        Object settingElevation = map.get("settingElevation");
        Object settingAddress = map.get("settingAddress");
        Object measureRange = map.get("measureRange");
        Object maxValue = map.get("maxValue");
        Object isMaxValueWaring = map.get("isMaxValueWaring");
        Object minValue = map.get("minValue");
        Object isMinValueWaring = map.get("isMinValueWaring");
        Object extraParameters = map.get("extraParameters");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Sensor sensor = sensorService.find(Long.valueOf(id.toString()));
        if (sensor == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(sensor.getStation())){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        sensorService.editSensor(sensor, name, description, factory, contact, phone, settingHeight, settingElevation, settingAddress, measureRange, maxValue, isMaxValueWaring, minValue, isMinValueWaring);
        JSONObject jsonObject;
        SensorAttribute sensorAttribute;
        if (extraParameters != null) {
            for (Object o : JSONArray.fromObject(extraParameters)) {
                jsonObject = JSONObject.fromObject(o);
                if (jsonObject.get("id") != null) {
                    sensorAttribute = sensorAttributeService.find(Long.valueOf(jsonObject.get("id").toString()));
                    if (sensorAttribute != null) {
                        sensorAttribute.setValue(jsonObject.get("value") == null ? null : jsonObject.get("value").toString());
                        sensorAttributeService.save(sensorAttribute);
                    }
                }
            }
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 仪表添加自定义属性
     * @param map <ul>
     *            <li>id：仪表id</li>
     *            <li>displayName: 属性名</li>
     * </ul>
     * @return <ul>
     *     <li>OK，添加成功</li>
     *     <li>DATA_REFUSE：不是自己的仪表</li>
     *     <li>DATA_NOEXIST:仪表不存在</li>
     *     <li>DATA_LOCK: 属性超多20个，不能再添加</li>
     * </ul>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/sensor/extra/create",method = RequestMethod.POST)
    public @ResponseBody Message extraCreate(@RequestBody Map<String,Object> map){
        Object id = map.get("id");
        Object displayName = map.get("displayName");
        Object value = map.get("value");
        if (id == null || displayName == null || value == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Sensor sensor = sensorService.find(Long.valueOf(id.toString()));
        if (sensor == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(sensor.getStation().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (sensorAttributeService.findBySensor(sensor).size() > 20) {
            return MessageService.message(Message.Type.DATA_LOCK);
        }
        SensorAttribute sensorAttribute = new SensorAttribute();
        sensorAttribute.setDisplayName(displayName.toString());
        sensorAttribute.setType(SensorAttribute.Type.CUSTOM);
        sensorAttribute.setValue(value.toString());
        sensorAttribute.setSensor(sensor);
        sensorAttributeService.save(sensorAttribute);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 仪表删除自定义属性
     * @param id 自定义属性id
     * @return <ul>
     *     <li>OK，添加成功</li>
     *     <li>DATA_REFUSE：不是自己的仪表</li>
     *     <li>DATA_NOEXIST:仪表不存在</li>
     *     <li>DATA_LOCK: 属性类型是系统属性，不能删除</li>
     * </ul>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/sensor/extra/delete/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message extraDelete(@PathVariable("id") Long id){
        SensorAttribute sensorAttribute = sensorAttributeService.find(id);
        if (sensorAttribute == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(sensorAttribute.getSensor().getStation().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (sensorAttribute.getType() == SensorAttribute.Type.SYSTEM) {
            return MessageService.message(Message.Type.DATA_LOCK);
        }
        sensorAttributeService.remove(sensorAttribute);
        return MessageService.message(Message.Type.OK);
    }


        /**
         * 编辑摄像头
         * @param map  包含测站station,以及摄像头参数,id,factory,contact,phone,settingHeight,cameraUrl
         * @return  message消息体,EXIST:仪表或测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
         */
    @StationIsExpire("camera")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/editCamera",method = RequestMethod.POST)
    public @ResponseBody Message editCamera(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object id = map.get("id");
        Object name = map.get("name");
        Object description = map.get("description");
        Object factory = map.get("factory");
        Object contact = map.get("contact");
        Object phone = map.get("phone");
        Object settingAddress = map.get("settingAddress");
        Object code = map.get("code");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Camera camera = cameraService.find(Long.valueOf(id.toString()));
        if(camera==null){
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(camera.getStation())){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        try {
            Camera camera1 = cameraService.findByCode(code.toString());
            if (camera1 != null) {
                return MessageService.message(Message.Type.DATA_EXIST);
            }
        } catch (Exception e) {
            return MessageService.message(Message.Type.FAIL);
        }
        cameraService.editCamera(camera,name,description,factory,contact,phone,settingAddress,code);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 仪表删除
     * @param id 仪表id
     * @return message消息体,EXIST:仪表不存在,OK:删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteSensor/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message deleteSensor(@PathVariable("id") Long id){
        Sensor sensor = sensorService.find(id);
        if (sensor == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(sensor.getStation())){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        sensorService.remove(sensor);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 摄像头删除
     * @param id 摄像头id
     * @return message消息体,EXIST:仪表不存在,OK:删除成功,FAIL:删除失败
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteCamera/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message deleteCamera(@PathVariable("id") Long id){
        Camera camera = cameraService.find(id);
        if (camera == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(camera.getStation())){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        cameraService.remove(camera);
        return MessageService.message(Message.Type.OK);
    }





    /**
     * 河道模型和水位流量关系曲线上传
     * @param request 请求消息体,包含测站id,文件名,以及文件内容
     * @return   message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:上传成功,FAIL:上传失败
     */
    @StationIsExpire("request")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadModel" ,method = RequestMethod.POST)
    public @ResponseBody Message uploadModel(HttpServletRequest request){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        Long id = Long.valueOf(request.getParameter("id"));
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            MultipartFile mFile = map.get("file");
            try{
                return MessageService.message(Message.Type.OK, stationService.readModelFile(mFile, station));
            }catch (Exception e){
                e.printStackTrace();
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 河道模型和水位流量关系曲线下载
     * @param id 测站id
     * @param response 响应消息体
     * @return message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:下载成功,FAIL:下载失败
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadModel",method = RequestMethod.GET)
    public @ResponseBody Message downloadModel(@RequestParam long id,HttpServletResponse response){
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        Workbook workbook = stationService.makeStationModelData(station);
        ByteArrayOutputStream bos = null;
        InputStream is = null;
        OutputStream output = null;
        try {
            bos = new ByteArrayOutputStream();
            workbook.write(bos);
            is = new ByteArrayInputStream(bos.toByteArray());
            String contentType = "application/vnd.ms-excel";
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"station.xlsx" + "\"");
            output = response.getOutputStream();
            byte b[] = new byte[1024];
            while (true) {
                int length = is.read(b);
                if (length == -1) {
                    break;
                }
                output.write(b, 0, length);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }finally {
            IOUtils.safeClose(bos);
            IOUtils.safeClose(is);
            IOUtils.safeClose(output);
        }
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 监测取得仪表参数：getParamters，GET
     * 监测系统取得所有已改变的参数列表
     * @param token 自定义安全令牌token
     * @return FAIL:获取失败，OK：获取成功，附带测站参数
     */
    @RequestMapping(value = "/sensor/getParameters", method = RequestMethod.GET)
    public @ResponseBody Message getParameters(@RequestParam String token){
        Message message = CommonController.parametersCheck(token);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if(!applicationTokenService.decrypt(token)){
            return MessageService.message(Message.Type.FAIL);
        }
        JSONArray paramters = stationService.getParameters();
        return MessageService.message(Message.Type.OK,paramters);
    }


    /**
     * 编辑测站封面照片
     * @param map <ul>
     *            <li>id：测站id</li>
     *            <li>pictureUrl：封面图片oss地址</li>
     * </ul>
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/pictureUrl/edit",method = RequestMethod.POST)
    public @ResponseBody Message editPictureUrl(@RequestBody Map<String,Object> map){
        Object id = map.get("id");
        Object pictureUrl = map.get("pictureUrl");
        if (id == null || pictureUrl == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Station station = stationService.find(Long.valueOf(id.toString()));
        if (station == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!station.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        station.setPictureUrl(pictureUrl.toString());
        stationService.save(station);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑仪表封面照片
     * @param map <ul>
     *            <li>id：仪表id</li>
     *            <li>pictureUrl：封面图片oss地址</li>
     * </ul>
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","user:abll"}, logical = Logical.OR)
    @RequestMapping(value = "/sensor/pictureUrl/edit",method = RequestMethod.POST)
    public @ResponseBody Message editSensorPictureUrl(@RequestBody Map<String,Object> map){
        Object id = map.get("id");
        Object pictureUrl = map.get("pictureUrl");
        if (id == null || pictureUrl == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Sensor sensor = sensorService.find(Long.valueOf(id.toString()));
        if (sensor == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!sensor.getStation().getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        sensor.setPictureUrl(pictureUrl.toString());
        sensorService.save(sensor);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 取消测站分享
     * @param map 包含用户userIds,以及测站stationId
     * @return message消息体,OK:取消成功,FAIL:取消失败,UNKNOWN:测站不属于自己
     */
    @RequiresAuthentication
    @RequiresRoles({"user:simple"})
    @RequestMapping(value = "/unShare", method = RequestMethod.POST)
    public @ResponseBody Message unShare(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        String userIdsStr = map.get("userIds").toString();
        User own = authentService.getUserFromSubject();
        Station station = stationService.find(Long.valueOf(map.get("stationId").toString()));
        if (station == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if(!station.getUser().getId().equals(own.getId())){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        List<String> userIds = Arrays.asList(userIdsStr.split(","));
        stationService.unShare(station,userIds);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 测站分享
     * @param map 包含用户userIds,以及测站stationIds
     * @return message消息体,OK:分享成功,FAIL:分享失败
     */
    @RequiresAuthentication
    @RequiresRoles({"user:simple"})
    @RequestMapping(value = "/shares", method = RequestMethod.POST)
    public @ResponseBody Message shares(@RequestBody Map<String,Object> map){
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        String userIdsStr = map.get("userIds").toString();
        User own = authentService.getUserFromSubject();
        String stationIdsStr = map.get("stationIds").toString();
        List<String> userIds = Arrays.asList(userIdsStr.split(","));
        List<String> stationIds = Arrays.asList(stationIdsStr.split(","));
        stationService.shares(stationIds,userIds,own);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 判断当前用户对该测站是否有修改权限
     * @param station 测站
     * @return true允许操作，false不允许操作
     */
    private boolean isOperate(Station station){
        User user = authentService.getUserFromSubject();
        if(user.getId().equals(station.getUser().getId())){
           return true;
        }
        return false;
    }



    /**
     * 将多个测站协同给多个子账号
     * @param map <ul>
     *            <li>stations:欲协同的多个测站,示例("12,34,24")</li>
     *            <li>accountIds:欲协同的多个子账号,示例("12,34,24")</li>
     * </ul>
     * @return <ul>
     *     <li>成功返回OK</li>
     *     <li>如果不是自己的测站，返回DATA_REFUSE </li>
     *     <li>失败返回FAIL</li>
     * </ul>
     */
    @RequiresAuthentication
    @RequiresRoles({"user:simple"})
    @RequestMapping(value = "/cooperateMul", method = RequestMethod.POST)
    public @ResponseBody Message cooperateMul(@RequestBody Map<String,Object> map) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object stationIds = map.get("stationIds");
        Object accountIds = map.get("accountIds");
        if (stationIds == null || accountIds == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        List<Station> stations=stationService.findByIdList(stationIds);
        User user = authentService.getUserFromSubject();
        if (stationService.stationIsBelongtoCurrentUser(stations, user)) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        List<Account> accounts = accountService.findByIdList(accountIds);
        stationService.cooperateMul(stations, accounts);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 取消测站查看协同
     * @param map <ul>
     *            <li>stationId，欲取消协同的测站</li>
     *            <li>accountIds，欲取消查看协同的多个子账号,示例("12,34,24")</li>
     * </ul>
     * @return <ul>
     *     <li>成功返回OK</li>
     *     <li>如果不是自己的测站，返回DATA_REFUSE </li>
     *     <li>失败返回FAIL</li>
     * </ul>
     */
    @RequiresAuthentication
    @RequiresRoles({"user:simple"})
    @RequestMapping(value = "/unCooperate", method = RequestMethod.POST)
    public @ResponseBody Message unCooperate(@RequestBody Map<String,Object> map) {
        Message message = CommonController.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object stationId = map.get("stationId");
        Object accountIds = map.get("accountIds");
        if (stationId == null || accountIds == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Station station=stationService.find(Long.valueOf(stationId.toString()));
        User user = authentService.getUserFromSubject();
        if (!station.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        List<Account> accounts = accountService.findByIdList(accountIds);
        stationService.unCooperate(station, accounts);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 子账户可查看测站列表
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/account/list", method = RequestMethod.GET)
    public @ResponseBody Message stationsList(){
        Account account = authentService.getAccountFromSubject();
        List<JSONObject> stations = stationService.getStations(account);
        pollingService.changeStationStatus(account, false);
        return MessageService.message(Message.Type.OK, stations);
    }

    /**
     * 验证token
     * @param token token
     * @param noticeStr 随机串
     * @param code 仪表唯一编码
     * @return ok 成功 UNAUTHORIZED 无权限
     */
    @RequestMapping(value = "/intendedEffectToken", method = RequestMethod.GET)
    public @ResponseBody
    Message intendedEffectToken(@RequestParam String token, @RequestParam String noticeStr, @RequestParam String code) {
        Message message = CommonController.parametersCheck(token, noticeStr, code);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (applicationTokenService.validateIntendedEffectToken(token,noticeStr, code)) {
            return MessageService.message(Message.Type.OK);
        }
        return MessageService.message(Message.Type.UNAUTHORIZED);
    }

}
