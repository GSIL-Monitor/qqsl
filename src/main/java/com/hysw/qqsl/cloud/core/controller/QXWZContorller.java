package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.annotation.util.IsExpire;
import com.hysw.qqsl.cloud.core.service.ApplicationTokenService;
import com.hysw.qqsl.cloud.core.service.DiffConnPollService;
import com.hysw.qqsl.cloud.core.service.PositionService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 千寻控制层
 *
 * @author chenl
 * @create 2017-09-20 下午2:23
 */
@Controller
@RequestMapping("/qxwz")
public class QXWZContorller {
    @Autowired
    private DiffConnPollService diffConnPollService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private ApplicationTokenService applicationTokenService;


    /**
     * 请求千寻账号密码
     *
     * @param token
     * @return
     */
//    @IsExpire
    @RequestMapping(value = "/getAccount", method = RequestMethod.GET)
    public @ResponseBody
    Message account(@RequestParam String token, @RequestParam String mac, @RequestParam String projectId) {
        //是否可以获取千寻账号
        Message message;
        if (token.length() == 0) {
            return new Message(Message.Type.FAIL);
        }
        if (mac == null || mac.trim().length() == 0 || mac.equals("null")|| projectId == null || projectId.trim().length() == 0 || projectId.equals("null")) {
            return new Message(Message.Type.FAIL);
        }
        try {
            message = positionService.isAllowConnectQXWZ(Long.valueOf(projectId));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if(message.getType()==Message.Type.NO_ALLOW){
            return message;
        }
        if (applicationTokenService.decrypt(token)) {
            return positionService.randomPosition(mac.trim(),projectId);
        }
        return new Message(Message.Type.FAIL);
    }

    /**
     * 是否拥有千寻连接权限
     * @param projectId
     * @return
     */
//    @IsExpire
    @RequestMapping(value = "/isAllowQxwz", method = RequestMethod.GET)
    public @ResponseBody
    Message isAllowQxwz(@RequestParam Long projectId) {
        //是否可以获取千寻账号
        return diffConnPollService.isAllowConnectQXWZ(projectId);
    }

    /**
     * 增加千寻账户
     * @param object
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/create",method = RequestMethod.POST)
    public @ResponseBody Message create(@RequestBody Map<String,Object> object){
        Message message = Message.parameterCheck(object);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        return diffConnPollService.addDiffConnPoll(object);
    }

    /**
     * 增加千寻账户
     * @param object
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/edit",method = RequestMethod.POST)
    public @ResponseBody Message edit(@RequestBody Map<String,Object> object){
        Message message = Message.parameterCheck(object);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        return diffConnPollService.editDiffConnPoll(object);
    }

    /**
     * 删除千寻账户
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/delete/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message delete(@PathVariable("id") Long id){
        return diffConnPollService.deteleDiffConnPoll(id);
    }

    /**
     * 千寻账户列表（使用情况）
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message lists(){
        return diffConnPollService.accountList();
    }

    /**
     * 心跳
     * @param objectMap
     * @return
     */
    @RequestMapping(value = "/heartBeat", method = RequestMethod.POST)
    public @ResponseBody Message heartBeat(@RequestBody  Map<String,String> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        String userName = objectMap.get("userName");
        String token = objectMap.get("token");
        if (applicationTokenService.decrypt(token)) {
            return positionService.changeDate(userName);
        }
        return new Message(Message.Type.FAIL);
    }

    /**
     * 千寻过期时间
     * @param objectMap
     * @return
     */
    @RequestMapping(value = "/setTimeout", method = RequestMethod.POST)
    public @ResponseBody Message setTimeout(@RequestBody  Map<String,String> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        String token = objectMap.get("token");
        String userName = objectMap.get("userName");
        String timeout = objectMap.get("timeout");
        if (applicationTokenService.decrypt(token)) {
            return positionService.changeTimeout(userName,timeout);
        }
        return new Message(Message.Type.FAIL);
    }

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public @ResponseBody Message init() {
        positionService.format();
        positionService.init();
        return new Message(Message.Type.OK);
    }
}
