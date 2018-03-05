package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import net.sf.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @anthor Administrator
 * @since 14:20 2018/1/18
 */
public class DiffConnPollServiceTest extends BaseTest {
    @Autowired
    private DiffConnPollService diffConnPollService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private UserService userService;

    @Test
    public void testIsAllowConnectQXWZ(){
        Message message = diffConnPollService.isAllowConnectQXWZ(1l);
        Assert.assertTrue(message.getType() == Message.Type.FAIL);
        message = diffConnPollService.isAllowConnectQXWZ(848l);
        Assert.assertTrue(message.getType() == Message.Type.FAIL);
        Package aPackage = packageService.findByUser(userService.find(1l));
        aPackage.setType(CommonEnum.PackageType.SUNRISE);
        packageService.save(aPackage);
        packageService.flush();
        message = diffConnPollService.isAllowConnectQXWZ(848l);
        Assert.assertTrue(message.getType() == Message.Type.OK);
    }

    @Test
    public void testAddDiffConnPoll(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userName","aaa");
        map.put("password","bbb");
        map.put("timeout",System.currentTimeMillis()+7*24*3600*1000l);
        Message message = diffConnPollService.addDiffConnPoll(map);
        Assert.assertTrue(message.getType() == Message.Type.OK);
        diffConnPollService.flush();
        List<DiffConnPoll> diffConnPolls = diffConnPollService.findAll();
        Assert.assertTrue(diffConnPolls.size() == 3);
        map = new LinkedHashMap<>();
        map.put("userName","aaa");
        map.put("password","bbb");
        message = diffConnPollService.addDiffConnPoll(map);
        Assert.assertTrue(message.getType() == Message.Type.FAIL);
    }

    @Test
    public void testEditDiffConnPoll(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 1l);
        map.put("timeout", System.currentTimeMillis() + 7 * 24 * 3600 * 1000l);
        Message message = diffConnPollService.editDiffConnPoll(map);
        Assert.assertTrue(message.getType() == Message.Type.OK);
        map = new LinkedHashMap<>();
        map.put("id", 1l);
//        map.put("timeout", System.currentTimeMillis() + 7 * 24 * 3600 * 1000l);
        message = diffConnPollService.editDiffConnPoll(map);
        Assert.assertTrue(message.getType() == Message.Type.FAIL);
    }

    @Test
    public void testAccountList(){
        Message message = diffConnPollService.accountList();
        Assert.assertTrue(message.getType() == Message.Type.OK);
        JSONArray data = (JSONArray) message.getData();
        Assert.assertTrue(!data.isEmpty());
    }
}
