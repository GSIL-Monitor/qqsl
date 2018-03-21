package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.build.Graph;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
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

    Log logger = LogFactory.getLog(getClass());

    /**
     * 坐标文件上传
     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
     */
    @PackageIsExpire(value = "request")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/coordinateFile", method = RequestMethod.POST)
    public @ResponseBody
    Message uploadCoordinate(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        String id = request.getParameter("projectId");
        String baseLevelType = request.getParameter("baseLevelType");
        String WGS84Type = request.getParameter("WGS84Type");
        Message message;
        JSONObject jsonObject = new JSONObject();
        if (id == null || baseLevelType == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Project project;
        Coordinate.WGS84Type wgs84Type = null;
        Coordinate.BaseLevelType levelType;
        try {
            message=isAllowUploadCoordinateFile(Long.valueOf(id));
            levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
            if (!WGS84Type.equals("")) {
                wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
            }
            project = projectService.find(Long.valueOf(id));
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
        }
        String central = coordinateService.getCoordinateBasedatum(project);
        if (central == null) {
            return MessageService.message(Message.Type.COOR_PROJECT_NO_CENTER);
        }
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                MultipartFile mFile = entry.getValue();
                String fileName = mFile.getOriginalFilename();
                // 限制上传文件的大小
                if (mFile.getSize() > CommonAttributes.CONVERT_MAX_SZIE) {
                    // return "文件过大无法上传";
                    logger.debug("文件过大");
                    jsonObject.put(entry.getKey(),Message.Type.FILE_TOO_MAX);
                    continue;
                }
                InputStream is;
                try {
                    is = mFile.getInputStream();
                } catch (IOException e) {
                    logger.info("坐标文件或格式异常");
                    jsonObject.put(entry.getKey(),Message.Type.COOR_FORMAT_ERROR);
                    continue;
                }
                String s = fileName.substring(fileName.lastIndexOf(".") + 1,
                        fileName.length());
                Map<List<Graph>, List<Build>> map1;
                try {
                    map1 = coordinateService.readExcels(is, central, s, project, wgs84Type);
                } catch (Exception e) {
                    logger.info("坐标文件或格式异常");
                    jsonObject.put(entry.getKey(),Message.Type.COOR_FORMAT_ERROR);
                    continue;
                }finally {
                    IOUtils.safeClose(is);
                }
                if (map1 == null) {
                    jsonObject.put(entry.getKey(),Message.Type.FAIL);
                    continue;
                }
                List<Graph> list = null;
                List<Build> builds = null;
                for (Map.Entry<List<Graph>, List<Build>> entry1 : map1.entrySet()) {
                    list = entry1.getKey();
                    builds = entry1.getValue();
                    break;
                }
                if (list == null || list.size() == 0) {
                    jsonObject.put(entry.getKey(),Message.Type.OK);
                    continue;
                }
                if (coordinateService.saveCoordinate(list, builds, project)) {
                    jsonObject.put(entry.getKey(),Message.Type.OK.getStatus());
                } else {
                    jsonObject.put(entry.getKey(),Message.Type.FAIL.getStatus());
                }
            }
        }
        return MessageService.message(Message.Type.OK,jsonObject);
    }

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
        if (fieldService.saveField(objectMap)) {
            return MessageService.message(Message.Type.OK);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
    }

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
            JSONObject desgin = fieldService.field(project, Build.Source.DESIGN);
            return MessageService.message(Message.Type.OK, desgin);
        } else if (Build.Source.FIELD.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            JSONObject field = fieldService.field(project, Build.Source.FIELD);
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
        JSONArray jsonArray = buildGroupService.getBuildJson(true);
        return MessageService.message(Message.Type.OK,jsonArray);
    }
    /**
     * 获取带有属性的建筑物结构（地质属性除外）
     * @return 地质属性以外的建筑物列表
     */
    @RequestMapping(value = "/getBuildJsons", method = RequestMethod.GET)
    public @ResponseBody Message getBuildJsons(){
        JSONArray jsonArray = buildGroupService.getBuildJson(false);
        return MessageService.message(Message.Type.OK,jsonArray);
    }

    /**
     * 获取地质属性的json结构
     * @return 地质属性列表
     */
    @RequestMapping(value = "/getGeologyJson", method = RequestMethod.GET)
//    @RequiresRoles(value = {"web"},logical = Logical.OR)
    public @ResponseBody Message getGeologyJson(){
        JSONObject jsonObject = buildGroupService.getGeologyJson();
        return MessageService.message(Message.Type.OK,jsonObject);
    }

    /**
     * 将外业内业坐标数据及建筑物写入excel
     * @param projectId 项目id
     * @param type 来源 DESIGN设计，FIELD外业
     * @param baseLevelType 坐标转换基准面类型
     * @param WGS84Type WGS84坐标格式
     * @return excel格式数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/writeExcel", method = RequestMethod.GET)
    public @ResponseBody
    Message writeExcel(@RequestParam long projectId, @RequestParam String type, @RequestParam String baseLevelType, @RequestParam String WGS84Type, HttpServletResponse response) {
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
        Workbook wb;
        if (Build.Source.DESIGN.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            wb = fieldService.writeExcel(project, Build.Source.DESIGN,wgs84Type);
        } else if (Build.Source.FIELD.toString().toLowerCase().equals(type.trim().toLowerCase())) {
            wb = fieldService.writeExcel(project, Build.Source.FIELD,wgs84Type);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + project.getName() + "--" + type.trim() + ".xls" + "\"");
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
     * 获取建筑物
     * @param id 建筑物id
     * @return 建筑物对象
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public @ResponseBody Message uploadBuild(@RequestParam long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Build build = buildService.find(id);
        if (build == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = buildService.buildJson(build);
        return MessageService.message(Message.Type.OK,jsonObject);
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
        if (projectId == null || type == null || centerCoor == null || remark == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (fieldService.newBuild(type, centerCoor, remark, projectId)) {
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
        Object attribes = objectMap.get("attribes");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Build build = buildService.find(Long.valueOf(id.toString()));
        if (build == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (fieldService.editBuild(build,id,remark,type,attribes)) {
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
        Coordinate coordinate = coordinateService.find(Long.valueOf(id.toString()));
        coordinate.setCoordinateStr(jsonObject.toString());
        coordinate.setDescription(description.toString());
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
        List<Build> builds = buildService.findByCoordinateId(id);
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

    private Message isAllowUploadCoordinateFile(Long id) {
        Project project = projectService.find(Long.valueOf(id));
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        List<Coordinate> coordinates = coordinateService.findByProject(project);
        if (coordinates.size()< CommonAttributes.COORDINATELIMIT) {
            return MessageService.message(Message.Type.OK);
        }
//        超过限制数量，返回已达到最大限制数量
        return MessageService.message(Message.Type.PACKAGE_LIMIT);
    }
}
