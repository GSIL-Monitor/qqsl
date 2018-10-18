package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.service.*;


import com.hysw.qqsl.cloud.annotation.util.PackageIsExpire;
import net.sf.json.JSONArray;

import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

/**
 * 外业测量控制层
 * Created by chenl on 17-4-7.
 */
@Controller
@RequestMapping("/fieldWork")
public class FieldWorkController {
    @Autowired
    private FieldWorkService fieldWorkService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private FieldWorkPointService fieldWorkPointService;

    Log logger = LogFactory.getLog(getClass());


    /**
     * 移动端外业保存
     * @param objectMap <ol><li>projectId项目id</li>，<li>userId用户id</li>，<li>name采集人</li>，<li>deviceMac手机mac</li>，<li>coordinates点集合（type点线面类型，center中心点[longitude经度,latitude纬度,elevation高程]，description描述，?delete删除标记，?attribes属性[code组级，alias别名，value值]，alias别名，?position定位点[longitude经度,latitude纬度,elevation高程]）</li></ol>?可以不传
     * @return FAIL参数验证失败，OK保存成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/saveField", method = RequestMethod.POST)
    public @ResponseBody Message saveField(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object projectId = objectMap.get("projectId");
        Object coordinates = objectMap.get("coordinates");
        Object name = objectMap.get("name");
        Object accountId = objectMap.get("userId");
        Object deviceMac = objectMap.get("deviceMac");
        if (projectId == null || coordinates == null || name == null || accountId == null || deviceMac == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Project project = projectService.find(Long.valueOf(projectId.toString()));
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }

        if (fieldWorkService.saveFieldWork(project, coordinates, name.toString(), accountId.toString(), deviceMac.toString())) {
            return MessageService.message(Message.Type.OK);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 返回外业线面数据
     * @param id 项目id
     * @return FAIL参数验证失败，EXIST项目不存在，OK成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/field", method = RequestMethod.GET)
    public @ResponseBody Message field(@RequestParam long id) {
        Project project = projectService.find(id);
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        List<FieldWork> fieldWorks = fieldWorkService.findByProject(project);
        JSONArray jsonArray = fieldWorkService.toJSON(fieldWorks);
        return MessageService.message(Message.Type.OK,jsonArray);
    }

    /**
     * 外业测量点移动到别的项目，实现外业的测量合并
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/rebindingProject", method = RequestMethod.POST)
    public @ResponseBody Message rebindingProject(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object fieldWorkId = objectMap.get("fieldWorkId");
        Object projectId = objectMap.get("projectId");
        if (fieldWorkId == null || projectId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        FieldWork fieldWork = fieldWorkService.find(Long.valueOf(fieldWorkId.toString()));
        Project project = projectService.find(Long.valueOf(projectId.toString()));
        if (fieldWork == null || project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        fieldWork.setProject(project);
        fieldWorkService.save(fieldWork);
        return MessageService.message(Message.Type.OK);
    }

    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/renamePointDescription", method = RequestMethod.POST)
    public @ResponseBody Message renamePointDescription(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object fieldWorkPointId = objectMap.get("fieldWorkPointId");
        Object description = objectMap.get("description");
        if (fieldWorkPointId == null || description == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        FieldWorkPoint fieldWorkPoint = fieldWorkPointService.find(Long.valueOf(fieldWorkPointId.toString()));
        if (fieldWorkPoint == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        fieldWorkPoint.setDescription(description.toString());
        fieldWorkPointService.save(fieldWorkPoint);
        return MessageService.message(Message.Type.OK);
    }


//    @RequestMapping(value = "/init", method = RequestMethod.GET)
//    public @ResponseBody Message init() {
//        positionService.format();
//        positionService.init();
//        return MessageService.message(Message.Type.OK);
//    }

    /**
     * 获取建筑物json数据
     * @return 建筑物列表
     */
    @RequestMapping(value = "/getSimpleBuildJsons", method = RequestMethod.GET)
    public @ResponseBody Message getSimpleBuildJsons(){
        JSONArray jsonArray = buildService.getBuildJson(false);
        return MessageService.message(Message.Type.OK,jsonArray);
    }

    /**
     * 获取带有属性的建筑物结构（地质属性除外）
     * @return 地质属性以外的建筑物列表
     */
    @RequestMapping(value = "/getBuildJsons", method = RequestMethod.GET)
    public @ResponseBody Message getBuildJsons(){
        JSONArray jsonArray = buildService.getBuildJson(true);
        return MessageService.message(Message.Type.OK,jsonArray);
    }


}
