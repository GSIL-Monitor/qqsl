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

    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadCoordinateConfirm", method = RequestMethod.GET)
    public @ResponseBody
    Message uploadCoordinateConfirm(HttpSession session) {
        JSONObject reslove = (JSONObject) session.getAttribute("upload");
        if (reslove == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        session.removeAttribute("upload");
        Long projectId = Long.valueOf(reslove.get("projectId").toString());
        Project project = projectService.find(projectId);
        List<Coordinate> coordinates = coordinateService.findByProject(project);
        List<Build> builds = buildService.findByProjectAndSource(project, Build.Source.DESIGN);
        JSONObject jsonObject;
        for (Object o : JSONArray.fromObject(reslove.get("msg"))) {
            jsonObject = JSONObject.fromObject(o);
            coordinateService.saveObject(jsonObject.get("noticeStr").toString(),builds,coordinates);
        }
        return MessageService.message(Message.Type.OK);
    }


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

//    /**
//     * 获取地质属性的json结构
//     * @return 地质属性列表
//     */
//    @RequestMapping(value = "/getGeologyJson", method = RequestMethod.GET)
////    @RequiresRoles(value = {"web"},logical = Logical.OR)
//    public @ResponseBody Message getGeologyJson(){
//        JSONObject jsonObject = buildGroupService.getGeologyJson();
//        return MessageService.message(Message.Type.OK,jsonObject);
//    }

    /**
     * 将内业坐标数据及建筑物写入excel
     * @param projectId 项目id
     * @param baseLevelType 坐标转换基准面类型
     * @param WGS84Type WGS84坐标格式
     * @return excel格式数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadDesignCoordinate", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadDesignCoordinate(@RequestParam long projectId, @RequestParam String baseLevelType, @RequestParam String WGS84Type, HttpServletResponse response) {
        Project project = projectService.find(projectId);
        Coordinate.BaseLevelType levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
        Coordinate.WGS84Type wgs84Type = null;
        if (!WGS84Type.equals("")) {
            wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
        }
        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
        }
        if (wgs84Type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Workbook wb = fieldWorkService.writeExcelByDesign(project,wgs84Type);
        if (wb == null) {
            return MessageService.message(Message.Type.COOR_PROJECT_NO_CENTER);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + project.getName() + "--" + "design" + ".xlsx" + "\"");
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
        } finally {
            IOUtils.safeClose(bos);
            IOUtils.safeClose(is);
            IOUtils.safeClose(output);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 将外业内业坐标数据及建筑物写入excel
     * @param projectId 项目id
     * @param baseLevelType 坐标转换基准面类型
     * @param WGS84Type WGS84坐标格式
     * @return excel格式数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadFieldWorkCoordinate", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadFieldWorkCoordinate(@RequestParam long projectId, @RequestParam String baseLevelType, @RequestParam String WGS84Type, HttpServletResponse response) {
        Project project = projectService.find(projectId);
        Coordinate.BaseLevelType levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
        Coordinate.WGS84Type wgs84Type = null;
        if (!WGS84Type.equals("")) {
            wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
        }
        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
        }
        if (wgs84Type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Workbook wb = fieldWorkService.writeExcelByFieldWork(project,wgs84Type);
        if (wb == null) {
            return MessageService.message(Message.Type.COOR_PROJECT_NO_CENTER);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + project.getName() + "--" + "fieldWork" + ".xlsx" + "\"");
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
        } finally {
            IOUtils.safeClose(bos);
            IOUtils.safeClose(is);
            IOUtils.safeClose(output);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 新建建筑物
     * @param objectMap <ol><li>type建筑物类型</li><li>centerCoor中心坐标</li><li>remark注释</li><li>projectId项目id</li></ol>
     * @return FAIL参数验证失败，OK新建成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/createBuild", method = RequestMethod.POST)
    public @ResponseBody Message newBuild(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object type = objectMap.get("type");
        Object centerCoor = objectMap.get("centerCoor");
        Object remark = objectMap.get("remark");
        Object projectId = objectMap.get("projectId");
        Object commonId = objectMap.get("coordinateId");
        commonId = 6443;
        if (projectId == null || type == null || centerCoor == null || remark == null || commonId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (fieldWorkService.newBuild(type, centerCoor, remark, projectId,commonId)) {
            return MessageService.message(Message.Type.OK);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 编辑建筑物
     * @param objectMap <ol><li>id建筑物id</li><li>remark注释（可不传）</li><li>type建筑物类型（可不传）</li><li>attribes属性集<ol><li>alias别名</li><li>value值</li></ol></li></ol>
     * @return FAIL参数验证失败，OK编辑成功，EXIST建筑物不存在
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editBuild", method = RequestMethod.POST)
    public @ResponseBody Message editBuild(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object id = objectMap.get("id");
        Object remark = objectMap.get("remark");
        Object type = objectMap.get("type");
        Object attributes = objectMap.get("attributes");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Build build = buildService.find(Long.valueOf(id.toString()));
        if (build == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (fieldWorkService.editBuild(build,remark,type,attributes)) {
            return MessageService.message(Message.Type.OK);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 删除建筑物
     * @param id 建筑物id
     * @return FAIL参数验证失败，EXIST建筑物不存在，OK删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteBuild/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deleteBuild(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Build build = buildService.find(id);
        if (build == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        buildService.remove(build);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 删除坐标线面上的某点(编辑线面)
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editShape", method = RequestMethod.POST)
    public @ResponseBody Message deletePoint(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object line = objectMap.get("line");
        Object builds = objectMap.get("build");
        Object description = objectMap.get("description");
        if (line == null && description == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        boolean b = coordinateService.checkCoordinateFormat(line);
        if (!b) {
            return MessageService.message(Message.Type.FAIL);
        }
        JSONObject jsonObject = JSONObject.fromObject(line);
        Object id = jsonObject.get("id");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        jsonObject.remove("id");
        String baseType = jsonObject.get("baseType").toString();
        jsonObject.remove("type");
        jsonObject.remove("baseType");
        Coordinate coordinate = coordinateService.find(Long.valueOf(id.toString()));
        coordinate.setCoordinateStr(jsonObject.toString());
        coordinate.setDescription(description.toString());
        coordinate.setCommonType(CommonEnum.CommonType.valueOf(baseType));
        coordinateService.save(coordinate);
        if (builds != null) {
            List<Integer> list = (List<Integer>) builds;
            for (Integer l : list) {
                Build build = buildService.find(Long.valueOf(l));
                if (build == null) {
                    return MessageService.message(Message.Type.DATA_NOEXIST);
                }
                buildService.remove(build);
            }
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除坐标线面
     * @param id 线面id
     * @return FAIL参数验证失败，OK删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteShape/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deletePLine(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Coordinate coordinate = coordinateService.find(id);
        if (coordinate == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        coordinateService.remove(coordinate);
        List<Build> builds = buildService.findByCommonId(id, Build.Source.DESIGN);
        buildService.removes(builds);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 新建坐标线面
     * @param objectMap <ol><li>line线面对象</li><li>projectId项目id</li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，OK渐渐成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/createShape", method = RequestMethod.POST)
    public @ResponseBody Message creatShape(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object line = objectMap.get("line");
        Object projectId = objectMap.get("projectId");
        Object description = objectMap.get("description");
        if (line == null || projectId == null || description == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        boolean b = coordinateService.checkCoordinateFormat(line);
        if (!b) {
            return MessageService.message(Message.Type.FAIL);
        }
        coordinateService.saveCoordinateFromPage(line,projectId,description);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 获取所有建筑物类型
     * @return OK：请求成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/templateInfo", method = RequestMethod.GET)
    public @ResponseBody Message templateInfo() {
        return MessageService.message(Message.Type.OK,fieldWorkService.getModelType());
    }

    /**
     * 根据类型下载对应模板
     * @param types  [a,b,c]
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadTemplate", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadTemplete(@RequestParam String[] types, HttpServletResponse response) {
        List<String> list = Arrays.asList(types);
        Workbook wb = fieldWorkService.downloadModel(list);
        if (wb == null) {
            return MessageService.message(Message.Type.FAIL);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "buildsTemplate"+ ".xlsx" + "\"");
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
        } finally {
            IOUtils.safeClose(bos);
            IOUtils.safeClose(is);
            IOUtils.safeClose(output);
        }
        return MessageService.message(Message.Type.OK);
    }
}
