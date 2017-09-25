package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.CertifyCache;
import com.hysw.qqsl.cloud.core.service.CertifyService;
import com.hysw.qqsl.cloud.core.service.UserService;
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
    private UserService userService;
    @Autowired
    private CertifyCache certifyCache;

    /**
     * 获取实名认证信息
     * @return
     */
    @RequestMapping(value = "/getPersonalCertify", method = RequestMethod.GET)
    public @ResponseBody Message getPersonalCertify() {
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return certifyService.getPersonalCertify(user);
    }

    /**
     * 获取企业认证信息
     * @return
     */
    @RequestMapping(value = "/getCompanyCertify", method = RequestMethod.GET)
    public @ResponseBody Message getCompanyCertify() {
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return certifyService.getCompanyCertify(user);
    }

    /**
     * 身份证实名认证
     * @param objectMap
     * @return
     */
    @RequestMapping(value = "/personalCertify", method = RequestMethod.POST)
    public @ResponseBody Message personalCertify(@RequestBody Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return certifyService.personalCertify(objectMap,user);
    }


    /**
     * 企业实名认证
     * @param objectMap
     * @return
     */
    @RequestMapping(value = "/companyCertify", method = RequestMethod.POST)
    public @ResponseBody Message companyCertify(@RequestBody Map<String,Object> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return certifyService.companyCertify(objectMap,user);
    }

//    /**
//     * 认证通过
//     * @return
//     */
//    @RequestMapping(value = "/certifyPass", method = RequestMethod.POST)
//    public @ResponseBody Message certifyPass(@RequestBody Map<String,Object> objectMap) {
//        Message message = Message.parameterCheck(objectMap);
//        if (message.getType() == Message.Type.FAIL) {
//            return message;
//        }
//        User user = authentService.getUserFromSubject();
//        if (user == null) {
//            return new Message(Message.Type.EXIST);
//        }
//        return certifyService.certifyPass(objectMap);
//    }

//    /**
//     * 认证不通过
//     * @return
//     */
//    @RequestMapping(value = "/certifyNotPass", method = RequestMethod.POST)
//    public @ResponseBody Message certifyNotPass(@RequestBody Map<String,Object> objectMap) {
//        Message message = Message.parameterCheck(objectMap);
//        if (message.getType() == Message.Type.FAIL) {
//            return message;
//        }
//        User user = authentService.getUserFromSubject();
//        if (user == null) {
//            return new Message(Message.Type.EXIST);
//        }
//        return certifyService.certifyNotPass(objectMap);
//    }

//    /**
//     * 返回实名认证是否通过
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "/identity", method = RequestMethod.GET)
//    public @ResponseBody Message identity(@RequestParam Long id) {
//        Certify certify;
//        try {
//            certify = certifyService.find(id);
//        } catch (Exception e) {
//            return new Message(Message.Type.FAIL);
//        }
//        return certifyService.identity(certify);
//    }

//    /**
//     * 返回企业认证是否通过
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "/company", method = RequestMethod.GET)
//    public @ResponseBody Message company(@RequestParam Long id) {
//        Certify certify;
//        try {
//            certify = certifyService.find(id);
//        } catch (Exception e) {
//            return new Message(Message.Type.FAIL);
//        }
//        return certifyService.company(certify);
//    }

    /**
     * 获取所有认证信息
     * @return
     */
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message getAllCertifyList() {
        return certifyService.getAllCertifyList();
    }

    /**
     * 获取用户认证信息
     *
     * @return
     */
    @RequestMapping(value = "/admin/getCertify", method = RequestMethod.GET)
    public @ResponseBody
    Message getCertifyByUser(@RequestParam Long id) {
        User user;
        try {
            user = userService.find(id);
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return certifyService.getCertifyByUser(user);
    }

    /**
     * 手动调用认证流程
     * @return
     */
    @RequestMapping(value = "/certification/admin", method = RequestMethod.POST)
    public @ResponseBody
    Message certification() {
        certifyCache.certification();
        return new Message(Message.Type.OK);
    }
//
//    /**
//     * 获取某条认证信息
//     * @return
//     */
//    @RequestMapping(value = "/getCertify/admin/{id}", method = RequestMethod.GET)
//    public @ResponseBody Message getCertify(@PathVariable Long id) {
//        return certifyService.getCertify(id);
//    }

//    /**
//     * 实名认证转企业认证
//     * @param objectMap
//     * @return
//     */
//    @RequestMapping(value = "/identityTranformCompany", method = RequestMethod.POST)
//    public @ResponseBody Message identityTranformCompany(@RequestBody Map<String,Object> objectMap) {
//        Message message = Message.parameterCheck(objectMap);
//        if (message.getType() == Message.Type.FAIL) {
//            return message;
//        }
//        User user = authentService.getUserFromSubject();
//        if (user == null) {
//            return new Message(Message.Type.EXIST);
//        }
//        return certifyService.identityTranformCompany(objectMap);
//    }


//    ?身份证认证
//    ?企业认证
//    ?实名认证未通过只能使用试用版套餐，不可购买任何类型产品
//    企业认证未通过，不可购买一定等级的套餐，测站？数据服务？
//    每周一，周四，启用认证线程，先进行实名认证，再进行企业认证

    }
