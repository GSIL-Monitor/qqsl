package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.CannedAccessControlList;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Oss;
import com.hysw.qqsl.cloud.core.service.OssService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
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

	@Test
	public void test1(){
		AccessControlList acl = ossService.getClient().getBucketAcl("qqsl");
		System.out.println(acl.toString());
		ossService.getClient().setBucketAcl("qqsl", CannedAccessControlList.PublicReadWrite);
		acl = ossService.getClient().getBucketAcl("qqsl");
		System.out.println(acl.toString());
	}
}
