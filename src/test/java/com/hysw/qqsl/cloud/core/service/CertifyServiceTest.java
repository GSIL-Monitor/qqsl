package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.HttpUtil;
import com.aliyun.oss.model.OSSObject;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.HttpRequestUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证测试
 *
 * @author chenl
 * @create 2017-08-30 下午2:36
 */
@Ignore
public class CertifyServiceTest extends BaseTest{
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private CertifyCache certifyCache;
    @Autowired
    private UserService userService;
    @Autowired
    private OssService ossService;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Test
    public void testGetImage(){
        Certify certify = new Certify();
        User user = new User();
        user.setId(17l);
        certify.setUser(user);
        JSONObject identity = certifyCache.getIdentity(certify);
        System.out.println(identity.toString());

    }

    /**
     * 认证全通过
     * @throws IOException
     */
    @Test
    public void testIdAndCompanyImageIsSame() throws IOException {
        Certify certify = new Certify();
        certify.setName("熊生伟");
        certify.setIdentityId("610403198410050010");
        certify.setLegal("熊生伟");
        certify.setCompanyName("青海鸿源水务建设有限公司");
        certify.setCompanyLicence("91632900679177522R");
        certify.setCompanyPhone("13000000000");
        certify.setCompanyAddress("西宁市");
        certify.setPersonalStatus(CommonEnum.CertifyStatus.AUTHEN);
        certify.setCompanyStatus(CommonEnum.CertifyStatus.AUTHEN);
        User user = userService.find(17l);
        certify.setUser(user);
        certifyService.save(certify);
        certifyCache.certification();
        Assert.assertTrue(certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS && certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS);

    }

    /**
     * 个人认证失败，企业不认证
     * @throws IOException
     */
    @Test
    public void testIdAndCompanyImageIsSame1() throws IOException {
        Certify certify = new Certify();
        certify.setName("熊生伟1");
        certify.setIdentityId("610403198410050010");
        certify.setLegal("熊生伟");
        certify.setCompanyName("青海鸿源水务建设有限公司");
        certify.setCompanyLicence("91632900679177522R");
        certify.setCompanyPhone("13000000000");
        certify.setCompanyAddress("西宁市");
        certify.setPersonalStatus(CommonEnum.CertifyStatus.AUTHEN);
        certify.setCompanyStatus(CommonEnum.CertifyStatus.AUTHEN);
        User user = userService.find(17l);
        certify.setUser(user);
        certifyService.save(certify);
        certifyCache.certification();
        Assert.assertTrue(certify.getPersonalStatus() == CommonEnum.CertifyStatus.NOTPASS && certify.getCompanyStatus() == CommonEnum.CertifyStatus.AUTHEN);
    }

    /**
     * 个人认证通过，企业认证失败
     * @throws IOException
     */
    @Test
    public void testIdAndCompanyImageIsSame2() throws IOException {
        Certify certify = new Certify();
        certify.setName("熊生伟");
        certify.setIdentityId("610403198410050010");
        certify.setLegal("熊生伟1");
        certify.setCompanyName("青海鸿源水务建设有限公司");
        certify.setCompanyLicence("91632900679177522R");
        certify.setCompanyPhone("13000000000");
        certify.setCompanyAddress("西宁市");
        certify.setPersonalStatus(CommonEnum.CertifyStatus.AUTHEN);
        certify.setCompanyStatus(CommonEnum.CertifyStatus.AUTHEN);
        User user = userService.find(17l);
        certify.setUser(user);
        certifyService.save(certify);
        certifyCache.certification();
        Assert.assertTrue(certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS && certify.getCompanyStatus() == CommonEnum.CertifyStatus.NOTPASS);
    }

    /**
     * 个人认证通过，企业认证通过（非同一人）
     * @throws IOException
     */
    @Test
    public void testIdAndCompanyImageIsSame3() throws IOException {
        Certify certify = new Certify();
        certify.setName("陈雷");
        certify.setIdentityId("411024198908237011");
        certify.setLegal("熊生伟");
        certify.setCompanyName("青海鸿源水务建设有限公司");
        certify.setCompanyLicence("91632900679177522R");
        certify.setCompanyPhone("13000000000");
        certify.setCompanyAddress("西宁市");
        certify.setPersonalStatus(CommonEnum.CertifyStatus.AUTHEN);
        certify.setCompanyStatus(CommonEnum.CertifyStatus.AUTHEN);
        User user = userService.find(16l);
        certify.setUser(user);
        certifyService.save(certify);
        certifyCache.certification();
        Assert.assertTrue(certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS && certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS);
    }

    @Test
    public void testCertify(){
        Certify certify = certifyService.find(27l);
        certifyCache.passPersonalCertification(certify);;
        System.out.println();
    }
}
