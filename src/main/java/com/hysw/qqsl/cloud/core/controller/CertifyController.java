package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.CertifyCache;
import com.hysw.qqsl.cloud.core.service.CertifyService;
import com.hysw.qqsl.cloud.core.service.UserService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 权限认证控制层
 *
 * @author chenl
 * @create 2017-08-29 上午10:59
 */
@Controller
@RequestMapping("/certify")
public class CertifyController {
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private CertifyCache certifyCache;

    /**
     * 获取实名认证信息
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getPersonalCertify", method = RequestMethod.GET)
    public @ResponseBody Message getPersonalCertify() {
        User user = authentService.getUserFromSubject();
        return certifyService.getPersonalCertify(user);
    }

    /**
     * 获取企业认证信息
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getCompanyCertify", method = RequestMethod.GET)
    public @ResponseBody Message getCompanyCertify() {
        User user = authentService.getUserFromSubject();
        return certifyService.getCompanyCertify(user);
    }

    /**
     * 身份证实名认证
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/personalCertify", method = RequestMethod.POST)
    public @ResponseBody Message personalCertify(@RequestBody Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return certifyService.personalCertify(objectMap,user);
    }


    /**
     * 企业实名认证
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/companyCertify", method = RequestMethod.POST)
    public @ResponseBody Message companyCertify(@RequestBody Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return certifyService.companyCertify(objectMap,user);
    }


    /**
     * 获取所有认证信息
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message getAllCertifyList() {
        return certifyService.getAllCertifyList();
    }

    /**
     * 获取用户认证信息
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/certify/{id}", method = RequestMethod.GET)
    public @ResponseBody
    Message getCertifyByUser(@PathVariable Long id) {
        Certify certify;
        try {
            certify = certifyService.find(id);
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if (certify == null) {
            return new Message(Message.Type.EXIST);
        }
        return new Message(Message.Type.OK, certifyService.certifyToJson(certify));
    }

    /**
     * 手动调用认证流程
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/startCertify", method = RequestMethod.POST)
    public @ResponseBody
    Message certification() {
        certifyCache.certification();
        return new Message(Message.Type.OK);
    }


//    ?身份证认证
//    ?企业认证
//    ?实名认证未通过只能使用试用版套餐，不可购买任何类型产品
//    企业认证未通过，不可购买一定等级的套餐，测站？数据服务？
//    每周一，周四，启用认证线程，先进行实名认证，再进行企业认证

    }
