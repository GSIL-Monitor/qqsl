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
    public void testAddDiffConnPoll(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userName","aaa");
        map.put("password","bbb");
        map.put("timeout",System.currentTimeMillis()+7*24*3600*1000l);
        boolean b = diffConnPollService.addDiffConnPoll(map);
        Assert.assertTrue(b);
        diffConnPollService.flush();
        List<DiffConnPoll> diffConnPolls = diffConnPollService.findAll();
        Assert.assertTrue(diffConnPolls.size() == 3);
        map = new LinkedHashMap<>();
        map.put("userName","aaa");
        map.put("password","bbb");
        b = diffConnPollService.addDiffConnPoll(map);
        Assert.assertTrue(!b);
    }

    @Test
    public void testEditDiffConnPoll(){
        DiffConnPoll diffConnPoll = diffConnPollService.find(1l);
        long timeout=System.currentTimeMillis() + 7 * 24 * 3600 * 1000l;
        diffConnPollService.editDiffConnPoll(diffConnPoll, timeout, 1l);
        diffConnPollService.flush();
        diffConnPoll = diffConnPollService.find(1l);
        Assert.assertTrue(diffConnPoll.getTimeout()==timeout);
    }

    @Test
    public void testAccountList(){
        JSONArray jsonArray = diffConnPollService.accountList();
        Assert.assertTrue(!jsonArray.isEmpty());
    }
}
