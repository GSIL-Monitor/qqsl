package com.hysw.qqsl.cloud.wechat.util;

import com.hysw.qqsl.cloud.wechat.service.GetAccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 加密并验证来源
 * Created by chenl on 17-6-27.
 */
@Service("encryptAndVerifiyService")
public class EncryptAndVerifiy {
    @Autowired
    private GetAccessTokenService getAccessTokenService;


    /**
     * 字典排序
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param accessToken 微信请求到的token
     * @return
     */
    private ArrayList<String> lexicographicOrder(String timestamp, String nonce, String accessToken) {
        ArrayList<String> list=new ArrayList<String>();
        list.add(nonce);
        list.add(timestamp);
        list.add(accessToken);

        Collections.sort(list);
        return list;
    }

    /**
     * 对字典排序后的字符串进行sha1加密
     * @param list
     * @return
     * @throws DigestException
     */
    private String sha1(ArrayList<String> list) throws DigestException {
        //获取信息摘要 - 参数字典排序后字符串
        String decrypt = list.get(0)+list.get(1)+list.get(2);
        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decrypt.getBytes());
            //获取字节数组
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString().toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new DigestException("签名错误！");
        }
    }

    /**
     * 加密后签名与微信加密签名进行比较
     *
     * @param signature 微信签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @return
     */
    public boolean isWeiXin(String signature, String timestamp, String nonce, boolean flag) throws DigestException {
        String token;
        if (flag) {
            token = "qqsl";
        }else{
            token = getAccessTokenService.getToken();
        }
        ArrayList<String> list = lexicographicOrder(timestamp, nonce, token);
        String ciphertext = sha1(list);
        if (signature.toLowerCase().equals(ciphertext.toLowerCase())) {
            return true;
        }
        return false;
    }


}
