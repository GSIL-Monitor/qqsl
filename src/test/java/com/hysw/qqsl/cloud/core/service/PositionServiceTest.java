package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.service.ApplicationTokenService;
import com.hysw.qqsl.cloud.core.service.DiffConnPollService;
import com.hysw.qqsl.cloud.core.service.FieldService;
import com.hysw.qqsl.cloud.core.service.PositionService;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by chenl on 17-4-10.
 */
public class PositionServiceTest extends BaseTest {
    @Autowired
    private PositionService positionService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private DiffConnPollService diffConnPollService;
    @Autowired
    private ApplicationTokenService applicationTokenService;
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

//    @Test
//    public void testRandomPosition(){
//        Assert.assertTrue(positionService.unuseds.size() == 101);
//        positionService.accountTimeout();
//        Assert.assertTrue(positionService.unuseds.size() == 100);
//        Message message = positionService.randomPosition(mac);
//        Assert.assertTrue(message.getType()== Message.Type.OK);
//        String s = null;
//        try {
//            s = RSACoderUtil.decryptAES(message.getData().toString(), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Assert.assertTrue(positionService.unuseds.size() == 99);
//        Assert.assertTrue(positionService.useds.size() == 1);
//        Assert.assertTrue(JSONObject.fromObject(s).get("userName").toString().equals(positionService.useds.get(0).getUserName()));
//        DiffConnPoll diffConnPoll = new DiffConnPoll();
//        diffConnPoll.setUserName("zhangyong200");
//        diffConnPoll.setTimeout(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000l);
//        positionService.editPosition(diffConnPoll);
//        Assert.assertTrue(positionService.unuseds.size() == 99);
//        Assert.assertTrue(positionService.useds.size() == 1);
//        Assert.assertTrue(positionService.timeout.size() == 1);
//        positionService.accountTimeout();
//        Assert.assertTrue(positionService.unuseds.size() == 100);
//        Assert.assertTrue(positionService.useds.size() == 1);
//        Assert.assertTrue(positionService.timeout.size() == 0);
//    }

//    @Test
//    public void testChangeTimeout(){
//        String timeout = String.valueOf(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000l);
//        String userName = "zhangyong201";
//        Message message = positionService.changeTimeout(userName, timeout);
//        Assert.assertTrue(message.getType()==Message.Type.EXIST);
//        String timeout1 = String.valueOf(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000l)+"s";
//        String userName1 = "zhangyong201";
//        Message message1 = positionService.changeTimeout(userName1, timeout1);
//        Assert.assertTrue(message1.getType()==Message.Type.FAIL);
//
//    }

//    @Test
//    public void testWriteAccount(){
//        DiffConnPoll diffConnPoll = diffConnPollService.findByUserName("xnqqsl");
//        if (diffConnPoll == null) {
//            diffConnPoll = new DiffConnPoll("xnqqsl","qhhysw",System.currentTimeMillis()+15*24*60*60*1000l);
//        }
//        diffConnPollService.save(diffConnPoll);
//
//    }

    @Test
    public void testInit(){
        positionService.format();
        positionService.init();
    }
}
