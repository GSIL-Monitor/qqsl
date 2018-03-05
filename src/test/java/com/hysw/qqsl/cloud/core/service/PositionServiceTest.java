package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.core.entity.element.Position;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * Created by chenl on 17-4-10.
 */
public class PositionServiceTest extends BaseTest {
    @Autowired
    private PositionService positionService;
    @Autowired
    private DiffConnPollService diffConnPollService;

    @Before
    public void before(){
        List<DiffConnPoll> diffConnPolls = diffConnPollService.findAll();
        for (DiffConnPoll diffConnPoll : diffConnPolls) {
            diffConnPoll.setTimeout(System.currentTimeMillis()+7*24*3600*1000l);
            diffConnPollService.save(diffConnPoll);
        }
    }

    @Test
    public void testInit(){
        positionService.format();
        Assert.assertTrue(positionService.getTimeout().size() == 0);
        Assert.assertTrue(positionService.getUseds().size() == 0);
        Assert.assertTrue(positionService.getUnuseds().size() == 0);
        positionService.init();
        Assert.assertTrue(positionService.getUnuseds().size() == 2);
    }

    @Test
    public void testRandomPosition(){
        Message message = positionService.randomPosition("aaa", "848");
        Object data = message.getData();
        boolean flag = false;
        String s = null;
        try {
            s = RSACoderUtil.decryptAES(data.toString(), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            flag = true;
        }
        JSONObject jsonObject = JSONObject.fromObject(s);
        Assert.assertTrue(!flag);
        Assert.assertNotNull(jsonObject);
        Object o = jsonObject.get("userName");
        message = positionService.changeDate(o.toString());
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }

    @Test
    public void testDeleteOneCache(){
        DiffConnPoll diffConnPoll = diffConnPollService.find(1l);
        positionService.deleteOneCache(diffConnPoll);
        Assert.assertTrue(positionService.getUseds().size() == 0);
        Assert.assertTrue(positionService.getUnuseds().size() == 2);
        Assert.assertTrue(positionService.getTimeout().size() == 0);
    }

    @Test
    public void testChangeTimeout(){
        DiffConnPoll diffConnPoll = diffConnPollService.find(1l);
        positionService.changeTimeout(diffConnPoll.getUserName(), "123456");
        for (Position position : positionService.getUnuseds()) {
            if (position.getUserName().equals(diffConnPoll.getUserName())) {
                Assert.assertTrue(position.getTimeout() == 123456);
            }
        }
    }

    @Test
    public void testIsAllowConnectQXWZ(){
        Message message = positionService.isAllowConnectQXWZ(848l);
        Assert.assertTrue(message.getType()== Message.Type.OK);
    }
}
