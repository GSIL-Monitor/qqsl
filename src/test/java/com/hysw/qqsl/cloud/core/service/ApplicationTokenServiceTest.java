package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.service.ApplicationTokenService;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ApplicationTokenServiceTest extends BaseTest {
    @Autowired
    private ApplicationTokenService applicationTokenService;

    /**
     * 测试新生成token并解析
     */
    @Test
    public void testGetToken(){
        applicationTokenService.makeToken();
        String token = applicationTokenService.getToken();
        boolean decrypt = applicationTokenService.decrypt(token);
        Assert.assertTrue(decrypt);
    }

    /**
     * 过期
     */
    @Test
    public void testTokenIsExpried(){
        String ciphertext = null;
        long l = 1234567890123l;
        try {
            ciphertext = RSACoderUtil.encryptAES(String.valueOf(l), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean decrypt = applicationTokenService.decrypt(ciphertext);
        Assert.assertTrue(!decrypt);
    }

    /**
     * 失败
     */
    @Test
    public void testError(){
        boolean decrypt = applicationTokenService.decrypt("sgaagagagasga");
        Assert.assertTrue(!decrypt);
    }

    /**
     * 解密token
     */
    @Test
    public void testDecrypt(){
        long l = System.currentTimeMillis();
        String token = null;
        try {
            token = RSACoderUtil.encryptAES(String.valueOf(l), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean decrypt = applicationTokenService.decrypt(token);
        Assert.assertTrue(decrypt);
        boolean decrypt1 = applicationTokenService.decrypt("7FE5B9C7687ACD21E6A4036AB2BCDA3F");
        Assert.assertTrue(!decrypt1);
        boolean decrypt2 = applicationTokenService.decrypt("a");
        Assert.assertTrue(!decrypt2);
        boolean decrypt3 = applicationTokenService.decrypt("");
        Assert.assertTrue(!decrypt3);
        boolean decrypt4 = applicationTokenService.decrypt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Assert.assertTrue(!decrypt4);
        boolean decrypt5= applicationTokenService.decrypt("111111111111111111111111111111111111111111111111");
        Assert.assertTrue(!decrypt5);
        boolean decrypt6 = applicationTokenService.decrypt("--------------------------------------------------");
        Assert.assertTrue(!decrypt6);
    }
}
