package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by chenl on 17-3-29.
 */
@Service("applicationTokenService")
public class ApplicationTokenService {
    @Autowired
    private UserService userService;
    @Autowired
    private SensorService sensorService;
    //密文缓存
    private String ciphertext = null;
    //效验token
    private Map<String, Long> intendedEffectToken = new HashedMap();
    //生成随机密码
    public void makeToken(){
        long l = System.currentTimeMillis();
        try {
            ciphertext = RSACoderUtil.encryptAES(String.valueOf(l), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //获取token
    public String getToken(){
        if (ciphertext == null) {
            makeToken();
        }
        return this.ciphertext;
    }
    /**
     * 解密token
     * @param token
     * @return
     */
    public boolean decrypt(String token) {
        if(!StringUtils.hasText(token)){
            return false;
        }
        String s = null;
        try {
            s = RSACoderUtil.decryptAES(token, CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (s == null||s.length()==0) {
            return false;
        }
        try {
            if (Math.abs(Long.valueOf(s) - System.currentTimeMillis()) > 10 * 60 * 1000l) {
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 生成效验token
     * @return
     * @param user
     */
    public JSONObject makeIntendedEffectToken(User user) {
        JSONObject jsonObject = new JSONObject(), jsonObject1 = new JSONObject();
        jsonObject.put("id", user.getId());
        jsonObject.put("phone", user.getPhone());
        jsonObject.put("password", user.getPassword());
        jsonObject.put("slat", "hyswqqsl");
        String s1 = UUID.randomUUID().toString().replaceAll("-","");
        jsonObject.put("uuid", s1);
        String s = DigestUtils.md5Hex(jsonObject.toString());
        intendedEffectToken.put(s,System.currentTimeMillis());
        jsonObject1.put("token", s);
        jsonObject1.put("noticeStr", s1);
        return jsonObject1;
    }

    /**
     * 效验token
     *
     * @param token
     * @return
     */
    public boolean validateIntendedEffectToken(String token, String noticeStr, String code) {
        Sensor sensor = sensorService.findByCode(code);
        if (sensor == null) {
            return false;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", sensor.getStation().getUser().getId());
        jsonObject.put("phone", sensor.getStation().getUser().getPhone());
        jsonObject.put("password", sensor.getStation().getUser().getPassword());
        jsonObject.put("slat", "hyswqqsl");
        jsonObject.put("uuid", noticeStr);
        if (!DigestUtils.md5Hex(jsonObject.toString()).equals(token)) {
            intendedEffectToken.remove(token);
            return false;
        }
        if (intendedEffectToken.containsKey(token)) {
            intendedEffectToken.remove(token);
            return true;
        }
        return false;
    }

    public void expiredIntendedEffectToken(){
        Iterator<Map.Entry<String, Long>> iterator = intendedEffectToken.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, Long> next = iterator.next();
            String key = next.getKey();
            Long value = next.getValue();
            if (System.currentTimeMillis() - value > 5 * 60 * 1000) {
                iterator.remove();
            }
        }
    }

}
