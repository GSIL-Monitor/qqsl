package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Create by leinuo on 18-4-8 下午3:08
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */

@Controller
@RequestMapping("/panorama")
public class PanoramaController {
    @Autowired
    private PanoramaService panoramaService;
    @Autowired
    private AuthentService authentService;

    @RequestMapping(value = "/tour.xml/{instanceId}", method = RequestMethod.GET,produces="application/xml; charset=UTF-8")
    public
    @ResponseBody
    void getTour(HttpServletResponse httpResponse,@PathVariable("instanceId") String instanceId) {
        String tourStr = panoramaService.getTour(instanceId);
        writer(httpResponse,tourStr);
    }

    @RequestMapping(value = "/tour.xml/skin.xml", method = RequestMethod.GET,produces="application/xml; charset=UTF-8")
    public
    @ResponseBody
    void getskin(HttpServletResponse httpResponse) {
        String skinStr = panoramaService.getSkin();
        writer(httpResponse,skinStr);
    }

    @RequestMapping(value = "/tour.xml/vtourskin.png", method = RequestMethod.GET)
    public
    @ResponseBody
    void getsPng(HttpServletResponse httpResponse) {
        httpResponse.setContentType("image/*");
        FileInputStream fis = null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(new ClassPathResource("/vtourskin.png").getFile());
            os = httpResponse.getOutputStream();
            int count = 0;
            byte[] buffer = new byte[1024 * 8];
            while ((count = fis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fis.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writer(HttpServletResponse httpResponse,String xmlStr){
        try {
            httpResponse.setContentType("text/xml;charset=" + CommonAttributes.CHARSET);
            httpResponse.getWriter().write(xmlStr);
            httpResponse.getWriter().flush();
            httpResponse.getWriter().close();
        }catch (Exception exceptioe){

        }
    }

    @RequestMapping(value = "/panorama/{instanceId}", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Message getPanoramaConfig(@PathVariable("instanceId") String instanceId) {
        JSONObject jsonObject = panoramaService.get(instanceId);
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 添加全景
     * @param objectMap
     * <ul>
     *     <li>name 名称</li>
     *     <li>coor 坐标，格式（经度,纬度,高程）</li>
     *     <li>region 行政区</li>
     *     <li>info 简介</li>
     *     <li>images
     *     <ul>
     *         <li>name 文件名</li>
     *         <li>fileName 文件唯一标示名</li>
     *     </ul>
     *     </li>
     * </ul>
     * @return
     * <ul>
     *     <li>OK 保存成功</li>
     *     <li>FAIL 操作失败</li>
     *     <li>PANORAMA_SLICE_ERROE 切图失败</li>
     * </ul>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody
    Message add(@RequestBody Map<String,Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object name = objectMap.get("name");
        Object coor = objectMap.get("coor");
        Object region = objectMap.get("region");
        Object isShare = objectMap.get("isShare");
        Object info = objectMap.get("info");
        Object images = objectMap.get("images");
        if (name == null || coor == null || isShare == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        JSONObject jsonObject1 = SettingUtils.checkCoordinateIsInvalid(coor.toString());
        if (jsonObject1 == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Object object = authentService.getUserFromSubject();
        if (object == null) {
            object = authentService.getAccountFromSubject();
        }
        return MessageService.message(Message.Type.valueOf(panoramaService.addPanorama(name,jsonObject1,region,isShare,info,images, new Panorama(), object)));
    }


    /**
     * 获取个人全景列表（无场景，无兴趣点）
     * @return
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public @ResponseBody Message lists(){
        User user = authentService.getUserFromSubject();
        List<Panorama> panoramas = null;
        if (user == null) {
            Account account = authentService.getAccountFromSubject();
            panoramas = panoramaService.findByAccount(account);
        }else{
            panoramas = panoramaService.findByUser(user);
        }
        return MessageService.message(Message.Type.OK,panoramaService.panoramasToJsonNoScene(panoramas));
    }

    /**
     * 获取所有审核通过的全景和用户自己建立的全景
     * @return
     */
    @RequestMapping(value = "/all/lists", method = RequestMethod.GET)
    public @ResponseBody Message allLists(){
        Object object = authentService.getUserFromSubject();
        List<Panorama> panoramas;
        if (object == null) {
            object = authentService.getAccountFromSubject();
            if (object == null) {
                panoramas = panoramaService.findAllPass(object);
            } else {
                panoramas = panoramaService.findAllPass(null);
            }
        }else{
            panoramas = panoramaService.findAllPass(object);
        }
        return MessageService.message(Message.Type.OK,panoramaService.panoramasToJsonHaveScene(panoramas));
    }

//    @RequiresAuthentication
//    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message adminLists(){
        List<Panorama> panoramas = panoramaService.findAllPending();
        if (panoramas == null || panoramas.size() == 0) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK,panoramaService.panoramasToJsonAdmin(panoramas));
    }
}
