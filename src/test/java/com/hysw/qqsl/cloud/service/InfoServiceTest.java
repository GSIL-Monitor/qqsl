package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.element.Info;
import com.hysw.qqsl.cloud.entity.data.Project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InfoServiceTest extends BaseTest {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private InfoService infoService;
	@Autowired
	private ProjectService projectService;

	private final String infoXML = "info.xml";
	private final String infoReNameXML = "infoErrorOfName.xml";
	@Test
	public void testInfoXML(){
		String select13 = "项目建议书,可研,初设(实施),施工阶段,验收阶段,运营阶段";
		String select12 = "国有,私有,混合";
		String select11 = "十一五前,十一五规划,十二五规划,十三五规划,其他";
		String select10 = "I等大-1型,II等大-2型,III等中型,IV等小-1型,V等小-2型";
		String select9= "100万以下,100万—500万,500万—1000万,1000万—5000万,5000万以上";
		String select8 = "新建,维修";
		String select7 ="人畜饮水工程,灌溉工程,防洪减灾工程,水土保持工程,农村小水电工程,供水保障工程";
		List<String> info13 = Arrays.asList(select13.split(","));
		List<String> info12 = Arrays.asList(select12.split(","));
		List<String> info11 = Arrays.asList(select11.split(","));
		List<String> info10 = Arrays.asList(select10.split(","));
		List<String> info9 = Arrays.asList(select9.split(","));
		List<String> info8 = Arrays.asList(select8.split(","));
		List<String> info7 = Arrays.asList(select7.split(","));
		List<Info> infos = infoService.makeInfos(infoXML);
		logger.info(infos.size());
		assertEquals(15, infos.size());
		assertEquals(infos.get(13).getSelectValues(),info13);
		assertEquals(infos.get(12).getSelectValues(),info12);
		assertEquals(infos.get(11).getSelectValues(),info11);
		assertEquals(infos.get(10).getSelectValues(),info10);
		assertEquals(infos.get(9).getSelectValues(),info9);
		assertEquals(infos.get(8).getSelectValues(),info8);
		assertEquals(infos.get(7).getSelectValues(),info7);
	}

	 @Test
	 public void testSaveInfo(){
		 Project project = projectService.findAll().get(0);
		 infoService.saveInfo(project, 9, "530");
		 assertTrue(project.getInfoStr().contains( "500万—1000万"));
	 }
}
   