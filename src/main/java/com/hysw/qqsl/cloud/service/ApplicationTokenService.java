package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by chenl on 17-3-29.
 */
@Service("applicationTokenService")
public class ApplicationTokenService {
    //密文缓存
    private String ciphertext = null;
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
            return false;
        }
        return true;
    }

}
