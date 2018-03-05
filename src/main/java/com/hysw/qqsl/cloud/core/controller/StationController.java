package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.annotation.util.StationIsExpire;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    /**
     * 获取token
     * @return message消息体,附带token令牌
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public @ResponseBody
    Message getToken() {
        return MessageService.message(Message.Type.OK, applicationTokenService.getToken());
    }


    /**
     * 河道模型和水位流量关系曲线上传
     * @param request 请求消息体,包含测站id,文件名,以及文件内容
     * @return   message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:上传成功,FAIL:上传失败
     */
    @StationIsExpire("request")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadModel" ,method = RequestMethod.POST)
    public @ResponseBody Message uploadModel(HttpServletRequest request){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        Long id = Long.valueOf(request.getParameter("id"));
        String fileName = request.getParameter("fileName");
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            MultipartFile mFile = map.get(fileName);
            try{
                return stationService.readModelFile(mFile,station);
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
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadModel",method = RequestMethod.GET)
    public @ResponseBody Message downloadModel(@RequestParam long id,HttpServletResponse response){
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
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
     * 获取测站列表包括分享的测站
     * @return message消息体,OK:获取成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists",method = RequestMethod.GET)
    public @ResponseBody Message getStations(){
        User user = authentService.getUserFromSubject();
        List<JSONObject> jsonObjectList = stationService.getStations(user);
        pollingService.changeStationStatus(user, false);
        return MessageService.message(Message.Type.OK,jsonObjectList);
    }

    /**
     * 测站编辑
     * @param map 包含测站id,以及测站类型type,测站名字name,测站描述description,测站地址address,测站坐标coor
     * @return message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
     */
    @StationIsExpire("station")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/edit",method = RequestMethod.POST)
    public @ResponseBody Message editStation(@RequestBody Map<String,Object> map){
        Message message = MessageService.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
           return message;
        }
        Map<String,Object> stationMap = (Map<String, Object>) map.get("station");
        if(stationMap.get("id")==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(stationMap.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        message = stationService.edit(stationMap,station);
        return message;
    }

    /**
     * 仪表添加
     * @param map 包含测站id,以及仪表参数factory,contact,phone,settingHeight,ciphertext,code
     * @return message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:添加成功,FAIL:添加失败
     */
    @StationIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/addSensor",method = RequestMethod.POST)
    public @ResponseBody Message addSensor(@RequestBody Map<String,Object> map){
        Message message = MessageService.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("id")==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        return stationService.addSensor((Map<String, Object>) map.get("sensor"),station);
    }

    /**
     * 添加摄像头
     * @param map 包含测站id,以及摄像头参数factory,contact,phone,settingHeight,cameraUrl
     * @return  message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:添加成功,FAIL:添加失败
     */
    @StationIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/addCamera",method = RequestMethod.POST)
    public @ResponseBody Message addCamera(@RequestBody Map<String,Object> map){
        Message message = MessageService.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("id")==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        return stationService.addCamera((Map<String, Object>) map.get("camera"),station);
    }
    /**
     * 仪表删除
     * @param id 仪表id
     * @return message消息体,EXIST:仪表不存在,OK:删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteSensor/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message deleteSensor(@PathVariable("id") Long id){
       Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return MessageService.message(Message.Type.EXIST);
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
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteCamera/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message deleteCamera(@PathVariable("id") Long id){
        Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!Sensor.Type.CAMERA.equals(sensor.getType())){
            return MessageService.message(Message.Type.FAIL);
        }
        sensorService.remove(sensor);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑仪表
     * @param map 包含测站station,以及仪表参数id,factory,contact,phone,settingHeight
     * @return message消息体,EXIST:仪表或测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
     */
    @StationIsExpire("sensor")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editSensor",method = RequestMethod.POST)
    public @ResponseBody Message editSensor(@RequestBody Map<String,Object> map){
        Message message = MessageService.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Map<String,Object> sensorMap = (Map<String,Object>)map.get("sensor");
        if(sensorMap.get("id")==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(sensorMap.get("id").toString());
        Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long stationId = Long.valueOf(sensorMap.get("station").toString());
        Station station = stationService.find(stationId);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        return sensorService.editSensor(sensorMap,sensor);
    }

    /**
     * 编辑摄像头
     * @param map  包含测站station,以及摄像头参数,id,factory,contact,phone,settingHeight,cameraUrl
     * @return  message消息体,EXIST:仪表或测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
     */
    @StationIsExpire("camera")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editCamera",method = RequestMethod.POST)
    public @ResponseBody Message editCamera(@RequestBody Map<String,Object> map){
        Message message = MessageService.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Map<String,Object> cameraMap = (Map<String,Object>)map.get("camera");
        if(cameraMap.get("id")==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(cameraMap.get("id").toString());
        Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long stationId = Long.valueOf(cameraMap.get("station").toString());
        Station station = stationService.find(stationId);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        return sensorService.editCamera(cameraMap,sensor);
    }

    /**
     * 编辑测站参数
     * @param map 包含测站id,以及参数信息,maxValue,minvalue,phone,sendStatus
     * @return message消息体,EXIST:测站不存在,NO_AUTHORIZE:无操作权限,OK:编辑成功,FAIL:编辑失败
     */
    @StationIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editParameter",method = RequestMethod.POST)
    public @ResponseBody Message editParameter(@RequestBody Map<String,Object> map){
        Message message = MessageService.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("id")==null){
            return MessageService.message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return MessageService.message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
        }
        message = stationService.editParameter((Map<String,Object>)map.get("parameter"),station);
        return message;
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
        Message message = MessageService.parameterCheck(map);
        if(!message.getType().equals(Message.Type.OK)){
            return message;
        }
        String userIdsStr = map.get("userIds").toString();
        User own = authentService.getUserFromSubject();
        Station station = stationService.find(Long.valueOf(map.get("stationId").toString()));
        if(station==null||!station.getUser().getId().equals(own.getId())){
            return MessageService.message(Message.Type.UNKNOWN);
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
        Message message = MessageService.parameterCheck(map);
        if(!message.getType().equals(Message.Type.OK)){
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
     * 监测取得仪表参数：getParamters，GET
     * 监测系统取得所有已改变的参数列表
     * @param token 自定义安全令牌token
     * @return FAIL:获取失败，OK：获取成功，附带测站参数
     */
    @RequestMapping(value = "/getParameters", method = RequestMethod.GET)
    public @ResponseBody Message getParameters(@RequestParam String token){
       Message message = MessageService.parametersCheck(token);
       if(Message.Type.FAIL.equals(message.getType())){
           return message;
       }
       if(!applicationTokenService.decrypt(token)){
           return MessageService.message(Message.Type.FAIL);
       }
        JSONArray paramters = stationService.getParameters();
        return MessageService.message(Message.Type.OK,paramters);
    }

}
