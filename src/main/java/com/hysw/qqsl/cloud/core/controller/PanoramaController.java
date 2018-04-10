package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.core.service.PanoramaService;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 全景控制层
 * @anthor Administrator
 * @since 9:05 2018/4/10
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

    /**
     * 添加全景
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody
    Message add(@RequestBody Map<String,Object> objectMap) {
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

//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/add1", method = RequestMethod.GET)
    public @ResponseBody Message add1() {
        String str = "{\"name\":\"全景名称1111111111\",\"info\":\"全景描述2222222222222222\",\"coor\":\"103.77645101765913,36.05377593481913,0\",\"isShare\":\"true\",\"region\":\"中国甘肃省兰州市七里河区兰工坪南街190号 邮政编码: 730050\",\"images\":[{\"name\":\"001-西宁\", \"fileName\":\"1522811870947bik.jpg\"},{\"name\":\"333-西安\",\"fileName\":\"152281187095756l.jpg\"}]}";
        Map<String, Object> map =JSONObject.fromObject(str);
        User user = new User();
        user.setId(26l);
        JSONObject jsonObject1 = SettingUtils.checkCoordinateIsInvalid(map.get("coor").toString());
        panoramaService.addPanorama(map.get("name"),jsonObject1,map.get("region"),map.get("isShare"),map.get("info"),map.get("images"), new Panorama(), user);
        return MessageService.message(Message.Type.OK);
    }
}
