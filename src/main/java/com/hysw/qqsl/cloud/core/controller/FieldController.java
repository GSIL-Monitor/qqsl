package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.annotation.util.IsCoordinateFile;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.service.*;


import com.hysw.qqsl.cloud.annotation.util.IsExpire;
import com.hysw.qqsl.cloud.annotation.util.IsFindCM;
import net.sf.json.JSONArray;

import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 外业测量控制层
 * Created by chenl on 17-4-7.
 */
@Controller
@RequestMapping("/field")
public class FieldController {
    @Autowired
    private FieldService fieldService;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private CoordinateService coordinateService;


    /**
     * 移动端外业保存
     * @param objectMap
     * @return
     */
    @IsExpire
    @IsCoordinateFile
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/saveField", method = RequestMethod.POST)
    public @ResponseBody Message saveField(@RequestBody  Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        message=fieldService.saveField(objectMap);
        return message;
    }

    /**
     * 返回外业线面数据
     * @param id
     * @param type
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/field", method = RequestMethod.GET)
    public @ResponseBody Message field(@RequestParam long id,@RequestParam String type) {
        Project project = projectService.find(id);
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        if (Build.Source.DESIGN.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            JSONObject desgin = fieldService.field(project, Build.Source.DESIGN);
            return new Message(Message.Type.OK, desgin);
        } else if (Build.Source.FIELD.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            JSONObject field = fieldService.field(project, Build.Source.FIELD);
            return new Message(Message.Type.OK, field);
        }else {
            return new Message(Message.Type.FAIL);
        }
    }


//    @RequestMapping(value = "/init", method = RequestMethod.GET)
//    public @ResponseBody Message init() {
//        positionService.format();
//        positionService.init();
//        return new Message(Message.Type.OK);
//    }

    /** 获取建筑物json数据
     * @return
     */
    @RequestMapping(value = "/getSimpleBuildJsons", method = RequestMethod.GET)
    public @ResponseBody Message getSimpleBuildJsons(){
        JSONArray jsonArray = buildGroupService.getBuildJson(true);
        return new Message(Message.Type.OK,jsonArray);
    }
    /**
     * 获取带有属性的建筑物结构（地质属性除外）
     * @return
     */
    @RequestMapping(value = "/getBuildJsons", method = RequestMethod.GET)
    public @ResponseBody Message getBuildJsons(){
        JSONArray jsonArray = buildGroupService.getBuildJson(false);
        return new Message(Message.Type.OK,jsonArray);
    }

    /**
     * 获取地质属性的json结构
     * @return
     */
    @RequestMapping(value = "/getGeologyJson", method = RequestMethod.GET)
//    @RequiresRoles(value = {"web"},logical = Logical.OR)
    public @ResponseBody Message getGeologyJson(){
        JSONObject jsonObject = buildGroupService.getGeologyJson();
        return new Message(Message.Type.OK,jsonObject);
    }


    /**
     * 将外业内业坐标数据及建筑物写入excel
     * @param id
     * @param type
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/writeExcel", method = RequestMethod.GET)
    public @ResponseBody Message writeExcel(@RequestParam long id,@RequestParam String type,HttpServletResponse response) {
        Project project = projectService.find(id);
        Workbook wb;
        if (Build.Source.DESIGN.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            wb = fieldService.writeExcel(project, Build.Source.DESIGN);
        } else if (Build.Source.FIELD.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            wb = fieldService.writeExcel(project, Build.Source.FIELD);
        } else {
            return new Message(Message.Type.FAIL);
        }
        ByteArrayOutputStream bos = null;
        InputStream is = null;
        OutputStream output = null;
        try {
            bos = new ByteArrayOutputStream();
            wb.write(bos);
            is = new ByteArrayInputStream(bos.toByteArray());
            String contentType = "application/vnd.ms-excel";
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + project.getName()+"--"+type.trim()+".xls" + "\"");
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
     * 获取建筑物
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public @ResponseBody Message uploadBuild(@RequestParam long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Build build = buildService.find(id);
        if (build == null) {
            return new Message(Message.Type.EXIST);
        }
        JSONObject jsonObject = buildService.buildJson(build);
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 新建建筑物
     * @param objectMap
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/createBuild", method = RequestMethod.POST)
    public @ResponseBody Message newBuild(@RequestBody  Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return fieldService.newBuild(objectMap);
    }

    /**
     * 编辑建筑物
     * @param objectMap
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editBuild", method = RequestMethod.POST)
    public @ResponseBody Message editBuild(@RequestBody  Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return fieldService.editBuild(objectMap);
    }

    /**
     * 删除建筑物
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteBuild/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deleteBuild(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Build build = buildService.find(id);
        if (build == null) {
            return new Message(Message.Type.EXIST);
        }
        buildService.remove(build);
        return new Message(Message.Type.OK);
    }


    /**
     * 删除坐标线面上的某点(编辑线面)
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editShape", method = RequestMethod.POST)
    public @ResponseBody Message deletePoint(@RequestBody  Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Object line = objectMap.get("line");
        Object builds = objectMap.get("build");
        Object description = objectMap.get("description");
        if (line == null && description == null) {
            return new Message(Message.Type.FAIL);
        }
        JSONObject jsonObject = JSONObject.fromObject(line);
        Object id = jsonObject.get("id");
        if (id == null) {
            return new Message(Message.Type.FAIL);
        }
        jsonObject.remove("id");
        Coordinate coordinate = coordinateService.find(Long.valueOf(id.toString()));
        coordinate.setCoordinateStr(jsonObject.toString());
        coordinate.setDescription(description.toString());
        coordinateService.save(coordinate);
        if (builds != null) {
            List<Integer> list = (List<Integer>) builds;
            for (Integer l : list) {
                Build build = buildService.find(Long.valueOf(l));
                if (build == null) {
                    return new Message(Message.Type.EXIST);
                }
                buildService.remove(build);
            }
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 删除坐标线面
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteShape/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deletePLine(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Coordinate coordinate = coordinateService.find(id);
        if (coordinate == null) {
            return new Message(Message.Type.FAIL);
        }
        coordinateService.remove(coordinate);
        List<Build> builds = buildService.findByCoordinateId(id);
        buildService.removes(builds);
        return new Message(Message.Type.OK);
    }

    /**
     * 新建坐标线面
     * @param objectMap
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/createShape", method = RequestMethod.POST)
    public @ResponseBody Message creatShape(@RequestBody  Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        return coordinateService.saveCoordinateFromPage(objectMap);
    }


}
