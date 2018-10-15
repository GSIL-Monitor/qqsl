package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import com.hysw.qqsl.cloud.core.entity.data.Project;
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
    private CoordinateService coordinateService;

    Log logger = LogFactory.getLog(getClass());

//
//    /**
//     * 移动端外业保存
//     * @param objectMap <ol><li>projectId项目id</li>，<li>userId用户id</li>，<li>name采集人</li>，<li>deviceMac手机mac</li>，<li>coordinates点集合（type点线面类型，center中心点[longitude经度,latitude纬度,elevation高程]，description描述，?delete删除标记，?attribes属性[code组级，alias别名，value值]，alias别名，?position定位点[longitude经度,latitude纬度,elevation高程]）</li></ol>?可以不传
//     * @return FAIL参数验证失败，OK保存成功
//     */
//    @PackageIsExpire
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/saveField", method = RequestMethod.POST)
//    public @ResponseBody Message saveField(@RequestBody  Map<String,Object> objectMap) {
//        Message message = CommonController.parameterCheck(objectMap);
//        if (message.getType() != Message.Type.OK) {
//            return message;
//        }
//        if (fieldWorkService.saveField(objectMap)) {
//            return MessageService.message(Message.Type.OK);
//        } else {
//            return MessageService.message(Message.Type.FAIL);
//        }
//    }

    /**
     * 返回外业线面数据
     * @param id 项目id
     * @param type 来源 DESIGN设计，FIELD外业
     * @return FAIL参数验证失败，EXIST项目不存在，OK成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/field", method = RequestMethod.GET)
    public @ResponseBody Message field(@RequestParam long id,@RequestParam String type) {
        Project project = projectService.find(id);
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (Build.Source.DESIGN.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            JSONObject desgin = fieldWorkService.field(project, Build.Source.DESIGN);
            return MessageService.message(Message.Type.OK, desgin);
        } else if (Build.Source.FIELD.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            JSONObject field = fieldWorkService.field(project, Build.Source.FIELD);
            return MessageService.message(Message.Type.OK, field);
        }else {
            return MessageService.message(Message.Type.FAIL);
        }
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
