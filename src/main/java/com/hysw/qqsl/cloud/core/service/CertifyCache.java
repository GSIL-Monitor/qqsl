package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Note;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 认证缓存
 *
 * @author chenl
 * @create 2017-08-30 下午1:16
 */
@Service("certifyCache")
public class CertifyCache {
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private EmailService emailService;
    @Autowired
    private NoteCache noteCache;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private UserService userService;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    /**
     * 根据身份证图片组合body信息组
     * @param certify
     */
    private String getIdentityBodys(Certify certify) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        String identity1 = certifyService.getAliImage(certify.getUser().getId(), "identity1.jpg");
        String identity2 = certifyService.getAliImage(certify.getUser().getId(), "identity2.jpg");
        if (identity1.equals("") || identity2.equals("")) {
            return "";
        }
        JSONObject face = getBody(identity1,"FACE");
        JSONObject back = getBody(identity2,"BACK");
        jsonArray.add(face);
        jsonArray.add(back);
        jsonObject.put("inputs", jsonArray);
        return jsonObject.toString().replace("FACE", "{\\\"side\\\":\\\"face\\\"}").replace("BACK","{\\\"side\\\":\\\"back\\\"}");
    }

    /**
     * 根据营业执照图片组合body信息组
     * @param certify
     */
    private String getCompanyBodys(Certify certify) {
        JSONObject image = new JSONObject();
        image.put("dataType", 50);
        String licence = certifyService.getAliImage(certify.getUser().getId(), "licence.jpg");
        if (licence.equals("")) {
            return "";
        }
        image.put("dataValue", licence);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", image);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("inputs", jsonArray);
        return jsonObject1.toString();
    }

    /**
     * 获取身份证图片上的信息
     * @param certify
     * @return
     */
    protected JSONObject getIdentity(Certify certify){
        String host = "https://dm-51.data.aliyun.com";
        String path = "/rest/160601/ocr/ocr_idcard.json";
        return changeNeedElementOfIdentityImage(httpRequestUtil.getIdMessage(host, path, getIdentityBodys(certify)));
    }

    /**
     * 获取营业执照上的信息
     * @param certify
     * @return
     */
    private JSONObject getCompany(Certify certify){
        String host = "https://dm-58.data.aliyun.com";
        String path = "/rest/160601/ocr/ocr_business_license.json";
        return changeNeedElementOfCompanyImage(httpRequestUtil.getIdMessage(host, path, getCompanyBodys(certify)));
    }

    /**
     * 获取营业执照照片必要信息
     * @param jsonObject
     * @return
     */
    private JSONObject changeNeedElementOfCompanyImage(JSONObject jsonObject){
        if (jsonObject == null) {
            return null;
        }
        JSONObject jsonObject1 = new JSONObject();
        JSONArray jsonArray= (JSONArray) jsonObject.get("outputs");
        for (Object o : jsonArray) {
            JSONObject outputs = (JSONObject) o;
            JSONObject outputValue= (JSONObject) outputs.get("outputValue");
            JSONObject dataValue = (JSONObject) outputValue.get("dataValue");
            if (dataValue.get("success") == null||dataValue.get("success").toString().equals("false")) {
                return null;
            }
            if (dataValue.get("reg_num")!=null) {
                jsonObject1.put("regNum", dataValue.get("reg_num"));
            }
            if (dataValue.get("name") != null) {
                jsonObject1.put("name", dataValue.get("name"));
            }
            if (dataValue.get("person") != null) {
                jsonObject1.put("person", dataValue.get("person"));
            }
            if (dataValue.get("valid_period") != null) {
                jsonObject1.put("validPeriod", dataValue.get("valid_period"));
            }
        }
        return jsonObject1;
    }

    /**
     * 获取身份证照片必要信息
     * @param jsonObject
     * @return
     */
    private JSONObject changeNeedElementOfIdentityImage(JSONObject jsonObject){
        if (jsonObject == null) {
            return null;
        }
        JSONObject jsonObject1 = new JSONObject();
        JSONArray jsonArray= (JSONArray) jsonObject.get("outputs");
        for (Object o : jsonArray) {
            JSONObject outputs = (JSONObject) o;
            JSONObject outputValue= (JSONObject) outputs.get("outputValue");
            JSONObject dataValue = (JSONObject) outputValue.get("dataValue");
            if (dataValue.get("success") == null||dataValue.get("success").toString().equals("false")) {
                return null;
            }
            if (dataValue.get("name")!=null) {
                jsonObject1.put("name", dataValue.get("name"));
            }
            if (dataValue.get("num") != null) {
                jsonObject1.put("num", dataValue.get("num"));
            }
            if (dataValue.get("end_date") != null) {
                jsonObject1.put("endDate", dataValue.get("end_date"));
            }
        }
        return jsonObject1;
    }

    /**
     * 组个body信息
     * @param base64
     * @param forword
     * @return
     */
     private JSONObject getBody(String base64,String forword) {
        JSONObject configure = new JSONObject();
        configure.put("dataType", 50);
        configure.put("dataValue", forword);
        JSONObject image = new JSONObject();
        image.put("dataType", 50);
        image.put("dataValue",base64);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("image", image);
        jsonObject.put("configure", String.valueOf(configure));
        return jsonObject;
    }

    /**
     * 保存身份证有限期至
     * @param certify
     * @param jsonObject
     */
    private boolean saveValidTill(Certify certify,JSONObject jsonObject){
        if (jsonObject.get("endDate") != null) {
            Date date = null;
            try {
                date = sdf.parse(jsonObject.get("endDate").toString());
            } catch (ParseException e) {
                certify.setIdentityAdvice("上传身份证反面模糊");
                return true;
            }
            certify.setValidTill(date);
            return false;
        }else{
            certify.setIdentityAdvice("上传身份证反面模糊");
            return true;
        }
    }

    /**
     * 保存营业执照有限期至
     * @param certify
     * @param jsonObject
     */
    private boolean saveValidPeriod(Certify certify,JSONObject jsonObject){
        if (jsonObject.get("validPeriod") != null) {
            Date date = null;
            try {
                date = sdf.parse(jsonObject.get("validPeriod").toString());
            } catch (ParseException e) {
                certify.setCompanyAdvice("上传营业执照模糊");
                return true;
            }
            certify.setValidPeriod(date);
            return false;
        }else{
            certify.setCompanyAdvice("上传营业执照模糊");
            return true;
        }
    }

    /**
     * 判断身份证号、姓名与身份证图像是否一致
     * @param certify
     * @return false 一致  true 不一致，notPass
     */
    boolean idAndIdentityImageIsSame(Certify certify){
        JSONObject jsonObject = getIdentity(certify);
        if (jsonObject == null||jsonObject.size()==0) {
            certify.setIdentityAdvice("上传身份证模糊或无法识别");
            return true;
        }
        if (saveValidTill(certify, jsonObject)) {
            return true;
        }
        if (certify.getName().equals(jsonObject.get("name").toString()) && certify.getIdentityId().equals(jsonObject.get("num").toString())) {
            return false;
        }else{
            certify.setIdentityAdvice("上传身份证正面模糊或者身份证与所填信息不符");
            return true;
        }
    }

    /**
     * 身份证与名字是否同一人
     * @param certify
     * @return false 一致  true 不一致，notPass
     */
    boolean nameAndIdIsSame(Certify certify){
        String host = "http://idcard.market.alicloudapi.com";
        String path = "/lianzhuo/idcard";
        Map<String, String> querys = new HashMap<>();
        querys.put("cardno", certify.getIdentityId());
        querys.put("name", certify.getName());
        JSONObject jsonObject = httpRequestUtil.nameAndIdIsSame(host, path, querys);
        if (identity(jsonObject)) {
            certify.setIdentityAdvice("姓名与身份证号不匹配");
            return true;
        }
        return false;
    }

    /**
     * 身份证号与名字是否匹配
     * @param jsonObject
     * @return
     */
    private boolean identity(JSONObject jsonObject){
        JSONObject resp1 = null;
        try {
            resp1 = (JSONObject) jsonObject.get("resp");
        } catch (Exception e) {
            logger.info("身份证解析失败");
            return true;
        }
        if (resp1 == null) {
            return true;
        }
        String code=resp1.get("code").toString();
        if (code.equals("0")) {
            return false;
        }else{
            return true;
        }
    }

    /**
     * 社会信用代码，法人，公司名称是否匹配
     * @param jsonObject
     * @return
     */
    private boolean company(JSONObject jsonObject){
        JSONObject resp1 = null;
        try {
            resp1 = (JSONObject) jsonObject.get("resp");
        } catch (Exception e) {
            logger.info("营业执照解析失败");
            return true;
        }
        if (resp1 == null) {
            return true;
        }
        String code=resp1.get("RespCode").toString();
        if (code.equals("201")) {
            return false;
        }else{
            return true;
        }
    }

    /**
     * 社会信用代码，法人，公司名称是否一致
     * @param certify
     * @return false 一致  true 不一致，notPass
     */
    private boolean companyNameAndIdIsSame(Certify certify){
        String host = "http://cverify.market.alicloudapi.com";
        String path = "/lianzhuo/cvertify";
        Map<String, String> querys = new HashMap<>();
        querys.put("code", certify.getCompanyLicence());
        querys.put("company", certify.getCompanyName());
        querys.put("legal", certify.getLegal());
        JSONObject jsonObject = httpRequestUtil.nameAndIdIsSame(host, path, querys);
        if (company(jsonObject)) {
            certify.setCompanyAdvice("社会信用代码、法人与公司名称不匹配");
            return true;
        }
        return false;
    }

    /**
     * 判断实名认证是否通过
     * @param certify
     * @return
     */
    private void passPersonalCertification(Certify certify){
        if (idAndIdentityImageIsSame(certify)) {
            certify.setPersonalStatus(CommonEnum.CertifyStatus.NOTPASS);
            certifyService.save(certify);
//            发送短信，邮件通知,站内信
            emailService.personalCertifyFail(certify);
            Note note = new Note(certify.getUser().getPhone(),"尊敬的水利云用户您好，您的实名认证由于==>"+certify.getIdentityAdvice()+"<==原因，导致认证失败，请重新进行认证。");
            noteCache.add(certify.getUser().getPhone(),note);
            userMessageService.personalCertifyFail(certify);
            return;
        }
        if (nameAndIdIsSame(certify)) {
            certify.setPersonalStatus(CommonEnum.CertifyStatus.NOTPASS);
            certifyService.save(certify);
            //            发送短信，邮件通知
            emailService.personalCertifyFail(certify);
            Note note = new Note(certify.getUser().getPhone(),"尊敬的水利云用户您好，您的实名认证由于==>"+certify.getIdentityAdvice()+"<==原因，导致认证失败，请重新进行认证。");
            noteCache.add(certify.getUser().getPhone(),note);
            userMessageService.personalCertifyFail(certify);
            return;
        }
        certify.setPersonalStatus(CommonEnum.CertifyStatus.PASS);
        certify.setIdentityAdvice(null);
        certifyService.save(certify);
        setRolesPass(certify.getUser(),"user:identify");
        //            发送短信，邮件通知
        emailService.personalCertifySuccess(certify);
        Note note = new Note(certify.getUser().getPhone(),"尊敬的水利云用户您好，您的实名认证已经通过认证，水利云将为您提供更多，更优质的服务。");
        noteCache.add(certify.getUser().getPhone(),note);
        userMessageService.personalCertifySuccess(certify);
    }

    /**
     * 认证通过,添加相应权限
     * @param user
     * @param s
     */
    private void setRolesPass(User user, String s) {
        String roles = user.getRoles();
        if(roles == null||!StringUtils.hasText(roles.toString())){
            roles="user:simple";
        }
        roles = roles + "," + s;
        user.setRoles(roles);
        userService.save(user);
    }

    /**
     * 判断企业认证是否通过
     * @param certify
     * @return
     */
    private void passCompanyCertification(Certify certify){
        if (idAndCompanyImageIsSame(certify)) {
            certify.setCompanyStatus(CommonEnum.CertifyStatus.NOTPASS);
            certifyService.save(certify);
            //            发送短信，邮件通知
            emailService.companyCertifyFail(certify);
            Note note = new Note(certify.getUser().getPhone(),"尊敬的水利云用户您好，您的企业认证由于==>"+certify.getCompanyAdvice()+"<==原因，导致认证失败，请重新进行认证。");
            noteCache.add(certify.getUser().getPhone(),note);
            userMessageService.companyCertifyFail(certify);
            return;
        }
        if (companyNameAndIdIsSame(certify)) {
            certify.setCompanyStatus(CommonEnum.CertifyStatus.NOTPASS);
            certifyService.save(certify);
            //            发送短信，邮件通知
            emailService.companyCertifyFail(certify);
            Note note = new Note(certify.getUser().getPhone(),"尊敬的水利云用户您好，您的企业认证由于==>"+certify.getCompanyAdvice()+"<==原因，导致认证失败，请重新进行认证。");
            noteCache.add(certify.getUser().getPhone(),note);
            userMessageService.companyCertifyFail(certify);
            return;
        }
        certify.setCompanyStatus(CommonEnum.CertifyStatus.PASS);
        certify.setCompanyAdvice(null);
        certifyService.save(certify);
        setRolesPass(certify.getUser(),"user:company");
        //            发送短信，邮件通知
        emailService.companyCertifySuccess(certify);
        Note note = new Note(certify.getUser().getPhone(),"尊敬的水利云用户您好，您的企业认证已经通过认证，水利云将为您提供更多企业级功能，更优质的企业级服务。");
        noteCache.add(certify.getUser().getPhone(),note);
        userMessageService.companyCertifySuccess(certify);
    }

    /**
     * 判断公司名称、法人、统一社会信用代码与营业执照图像是否一致
     * @param certify
     * @return false 一致  true 不一致，notPass
     */
    private boolean idAndCompanyImageIsSame(Certify certify){
        JSONObject jsonObject = getCompany(certify);
        if (jsonObject == null || jsonObject.size() == 0) {
            certify.setCompanyAdvice("上传营业执照模糊或无法识别");
            return true;
        }
        if (saveValidPeriod(certify, jsonObject)) {
            return true;
        }
        if (certify.getLegal().equals(jsonObject.get("person").toString()) && certify.getCompanyName().equals(jsonObject.get("name").toString())&& certify.getCompanyLicence().equals(jsonObject.get("regNum").toString())) {
            return false;
        }else{
            certify.setCompanyAdvice("上传营业执照与所填信息不符");
            return true;
        }
    }

//    判断身份证号与姓名与身份证图像是否一致
//    判断身份证号与姓名是否匹配
//    判断实名认证是否通过
//    判断企业名称，编号，名字与企业资质图像是否一致
//    判断企业名称，编号，名字是否匹配
//    判断企业认证是否通过

    /**
     * 实名认证
     */
    void identityCertification(){
        List<Certify> certifies = certifyService.findByPersonalStatus();
        for (Certify certify : certifies) {
            passPersonalCertification(certify);
        }
    }

    /**
     * 企业认证
     */
    void companyCertification(){
        List<Certify> certifies = certifyService.findByCompanyStatus();
        for (Certify certify : certifies) {
            passCompanyCertification(certify);
        }
    }

    /**
     * 认证
     */
    public void certification(){
//        实名认证
        identityCertification();
        certifyService.flush();
//        企业认证
        companyCertification();
        certifyService.flush();
//        认证完成刷新缓存
        refresh();
    }

    /**
     * 自动刷新认证缓存
     */
    private void refresh(){
        Cache cache1 = cacheManager.getCache("certifyAllCache");
        cache1.removeAll();
        certifyService.certifyCache();
    }

    public void expire(){
        List<Certify> certifies = (List<Certify>) SettingUtils.objectCopy(certifyService.findAll());
        for (Certify certify : certifies) {
            if (certify.getValidTill() == null) {
                continue;
            }
            if (certify.getValidTill().getTime() - System.currentTimeMillis() > 90 * 24 * 3600 * 1000l) {

            } else if (certify.getValidTill().getTime() - System.currentTimeMillis() <= 90 * 24 * 3600 * 1000l && certify.getValidTill().getTime() - System.currentTimeMillis() > 0) {
                certify.setPersonalStatus(CommonEnum.CertifyStatus.EXPIRING);
                String message = "尊敬的水利云用户您好，您的实名认证即将过期，为了方便您继续使用水利云功能，请您重新进行认证。";
                emailService.emailNotice(certify.getUser().getEmail(),"水利云实名认证即将过期",message);
                Note note = new Note(certify.getUser().getPhone(),message);
                noteCache.add(certify.getUser().getPhone(),note);
                userMessageService.emailNotice(certify.getUser(),message);
            } else if (certify.getValidTill().getTime() - System.currentTimeMillis() <= 0) {
                certify.setPersonalStatus(CommonEnum.CertifyStatus.EXPIRED);
                rolesExpired(certify.getUser(),"user:identify");
                String message = "尊敬的水利云用户您好，您的实名认证已过期，为了方便您继续使用水利云功能，请您重新进行认证。";
                emailService.emailNotice(certify.getUser().getEmail(),"水利云实名认证已过期",message);
                Note note = new Note(certify.getUser().getPhone(),message);
                noteCache.add(certify.getUser().getPhone(),note);
                userMessageService.emailNotice(certify.getUser(),message);
            }
            if (certify.getValidPeriod() == null) {
                continue;
            }
            if (certify.getValidPeriod().getTime() - System.currentTimeMillis() > 90 * 24 * 3600 * 1000l) {

            } else if (certify.getValidPeriod().getTime() - System.currentTimeMillis() <= 90 * 24 * 3600 * 1000l && certify.getValidPeriod().getTime() - System.currentTimeMillis() > 0) {
                certify.setCompanyStatus(CommonEnum.CertifyStatus.EXPIRING);
                String message = "尊敬的水利云用户您好，您的企业认证即将过期，为了方便您继续使用水利云功能，请您重新进行认证。";
                emailService.emailNotice(certify.getUser().getEmail(),"水利云企业认证即将过期",message);
                Note note = new Note(certify.getUser().getPhone(),message);
                noteCache.add(certify.getUser().getPhone(),note);
                userMessageService.emailNotice(certify.getUser(),message);
            } else if (certify.getValidPeriod().getTime() - System.currentTimeMillis() <= 0) {
                certify.setCompanyStatus(CommonEnum.CertifyStatus.EXPIRED);
                rolesExpired(certify.getUser(),"user:company");
                String message = "尊敬的水利云用户您好，您的企业认证已过期，为了方便您继续使用水利云功能，请您重新进行认证。";
                emailService.emailNotice(certify.getUser().getEmail(),"水利云企业认证已过期",message);
                Note note = new Note(certify.getUser().getPhone(),message);
                noteCache.add(certify.getUser().getPhone(),note);
                userMessageService.emailNotice(certify.getUser(),message);
            }
            certifyService.save(certify);
        }
        refresh();
    }

    /**
     * 认证过期删除相应权限
     * @param user
     * @param s
     */
    private void rolesExpired(User user,String s){
        String roles = user.getRoles();
        String[] split = roles.split(",");
        String roles1 = "";
        for (int i = 0; i < split.length; i++) {
            if (!split[i].equals(s)) {
                roles1 = roles1 + split[i];
            }
        }
        user.setRoles(roles);
        userService.save(user);
    }

}
