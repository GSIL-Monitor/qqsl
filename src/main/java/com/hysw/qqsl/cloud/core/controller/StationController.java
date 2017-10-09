package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.MonitorService;
import com.hysw.qqsl.cloud.core.service.SensorService;
import com.hysw.qqsl.cloud.core.service.StationService;
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

    /**
     * 河道模型和水位流量关系曲线上传
     * @param request
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadModel" ,method = RequestMethod.POST)
    public @ResponseBody Message uploadModel(HttpServletRequest request){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        Long id = Long.valueOf(request.getParameter("id"));
        String fileName = request.getParameter("fileName");
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
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
                return new Message(Message.Type.FAIL);
            }
        }
            return new Message(Message.Type.FAIL);
    }

    /**
     * 河道模型和水位流量关系曲线下载
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadModel",method = RequestMethod.GET)
    public @ResponseBody Message downloadModel(@RequestParam long id,HttpServletResponse response){
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
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
            return new Message(Message.Type.FAIL);
        }finally {
            IOUtils.safeClose(bos);
            IOUtils.safeClose(is);
            IOUtils.safeClose(output);
        }
        return new Message(Message.Type.OK);
    }
    /**
     * 获取测站列表包括分享的测站
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists",method = RequestMethod.GET)
    public @ResponseBody Message getStations(){
        User user = authentService.getUserFromSubject();
        List<JSONObject> jsonObjectList = stationService.getStations(user);
        return new Message(Message.Type.OK,jsonObjectList);
    }

    /**
     * 测站编辑
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/edit",method = RequestMethod.POST)
    public @ResponseBody Message editStation(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
           return message;
        }
        Map<String,Object> stationMap = (Map<String, Object>) map.get("station");
        if(stationMap.get("id")==null){
            return new Message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(stationMap.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        message = stationService.edit(stationMap,station);
        return message;
    }

    /**
     * 仪表添加
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/addSensor",method = RequestMethod.POST)
    public @ResponseBody Message addSensor(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("id")==null){
            return new Message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        return stationService.addSensor((Map<String, Object>) map.get("sensor"),station);
    }

    /**
     * 添加摄像头
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/addCamera",method = RequestMethod.POST)
    public @ResponseBody Message addCamera(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("id")==null){
            return new Message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        return stationService.addCamera((Map<String, Object>) map.get("camera"),station);
    }
    /**
     * 仪表删除
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteSensor/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message deleteSensor(@PathVariable("id") Long id){
       Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        sensorService.remove(sensor);
        return new Message(Message.Type.OK);
    }

    /**
     * 摄像头删除
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteCamera/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message deleteCamera(@PathVariable("id") Long id){
        Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!Sensor.Type.CAMERA.equals(sensor.getType())){
            return new Message(Message.Type.FAIL);
        }
        sensorService.remove(sensor);
        return new Message(Message.Type.OK);
    }

    /**
     * 编辑仪表
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editSensor",method = RequestMethod.POST)
    public @ResponseBody Message editSensor(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Map<String,Object> sensorMap = (Map<String,Object>)map.get("sensor");
        if(sensorMap.get("id")==null){
            return new Message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(sensorMap.get("id").toString());
        Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        Long stationId = Long.valueOf(sensorMap.get("station").toString());
        Station station = stationService.find(stationId);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        return sensorService.editSensor(sensorMap,sensor);
    }

    /**
     * 编辑摄像头
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editCamera",method = RequestMethod.POST)
    public @ResponseBody Message editCamera(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Map<String,Object> cameraMap = (Map<String,Object>)map.get("camera");
        if(cameraMap.get("id")==null){
            return new Message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(cameraMap.get("id").toString());
        Sensor sensor = sensorService.find(id);
        if(sensor==null||sensor.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        Long stationId = Long.valueOf(cameraMap.get("station").toString());
        Station station = stationService.find(stationId);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        return sensorService.editCamera(cameraMap,sensor);
    }

    /**
     * 编辑测站参数
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editParameter",method = RequestMethod.POST)
    public @ResponseBody Message editParameter(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("id")==null){
            return new Message(Message.Type.EXIST);
        }
        Long id = Long.valueOf(map.get("id").toString());
        Station station = stationService.find(id);
        if(station==null||station.getId()==null){
            return new Message(Message.Type.EXIST);
        }
        if(!isOperate(station)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        message = stationService.editParameter((Map<String,Object>)map.get("parameter"),station);
      //  message = stationService.editParameter((Map<String,Object>)map.get("parameter"),new Station());
        return message;
    }

    /**
     * 取消测站分享
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
        Station station = stationService.find(Long.valueOf(map.get("stationId").toString()));
        if(station==null||!station.getUser().getId().equals(own.getId())){
            return new Message(Message.Type.UNKNOWN);
        }
        List<String> userIds = Arrays.asList(userIdsStr.split(","));
        stationService.unShare(station,userIds,own);
        return new Message(Message.Type.OK);
    }

    /**
     * 测站分享
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles({"user:hydrology"})
    @RequestMapping(value = "/shares", method = RequestMethod.POST)
    public @ResponseBody Message shares(@RequestBody Map<String,Object> map){
        Message message = Message.parameterCheck(map);
        if(!message.getType().equals(Message.Type.OK)){
            return message;
        }
        String userIdsStr = map.get("userIds").toString();
        User own = authentService.getUserFromSubject();
        String stationIdsStr = map.get("stationIds").toString();
        List<String> userIds = Arrays.asList(userIdsStr.split(","));
        List<String> sensorIds = Arrays.asList(stationIdsStr.split(","));
        stationService.shares(sensorIds,userIds,own);
        return new Message(Message.Type.OK);
    }

    /**
     * 判断当前用户对该测站是否有修改权限
     * @param station
     * @return
     */
    private boolean isOperate(Station station){
        User user = authentService.getUserFromSubject();
        if(user.getId().equals(station.getUser().getId())){
           return true;
        }
        return false;
    }
}
