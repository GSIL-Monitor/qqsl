package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
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
}
