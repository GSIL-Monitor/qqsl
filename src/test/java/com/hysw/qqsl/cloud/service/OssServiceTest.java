package com.hysw.qqsl.cloud.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.aliyun.oss.model.LifecycleRule.RuleStatus;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.entity.data.Oss;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class OssServiceTest extends BaseTest {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private OssService ossService;

	@Test
	public void testSave() throws Exception{
	 Long userId = 1l;
	 Long projectId = 1l;
     Oss oss;
     String treePath = "project/15bd99f835f96cfaa50b3c985b6e494d/"+"2/23/231/231A/";
     for(int i=0;i<100;i++){
     	oss = new Oss(treePath+(int)(Math.random()*100),userId,projectId);
     	ossService.save(oss);
	 }
	 List<Oss> ossList = ossService.findAll();
	 Assert.assertNotNull(ossList);
	}
	
}
