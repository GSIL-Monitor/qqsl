package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.ElementDB;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElementDBServiceTest extends BaseTest {

	@Autowired
	private ElementDBService elementDBService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	private List<ElementDB> listElementDBs = new ArrayList<ElementDB>();
    private static final String CODE = "1231278ABCF98ABFE1";
	private static final String USER_NAME = "qqsl";
	@Before
	public void setUp(){
		//初始化参数
		User user = userService.findByUserName(USER_NAME);
		Project project = new Project();
		project.setName("2016西宁灌溉工程");
		project.setUser(user);
		project.setPlanning((long) (int)(Math.random() * 5));
		project.setCode(CODE);
		projectService.setType(project,1);
		if(projectService.findByCode(CODE,1l).size()==0){
			JSONObject jsonObject = projectService.createProject(project);
			assertTrue(!jsonObject.isEmpty());
		}
		project = projectService.findByCode(CODE,1l).get(0);
		ElementDB elementDB0 = new ElementDB();
		elementDB0.setProject(project);
		elementDB0.setAlias("24A6");
		elementDB0.setValue("10");
		elementDB0.setElementDataStr("");
		ElementDB elementDB1 = new ElementDB();
		elementDB1.setProject(project);
		elementDB1.setAlias("23A6");
		elementDB1.setValue("10");
		elementDB1.setElementDataStr("");
		ElementDB elementDB2 = new ElementDB();
		elementDB2.setProject(project);
		elementDB2.setAlias("23A11");
		elementDB2.setValue("10");
		elementDB2.setElementDataStr("");
		ElementDB elementDB3 = new ElementDB();
		elementDB3.setProject(project);
		elementDB3.setAlias("24A11");
		elementDB3.setValue("10");
		elementDB3.setElementDataStr("");
		listElementDBs.add(elementDB0);
		listElementDBs.add(elementDB1);
		listElementDBs.add(elementDB2);
		listElementDBs.add(elementDB3);
		
	}
	@Test
	public void testSaveElement(){
		ElementDB elementDB = null;
		List<ElementDB> elementDBs = null;
		 elementDBs = elementDBService.findByProject(null, null);
		 //验证null
		 assertEquals(0, elementDBs.size());
		 elementDB = listElementDBs.get(0);
		 elementDBs = elementDBService.findByProject(elementDB.getProject().getId(), elementDB.getAlias());
		 assertEquals(0, elementDBs.size());
		 elementDB = listElementDBs.get(1);
		 saveElementDB(elementDB);
		 elementDBs = elementDBService.findByProject(elementDB.getProject().getId(), elementDB.getAlias());
		 //验证是否保存成功
		 assertEquals(elementDB.getAlias(), listElementDBs.get(1).getAlias());
		clearData();
	}
	public void saveElementDB(ElementDB elementDB){
		List<ElementDB> elementDBs = elementDBService.findByProject(elementDB.getProject().getId(), elementDB.getAlias());
		if(elementDBs.size()==0){
			elementDBService.save(elementDB);	
		}else{
			elementDBs.get(0).setValue(elementDB.getValue());
			elementDBs.get(0).setElementDataStr(elementDB.getElementDataStr());
			elementDBService.save(elementDBs.get(0));	
		}
	}
	@Test
	public void testGetCoordinate(){
		ElementDB elementDB = null;
		elementDB = listElementDBs.get(2);
		List<ElementDB> elementDBs = elementDBService.findByProject(elementDB.getProject().getId(), elementDB.getAlias());
		saveElementDB(elementDB);
		elementDBs = elementDBService.findByProject(elementDB.getProject().getId(), elementDB.getAlias());
		assertEquals(1,elementDBs.size());
		elementDB = listElementDBs.get(3);
		//保存坐标
		saveElementDB(elementDB);
		elementDBs = elementDBService.findByProject(elementDB.getProject().getId(), elementDB.getAlias());
		assertEquals(1,elementDBs.size());
		List<String> aliass = new ArrayList<String>();
		aliass.add(listElementDBs.get(2).getAlias());
		aliass.add(listElementDBs.get(3).getAlias());
		elementDBs = elementDBService.findProjectCenter(elementDB.getProject().getId(), aliass);
	    //验证坐标获取
		assertEquals(2,elementDBs.size());
		clearData();
	}
	public void clearData(){
		Project project = projectService.findByCode(CODE,1l).get(0);
		List<ElementDB> elementDBs = elementDBService.findByProject(project.getId());
		int k = elementDBs.size();
		assertTrue(elementDBs.size()>0);
		for(int i=0;i<k;i++){
			elementDBService.remove(elementDBs.get(i));
		}
		projectService.remove(project);
	}


}
