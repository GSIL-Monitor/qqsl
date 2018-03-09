package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.CertifyCache;
import com.hysw.qqsl.cloud.core.service.CertifyService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import net.sf.json.JSONArray;
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
 * @since  2017-08-29 上午10:59
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
     * @return 实名认证对象
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getPersonalCertify", method = RequestMethod.GET)
    public @ResponseBody
    Message getPersonalCertify() {
        User user = authentService.getUserFromSubject();
        Certify certify = certifyService.findByUser(user);
        return MessageService.message(Message.Type.OK,certifyService.personalCertifyToJson(certify));
    }

    /**
     * 获取企业认证信息
     * @return 企业认证对象
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getCompanyCertify", method = RequestMethod.GET)
    public @ResponseBody Message getCompanyCertify() {
        User user = authentService.getUserFromSubject();
        Certify certify = certifyService.findByUser(user);
        return MessageService.message(Message.Type.OK,certifyService.companyCertifyToJson(certify));
    }

    /**
     * 身份证实名认证
     * @param objectMap name姓名，identityId身份证号
     * @return FAIL参数验证失败，EXIST认证已通过，不可更改，OK提交成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/personalCertify", method = RequestMethod.POST)
    public @ResponseBody Message personalCertify(@RequestBody Map<String,Object> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        Object name = objectMap.get("name");
        Object identityId = objectMap.get("identityId");
        if (name == null || identityId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Certify certify;
        try {
            certify = certifyService.findByUser(user);
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (certify == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS) {
//                认证已通过，不可更改
            return MessageService.message(Message.Type.CERTIFY_REPEAT);
        }
        certifyService.personalCertify(user,certify,name.toString(),identityId.toString());
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 企业实名认证
     * @param objectMap legal法人姓名，companyName企业名称，companyAddress企业地址，companyPhone企业电话，companyLicence社会统一编码
     * @return FAIL参数验证失败，EXIST认证已通过，不可更改，OK提交成功，NO_ALLOW个人认证未通过不能进行企业认证
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/companyCertify", method = RequestMethod.POST)
    public @ResponseBody Message companyCertify(@RequestBody Map<String,Object> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        Object legal = objectMap.get("legal");
        Object companyName = objectMap.get("companyName");
        Object companyAddress = objectMap.get("companyAddress");
        Object companyPhone = objectMap.get("companyPhone");
        Object companyLicence = objectMap.get("companyLicence");
        if (legal == null || companyName == null || companyAddress == null || companyPhone == null || companyLicence == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Certify certify;
        try {
            certify = certifyService.findByUser(user);
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (certify == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (certify.getPersonalStatus() != CommonEnum.CertifyStatus.PASS) {
//            个人认证未通过不能进行企业认证
            return MessageService.message(Message.Type.CERTIFY_NO_PERSONAL);
        }
        if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS||certifyService.findByCompanyLicence(companyLicence.toString())) {
//            认证已通过，不可更改
            return MessageService.message(Message.Type.CERTIFY_REPEAT);
        }
//        企业许可证编号
       /* if (findByCompanyLicence(companyLicence.toString())) {
            return MessageService.message(Message.Type.OTHER);
        }*/
        certifyService.companyCertify(user, certify, legal.toString(), companyName.toString(), companyAddress.toString(), companyPhone.toString(), companyLicence.toString());
        return MessageService.message(Message.Type.OK);
    }


    /**
     * 获取所有认证信息
     * @return 认证信息列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/lists", method = RequestMethod.GET)
    public @ResponseBody Message getAllCertifyList() {
        JSONArray jsonArray = certifyService.getAllCertifyList();
        return MessageService.message(Message.Type.OK, jsonArray);
    }

    /**
     * 获取用户认证信息
     * @param id 认证id
     * @return 认证对象
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
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (certify == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        return MessageService.message(Message.Type.OK, certifyService.certifyToJson(certify));
    }

    /**
     * 手动调用认证流程
     * @return OK认证完毕
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/startCertify", method = RequestMethod.POST)
    public @ResponseBody
    Message certification() {
        certifyCache.certification();
        return MessageService.message(Message.Type.OK);
    }


//    ?身份证认证
//    ?企业认证
//    ?实名认证未通过只能使用试用版套餐，不可购买任何类型产品
//    企业认证未通过，不可购买一定等级的套餐，测站？数据服务？
//    每周一，周四，启用认证线程，先进行实名认证，再进行企业认证

    }
