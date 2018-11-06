package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.buildModel.Elevation;
import com.hysw.qqsl.cloud.core.entity.buildModel.Line;
import com.hysw.qqsl.cloud.core.entity.buildModel.PLACache;
import com.hysw.qqsl.cloud.core.entity.buildModel.SheetObject;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.xerces.xs.datatypes.ObjectList;
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
 * 内业控制层
 * Created by chenl on 17-4-7.
 */
@Controller
@RequestMapping("/shape")
public class ShapeController {
    @Autowired
    private ShapeService shapeService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private ShapeAttributeService shapeAttributeService;
    @Autowired
    private ShapeCoordinateService shapeCoordinateService;
    @Autowired
    private BuildAttributeService buildAttributeService;
    @Autowired
    private LineService lineService;

    Log logger = LogFactory.getLog(getClass());

    /**
     * 建筑物属性模板下载
     * @param types  [a,b,c]
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadShapeModel", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadShapeModel(@RequestParam String[] types, HttpServletResponse response) {
        List<String> list = Arrays.asList(types);
        Workbook wb = shapeService.downloadShapeModel(list);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "shapeTemplate"+ ".xlsx" + "\"");
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
     * 线面坐标文件上传
     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
     */
//    @PackageIsExpire(value = "request")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadShape", method = RequestMethod.POST)
    public @ResponseBody
    Message uploadShape(HttpServletRequest request) {
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
            project = projectService.find(Long.valueOf(id));
            message=isAllowUploadCoordinateFile(project);
            levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
            if (!WGS84Type.equals("")) {
                wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
            }
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
        Map<String, Workbook> wbs = new HashMap<>();
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                shapeService.uploadCoordinate(entry,jsonObject,wbs);
            }
        }
        if (!jsonObject.isEmpty()) {
            return MessageService.message(Message.Type.COOR_FORMAT_ERROR, jsonObject);
        }
        SheetObject sheetObject = new SheetObject();
        shapeService.getAllSheet(wbs,sheetObject);
//		进入错误处理环节
        if (sheetObject.getUnknowWBs().size() != 0) {
            return MessageService.message(Message.Type.COOR_UNKONW_SHEET_TYPE,shapeService.errorMsg(sheetObject.getUnknowWBs()));
        }
        PLACache plaCache = shapeService.reslove(sheetObject, central, wgs84Type, project);
        if (plaCache == null) {
            return MessageService.message(Message.Type.OK);
        }
        JSONArray jsonArray = shapeService.pickedErrorMsg(plaCache);
        return MessageService.message(Message.Type.COOR_RETURN_PROMPT, jsonArray);
    }

    /**
     * 剖面模板下载
     * @param types  [a,b,c]
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadShapeAttributeModel", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadShapeAttribute(@RequestParam String[] types, HttpServletResponse response) {
        List<String> list = Arrays.asList(types);
        Workbook wb = shapeService.downloadModel(list);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "lineSectionPlaneTemplate"+ ".xlsx" + "\"");
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
     * 剖面属性上传
     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
     */
//    @PackageIsExpire(value = "request")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadShapeAttribute", method = RequestMethod.POST)
    public @ResponseBody
    Message uploadShapeAttribute(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        String shapeId = request.getParameter("shapeId");
        Message message;
        JSONObject jsonObject = new JSONObject();
        if (shapeId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape;
        try {
            shape = shapeService.find(Long.valueOf(shapeId));
            if (shape == null) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            message=isAllowUploadCoordinateFile(shape.getProject());
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Workbook> wbs = new HashMap<>();
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                shapeService.uploadCoordinate(entry,jsonObject,wbs);
            }
        }
        if (!jsonObject.isEmpty()) {
            return MessageService.message(Message.Type.COOR_FORMAT_ERROR, jsonObject);
        }
        SheetObject sheetObject = new SheetObject();
        shapeService.getAllSheet(wbs,sheetObject);
//		进入错误处理环节
        if (sheetObject.getUnknowWBs().size() != 0) {
            return MessageService.message(Message.Type.COOR_UNKONW_SHEET_TYPE,shapeService.errorMsg(sheetObject.getUnknowWBs()));
        }
        shapeService.reslove(sheetObject, shape);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 建筑物属性模板下载
     * @param types  [a,b,c]
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadBuildModel", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadBuildModel(@RequestParam String[] types, HttpServletResponse response) {
        List<String> list = Arrays.asList(types);
        Workbook wb = buildService.downloadBuildModel(list);
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

    /**
     * 建筑物属性上传(多建筑物)
     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
     */
//    @PackageIsExpire(value = "request")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadBuild", method = RequestMethod.POST)
    public @ResponseBody
    Message uploadBuildAttribute(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        String shapeId = request.getParameter("shapeId");
        String baseLevelType = request.getParameter("baseLevelType");
        String WGS84Type = request.getParameter("WGS84Type");
        Message message;
        JSONObject jsonObject = new JSONObject();
        if (shapeId == null || baseLevelType == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape;
        Coordinate.WGS84Type wgs84Type = null;
        Coordinate.BaseLevelType levelType;
        try {
            shape = shapeService.find(Long.valueOf(shapeId));
            message=isAllowUploadCoordinateFile(shape.getProject());
            levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
            if (!WGS84Type.equals("")) {
                wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
            }
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
        }
        String central = coordinateService.getCoordinateBasedatum(shape.getProject());
        if (central == null) {
            return MessageService.message(Message.Type.COOR_PROJECT_NO_CENTER);
        }
        Map<String, Workbook> wbs = new HashMap<>();
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                shapeService.uploadCoordinate(entry,jsonObject,wbs);
            }
        }
        if (!jsonObject.isEmpty()) {
            return MessageService.message(Message.Type.COOR_FORMAT_ERROR, jsonObject);
        }
        SheetObject sheetObject = new SheetObject();
        shapeService.getAllSheet(wbs,sheetObject);
//		进入错误处理环节
        if (sheetObject.getUnknowWBs().size() != 0) {
            return MessageService.message(Message.Type.COOR_UNKONW_SHEET_TYPE,shapeService.errorMsg(sheetObject.getUnknowWBs()));
        }
        PLACache plaCache = shapeService.reslove(sheetObject, central, wgs84Type, shape.getProject(), shape);
        if (plaCache == null) {
            return MessageService.message(Message.Type.OK);
        }
        JSONArray jsonArray = shapeService.pickedErrorMsg1(plaCache);
        return MessageService.message(Message.Type.COOR_RETURN_PROMPT, jsonArray);
    }

    /**
     * 图形下载
     * @param projectId  projectId
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadShape", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadShape(@RequestParam Long projectId, HttpServletResponse response) {
        Project project = projectService.find(projectId);
        List<Shape> shapes = shapeService.findByProject(project);
        Workbook wb = shapeService.downloadShape(shapes);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "shape"+ ".xlsx" + "\"");
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
     * 图形剖面下载
     * @param projectId  projectId
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadShapeAttribute", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadSectionPlane(@RequestParam Long projectId, HttpServletResponse response) {
        Project project = projectService.find(projectId);
        List<Shape> shapes = shapeService.findByProject(project);
        Workbook wb = shapeService.downloadSectionPlane(shapes);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "lineSectionPlaneTemplate"+ ".xlsx" + "\"");
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
     * 建筑物属性下载
     * @param projectId  projectId
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadBuild", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadBuild(@RequestParam Long projectId, @RequestParam String baseLevelType, @RequestParam String WGS84Type, HttpServletResponse response) {
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
        Workbook wb = buildService.downloadBuild(project,wgs84Type);
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





    private Message isAllowUploadCoordinateFile(Project project) {
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


    /**
     * 获取建筑物
     * @param id 建筑物id
     * @return 建筑物对象
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public @ResponseBody Message getBuild(@RequestParam long id) {
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
     * 获取图形线面
     * @param id 建筑物id
     * @return 建筑物对象
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape", method = RequestMethod.GET)
    public @ResponseBody Message getShape(@RequestParam long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Shape shape = shapeService.find(id);
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = shapeService.buildJson(shape);
        return MessageService.message(Message.Type.OK,jsonObject);
    }

    /**
     * 获取图形属性
     * @param shapeId 图形id
     * @return 建筑物对象
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shapeAttribute", method = RequestMethod.GET)
    public @ResponseBody Message getShapeAttribute(@RequestParam long shapeId) {
        Message message = CommonController.parametersCheck(shapeId);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Shape shape = shapeService.find(shapeId);
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONArray jsonArray = shapeAttributeService.buildJson(shape);
        return MessageService.message(Message.Type.OK,jsonArray);
    }

    /**
     * 获取所有图形线面类型
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shapeTemplateInfo", method = RequestMethod.GET)
    public @ResponseBody Message shapeTemplateInfo() {
        return MessageService.message(Message.Type.OK,shapeService.getModelType());
    }

    /**
     * 获取所有图形线面属性类型
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shapeAttributeTemplateInfo", method = RequestMethod.GET)
    public @ResponseBody Message shapeAttributeTemplateInfo() {
        return MessageService.message(Message.Type.OK,shapeAttributeService.getModelType());
    }

    /**
     * 获取所有建筑物类型
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/buildTemplateInfo", method = RequestMethod.GET)
    public @ResponseBody Message buildTemplateInfo() {
        return MessageService.message(Message.Type.OK,buildService.getModelType());
    }

    /**
     * 删除图形线面
     * @param id 线面id
     * @return FAIL参数验证失败，OK删除成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteShape/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deleteShape(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Shape shape = shapeService.find(id);
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        List<ShapeAttribute> shapeAttributes = shapeAttributeService.findByShape(shape);
        for (ShapeAttribute shapeAttribute : shapeAttributes) {
            shapeAttributeService.remove(shapeAttribute);
        }
        List<ShapeCoordinate> shapeCoordinates = shapeCoordinateService.findByShape(shape);
        for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
            if (shapeCoordinate.getBuild() != null) {
                buildService.remove(shapeCoordinate.getBuild());
            }
            shapeCoordinateService.remove(shapeCoordinate);
        }
        shapeService.remove(shape);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除图形线面某点
     * @param shapeCoordinateId 线面坐标id
     * @return FAIL参数验证失败，OK删除成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteShapeCoordinate/{shapeCoordinateId}", method = RequestMethod.DELETE)
    public @ResponseBody Message deleteShapeCoordinate(@PathVariable("shapeCoordinateId") Long shapeCoordinateId) {
        Message message = CommonController.parametersCheck(shapeCoordinateId);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(shapeCoordinateId);
        if (shapeCoordinate == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (shapeCoordinate.getBuild() != null) {
            buildService.remove(shapeCoordinate.getBuild());
        }
        if (shapeCoordinate.getParent()==null) {
            ShapeCoordinate next = shapeCoordinate.getNext();
            next.setParent(null);
            shapeCoordinateService.save(next);
        } else {
            ShapeCoordinate parent = shapeCoordinate.getParent();
            ShapeCoordinate next = shapeCoordinate.getNext();
            parent.setNext(next);
            next.setParent(parent);
            shapeCoordinateService.save(parent);
            shapeCoordinateService.save(next);
        }
        shapeCoordinateService.remove(shapeCoordinate);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 编辑图形
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
    @SuppressWarnings("unchecked")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editShape", method = RequestMethod.POST)
    public @ResponseBody Message deletePoint(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object shape = objectMap.get("shape");
        if (shape == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        shapeService.editShape(shape);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 新建图形
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
    @SuppressWarnings("unchecked")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/newShape", method = RequestMethod.POST)
    public @ResponseBody Message newShape(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object shape = objectMap.get("shape");
        Object type = objectMap.get("type");
        Object remark = objectMap.get("remark");
        Object projectId = objectMap.get("projectId");
        if (shape == null || type == null || remark == null || projectId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        List<ShapeCoordinate> shapeCoordinates = new ArrayList<>();
        Shape shape1 = new Shape();
        shape1.setCommonType(CommonEnum.CommonType.valueOf(type.toString()));
        shape1.setRemark(remark.toString());
        shape1.setProject(projectService.find(Long.valueOf(projectId.toString())));
        Line line = null;
        Elevation elevation;
        ShapeCoordinate shapeCoordinate;
        List<Object> list = (List<Object>) shape;
        for (Object list1 : list) {
            List<Map<String, Object>> map = (List<Map<String, Object>>) list1;
            ShapeCoordinate next = null;
            for (Map<String, Object> map1 : map) {
                shapeCoordinate = new ShapeCoordinate();
                shapeCoordinate.setLat(map1.get("lat").toString());
                shapeCoordinate.setLon(map1.get("lon").toString());
                for (Line line1 : lineService.getLines()) {
                    if (line1.getCommonType() == shape1.getCommonType()) {
                        line = (Line) SettingUtils.objectCopy(line1);
                    }
                }
                for (int i = 3; i <= line.getCellProperty().split(",").length-2; i++) {
                    elevation = new Elevation("0", line.getCellProperty(), i, shape1,shapeCoordinate);
                    shapeCoordinate.setElevation(elevation);
                }
                shapeCoordinate.setShape(shape1);
//                shapeCoordinateService.save(shapeCoordinate);
                if (shapeCoordinates.size() != 0) {
                    shapeCoordinate.setParent(shapeCoordinates.get(shapeCoordinates.size()-1));
                    shapeCoordinates.get(shapeCoordinates.size()-1).setNext(shapeCoordinate);
                }
                shapeCoordinates.add(shapeCoordinate);
            }
            shapeCoordinates.get(shapeCoordinates.size()-1).setNext(next);
        }
        shape1.setShapeCoordinates(shapeCoordinates);
        shapeService.save(shape1);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑图形属性
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
//    @SuppressWarnings("unchecked")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editShapeAttribute", method = RequestMethod.POST)
    public @ResponseBody Message editShapeAttribute(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object shapeId = objectMap.get("shapeId");
        Object shapeAttributes = objectMap.get("shapeAttributes");
        if (shapeId == null || shapeAttributes == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape = shapeService.find(Long.valueOf(shapeId.toString()));
        ShapeAttribute shapeAttribute1;
        for (Map<String, Object> shapeAttribute : (List<Map<String, Object>>) shapeAttributes) {
            if (shapeAttribute.get("id") != null) {
                shapeAttribute1 = shapeAttributeService.find(Long.valueOf(shapeAttribute.get("id").toString()));
                shapeAttribute1.setValue(shapeAttribute.get("value").toString());
            } else {
                shapeAttribute1 = new ShapeAttribute();
                shapeAttribute1.setAlias(shapeAttribute.get("alias").toString());
                shapeAttribute1.setValue(shapeAttribute.get("value").toString());
                shapeAttribute1.setShape(shape);
            }
            shapeAttributeService.save(shapeAttribute1);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑建筑物属性
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
//    @SuppressWarnings("unchecked")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editBuildAttribute", method = RequestMethod.POST)
    public @ResponseBody Message editBuildAttribute(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object buildId = objectMap.get("buildId");
        Object buildAttributes = objectMap.get("buildAttributes");
        if (buildId == null || buildAttributes == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Build build = buildService.find(Long.valueOf(buildId.toString()));
        BuildAttribute buildAttribute1;
        for (Map<String, Object> buildAttribute : (List<Map<String, Object>>) buildAttributes) {
            if (buildAttribute.get("id") != null) {
                buildAttribute1 = buildAttributeService.find(Long.valueOf(buildAttribute.get("id").toString()));
                buildAttribute1.setValue(buildAttribute.get("value").toString());
            } else {
                buildAttribute1 = new BuildAttribute();
                buildAttribute1.setValue(buildAttribute.get("value").toString());
                buildAttribute1.setAlias(buildAttribute.get("alias").toString());
                buildAttribute1.setBuild(build);
            }
            buildAttributeService.save(buildAttribute1);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * BIM获取建筑物属性信息
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/bim/build/{id}", method = RequestMethod.GET)
    public @ResponseBody Object getBimBuild(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Build build = buildService.find(id);
        if (build == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return buildService.toJSON(build);
    }

    /**
     * BIM获取图形及其属性信息
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/bim/shape/{id}", method = RequestMethod.GET)
    public @ResponseBody Message getBimShape(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Shape shape = shapeService.find(id);
        List<ShapeCoordinate> shapeCoordinates = shapeCoordinateService.findByShape(shape);
        List<ShapeAttribute> shapeAttributes = shapeAttributeService.findByShape(shape);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shapeCoordinate", shapeCoordinateService.toJSON(shapeCoordinates));
        jsonObject.put("shapeAttribute", shapeAttributeService.toJSON(shapeAttributes));
        return MessageService.message(Message.Type.OK, jsonObject);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public @ResponseBody Object test() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("id", 1);
        jsonObject1.put("lon", 102);
        jsonObject1.put("lat", 35);
        jsonArray.add(jsonObject1);
        jsonObject1 = new JSONObject();
        jsonObject1.put("lon", 103);
        jsonObject1.put("lat", 36);
        jsonArray.add(jsonObject1);
        jsonObject1 = new JSONObject();
        jsonObject1.put("lon", 104);
        jsonObject1.put("lat", 35);
        jsonArray.add(jsonObject1);
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.add(jsonArray);
        jsonArray1.add(jsonArray);
        jsonArray1.add(jsonArray);
        jsonObject.put("shape", jsonArray1);
        return jsonObject;
    }

}
