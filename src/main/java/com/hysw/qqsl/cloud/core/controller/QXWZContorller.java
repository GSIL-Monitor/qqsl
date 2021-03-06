package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import net.sf.json.JSONArray;
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
 * @since  2017-09-20 下午2:23
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
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private TradeService tradeService;

    /**
     * 请求千寻账号密码
     * @param token 加密口令
     * @param mac 手机mac
     * @param projectId 项目id
     * @return <br/>
     * <ol>
     *     <li>FAIL:参数验证失败</li>
     *     <li>EXIST:套餐不存在</li>
     *     <li>EXPIRED:已过期</li>
     *     <li>OK:有对应权限</li>
     *     <li>NO_ALLOW:不允许链接</li>
     * </ol>
     */
//    @IsExpire
    @RequestMapping(value = "/getAccount", method = RequestMethod.GET)
    public @ResponseBody
    Message account(@RequestParam String token, @RequestParam String mac, @RequestParam String projectId) {
        //是否可以获取千寻账号
        Message message;
        if (token.length() == 0) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (mac == null || mac.trim().length() == 0 || mac.equals("null")|| projectId == null || projectId.trim().length() == 0 || projectId.equals("null")) {
            return MessageService.message(Message.Type.FAIL);
        }
        try {
            message = isAllowConnectQXWZ(Long.valueOf(projectId));
        } catch (Exception e) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (applicationTokenService.decrypt(token)) {
            Project project = null;
            try {
                project = projectService.find(Long.valueOf(projectId));
            } catch (Exception e) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            if (project == null) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            String s = positionService.randomPosition(mac.trim(), project);
            if (s.equals("")) {
                return MessageService.message(Message.Type.QXWZ_FULL);
            }
            return MessageService.message(Message.Type.OK, s);
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 是否拥有千寻连接权限
     * 套餐是否含有千寻功能（不判断限制数）
     * @param projectId 项目id
     * @return <br/>
     * <ol>
     *     <li>FAIL:参数验证失败</li>
     *     <li>EXPIRED:已过期</li>
     *     <li>OK:有对应权限</li>
     * </ol>
     */
//    @IsExpire
    @RequestMapping(value = "/isAllowQxwz", method = RequestMethod.GET)
    public @ResponseBody
    Message isAllowQxwz(@RequestParam Long projectId) {
        //是否可以获取千寻账号
        Project project = projectService.find(projectId);
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        com.hysw.qqsl.cloud.pay.entity.data.Package aPackage = packageService.findByUser(project.getUser());
        if (aPackage == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (aPackage.getExpireDate().getTime() < System.currentTimeMillis()) {
            return MessageService.message(Message.Type.PACKAGE_EXPIRED);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.FINDCM) {
                return MessageService.message(Message.Type.OK);
            }
        }
        return MessageService.message(Message.Type.PACKAGE_LIMIT);
    }

    /**
     * 增加千寻账户
     * @param object <br/>
     *               <ol>
     *               <li>userName:千寻账号</li>
     *               <li>password:密码</li>
     *               <li>timeout:过期时间</li>
     *               </ol>
     * @return <br/>
     * <ol>
     *     <li>FAIL:参数验证失败</li>
     *     <li>OK:添加成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/create",method = RequestMethod.POST)
    public @ResponseBody Message create(@RequestBody Map<String,Object> object){
        Message message = CommonController.parameterCheck(object);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        boolean flag = diffConnPollService.addDiffConnPoll(object);
        if (flag) {
            return MessageService.message(Message.Type.OK);
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 编辑千寻账户
     * @param object <br/>
     *               <ol>
     *               <li>id:千寻实体id</li>
     *               <li>timeout:过期时间</li>
     *               </ol>
     * @return <br/>
     * <ol>
     *     <li>FAIL:参数验证失败</li>
     *     <li>OK:更新成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/edit",method = RequestMethod.POST)
    public @ResponseBody Message edit(@RequestBody Map<String,Object> object){
        Message message = CommonController.parameterCheck(object);
        if(message.getType()!= Message.Type.OK){
            return message;
        }
        Object id = object.get("id");
        Object timeout = object.get("timeout");
        if (id == null || timeout==null) {
            return MessageService.message(Message.Type.FAIL);
        }
        DiffConnPoll diffConnPoll;
        long l;
        try {
            l=Long.valueOf(timeout.toString());
            diffConnPoll = diffConnPollService.find(Long.valueOf(id.toString()));
        } catch (Exception e) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (diffConnPoll == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        diffConnPollService.editDiffConnPoll(diffConnPoll,l,id);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除千寻账户
     * @param id 千寻实体id
     * @return OK 删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/delete/{id}",method = RequestMethod.DELETE)
    public @ResponseBody Message delete(@PathVariable("id") Long id){
        diffConnPollService.deteleDiffConnPoll(id);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 千寻账户列表（使用情况）
     * @return 千寻账号列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"})
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message lists(){
        JSONArray jsonArray = diffConnPollService.accountList();
        return MessageService.message(Message.Type.OK, jsonArray);
    }

    /**
     * 心跳
     * @param objectMap <br/>
     *                  <ol>
     *                  <li>token:加密令牌</li>
     *                  <li>userName:账号</li>
     *                  </ol>
     * @return <br/> <br/>
     * <ol>
     *     <li>FAIL:参数验证失败</li>
     *     <li>OK:更新成功</li>
     * </ol>
     */
    @RequestMapping(value = "/heartBeat", method = RequestMethod.POST)
    public @ResponseBody Message heartBeat(@RequestBody  Map<String,String> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        String userName = objectMap.get("userName");
        String token = objectMap.get("token");
        if (applicationTokenService.decrypt(token)) {
            if (positionService.changeDate(userName)) {
                return MessageService.message(Message.Type.OK);
            }else{
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.FAIL);
    }

    /**
     * 千寻过期时间
     * @param objectMap <br/>
     *                  <ol>
     *                  <li>token:加密令牌</li>
     *                  <li>userName:账号</li>
     *                  <li>timeout:过期时间戳</li>
     *                  </ol>
     * @return <br/>
     * <ol>
     *     <li>FAIL:参数验证失败</li>
     *     <li>EXIST:实体对象不存在</li>
     *     <li>OK:设置成功</li>
     * </ol>
     */
    @RequestMapping(value = "/setTimeout", method = RequestMethod.POST)
    public @ResponseBody Message setTimeout(@RequestBody  Map<String,String> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        String token = objectMap.get("token");
        String userName = objectMap.get("userName");
        String timeout = objectMap.get("timeout");
        if (applicationTokenService.decrypt(token)) {
            DiffConnPoll diffConnPoll = diffConnPollService.findByUserName(userName);
            if (diffConnPoll == null) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            if (positionService.changeTimeout(diffConnPoll, timeout)) {
                return MessageService.message(Message.Type.OK);
            } else {
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.FAIL);
    }

//    /**
//     * 初始化千寻账户缓存
//     * @return OK初始化成功
//     */
//    @RequestMapping(value = "/init", method = RequestMethod.GET)
//    public @ResponseBody Message init() {
//        positionService.format();
//        positionService.init();
//        return MessageService.message(Message.Type.OK);
//    }

    /**
     * 是否允许连接千寻位置(含限制数)
     * @param projectId
     * @return
     */
    private Message isAllowConnectQXWZ(Long projectId){
        Project project = projectService.find(projectId);
        int i = positionService.findByUserInUseds(project.getUser());
        com.hysw.qqsl.cloud.pay.entity.data.Package aPackage = packageService.findByUser(project.getUser());
        if (aPackage == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (aPackage.getExpireDate().getTime() < System.currentTimeMillis()) {
            return MessageService.message(Message.Type.PACKAGE_EXPIRED);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.FINDCM && i < packageItem.getLimitNum()) {
                return MessageService.message(Message.Type.OK);
            }
        }
        return MessageService.message(Message.Type.PACKAGE_LIMIT);
    }
}
