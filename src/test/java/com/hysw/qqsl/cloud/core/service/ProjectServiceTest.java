package com.hysw.qqsl.cloud.core.service;

import static org.junit.Assert.*;

import java.util.*;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonTest;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.element.*;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.core.entity.data.ElementDB;
import com.hysw.qqsl.cloud.core.entity.data.ElementDataGroup;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import org.springframework.util.StringUtils;


public class ProjectServiceTest extends BaseTest {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	@Autowired
	private ElementService elementService;
	@Autowired
	private UnitService unitService;
	@Autowired
	private ElementDataGroupService elementDataGroupService;
	@Autowired
	private ElementDBService elementDBService;
	private static final String[] contactUnitAliass={"1","11","12","13","14","15","2","3","4"};
	@BeforeClass
	public static void testStart(){
		System.out.println("projectService1Test start");
	}
	@Before
	public void setUp(){
		User user = userService.findByUserName(CommonTest.USER_NAME);
		List<Project> projects = projectService.findByCode(CommonTest.PROJECT_CODE,user.getId());
		if(projects.size()==0){
			long planningId =2;
			User user1 = userService.findByUserName(CommonTest.USER_NAME);
			Project project = new Project();
			project.setUser(user1);
			project.setPlanning(planningId);
			project.setCode(CommonTest.PROJECT_CODE);
			project.setName(CommonTest.PROJECT_NAME);
			projectService.setType(project,1);
			try {
				projectService.createProject(project);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 测试数据清除
	 * @throws Exception
     */
//	@After
//	public void tearDown() throws Exception {
//		List<Project> projects = projectService.findAll();
//		if(projects.size()>0){
//			projectService.removeAll(projects);
//		}
//		System.out.println("over");
//	}

	@AfterClass
	public static void testOver(){
		System.out.println("projectService1Test over!");
	}
	/**
	 * 测试项目缓存刷新
	 */
	@Test
	public void testRefreshProjectCache(){
//       List<Project> projects = projectCache.findAll(null);
//		assertTrue(projects.size()>0);
//		projectCache.refreshProjects();
//		projects = projectCache.getProjects();
//		assertEquals(projects.size(),0);
		/*projectService.refreshCache();
		//复合要素缓存刷新验证
		List<ElementGroup> elementGroups = elementGroupService.testGetAgrElementGroups();
		assertEquals(elementGroups,null);
		//单元缓存刷新验证
		List<Unit> units1 = unitService.getAgr();
		assertEquals(units1,null);
		List<Unit> units2 = unitService.getAgrModel();
		assertEquals(units2,null);
		//要素数据缓存刷新
		List<ElementDataGroup> elementDataGroups = elementDataGroupService.testGetElementDataGroups();
		assertEquals(elementDataGroups,null);
		elementDataGroups = elementDataGroupService.testGetSimpleElementDataGroups();
		assertEquals(elementDataGroups,null);
		List<Info> infos = infoService.getTestInfos();
		assertEquals(infos.size(),0);
		List<Project> projects1 = projectCache.findAll(null);
		assertTrue(projects1.size()==projects.size());*/
	}

	/**
	 * 新建项目业务测试
	 */
	@Test
	public void testCreate(){
		//取得所属用户
		User user = userService.findByUserName(CommonTest.USER_NAME);
		assertNotNull(user.getId());
		Map<String,Object> map = new HashMap<>();
		map.put("type",1);
		map.put("planning",2);
		map.put("prefix",user.getUserName());
		map.put("order",1);
		map.put("name","2016人饮");
		Project project = null;
		Message message = null;
		try{
			project = projectService.convertMap(map,user,false);
			message = projectService.createProject(project);
		}catch (Exception e){
           e.printStackTrace();
		}
		assertTrue(message.getType()== Message.Type.OK);
		//id重复保存失败
		message = projectService.createProject(project);
		assertTrue(message.getType()== Message.Type.EXIST);
		User user1 = userService.findByUserName(CommonTest.USER_NAME);
		//判断是否更新项目前缀和序号
		assertTrue(JSONObject.fromObject(user1.getPrefixOrderStr()).get("order").toString().equals("1"));
		//查看项目是否保存成功
		List<Project> projects1 = projectService.findByCode(user.getUserName()+1,user1.getId());
		assertTrue(projects1.size()==1);
		assertTrue(projects1.get(0).getCode().equals(user.getUserName()+1));
	}
	/**
	 * 测试保存项目成功
	 */
	@Test
	public void testSaveProject() {
		String newCode = "1234657456743451";
		List<Project> projects = projectService.findByCode(newCode,1l);
		User user = userService.findByUserName(CommonTest.USER_NAME);
		long planningId = 2;
		Message me = null;
		Project project = new Project();
		project.setPlanning(planningId);
		project.setUser(user);
		project.setCode(newCode);
		project.setName("测试工程");
		projectService.setType(project, 0);
		if (projects.size() == 0) {
			try {
				me = projectService.createProject(project);
			} catch (Exception e) {
				logger.info(e.getMessage());
				return;
			}
		}
		Assert.assertTrue(me.getType()==Message.Type.OK);
	}

    /**
	 * 测试修改项目
	 */
	@Test
	public void testUpdate(){
		create();
		User user = userService.findByUserName(CommonTest.USER_NAME);
		List<Project> projects = projectService.findByCode("qqsl2",user.getId());
		assertTrue(projects.size()>0);
        Map<String,Object> map = new HashMap<>();
		map.put("type",projects.get(0).getType().ordinal());
		map.put("planningId",4);
		map.put("name","2016人饮1");
		map.put("code","qqsl12");
		map.put("id",projects.get(0).getId());
		try {
		Project project = projectService.convertMap(map,user,true);
		Message  message = 	projectService.updateProject(project);
			assertTrue(message.getType() == Message.Type.OK);
		}catch (Exception e){
			return;
		}
		Project project1 = projectService.find(projects.get(0).getId());
		assertTrue(project1.getCode().equals("qqsl12"));
		assertTrue(project1.getPlanning()==4);
	}

	@Test
    public void create(){
		User user = userService.findByUserName(CommonTest.USER_NAME);
		assertNotNull(user.getId());
		Map<String,Object> map = new HashMap<>();
		map.put("type",2);
		map.put("planning",2);
		map.put("prefix",user.getUserName());
		map.put("order",2);
		map.put("name","2016人饮11");
		Project project;
		Message message = null;
		try{
			project = projectService.convertMap(map,user,false);
			message = projectService.createProject(project);
		}catch (Exception e){
			e.printStackTrace();
		}
		assertTrue(message.getType()== Message.Type.OK);
	}

	/**
	 *项目列表获取测试
	 */
	@Test
	public void testGetProjects(){
		//数据准备
		List<String> codes = dataPrepareOfTestGetProjects();
		User user = userService.findByUserName("qqslTest");
		Message message = projectService.getProjects(0,user);
		assertTrue(message.getType()== Message.Type.OK);
		assertTrue(message.getData()!=null);
		List<JSONObject> projectJsons = JSONArray.fromObject(message.getData());
		assertTrue(projectJsons.size()==codes.size());
		assertEquals(projectJsons.get(0).get("code").toString(),codes.get(0));
		List<JSONArray> infoJsons = JSONArray.fromObject(projectJsons.get(0).get("infoStr"));
		assertTrue(infoJsons.size()>0);
		for(int i = 0;i<infoJsons.size();i++){
			assertTrue(infoJsons.get(i)!=null);
		}
		List<Project> projects;
		for(int k=0;k<codes.size();k++){
		   projects = projectService.findByCode(codes.get(k),user.getId());
			assertTrue(projects.size()>0);
			projectService.remove(projects.get(0));
//			projectCache.remove(projects.get(0));
		}
		userService.remove(user);
	}

	public List<String> dataPrepareOfTestGetProjects(){
		//数据准备
		List<String> codes = new ArrayList<>();
		User user = userService.findByUserName("qqslTest");
		if(user==null){
			user = new User();
			user.setName("liujianbin");
			user.setUserName("qqslTest");
			user.setPhone("18661925010");
//			user.setType(User.Type.USER);
			user.setPassword(DigestUtils.md5("111111").toString());
			userService.save(user);
		}
		assertNotNull(user.getId());
		Map<String,Object> map;
		Message message = null;
		for(int i=0;i<20;i++){
			map = new HashMap<>();
			map.put("type",(int) (Math.random() * 6));
			map.put("planning",(int) (Math.random() * 5));
			map.put("prefix",user.getUserName()+"hysw");
			map.put("order",i);
			map.put("name","2016人饮西宁工程"+i);
			codes.add(user.getUserName()+"hysw"+i);
			Project project;
			try{
				project = projectService.convertMap(map,user,false);
				message = projectService.createProject(project);
			}catch (Exception e){
				e.printStackTrace();
			}
			assertEquals(message.getType(),Message.Type.OK);
		}
		assertTrue(codes.size()==20);
		return codes;
	}

	/**
	 * 测试构建单个项目(灌溉)
	 */
	@Test
	public void testGetProject(){
		//数据准备
		dataPrepareOfTestGetProject(0);
		//构建简介，基本信息，中心点坐标，坐标数据，要素组数据
		User user = userService.findByUserName("qqsl");
		Project project = projectService.findByCode("projectJsonTest1",user.getId()).get(0);
		JSONObject projectJson = projectService.makeProjectJson(project,true);
        assertNotNull(projectJson);
	}

	/**
	 * 保存项目下所有要素，要素数据的值
	 */
	public void dataPrepareOfTestGetProject(int type){
		//数据准备要刷新缓存，不能只删除数据库数据，否则不一致，测试无法通过
//		projectService.refreshProjects();
		User user = userService.findByUserName("qqsl");
		//新建测试项目
		Map<String,Object> map  = new HashMap<>();;
			map.put("type",type);
			map.put("planning",(int) (Math.random() * 5));
			map.put("prefix","projectJsonTest");
			map.put("order",1);
			map.put("name","2016西宁灌溉工程");
			try{
				Project project = projectService.convertMap(map,user,false);
				Message message = projectService.createProject(project);
				assertEquals(message.getType(),Message.Type.OK);
			}catch (Exception e){
				e.printStackTrace();
			}
		Project project = projectService.findByCode("projectJsonTest1",user.getId()).get(0);
		//保存要素
		List<Unit> units = unitService.makeUnit(project.getType());
		List<String> contactList = Arrays.asList(contactUnitAliass);
		Map<String,Object> elementGroupMap;
		List<ElementGroup> elementGroups;
		List<Element> elements;
		List<Object> elementObjects;
		Element element;
		Element.Type  elementType;
		Unit unit;
		for(int i=0;i<units.size();i++){
			unit = units.get(i);
			unit.setProject(project);
			elementGroups = unit.getElementGroups();
          if(contactList.contains(unit.getAlias())){
			  elementGroupMap = new HashMap<>();
				  elements = new ArrayList<>();
				  for(int k=0;k<elementGroups.get(0).getElements().size();k++){
					  element = elementGroups.get(0).getElements().get(k);
					  elementType =  element.getType();
					  if(elementType== Element.Type.TEL){
						  element.setValue("18661925010");
					  }else if(elementType == Element.Type.EMAIL){
						  element.setValue("1321404703@qqcom");
					  }else if(elementType == Element.Type.SELECT){
                          element.setValue(element.getSelects().get((int)(Math.random() * element.getSelects().size())));
					  }else if(elementType== Element.Type.DATE){
						  element.setValue(new Date().toString());
					  }else{
						  element.setValue(unit.getName()+element.getName());
					  }
					  elements.add(element);
				  }
			  elementObjects = getElementMap(elements);
			  elementGroupMap.put("elements",elementObjects);
			  elementService.doSaveElement(elementGroupMap,unit,user);
		  }else{
			  for(int k=0;k<elementGroups.size();k++){
				  elementGroupMap = new HashMap<>();
				  elements = new ArrayList<>();
				  for(int j=0;j<elementGroups.get(k).getElements().size();j++) {
					  element = elementGroups.get(k).getElements().get(j);
					 elementType =  element.getType();
					  if (elementType==Element.Type.TEXT) {
						element.setValue(element.getName());
					  } else if (elementType.equals(Element.Type.NUMBER)) {
						  element.setValue(j+"");
					  } else if (elementType.equals(Element.Type.DATE_REGION)) {
						  element.setValue(j+"");
					  } else if (elementType.equals(Element.Type.SELECT)) {
						  element.setValue(element.getSelects().get((int)(Math.random() * element.getSelects().size())));
					  } else if (elementType.equals(Element.Type.AREA)) {
						  element.setValue("青海西宁");
					  } else if (elementType.equals(Element.Type.DATE)) {
						  element.setValue(new Date().toString());
					  } else if (elementType.equals(Element.Type.TEL)) {
						  element.setValue("18661925010");
					  } else if (elementType.equals(Element.Type.EMAIL)) {
						  element.setValue("18661925010@qq.com");
					  } else if (elementType.equals(Element.Type.TEXT_AREA)) {
						 element.setValue("项目构建测试");
					  } else if (elementType.equals(Element.Type.LABEL)) {
						 //链式结构
						 //构建要素数据组
						  List<ElementDataGroup> elementDataGroups = new ArrayList<>();
						  ElementDataGroup elementDataGroup;
						  ElementData elementData;
						  if(element.getElementDataGroupType()!=null&&element.getElementDataSelects()!=null){
							  for(int h=0;h<element.getElementDataSelects().size();h++){
								  elementDataGroup = elementDataGroupService.makeElementDataGroup(
										  element.getAlias(), element.getElementDataSelects().get(h),element.getElementDataGroupType().toString(), project.getId());
								  for(int f=0;f<elementDataGroup.getElementDatas().size();f++){
									  elementData = elementDataGroup.getElementDatas().get(f);
									  if(!StringUtils.hasText(elementDataGroup.getName())){
										  elementDataGroup.setName("测试elementDataGroup");
										  elementData.setName("测试elementData");
									  }
									  elementData.setValue("111");
								  }
								  elementDataGroupService.saveElementDataGroup(elementDataGroup);
								  elementDataGroups.add(elementDataGroup);
							  }
						  }
						  element.setElementDataGroups(elementDataGroups);
					  } else if (elementType.equals("select_text")) {
						  element.setValue(element.getSelects().get((int)(Math.random() * element.getSelects().size())));
					  } else if (elementType.equals(Element.Type.COORDINATE)) {
						  element.setValue("102.722,36.091,0");
					  } else if (elementType.equals("coordinate_upload")) {
						  //坐标上传
					  } else if (elementType.equals(Element.Type.MAP)) {
						  element.setValue("黄河流域");
					  } else if (elementType.equals(Element.Type.CHECKBOX)) {
						  element.setValue(element.getSelects().get((int)(Math.random() * element.getSelects().size())));
					  }else if(elementType.equals(Element.Type.FILE_UPLOAD)){
						  //文件上传
					  }else if(elementType.equals(Element.Type.DAQ)){

					  }else if(elementType.equals(Element.Type.SECTION_UPLOAD)){

					  }
					  elements.add(element);
				  }
				  elementObjects = getElementMap(elements);
				  elementGroupMap.put("elements",elementObjects);
				  elementService.doSaveElement(elementGroupMap,unit,user);
			  }
		  }
		}

	}

	/**
	 * 测试数据准备
	 * @param elements
	 * @return
     */
	public List<Object> getElementMap(List<Element> elements){
		List<Object> listObjects = new ArrayList<>();
		List<Object> elementDataGroups;
		List<Object> elementDatas;
		Map<String,Object> elementMap;
		Map<String,Object> elementDataGroupMap;
		Map<String,Object> elementDataMap;
		Element element;
		ElementDataGroup elementDataGroup;
		ElementData elementData;
		for(int i=0;i<elements.size();i++){
			elementDataGroups = new ArrayList<>();
			element = elements.get(i);
			elementMap = new HashMap<>();
			elementMap.put("name",element.getName());
			elementMap.put("value",element.getValue());
			elementMap.put("selects",element.getSelects());
			elementMap.put("selects",element.getSelects());
            elementMap.put("alias",element.getAlias());
			for(int j=0;j<element.getElementDataGroups().size();j++){
				elementDataGroupMap = new HashMap<>();
				elementDataGroup = element.getElementDataGroups().get(j);
				elementDataGroupMap.put("name",elementDataGroup.getName());
				elementDataGroupMap.put("dataType",elementDataGroup.getDataType());
				elementDataGroupMap.put("id",elementDataGroup.getId());
				elementDatas = new ArrayList<>();
				for (int k=0;k<elementDataGroup.getElementDatas().size();k++){
					elementDataMap = new HashMap<>();
					elementData = elementDataGroup.getElementDatas().get(k);
					elementDataMap.put("name",elementData.getName());
					elementDataMap.put("value",elementData.getValue());
					elementDataMap.put("unit",elementData.getUnit());
					elementDataMap.put("description",elementData.getDescription());
					elementDataMap.put("type",elementData.getType());
					elementDataMap.put("parent",elementData.getParent());
				    elementDatas.add(elementDataMap);
				}
				elementDataGroupMap.put("elementDatas",elementDatas);
				elementDataGroups.add(elementDataGroupMap);
			}
			elementMap.put("elementDataGroups",elementDataGroups);
			listObjects.add(elementMap);
		}
		return listObjects;
	}

	/**
	 *人饮项目类型的要素输出测试
	 */
	@Test
	public void testDriProjectExportValues(){
		dataPrepareOfTestGetProject(0);
		List<JsonUnit> jsonUnits;
		String projectType = Project.Type.DRINGING_WATER.toString();
		User user = userService.getSimpleUser(userService.find(1l));
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		List<Project> projects = projectService.findByCode("projectJsonTest1",1l);
		assertTrue(projects.size()==1);
		jsonUnits = new ArrayList<>();
		jsonUnits =  projectService.getExportValues(user,Project.Type.DRINGING_WATER,jsonUnits,aliass.get(0));
		assertTrue(jsonUnits.size()==3);

	}

	/**
	 *灌溉项目类型的要素输出测试
	 */
	@Test
	public void testAgrProjectExportValues(){
		dataPrepareOfTestGetProject(1);
		List<JsonUnit> jsonUnits;
		User user = userService.getSimpleUser(userService.find(1l));
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		List<Project> projects = projectService.findByCode("projectJsonTest1",1l);
		assertTrue(projects.size()==1);
		jsonUnits = new ArrayList<>();
		jsonUnits =  projectService.getExportValues(user,Project.Type.AGRICULTURAL_IRRIGATION,jsonUnits,aliass.get(0));
		assertTrue(jsonUnits.size()==3);
	}

	/**
	 *防洪项目类型的要素输出测试
	 */
	@Test
	public void testFloProjectExportValues(){
		dataPrepareOfTestGetProject(2);
		List<JsonUnit> jsonUnits;
		String projectType = Project.Type.FLOOD_DEFENCES.toString();
		User user = userService.getSimpleUser(userService.find(1l));
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		List<Project> projects = projectService.findByCode("projectJsonTest1",1l);
		assertTrue(projects.size()==1);
		jsonUnits = new ArrayList<>();
		jsonUnits =  projectService.getExportValues(user,Project.Type.FLOOD_DEFENCES,jsonUnits,aliass.get(0));
		assertTrue(jsonUnits.size()==1);
	}

	/**
	 *水土保持项目类型的要素输出测试
	 */
	@Test
	public void testConProjectExportValues(){
		dataPrepareOfTestGetProject(3);
		List<JsonUnit> jsonUnits;
		String projectType = Project.Type.CONSERVATION.toString();
		User user = userService.getSimpleUser(userService.find(1l));
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		List<Project> projects = projectService.findByCode("projectJsonTest1",1l);
		assertTrue(projects.size()==1);
		for(int i = 0;i<aliass.size();i++){
			if(aliass.get(i).equals("211")||aliass.get(i).equals("212")){
				continue;
			}
			jsonUnits = new ArrayList<>();
			jsonUnits =  projectService.getExportValues(user,Project.Type.CONSERVATION,jsonUnits,aliass.get(i));
			assertTrue(jsonUnits.size()==1);
		}
	}

	/**
	 *小水电项目类型的要素输出测试
	 */
	@Test
	public void testHydProjectExportValues(){
		dataPrepareOfTestGetProject(4);
		List<JsonUnit> jsonUnits;
		String projectType = Project.Type.HYDROPOWER_ENGINEERING.toString();
		User user = userService.getSimpleUser(userService.find(1l));
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		List<Project> projects = projectService.findByCode("projectJsonTest1",1l);
		assertTrue(projects.size()==1);
		for(int i = 0;i<aliass.size();i++){
			jsonUnits = new ArrayList<>();
			if(aliass.get(i).equals("211")||aliass.get(i).equals("212")){
				continue;
			}
			jsonUnits =  projectService.getExportValues(user,Project.Type.HYDROPOWER_ENGINEERING,jsonUnits,aliass.get(i));
			assertTrue(jsonUnits.size()==1);
		}
	}

	/**
	 *供水保障项目类型的要素输出测试
	 */
	@Test
	public void testWatProjectExportValues(){
		dataPrepareOfTestGetProject(5);
		List<JsonUnit> jsonUnits;
		String projectType = Project.Type.WATER_SUPPLY.toString();
		User user = userService.getSimpleUser(userService.find(1l));
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		List<Project> projects = projectService.findByCode("projectJsonTest1",1l);
		assertTrue(projects.size()==1);
		for(int i = 0;i<aliass.size();i++){
			jsonUnits = new ArrayList<>();
			if(aliass.get(i).equals("211")||aliass.get(i).equals("212")){
				continue;
			}
			jsonUnits =  projectService.getExportValues(user,Project.Type.WATER_SUPPLY,jsonUnits,aliass.get(i));
			assertTrue(jsonUnits.size()==1);
		}
	}

	/**
	 * 测试构建单个项目
	 */
	@Test
	public void testGetProjectJson(){
		dataPrepareOfTestGetProject(0);
		User user = userService.getSimpleUser(userService.find(1l));
		Project project = projectService.findByCode("projectJsonTest1",user.getId()).get(0);
		Message message = projectService.getProjectBySubject(project.getId(),user);
		assertTrue(message.getType()== Message.Type.OK);
		JSONObject projectJson = (JSONObject) message.getData();
		JSONObject noteJson = (JSONObject) projectJson.get("note");
		assertNotNull(noteJson);
	}
	/**
	 * 构建指定项目下的指定名字的单元
	 *
	 * @throws QQSLException
	 */
	@Test
	public void testBuildUnitByUnitAlias() throws QQSLException {
		User user = userService.findByUserName("qqsl");
		long planningId = 2;
		String rename = "同仁县";
		long l = System.currentTimeMillis();
		String newcode = String.valueOf(l);
		int index = 2;
		Project project = new Project();
		project.setName(rename);
		project.setPlanning(planningId);
		project.setCode(newcode);
		project.setUser(user);
		projectService.setType(project,index);
		List<Project> projects = projectService.findByCode(newcode,1l);
		if (projects.size() == 0) {
			projectService.createProject(project);
			projects = projectService.findByCode(newcode,1l);
		}
		String unitAlias = "23";
		Unit unit = projectService.buildUnitByUnitAlias(projects.get(0),
				unitAlias);
		assertTrue(unit != null);
		assertEquals(unit.getName(),"可研");
	}

	/**
	 * 查找用户所属项目
	 */
	@Test
	public void testFindByUser() {
		User user = userService.findByUserName("qqsl");
		List<Project> projects = projectService.findByUser(user);
		assertTrue(projects.size() !=0);
	}

	/**
	 * 项目缓存测试
	 */
	@Test
	public void testRefreshProjects(){
		//缓存与数据库不一致
		User user = userService.findByUserName(CommonTest.USER_NAME);
		projectService.remove(projectService.findByUser(user).get(0));
//		assertTrue(projectService.findAll().size()!=projectCache.findAll(null).size());
		//刷新缓存
//	    projectCache.refreshProjects();
//		assertTrue(projectCache.findAll(null).size()==0);
		//验证缓存与数据库一致
//		projectService.refreshProjects();
//		assertTrue(projectService.findAll().size()==projectCache.findAll(null).size());
	}

	/**
	 * 测试url添加到数据库的同时是否更新到缓存
	 */
	@Test
	public void testEditPanoramicUrl(){
		User user = userService.findByUserName(CommonTest.USER_NAME);
		List<Project> projects = projectService.findByCode(CommonTest.PROJECT_CODE,user.getId());
		Project project = projectService.find(projects.get(0).getId());
		projectService.editPanoramicUrl("/map/url",project);
		Project project1 = projectService.find(project.getId());
		assertTrue(project1.getInfoStr().contains("/map/url"));
		project1 = projectService.find(project.getId());
		assertTrue(project1.getInfoStr().contains("/map/url"));
	}


	/**
	 * 清楚坏数据
	 */
	@Test
	public void clearData(){
		String newAlias[] = {"21A1","21A2","21A3","21A4","21B1","21C1"};
		List<ElementDB> elementDBS = elementDBService.findElementDBs(Arrays.asList(newAlias));
		if(elementDBS.size()>0){
			removeElementDBs(elementDBS);
		}
	}

	/**
	 * 修改地理信息，更新要素别名
	 */
	//@Test
	public void updateDili(){
	//新别名21A1  21A2  21A3  21A4 21B1 21C1
		String newAlias[] = {"21A1","21A2","21A3","21A4","21B1","21C1"};
		// 23A11 23A1  23A2  24A11  24A1  24A2
		String oldAlias23[] = {"23A11","23A1","23A2"};
		String oldAlias24[] = {"24A11","24A1","24A2"};
		//水土保持项目
		String oldAlias23w[] = {"23A11","23A1","23A2","23A2"};
		String oldAlias24w[] = {"24A11","24A1","24A2","24A3"};
		List<ElementDB> elementDBS = elementDBService.findElementDBs(Arrays.asList(newAlias));
		List<ElementDB> elementDBs23,elementDBs24;
		List<Project> projects = projectService.findAll();
			for(int i=0;i<projects.size();i++){
				if(projects.get(i).getType().equals(Project.Type.CONSERVATION)){
					elementDBs23 = elementDBService.findElementDBChilds(projects.get(i).getId(),Arrays.asList(oldAlias23w));
					elementDBs24 = elementDBService.findElementDBChilds(projects.get(i).getId(),Arrays.asList(oldAlias24w));
					if(elementDBs23.size()==0&&elementDBs24.size()==0){
						continue;
					}else{
						logger.info("水土保持项目");
						makeSure(elementDBs23,elementDBs24,oldAlias23w,oldAlias24w);
					}
				}else{
					elementDBs23 = elementDBService.findElementDBChilds(projects.get(i).getId(),Arrays.asList(oldAlias23));
					elementDBs24 = elementDBService.findElementDBChilds(projects.get(i).getId(),Arrays.asList(oldAlias24));
					if(elementDBs23.size()==0&&elementDBs24.size()==0){
						continue;
					}else{
						makeSure(elementDBs23,elementDBs24,oldAlias23,oldAlias24);
					}
				}
			}
	}

    private void getJson(List<ElementDB> elementDBS){
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;
		for(int i=0;i<elementDBS.size();i++){
			jsonObject = new JSONObject();
			jsonObject.put("id",elementDBS.get(i).getId());
			jsonObject.put("alias",elementDBS.get(i).getAlias());
			jsonObject.put("value",elementDBS.get(i).getValue());
			jsonObject.put("project",elementDBS.get(i).getProject().getId());
			jsonArray.add(jsonObject);
		}
		logger.info(jsonArray);
	}

	private void makeSure(List<ElementDB> elementDBS1,List<ElementDB> elementDBS2,String[] alias1,String[] alias2){
		logger.info("23-----------");
		getJson(elementDBS1);
		logger.info("24-----------");
		getJson(elementDBS2);
		if(elementDBS1.size()>0&&elementDBS2.size()>0){
			replaceAlias(elementDBS2,alias2);
		    removeElementDBs(elementDBS1);
		}
		if(elementDBS1.size()>0&&elementDBS2.size()==0){
			replaceAlias(elementDBS1,alias1);
		}
		if(elementDBS2.size()>0&&elementDBS1.size()==0){
			replaceAlias(elementDBS2,alias2);
		}
		logger.info("alias 23-----------");
		getJson(elementDBS1);
		logger.info("alias 24-----------");
		getJson(elementDBS2);
	}

	private void replaceAlias(List<ElementDB> elementDBS,String[] oldAlias){
		String newAlias[] = {"21A1","21A2","21A3","21A4","21B1","21C1"};
		for (int j=0;j<elementDBS.size();j++){
			if(elementDBS.get(j).getAlias().equals(oldAlias[0])){
				elementDBS.get(j).setAlias(newAlias[0]);
			}
			if(elementDBS.get(j).getAlias().equals(oldAlias[1])){
				elementDBS.get(j).setAlias(newAlias[1]);
			}
			if(elementDBS.get(j).getAlias().equals(oldAlias[2])){
				elementDBS.get(j).setAlias(newAlias[2]);
			}
			if(oldAlias.length==4){
				if(elementDBS.get(j).getAlias().equals(oldAlias[3])){
					elementDBS.get(j).setAlias(newAlias[3]);
				}
			}
		}
	}


	private void removeElementDBs(List<ElementDB> elementDBS){
		getJson(elementDBS);
		int i=elementDBS.size();
		for(int k=0;k<i;k++){
			elementDBService.remove(elementDBS.get(k));
		}

	}

	@Test
	public void cooperateSetNull(){
		List<Project> projects = projectService.findAll();
		for(int i=0;i<projects.size();i++){
			projects.get(i).setViews(null);
			projects.get(i).setCooperate(null);
			projects.get(i).setShares(null);
			projectService.save(projects.get(i));
		}
	}

	@Test
	public void testUploadFileSize(){
		Map<String, Object> map = new HashMap<>();
		map.put("projectId", 848);
		map.put("fileSize", 134567);
		map.put("alias","231A" );
		map.put("fileNames", "aaaaaaaaaaaa");
		User user = userService.find(1l);
		Message message = projectService.uploadFileSize(map, user);
		Assert.assertTrue(message.getType() == Message.Type.OK);
	}

	@Test
	public void testDownloadFileSize(){
		Map<String, Object> map = new HashMap<>();
		map.put("projectId", 848);
		map.put("fileSize", 134567);
		map.put("alias","231A" );
		map.put("fileName", "aaaaaaaaaaaa");
		User user = userService.find(1l);
		Message message = projectService.downloadFileSize(map, user);
		Assert.assertTrue(message.getType() == Message.Type.OK);
	}

	@Test
	public void testDeleteFileSize(){
		Map<String, Object> map = new HashMap<>();
		map.put("projectId", 848);
		map.put("fileSize", 134567);
		map.put("alias","231A" );
		map.put("fileName", "aaaaaaaaaaaa");
		User user = userService.find(1l);
		Message message = projectService.deleteFileSize(map, user);
		Assert.assertTrue(message.getType() == Message.Type.OK);
	}

	@Test
	public void testDelete(){
		Map<String, Object> map = new HashMap<>();
		map.put("projectId", 29);
		map.put("fileSize", 134567);
	}
}