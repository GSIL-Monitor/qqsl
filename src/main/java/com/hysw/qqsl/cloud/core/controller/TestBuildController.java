package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.buildModel.PLACache;
import com.hysw.qqsl.cloud.core.entity.buildModel.SheetObject;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.Shape;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/10/18
 */
@Controller("/testBuild")
public class TestBuildController {
    @Autowired
    private BuildService buildService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ShapeService shapeService;
    @Autowired
    private CoordinateService coordinateService;
//    要求一个类型只能上传一个建筑物，再次上传替换处理
//    1.建筑物模板下载
    /**
     * 获取所有建筑物类型
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/buildTemplateInfo", method = RequestMethod.GET)
    public @ResponseBody
    Message buildTemplateInfo() {
        return MessageService.message(Message.Type.OK,buildService.getModelType());
    }
//    2.建筑物excel上传
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

//    3.建筑物excel下载
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
//    4.bim获取建筑物信息
    /**
     * BIM获取建筑物属性信息
     * @return OK：请求成功
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/bim/build/{id}", method = RequestMethod.GET)
    public @ResponseBody Message getBimBuild(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Build build = buildService.find(id);
        if (build == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        JSONArray jsonArray = buildService.toJSON(build);
        return MessageService.message(Message.Type.OK, jsonArray);
    }
//    5.前台获取建筑物信息
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
