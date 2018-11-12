package com.hysw.qqsl.cloud.core.controller;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.buildModel.PLACache;
import com.hysw.qqsl.cloud.core.entity.buildModel.SheetObject;
import com.hysw.qqsl.cloud.core.entity.data.*;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/10/18
 */
@Controller
@RequestMapping("/testBuild")
public class TestBuildController {
    @Autowired
    private NewBuildService newBuildService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private NewBuildAttributeService newBuildAttributeService;
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
        return MessageService.message(Message.Type.OK,newBuildService.getModelType());
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
        String type = request.getParameter("type");
        String childType = request.getParameter("childType");
        CommonEnum.CommonType commonType = null;
        Build.ChildType childType1 = null;
        if (childType != null && !childType.equals("")) {
            childType1 = Build.ChildType.valueOf(childType.toUpperCase());
        } else if (type != null && !type.equals("")) {
            commonType = CommonEnum.CommonType.valueOf(type.toUpperCase());
        } else {
            return MessageService.message(Message.Type.FAIL);
        }

        JSONObject jsonObject = new JSONObject();
        Map<String, Workbook> wbs = new HashMap<>();
        if(multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> map = multiRequest.getFileMap();
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                newBuildService.uploadCoordinate(entry,jsonObject,wbs);
            }
        }
        if (!jsonObject.isEmpty()) {
            return MessageService.message(Message.Type.COOR_FORMAT_ERROR, jsonObject);
        }
        SheetObject sheetObject = new SheetObject();
        newBuildService.getAllSheet(wbs,sheetObject);
//		进入错误处理环节
        if (sheetObject.getUnknowWBs().size() != 0) {
            return MessageService.message(Message.Type.COOR_UNKONW_SHEET_TYPE,newBuildService.errorMsg(sheetObject.getUnknowWBs()));
        }
        if (sheetObject.getBuildWBs().size() != 1) {
            return MessageService.message(Message.Type.FAIL);
        }
        NewBuild newBuild = newBuildService.readSheet(sheetObject, commonType, childType1);
        if (newBuild == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK,newBuildService.buildJson(newBuild));
    }

//    3.建筑物excel下载

    /**
     * 建筑物属性下载
     *
     * @param response  响应
     * @return OK:下载成功 Fail:下载失败
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/downloadBuild", method = RequestMethod.GET)
    public @ResponseBody
    Message downloadBuild(@RequestParam String type, @RequestParam String childType, HttpServletResponse response) {
        Build.ChildType childType1 = null;
        CommonEnum.CommonType commonType = null;
        if (type != null && !type.equals("")) {
            commonType = CommonEnum.CommonType.valueOf(type.toUpperCase());
        }
        if (childType != null && !childType.equals("")) {
            childType1 = Build.ChildType.valueOf(childType.toUpperCase());
        }
        Workbook wb = newBuildService.downloadBuild(commonType, childType1);
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
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "buildsTemplate" + ".xlsx" + "\"");
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
    @RequestMapping(value = "/bim/build", method = RequestMethod.GET)
    public @ResponseBody Object getBimBuild(@RequestParam String type ,@RequestParam String childType) {
        Build.ChildType childType1 = null;
        CommonEnum.CommonType commonType = null;
        if (type != null && !type.equals("")) {
            commonType = CommonEnum.CommonType.valueOf(type.toUpperCase());
        }
        if (childType != null && !childType.equals("")) {
            childType1 = Build.ChildType.valueOf(childType.toUpperCase());
        }
        NewBuild newBuild = newBuildService.findbyTypeAndChildType(childType1,commonType);
        if (newBuild == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        return newBuildService.toJSON(newBuild);
    }
//    5.前台获取建筑物信息
    /**
     * 获取建筑物
     * @param type
     * @param childType
     * @return 建筑物对象
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public @ResponseBody Message getBuild(@RequestParam String type ,@RequestParam String childType) {
        Build.ChildType childType1 = null;
        CommonEnum.CommonType commonType = null;
        if (type != null && !type.equals("")) {
            commonType = CommonEnum.CommonType.valueOf(type.toUpperCase());
        }
        if (childType != null && !childType.equals("")) {
            childType1 = Build.ChildType.valueOf(childType.toUpperCase());
        }
        NewBuild newBuild = newBuildService.findbyTypeAndChildType(childType1,commonType);
        if (newBuild == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = newBuildService.buildJson(newBuild);
        return MessageService.message(Message.Type.OK,jsonObject);
    }

//    /**
//     * 编辑建筑物属性
//     * @param objectMap <ol><li>line线面对象</li><li>build建筑物集<ol><li>建筑物id</li></ol></li><li>description描述</li></ol>
//     * @return FAIL参数验证失败，OTHER坐标格式错误，EXIST建筑物不存在，OK编辑成功
//     */
////    @SuppressWarnings("unchecked")
////    @RequiresAuthentication
////    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/editBuildAttribute", method = RequestMethod.POST)
//    public @ResponseBody Message editBuildAttribute(@RequestBody  Map<String,Object> objectMap) {
//        Message message = CommonController.parameterCheck(objectMap);
//        if (message.getType() != Message.Type.OK) {
//            return message;
//        }
//        Object buildId = objectMap.get("buildId");
//        Object newBuildAttributes = objectMap.get("newBuildAttributes");
//        if (buildId == null || newBuildAttributes == null) {
//            return MessageService.message(Message.Type.FAIL);
//        }
//        NewBuild newBuild = newBuildService.find(Long.valueOf(buildId.toString()));
//        NewBuildAttribute newBuildAttribute1;
//        for (Map<String, Object> newBuildAttribute : (List<Map<String, Object>>) newBuildAttributes) {
//            if (newBuildAttribute.get("id") != null) {
//                newBuildAttribute1 = newBuildAttributeService.find(Long.valueOf(newBuildAttribute.get("id").toString()));
//                newBuildAttribute1.setValue(newBuildAttribute.get("value").toString());
//            } else {
//                newBuildAttribute1 = new NewBuildAttribute();
//                newBuildAttribute1.setValue(newBuildAttribute.get("value").toString());
//                newBuildAttribute1.setAlias(newBuildAttribute.get("alias").toString());
//                newBuildAttribute1.setBuild(newBuild);
//            }
//            newBuildAttributeService.save(newBuildAttribute1);
//        }
//        return MessageService.message(Message.Type.OK);
//    }

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
        Workbook wb = newBuildService.downloadBuildModel(list);
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
