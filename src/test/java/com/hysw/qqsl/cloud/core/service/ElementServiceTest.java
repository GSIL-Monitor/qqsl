package com.hysw.qqsl.cloud.core.service;


import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * 9.要素保存业务测试
    要素值是否符合该要素
 * @author Administrator
 *
 */
public class ElementServiceTest extends BaseTest {

	@Autowired
	private ElementService elementService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UnitService unitService;
	@Autowired
	private UserService userService;
	private static final String CODE = "1231278ABCF98ABFE1";
	private static final String USER_NAME = "qqsl";
	@Before
	public void setUp() {
		Map<String, Object> map = new HashMap<>();
		map.put("type", Project.Type.AGRICULTURAL_IRRIGATION.ordinal());
		map.put("planning", (int) (Math.random() * 5));
		map.put("prefix", "1231278ABCF98ABFE");
		map.put("order", 1);
		map.put("name", "2016西宁灌溉工程");
		User user = userService.findByUserName(USER_NAME);
		Project project;
		if (projectService.findByCode(CODE, 1l).size() == 0) {
			try{
				project = projectService.convertMap(map,user,false);
				Message message = projectService.createProject(project);
				assertTrue(message.getType() == Message.Type.OK);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		project = projectService.findByCode(CODE, 1l).get(0);
		assertTrue(project.getId()!=null);
	}
	@Test
	public void testSaveElement(){
		Element element = new Element();
		List<Project> projects = projectService.findAll();
		element.setProject(projects.get(0));
		element.setElementDataStr("9:eee");
		element.setAlias("123445");
		element.setValue("hh");
	}
	@Test
	public void testBulidElement(){
		List<Project> projects = projectService.findByCode(CODE,1l);
		Unit unit = unitService.findUnit("11", true, projects.get(0));
		List<Element> elements = elementService.bulidExportElement(unit);
		Assert.assertTrue(elements.size()>0);
	}
}
