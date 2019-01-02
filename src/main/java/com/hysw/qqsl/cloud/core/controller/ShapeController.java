package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.annotation.util.PackageIsExpire;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.buildModel.*;
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
    private AuthentService authentService;
    @Autowired
    private BuildDynAttributeService buildDynAttributeService;

    Log logger = LogFactory.getLog(getClass());

    /**
     * 获取所有图形线面类型
     * @return OK：请求成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/lists", method = RequestMethod.GET)
    public @ResponseBody Message lists(@RequestParam long projectId) {
        Project project = projectService.find(projectId);
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(project.getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        return MessageService.message(Message.Type.OK,shapeService.getLists(project));
    }

    /**
     * 获取所有图形线面类型
     * @return OK：请求成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/type/download", method = RequestMethod.GET)
    public @ResponseBody Message shapeTemplateInfo() {
        return MessageService.message(Message.Type.OK,shapeService.getModelType());
    }


    /**
     * 图形线面模板下载
     * @param types  a,b,c
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/template/download", method = RequestMethod.GET)
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
     * 上传图形线面
     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
     */
    @PackageIsExpire(value = "request")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/upload", method = RequestMethod.POST)
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
     * 图形线面下载
     * @param projectId  projectId
     * @param response 响应
     * @return OK:下载成功 Fail:下载失败
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/download", method = RequestMethod.GET)
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
     * 获取图形线面详情
     * @param id 建筑物id
     * @return 建筑物对象
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/details/{id}", method = RequestMethod.GET)
    public @ResponseBody Message getShape(@PathVariable("id") Long id) {
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
     * 新建图形线面
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/create", method = RequestMethod.POST)
    public @ResponseBody Message newShape(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object coors = objectMap.get("coors");
        Object type = objectMap.get("type");
        Object remark = objectMap.get("remark");
        Object projectId = objectMap.get("projectId");
        if (coors == null || type == null || remark == null || projectId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape = shapeService.newShape(coors, type, remark, projectId);
        return MessageService.message(Message.Type.OK,shapeService.buildJson(shape));
    }

    /**
     * 编辑图形线面
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/edit", method = RequestMethod.POST)
    public @ResponseBody Message editShape(@RequestBody  Map<String,Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object shapeId = objectMap.get("id");
        Object remark = objectMap.get("remark");
        Object coors = objectMap.get("coors");
        if (coors == null || shapeId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape = shapeService.editShape(coors,shapeId,remark);
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        return MessageService.message(Message.Type.OK,shapeService.buildJson(shape));
    }


    /**
     * 删除图形线面
     * @param id 线面id
     * @return FAIL参数验证失败，OK删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/delete/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deleteShape(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Shape shape = shapeService.find(id);
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(shape.getProject().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        shapeService.remove(shape);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除图形线面某点
     * @param id 线面坐标id
     * @return FAIL参数验证失败，OK删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/coordinate/delete/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deleteShapeCoordinate(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(id);
        if (shapeCoordinate == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(shapeCoordinate.getShape().getProject().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
//        if (shapeCoordinate.getBuild() != null) {
//            buildService.remove(shapeCoordinate.getBuild());
//        }
        shapeCoordinateService.deleteShapeCoordinateById(shapeCoordinate);
        return MessageService.message(Message.Type.OK);
    }

//    /**
//     * 取得图形线面下某点详情
//     *
//     * @param id 线面点id
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/coordinate/details/{id}", method = RequestMethod.GET)
//    public @ResponseBody
//    Message coordinateDetailsId(@PathVariable("id") Long id) {
//        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(id);
//        if (shapeCoordinate == null) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        User user = authentService.getUserFromSubject();
//        if (!user.getId().equals(shapeCoordinate.getShape().getProject().getUser().getId())) {
//            return MessageService.message(Message.Type.DATA_REFUSE);
//        }
//        JSONObject jsonObject = shapeCoordinateService.getCoordinateDetails(shapeCoordinate);
//        return MessageService.message(Message.Type.OK,jsonObject);
//    }

//    /**
//     * 编辑图形线面下某点高程组
//     *
//     * @param objectMap <ul>
//     *                  <li>id：线面点id</li>
//     *                  <li>elevations:[{{top-ele:xxx,name:xxx,value:xxx}}, {...}]</li>
//     *                  </ul>
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/coordinate/elevation/edit", method = RequestMethod.POST)
//    public @ResponseBody
//    Message coordinateElevationEdit(@RequestBody Map<String, Object> objectMap) {
//        Object id = objectMap.get("id");
//        Object elevations = objectMap.get("elevations");
//        if (id == null || elevations == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(Long.valueOf(id.toString()));
//        if (shapeCoordinate == null) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        User user = authentService.getUserFromSubject();
//        if (!user.getId().equals(shapeCoordinate.getShape().getProject().getUser().getId())) {
//            return MessageService.message(Message.Type.DATA_REFUSE);
//        }
//        shapeCoordinate.setElevations(elevations.toString());
//        shapeCoordinateService.save(shapeCoordinate);
//        return MessageService.message(Message.Type.OK);
//    }

    /**
     * 新建图形线面剖面属性
     *
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/attribute/create", method = RequestMethod.POST)
    public @ResponseBody
    Message coordinateDetailsId(@RequestBody Map<String, Object> objectMap) {
        Object id = objectMap.get("id");
        Object type = objectMap.get("type");
        if (id == null || type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape = shapeService.find(Long.valueOf(id.toString()));
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(shape.getProject().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (shape.getChildType() != null) {
            return MessageService.message(Message.Type.DATA_EXIST);
        }
        shape.setChildType(LineSectionPlaneModel.Type.valueOf(type.toString().toUpperCase()));
        shapeService.save(shape);
        JSONObject jsonObject = shapeService.buildJson(shape);
        jsonObject.remove("remark");
        jsonObject.remove("id");
        jsonObject.remove("type");
        jsonObject.remove("coors");
        jsonObject.remove("childType");
        return MessageService.message(Message.Type.OK, jsonObject);
    }

    /**
     * 删除图形剖面属性
     *
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/attribute/remove", method = RequestMethod.POST)
    public @ResponseBody
    Message shapeAttributeRemove(@RequestBody Map<String, Object> objectMap) {
        Object id = objectMap.get("id");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape = shapeService.find(Long.valueOf(id.toString()));
        List<ShapeAttribute> shapeAttributes = shapeAttributeService.findByShape(shape);
        if (shapeAttributes == null || shapeAttributes.size() == 0) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(shape.getProject().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        for (ShapeAttribute shapeAttribute : shapeAttributes) {
            shapeAttributeService.remove(shapeAttribute);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑图形线面剖面属性
     *
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/shape/attribute/edit", method = RequestMethod.POST)
    public @ResponseBody
    Message shapeAttributeEdit(@RequestBody Map<String, Object> objectMap) {
        Object id = objectMap.get("id");
        Object attributes = objectMap.get("attributes");
        if (id == null || attributes == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Shape shape = shapeService.find(Long.valueOf(id.toString()));
        if (shape == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(shape.getProject().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        shapeAttributeService.editShapeAttribute(shape,attributes);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 获取建筑物类型
     * @return OK：请求成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build/type/list", method = RequestMethod.GET)
    public @ResponseBody Message buildTemplateInfo() {
        return MessageService.message(Message.Type.OK,buildService.getModelType());
    }

//    /**
//     * 建筑物模板下载
//     * @param types  [a,b,c]
//     * @param response 响应
//     * @return OK:下载成功 Fail:下载失败
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/build/template/download", method = RequestMethod.GET)
//    public @ResponseBody
//    Message downloadBuildModel(@RequestParam String[] types, HttpServletResponse response) {
//        List<String> list = Arrays.asList(types);
//        Workbook wb = buildService.downloadBuildModel(list);
//        if (wb == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        ByteArrayOutputStream bos = null;
//        InputStream is = null;
//        OutputStream output = null;
//        try {
//            bos = new ByteArrayOutputStream();
//            wb.write(bos);
//            is = new ByteArrayInputStream(bos.toByteArray());
//            String contentType = "application/vnd.ms-excel";
//            response.setContentType(contentType);
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + "buildsTemplate"+ ".xlsx" + "\"");
//            output = response.getOutputStream();
//            byte b[] = new byte[1024];
//            while (true) {
//                int length = is.read(b);
//                if (length == -1) {
//                    break;
//                }
//                output.write(b, 0, length);
//            }
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            return MessageService.message(Message.Type.FAIL);
//        } finally {
//            IOUtils.safeClose(bos);
//            IOUtils.safeClose(is);
//            IOUtils.safeClose(output);
//        }
//        return MessageService.message(Message.Type.OK);
//    }

//    /**
//     * 单建筑物上传
//     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
//     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
//     */
//    @PackageIsExpire(value = "request")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/single/uploadBuild", method = RequestMethod.POST)
//    public @ResponseBody
//    Message singleUploadBuild(HttpServletRequest request) {
//        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
//        String shapeCoordinateId = request.getParameter("shapeCoordinateId");
//        String baseLevelType = request.getParameter("baseLevelType");
//        String WGS84Type = request.getParameter("WGS84Type");
//        Message message;
//        JSONObject jsonObject = new JSONObject();
//        if (shapeCoordinateId == null || baseLevelType == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        ShapeCoordinate shapeCoordinate;
//        Coordinate.WGS84Type wgs84Type = null;
//        Coordinate.BaseLevelType levelType;
//        try {
//            shapeCoordinate = shapeCoordinateService.find(Long.valueOf(shapeCoordinateId));
//            message=isAllowUploadCoordinateFile(shapeCoordinate.getShape().getProject());
//            levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
//            if (!WGS84Type.equals("")) {
//                wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
//            }
//        } catch (Exception e) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        if (message.getType() != Message.Type.OK) {
//            return message;
//        }
//        if (shapeCoordinate == null) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
//            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
//        }
//        String central = coordinateService.getCoordinateBasedatum(shapeCoordinate.getShape().getProject());
//        if (central == null) {
//            return MessageService.message(Message.Type.COOR_PROJECT_NO_CENTER);
//        }
//        Map<String, Workbook> wbs = new HashMap<>();
//        if(multipartResolver.isMultipart(request)) {
//            //转换成多部分request
//            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
//            Map<String, MultipartFile> map = multiRequest.getFileMap();
//            MultipartFile file = map.get("file");
//            shapeService.uploadCoordinate(file,jsonObject,wbs);
//        }
//        if (wbs.size() != 1) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        if (!jsonObject.isEmpty()) {
//            return MessageService.message(Message.Type.COOR_FORMAT_ERROR, jsonObject);
//        }
//        SheetObject sheetObject = new SheetObject();
//        shapeService.getAllSheet(wbs,sheetObject);
////		进入错误处理环节
//        if (sheetObject.getUnknowWBs().size() != 0) {
//            return MessageService.message(Message.Type.COOR_UNKONW_SHEET_TYPE,shapeService.errorMsg(sheetObject.getUnknowWBs()));
//        }
//        PLACache plaCache = shapeService.reslove(sheetObject, central, wgs84Type, shapeCoordinate.getShape().getProject(), shapeCoordinate);
//        if (plaCache == null) {
//            return MessageService.message(Message.Type.OK);
//        }
//        JSONArray jsonArray = shapeService.pickedErrorMsg1(plaCache);
//        return MessageService.message(Message.Type.COOR_RETURN_PROMPT, jsonArray);
//    }

//    /**
//     * 多建筑物上传
//     * @param request projectId项目Id，baseLevelType坐标转换基准面类型，WGS84Type-WGS84坐标格式
//     * @return FAIL参数验证失败，EXIST项目不存在或者中心点为空，OTHER已达到最大限制数量，OK上传成功
//     */
//    @PackageIsExpire(value = "request")
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/multiple/uploadBuild", method = RequestMethod.POST)
//    public @ResponseBody
//    Message uploadBuildAttribute(HttpServletRequest request) {
//        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
//        String shapeId = request.getParameter("shapeId");
//        String baseLevelType = request.getParameter("baseLevelType");
//        String WGS84Type = request.getParameter("WGS84Type");
//        Message message;
//        JSONObject jsonObject = new JSONObject();
//        if (shapeId == null || baseLevelType == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        Shape shape;
//        Coordinate.WGS84Type wgs84Type = null;
//        Coordinate.BaseLevelType levelType;
//        try {
//            shape = shapeService.find(Long.valueOf(shapeId));
//            message=isAllowUploadCoordinateFile(shape.getProject());
//            levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
//            if (!WGS84Type.equals("")) {
//                wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
//            }
//        } catch (Exception e) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        if (message.getType() != Message.Type.OK) {
//            return message;
//        }
//        if (shape == null) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
//            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
//        }
//        String central = coordinateService.getCoordinateBasedatum(shape.getProject());
//        if (central == null) {
//            return MessageService.message(Message.Type.COOR_PROJECT_NO_CENTER);
//        }
//        Map<String, Workbook> wbs = new HashMap<>();
//        if(multipartResolver.isMultipart(request)) {
//            //转换成多部分request
//            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
//            Map<String, MultipartFile> map = multiRequest.getFileMap();
//            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
//                shapeService.uploadCoordinate(entry,jsonObject,wbs);
//            }
//        }
//        if (!jsonObject.isEmpty()) {
//            return MessageService.message(Message.Type.COOR_FORMAT_ERROR, jsonObject);
//        }
//        SheetObject sheetObject = new SheetObject();
//        shapeService.getAllSheet(wbs,sheetObject);
////		进入错误处理环节
//        if (sheetObject.getUnknowWBs().size() != 0) {
//            return MessageService.message(Message.Type.COOR_UNKONW_SHEET_TYPE,shapeService.errorMsg(sheetObject.getUnknowWBs()));
//        }
//        PLACache plaCache = shapeService.reslove(sheetObject, central, wgs84Type, shape.getProject(), shape);
//        if (plaCache == null) {
//            return MessageService.message(Message.Type.OK);
//        }
//        JSONArray jsonArray = shapeService.pickedErrorMsg1(plaCache);
//        return MessageService.message(Message.Type.COOR_RETURN_PROMPT, jsonArray);
//    }

//    /**
//     * 单建筑物下载
//     *
//     * @param coordinateId
//     * @param response
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/build/single/download", method = RequestMethod.GET)
//    public @ResponseBody
//    Message buildSingleDownload(@RequestParam Long coordinateId, @RequestParam String baseLevelType, @RequestParam String WGS84Type, HttpServletResponse response) {
//        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(coordinateId);
//        if (shapeCoordinate == null) {
//            return MessageService.message(Message.Type.DATA_NOEXIST);
//        }
//        Coordinate.BaseLevelType levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
//        Coordinate.WGS84Type wgs84Type = null;
//        if (!WGS84Type.equals("")) {
//            wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
//        }
//        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
//            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
//        }
//        if (wgs84Type == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        Workbook wb = buildService.downloadBuild(shapeCoordinate,wgs84Type);
//        if (wb == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        ByteArrayOutputStream bos = null;
//        InputStream is = null;
//        OutputStream output = null;
//        try {
//            bos = new ByteArrayOutputStream();
//            wb.write(bos);
//            is = new ByteArrayInputStream(bos.toByteArray());
//            String contentType = "application/vnd.ms-excel";
//            response.setContentType(contentType);
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + "buildsTemplate"+ ".xlsx" + "\"");
//            output = response.getOutputStream();
//            byte b[] = new byte[1024];
//            while (true) {
//                int length = is.read(b);
//                if (length == -1) {
//                    break;
//                }
//                output.write(b, 0, length);
//            }
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            return MessageService.message(Message.Type.FAIL);
//        } finally {
//            IOUtils.safeClose(bos);
//            IOUtils.safeClose(is);
//            IOUtils.safeClose(output);
//        }
//        return MessageService.message(Message.Type.OK);
//    }

//    /**
//     * 多建筑物下载
//     * @param projectId  projectId
//     * @param response 响应
//     * @return OK:下载成功 Fail:下载失败
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/build/multiple/download", method = RequestMethod.GET)
//    public @ResponseBody
//    Message downloadBuild(@RequestParam Long projectId, @RequestParam String baseLevelType, @RequestParam String WGS84Type, HttpServletResponse response) {
//        Project project = projectService.find(projectId);
//        Coordinate.BaseLevelType levelType = Coordinate.BaseLevelType.valueOf(baseLevelType);
//        Coordinate.WGS84Type wgs84Type = null;
//        if (!WGS84Type.equals("")) {
//            wgs84Type = Coordinate.WGS84Type.valueOf(WGS84Type);
//        }
//        if (levelType == Coordinate.BaseLevelType.CGCS2000) {
//            wgs84Type = Coordinate.WGS84Type.PLANE_COORDINATE;
//        }
//        if (wgs84Type == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        Workbook wb = buildService.downloadBuild(project,wgs84Type);
//        if (wb == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        ByteArrayOutputStream bos = null;
//        InputStream is = null;
//        OutputStream output = null;
//        try {
//            bos = new ByteArrayOutputStream();
//            wb.write(bos);
//            is = new ByteArrayInputStream(bos.toByteArray());
//            String contentType = "application/vnd.ms-excel";
//            response.setContentType(contentType);
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + "buildsTemplate"+ ".xlsx" + "\"");
//            output = response.getOutputStream();
//            byte b[] = new byte[1024];
//            while (true) {
//                int length = is.read(b);
//                if (length == -1) {
//                    break;
//                }
//                output.write(b, 0, length);
//            }
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            return MessageService.message(Message.Type.FAIL);
//        } finally {
//            IOUtils.safeClose(bos);
//            IOUtils.safeClose(is);
//            IOUtils.safeClose(output);
//        }
//        return MessageService.message(Message.Type.OK);
//    }

    /**
     * 获取建筑物详情
     * @param id 建筑物id
     * @return 建筑物对象
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/point/details/{id}", method = RequestMethod.GET)
    public @ResponseBody Message getBuild(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(id);
        if (shapeCoordinate == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!user.getId().equals(shapeCoordinate.getShape().getProject().getUser().getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        JSONObject jsonObject = shapeCoordinateService.buildJson(shapeCoordinate);
        return MessageService.message(Message.Type.OK,jsonObject);
    }

    /**
     * 新建建筑物
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build/create", method = RequestMethod.POST)
    public @ResponseBody Message buildCreate(@RequestBody  Map<String,Object> objectMap) {
        Object id = objectMap.get("id");
        Object type = objectMap.get("type");
        Object childType = objectMap.get("childType");
        if (id == null && type == null) {
            return MessageService.message(Message.Type.PARAMETER_ERROR);
        }
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(Long.valueOf(id.toString()));
        if (shapeCoordinate.getBuild() != null) {
            return MessageService.message(Message.Type.DATA_EXIST);
        }
        Build build = new Build();
        build.setType(CommonEnum.CommonType.valueOf(type.toString()));
        if (childType != null && !childType.toString().equals("")) {
            build.setChildType(Build.ChildType.valueOf(childType.toString()));
        }
        build.setShapeCoordinate(shapeCoordinate);
        buildService.save(build);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 编辑建筑物
     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build/edit", method = RequestMethod.POST)
    public @ResponseBody Message editBuildAttribute(@RequestBody  Map<String,Object> objectMap) {
        Object buildId = objectMap.get("id");
        Object buildAttributes = objectMap.get("attributes");
        Object dyn = objectMap.get("dynAttributes");
        if (buildId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Build build = buildService.find(Long.valueOf(buildId.toString()));
        if (build == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        String s = buildAttributeService.editBuildAttribute(build, buildAttributes);
        if (s == null) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (s.equals("")) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (s.equals("2")) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        buildService.save(build);
        if (dyn != null) {
            JSONArray jsonArray = JSONArray.fromObject(dyn);
            BuildDynAttribute buildDynAttribute;
            JSONObject jsonObject;
            for (Object o : jsonArray) {
                jsonObject = JSONObject.fromObject(o);
                Object id = jsonObject.get("id");
                Object groupAlias = jsonObject.get("groupAlias");
                Object alias = jsonObject.get("alias");
                Object code = jsonObject.get("code");
                Object value = jsonObject.get("value");
                if (groupAlias == null || alias == null || code == null || value == null) {
                    return MessageService.message(Message.Type.PARAMETER_ERROR);
                }
                if (id != null) {
                    buildDynAttribute = buildDynAttributeService.find(Long.valueOf(id.toString()));
                    buildDynAttribute.setCode(Integer.valueOf(code.toString()));
                    buildDynAttribute.setValue(value.toString());
                } else {
                    buildDynAttribute = new BuildDynAttribute();
                    buildDynAttribute.setValue(value.toString());
                    buildDynAttribute.setCode(Integer.valueOf(code.toString()));
                    buildDynAttribute.setAlias(alias.toString());
                    buildDynAttribute.setGroupAlias(groupAlias.toString());
                    buildDynAttribute.setBuild(build);
                }
                buildDynAttributeService.save(buildDynAttribute);
            }
        }
        JSONObject jsonObject = buildService.buildJson(build);
        return MessageService.message(Message.Type.OK,jsonObject);
    }

    /**
     * 建筑物动态组属性获取
     * @param groupAlias 组别名
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build/dyn", method = RequestMethod.GET)
    public @ResponseBody Message builDDyn(@RequestParam String groupAlias) {
        AttributeGroup attributeGroup = buildDynAttributeService.getAttributeGroup(groupAlias);
        if (attributeGroup == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONArray jsonArray = buildDynAttributeService.toJSON(attributeGroup);
        return MessageService.message(Message.Type.OK,jsonArray);
    }

    /**
     * 删除建筑物动态属性
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build/dyn/remove", method = RequestMethod.POST)
    public @ResponseBody Message deleteDyn(@RequestBody  Map<String,Object> objectMap) {
        Object buildId = objectMap.get("id");
        Object code = objectMap.get("code");
        Object groupAlias = objectMap.get("groupAlias");
        if (buildId == null || code == null || groupAlias == null) {
            return MessageService.message(Message.Type.PARAMETER_ERROR);
        }
        Build build = buildService.find(Long.valueOf(buildId.toString()));
        List<BuildDynAttribute> buildDynAttributes = buildDynAttributeService.findByBuildAndCodeAndGroupAlias(build, code, groupAlias);
        if (buildDynAttributes.size() == 0) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        for (BuildDynAttribute buildDynAttribute : buildDynAttributes) {
            buildDynAttributeService.remove(buildDynAttribute);
        }
        buildDynAttributeService.changeCode(build, code, groupAlias);
        return MessageService.message(Message.Type.OK);
    }



    /**
     * 地图上新建建筑物
     * @param objectMap
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/map/build/create", method = RequestMethod.POST)
    public @ResponseBody Message newBuild(@RequestBody  Map<String,Object> objectMap) {
        Object shapeCoordinateId = objectMap.get("shapeCoordinateId");
        Object projectId = objectMap.get("projectId");
        Object remark = objectMap.get("remark");
        Object type = objectMap.get("type");
        if (shapeCoordinateId == null || remark == null || type == null || projectId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(Long.valueOf(shapeCoordinateId.toString()));
        if (shapeCoordinate == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (shapeCoordinate.getBuild() != null) {
            return MessageService.message(Message.Type.DATA_EXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!shapeCoordinate.getShape().getProject().getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        Build build = new Build();
        build.setShapeCoordinate(shapeCoordinate);
        build.setType(CommonEnum.CommonType.valueOf(type.toString().toUpperCase()));
        build.setRemark(remark.toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", shapeCoordinate.getLon());
        jsonObject.put("lat", shapeCoordinate.getLat());
        build.setPositionCoor(jsonObject.toString());
        build.setProjectId(Long.valueOf(projectId.toString()));
        buildService.save(build);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 地图上编辑建筑物
     * @param objectMap
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/map/build/edit", method = RequestMethod.POST)
    public @ResponseBody Message editBuild(@RequestBody  Map<String,Object> objectMap) {
        Object id = objectMap.get("id");
        Object remark = objectMap.get("remark");
        if (id == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Build build = buildService.find(Long.valueOf(id.toString()));
        if (remark != null) {
            build.setRemark(remark.toString());
        }
        buildService.save(build);
        return MessageService.message(Message.Type.OK);
    }


    /**
     * BIM取得建筑物详情
     * @param id 建筑物id
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
            return MessageService.message(Message.Type.DATA_NOEXIST);
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
        jsonObject.put("id", shape.getId());
        jsonObject.put("type", shape.getCommonType());
        jsonObject.put("childType", shape.getChildType() == null ? null : shape.getChildType());
        jsonObject.put("remark", shape.getRemark());
        jsonObject.put("shapeCoordinate", shapeCoordinateService.toJSON(shapeCoordinates));
        jsonObject.put("shapeAttribute", shapeAttributeService.toJSON(shapeAttributes));
        return MessageService.message(Message.Type.OK, jsonObject);
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




}
