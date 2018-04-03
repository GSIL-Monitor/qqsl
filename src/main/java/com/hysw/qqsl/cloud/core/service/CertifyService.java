package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.dao.CertifyDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 权限认证服务类
 *
 * @author chenl
 * @create 2017-08-29 上午10:56
 */
@Service("certifyService")
public class CertifyService extends BaseService<Certify, Long> {
    @Autowired
    private CertifyDao certifyDao;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private OssService ossService;
    @Autowired
    private UserService userService;

    @Autowired
    public void setBaseDao(CertifyDao certifyDao) {
        super.setBaseDao(certifyDao);
    }


    /**
     * 身份证实名认证
     *
     * @param user
     * @param certify
     * @param name
     * @param identityId
     * @return
     */
    public void personalCertify(User user, Certify certify, String name, String identityId) {
        certify.setName(name);
        certify.setIdentityId(identityId);
        certify.setIdentityAdvice(null);
        certify.setPersonalStatus(CommonEnum.CertifyStatus.AUTHEN);
        save(certify);
        user.setPersonalStatus(CommonEnum.CertifyStatus.AUTHEN);
        userService.save(user);
    }

    /**
     * 身份认证信息返回json
     * @param certify
     * @return
     */
    public JSONObject personalCertifyToJson(Certify certify) {
        JSONObject jsonObject = new JSONObject();
        if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.UNAUTHEN) {
            jsonObject.put("personalStatus", certify.getPersonalStatus());
            return jsonObject;
        }
        jsonObject.put("name", certify.getName());
        jsonObject.put("identityId", certify.getIdentityId());
        jsonObject.put("personalStatus", certify.getPersonalStatus());
        if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.NOTPASS) {
            jsonObject.put("identityAdvice", certify.getIdentityAdvice());
        }
        return jsonObject;
    }

    /**
     * 根据用户查询认证信息
     * @param user
     * @return
     */
    public Certify findByUser(User user) {
        List<Certify> certifies = findAll();
        for (Certify certify : certifies) {
            if (certify.getUser().getId().equals(user.getId())) {
                return (Certify) SettingUtils.objectCopy(certify);
            }
        }
        return null;
    }

    /**
     * 企业实名认证
     * @param user
     * @param certify
     * @param legal
     * @param companyName
     * @param companyAddress
     * @param companyPhone
     * @param companyLicence
     */
    public void companyCertify(User user, Certify certify, String legal, String companyName, String companyAddress, String companyPhone, String companyLicence) {
        certify.setLegal(legal);
        certify.setCompanyName(companyName);
        certify.setCompanyAddress(companyAddress);
        certify.setCompanyPhone(companyPhone);
        certify.setCompanyLicence(companyLicence);
        certify.setCompanyAdvice(null);
        certify.setCompanyStatus(CommonEnum.CertifyStatus.AUTHEN);
        save(certify);
        user.setCompanyStatus(CommonEnum.CertifyStatus.AUTHEN);
        userService.save(user);
    }

    /**
     * 查询企业编号是否唯一
     * @param licence
     * @return false唯一    true不唯一
     */
    public boolean findByCompanyLicence(String licence){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("companyLicence", licence));
        List<Certify> list = certifyDao.findList(0, null, filters);
        if (list == null || list.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 企业认证信息返回JSON
     * @param certify
     * @return
     */
    public JSONObject companyCertifyToJson(Certify certify) {
        JSONObject jsonObject = new JSONObject();
        if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.UNAUTHEN) {
            jsonObject.put("companyStatus", certify.getCompanyStatus());
            return jsonObject;
        }
        jsonObject.put("legal", certify.getLegal());
        jsonObject.put("companyName",certify.getCompanyName());
        jsonObject.put("companyAddress", certify.getCompanyAddress());
        jsonObject.put("companyPhone", certify.getCompanyPhone());
        jsonObject.put("companyLicence",certify.getCompanyLicence() );
        jsonObject.put("companyStatus", certify.getCompanyStatus());
        if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.NOTPASS) {
            jsonObject.put("companyAdvice", certify.getCompanyAdvice());
        }
        return jsonObject;
    }

    /**
     * 查询认证中的所有个人认证
     * @return
     */
    public List<Certify> findByPersonalStatus() {
        List<Certify> certifies = findAll();
        List<Certify> certifies1 = new ArrayList<>();
        for (Certify certify : certifies) {
            if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.AUTHEN) {
                certifies1.add((Certify) SettingUtils.objectCopy(certify));
            }
        }
        return certifies1;
    }

    /**
     * 查询认证中的所有企业认证
     * @return
     */
    public List<Certify> findByCompanyStatus() {
        List<Certify> certifies = findAll();
        List<Certify> certifies1 = new ArrayList<>();
        for (Certify certify : certifies) {
            if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS && certify.getCompanyStatus() == CommonEnum.CertifyStatus.AUTHEN) {
                certifies1.add((Certify) SettingUtils.objectCopy(certify));
            }
        }
        return certifies1;
    }

    /**
     * 添加certify组缓存
     */
    public void certifyCache() {
        List<Certify> certifies = certifyDao.findList(0, null, null);
        Cache cache = cacheManager.getCache("certifyAllCache");
        net.sf.ehcache.Element element = new net.sf.ehcache.Element("certify", certifies);
        cache.put(element);
    }

    @Override
    public List<Certify> findAll() {
        Cache cache = cacheManager.getCache("certifyAllCache");
        net.sf.ehcache.Element element = cache.get("certify");
        List<Certify> certifies=(List<Certify>) element.getValue();
        return certifies;
    }

    /**
     * 根据阿里云固定地址获取认证照片的base64
     * @param id
     * @param image
     * @return
     */
    public String getAliImage(Long id,String image) {
        InputStream is = null;
        String face = "";
        try {
            is = ossService.downloadFile("qqsl", "user/" + id + "/" + image);
        } catch (OSSException e) {
            return face;
        }
        ByteArrayOutputStream outputStream = null;
        try {
            byte[] buff = new byte[1024];
            outputStream = new ByteArrayOutputStream();
            int size = 0;
            while ((size = is.read(buff)) != -1) {
                outputStream.write(buff, 0, size);
            }
//            FileOutputStream fileOutputStream = new FileOutputStream("haha2");
//            fileOutputStream.write(outputStream.toByteArray());
            Base64 base64 = new Base64();
            face = base64.encodeAsString(outputStream.toByteArray());
        } catch (IOException e) {
            return face;
        }finally {
            IOUtils.safeClose(outputStream);
            IOUtils.safeClose(is);
        }
        return face;
    }


    /**
     * 获取认证信息列表
     * @return
     */
    public JSONArray getAllCertifyList() {
        List<Certify> certifies = findAll();
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Certify certify : certifies) {
            jsonObject = new JSONObject();
            jsonObject.put("id", certify.getId());
            jsonObject.put("phone", certify.getUser().getPhone());
            jsonObject.put("personalStatus", certify.getPersonalStatus());
            jsonObject.put("companyStatus", certify.getCompanyStatus());
            jsonObject.put("createDate", certify.getCreateDate().getTime());
            jsonObject.put("modifyDate", certify.getModifyDate().getTime());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * certifyToJson
     * @param certify
     * @return
     */
    public JSONObject certifyToJson(Certify certify) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", certify.getId());
        if (certify.getName() != null) {
            jsonObject.put("name", certify.getName());
        }
        if (certify.getIdentityId() != null) {
            jsonObject.put("identityId", certify.getIdentityId());
        }
        if (certify.getIdentityAdvice() != null) {
            jsonObject.put("identityAdvice", certify.getIdentityAdvice());
        }
        if (certify.getValidTill() != null) {
            jsonObject.put("validTill", certify.getValidTill());
        }
        jsonObject.put("personalStatus", certify.getPersonalStatus());
        if (certify.getLegal() != null) {
            jsonObject.put("legal", certify.getLegal());
        }
        if (certify.getCompanyName() != null) {
            jsonObject.put("companyName", certify.getCompanyName());
        }
        if (certify.getCompanyLicence() != null) {
            jsonObject.put("companyLience", certify.getCompanyLicence());
        }
        if (certify.getCompanyAdvice() != null) {
            jsonObject.put("companyAdvice", certify.getCompanyAdvice());
        }
        jsonObject.put("companyStatus", certify.getCompanyStatus());
        if (certify.getCompanyPhone() != null) {
            jsonObject.put("companyPhone", certify.getCompanyPhone());
        }
        if (certify.getCompanyAddress() != null) {
            jsonObject.put("companyAddress", certify.getCompanyAddress());
        }
        if (certify.getValidPeriod() != null) {
            jsonObject.put("validPeriod", certify.getValidPeriod());
        }
        return jsonObject;
    }

}
