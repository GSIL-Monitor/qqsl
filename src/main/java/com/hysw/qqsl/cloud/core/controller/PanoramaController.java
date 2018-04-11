package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private UserService userService;
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
    private void writer(HttpServletResponse httpResponse,String xmlStr){
        try {
            httpResponse.setContentType("text/xml;charset=" + CommonAttributes.CHARSET);
            httpResponse.getWriter().write(xmlStr);
            httpResponse.getWriter().flush();
            httpResponse.getWriter().close();
        }catch (Exception exceptioe){

        }
    }
    @RequestMapping(value = "/{instanceId}", method = RequestMethod.GET)
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
        User user = authentService.getUserFromSubject();
        boolean flag;
        if (user == null) {
            Account account = authentService.getAccountFromSubject();
            flag = panoramaService.addPanorama(name,jsonObject1,region,isShare,info,images, new Panorama(), account);
        } else {
            flag = panoramaService.addPanorama(name,jsonObject1,region,isShare,info,images, new Panorama(), user);
        }
        if (!flag) {
            return MessageService.message(Message.Type.PANORAMA_SLICE_ERROE);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 需删除（仅供测试）
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/add1", method = RequestMethod.GET)
    public @ResponseBody Message add1() throws IOException, InterruptedException {
        String str = "{\"name\":\"全景名称1111111111\",\"info\":\"全景描述2222222222222222\",\"coor\":\"103.77645101765913,36.05377593481913,0\",\"isShare\":\"true\",\"region\":\"中国甘肃省兰州市七里河区兰工坪南街190号 邮政编码: 730050\",\"images\":[{\"name\":\"001-西宁\", \"fileName\":\"11522811870947bik.jpg\"},{\"name\":\"333-西安\",\"fileName\":\"152281187095756l.jpg\"}]}";
        Map<String, Object> map =JSONObject.fromObject(str);
        User user = new User();
        user.setId(26l);
        JSONObject jsonObject1 = SettingUtils.checkCoordinateIsInvalid(map.get("coor").toString());
        panoramaService.addPanorama(map.get("name"),jsonObject1,map.get("region"),map.get("isShare"),map.get("info"),map.get("images"), new Panorama(), user);
        return MessageService.message(Message.Type.OK);
    }
}
