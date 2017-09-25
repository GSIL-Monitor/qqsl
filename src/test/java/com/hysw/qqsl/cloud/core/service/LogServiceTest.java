package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.service.LogService;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by leinuo on 17-1-9.
 */
public class LogServiceTest extends BaseTest {
    @Autowired
    private LogService logService;
    @Test
    public void getLogsByProject(){
        Long id = 616l;
        List<JSONObject> logs = logService.getLogJsonsByProject(id);
        Assert.assertNotNull(logs);
    }
    @Test
    public void getLogJson(){
        Long id = 616l;
        List<JSONObject> logJsons = logService.getLogJsonsByProject(id);
        Assert.assertNotNull(logJsons);
    }

}
