package com.hysw.qqsl.cloud.core.service;

import java.io.*;
import java.util.*;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.build.*;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.QQSLException;

public class CoordinateServiceTest extends BaseTest {
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private CoordinateService coordinateService;
	@Autowired
	private ElementDBService elementDBService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserService userService;
	@Autowired
	private BuildGroupService buildGroupService;
	@Autowired
	private BuildService buildService;
	@Autowired
	private FieldService fieldService;
	String str = "泉室,QS,截水廊道,JSLD,大口井,DKJ,土井,TJ,机井,JJ,涝池,LC,闸,FSZ,倒虹吸,DHX,跌水,DS,消力池,XIAOLC,护坦,HUT,海漫,HAIM,渡槽,DC,涵洞,HD,隧洞,SD,农口,NK,斗门,DM,公路桥,GLQ,车便桥,CBQ,各级渠道,GJQD,检查井,JCJ,分水井,FSJ,供水井,GSJ,减压井,JYJ,减压池,JYC,排气井,PAIQJ,放水井,FANGSJ,蓄水池,XSC,各级管道,GJGD,防洪堤,FHD,排洪渠,PHQ,挡墙,DANGQ,淤地坝,YDB,谷坊,GF,溢洪道,YHD,滴灌,DG,喷头,PT,给水栓,JSS,施肥设施,SFSS,过滤系统,GLXT,林地,LD,耕地,GD,草地,CD,居民区,JMQ,工矿区,GKQ,电力,DL,次级交通,CJJT,河床,HEC,水面,SHUIM,水位,SHUIW,水文,SHUIWEN,雨量,YUL,水质,SHUIZ,泵站,BZ,电站厂房,DZCF,地质点,DIZD,其他,TSD,普通点,POINT,供水干管,GSGG,供水支管,GSZG,供水斗管,GSDG,供水干渠,GSGQ,供水支渠,GSZQ,供水斗渠,GSDQ,排水干管,PSGG,排水支管,PSZG,排水斗管,PSDG,排水干渠,PSGQ,排水支渠,PSZQ,排水斗渠,PSDQ,灌溉范围,GGFW,保护范围,BHFW,供水区域,GSQY,治理范围,ZLFW,库区淹没范围,KQYMFW,水域,SHUIY,公共线面,GONGGXM";

	/**
	 * 测试点线面类型数据长度以及分类是否一一对应
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa0() throws IOException {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		List<String> list = new ArrayList<String>();
		boolean flag = true;
		list = Arrays.asList(str.split(","));
		for (int i = 0; i < list.size(); i = i + 2) {
			map.put(list.get(i), list.get(i + 1));
		}
		for (int j = 0; j < CommonAttributes.BASETYPEC.length; j++) {
			String value = map.get(CommonAttributes.BASETYPEC[j]);
			if (!value.equals(CommonAttributes.BASETYPEE[j])) {
				logger.info(CommonAttributes.BASETYPEC[j] + ":"
						+ CommonAttributes.BASETYPEE[j]);
				flag = false;
			}
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试文件有数据，但数据中全错
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaaxlsx() throws Exception {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "111111.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/111111.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		Message message = coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.DEGREE);
		Map<List<Graph>, List<Build>> map = (Map<List<Graph>, List<Build>>) message.getData();
		List<Graph> list = null;
		for (Map.Entry<List<Graph>, List<Build>> entry : map.entrySet()) {
			list = entry.getKey();
			break;
		}
		logger.info(list.size());
		Assert.assertTrue(list.size() == 0);
	}

	/**
	 * 测试损坏文件
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa1xls() throws IOException {
		boolean flag = false;

		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "123.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/123.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		try {
			String fileName=testFile.getOriginalFilename();
			String s = fileName.substring(fileName.lastIndexOf(".") + 1,
					fileName.length());
			Message message = coordinateService.readExcels(testFile.getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.DEGREE);
			Assert.assertTrue(message.getType()== Message.Type.FAIL);
		} catch (Exception e) {
			flag = true;
		}
        Assert.assertTrue(flag);
	}

	/**
	 * 测试过滤数据
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa2xlsx() throws IOException {
        boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();

		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "2.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/2.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
        try {
        	coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
        } catch (Exception e) {
            flag = true;
        }
        Assert.assertTrue(flag);
	}

	/**
	 * 测试过滤数据
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa2xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();

		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "2.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/2.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试经度向下越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa3xlsx() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();

		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "3.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/3.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试经度向下越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa3xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();

		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "3.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/3.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试经度向上越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa4xlsx() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "4.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/4.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试经度向上越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa4xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "4.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/4.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试纬度向下越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa5xlsx() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "5.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/5.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试纬度向下越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa5xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "5.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/5.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试纬度向上越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa6xlsx() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "6.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/6.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试纬度向上越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa6xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "6.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/6.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试高程越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa7xlsx() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "7.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/7.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试高程越界
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa7xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "7.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/7.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		try {
			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(flag);
	}

	/**
	 * 测试成功文件
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa8xlsx() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "8.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/8.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		List<Graph> list = null;
		try {
			Message message = coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
			Map<List<Graph>, List<Build>> map = (Map<List<Graph>, List<Build>>) message.getData();
			for (Map.Entry<List<Graph>, List<Build>> entry : map.entrySet()) {
				list = entry.getKey();
				break;
			}
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(!flag);
		Assert.assertTrue(list.size()==10);
	}

	/**
	 * 测试成功文件
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa8xls() throws IOException {
		boolean flag = false;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "8.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/8.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		List<Graph> list = null;
		try {
			Message message = coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
			Map<List<Graph>, List<Build>> map = (Map<List<Graph>, List<Build>>) message.getData();
			for (Map.Entry<List<Graph>, List<Build>> entry : map.entrySet()) {
				list = entry.getKey();
				break;
			}
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(!flag);
		Assert.assertTrue(list.size()==10);
	}

	/**
	 * 测试后缀不在解析范围内的文件
	 * 
	 * @throws IOException
	 */
	@Test
	public void aaaaa9txt() throws Exception {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "9.txt");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/9.txt").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		Message message=coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE);
		Assert.assertTrue(message.getType()== Message.Type.FAIL);
	}

	/**
	 * 测试坐标文件成功
	 */
	/*@Test
	public void textGetCoordinate() {
		String center="102.35,35,2002";
		CoordinateBase coordinateBase = coordinateService
				.getCoordinate(center);
		Assert.assertTrue(coordinateBase.getLongitude().equals("102.35")
				&& coordinateBase.getElevation().equals("2002")
				&& coordinateBase.getLatitude().equals("35"));
	}
*/
	/**
	 * 测试坐标文件element值为空
	 */
	/*@Test
	public void textGetCoordinateValue() {
		CoordinateBase coordinateBase = coordinateService
				.getCoordinate("");
		Assert.assertTrue(coordinateBase == null);
	}*/

	/**
	 * 测试点数据转化为字符串
	 */
	@Test
	public void testListToStringPoing() {
		JSONObject jsonObject = new JSONObject();
		List<Graph> list = new ArrayList<Graph>();
		Graph graph;
		CoordinateBase coordinateBase;
		List<CoordinateBase> coordinates;
		for (int i = 0; i <= 87; i++) {
			coordinateBase = new CoordinateBase();
			coordinateBase.setElevation(String.valueOf(i));
			coordinateBase.setLatitude(String.valueOf(i + 1));
			coordinateBase.setLongitude(String.valueOf(i + 2));
			coordinates = new ArrayList<>();
			coordinates.add(coordinateBase);
			graph = new Graph();
			graph.setCoordinates(coordinates);
			graph.setBaseType(CommonEnum.CommonType.SD);
			list.add(graph);
		}
		String str2 = coordinateService.listToString(list,jsonObject);
		Assert.assertTrue(jsonObject.size() > 0);
	}

	/**
	 * 测试写入数据库service
	 * @throws IOException
	 * @throws QQSLException 
	 */
	@Test
	public void testUploadCoordinateToData() throws IOException, QQSLException {
		User user =userService.findByUserName("qqsl");
		List<Project> projectses=projectService.findByCode("123456711",1l);
		String treePath;
		long id;
		if(projectses.size()==0){
			Project project = new Project();
			project.setCode("123456711");
			project.setUser(user);
			project.setName("同仁县");
			project.setPlanning(2l);
			projectService.setType(project,2);
			projectService.createProject(project);
			List<Project> projects=projectService.findByCode("123456711",1l);
			treePath=projects.get(0).getTreePath();
			id=projects.get(0).getId();
			ElementDB elementDB=new ElementDB();
			elementDB.setAlias("23A11");
			elementDB.setValue("102,35,2002");
			elementDB.setProject(projects.get(0));
			elementDBService.save(elementDB);	
		}else{
			treePath=projectses.get(0).getTreePath();
			id=projectses.get(0).getId();
		}
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "8.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/8.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile mFile = new CommonsMultipartFile(fileItem);
		String str1="线坐标,供水干管,"+treePath+"/2,"+id;
		String[] fileInfos =str1.split(",");
		String fileName = mFile.getOriginalFilename();
//			Message me = coordinateService.uploadCoordinateToData(mFile.getFileItem().getInputStream(), fileInfos,
//				fileName);
//		Assert.assertTrue(me==null);
	}
	
	/**
	 * 测试写入数据库service null
	 * @throws IOException
	 * @throws QQSLException 
	 */
	@Test
	public void testUploadCoordinateToDataNull() throws IOException, QQSLException {
		User user =userService.findByUserName("qqsl");
		String newcode="12345673253563564523452";
		List<Project> projectses=projectService.findByCode(newcode,1l);
		String treePath;
		long id;
		Project project = null;
		if(projectses.size()==0){
			project = new Project();
			project.setCode(newcode);
			project.setUser(user);
			project.setName("同仁县");
			project.setPlanning(2l);
			projectService.setType(project,2);
			projectService.createProject(project);
			List<Project> projects=projectService.findByCode(newcode,1l);
			treePath=projects.get(0).getTreePath();
			id=projects.get(0).getId();
			ElementDB elementDB=new ElementDB();
			elementDB.setAlias("23A11");
			elementDB.setProject(projects.get(0));
			elementDBService.save(elementDB);	
		}else{
			treePath=projectses.get(0).getTreePath();
			id=projectses.get(0).getId();
		}
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "123.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/123.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile mFile = new CommonsMultipartFile(fileItem);
		String str1="线坐标,供水干管,"+treePath+"/2,"+id;
		String fileName = mFile.getOriginalFilename();
		Message me = coordinateService.uploadCoordinateToData(mFile.getFileItem().getInputStream(),
				project, "102", fileName, Coordinate.WGS84Type.DEGREE);
		Assert.assertTrue(me.getType().toString().equals(Message.Type.FAIL.toString()));
	}
	
	/**
	 * 测试写入数据库service Central
	 * @throws IOException
	 * @throws QQSLException 
	 */
	@Test
	public void testUploadCoordinateToDataCentral() throws IOException, QQSLException {
		User user =userService.findByUserName("qqsl");
		String newcode="1234567433577";
		List<Project> projectses=projectService.findByCode(newcode,1l);
		String treePath;
		long id;
		Project project = null;
		List<ElementDB> elementDBs;
		if(projectses.size()==0){
			project = new Project();
			project.setCode(newcode);
			project.setUser(user);
			project.setName("同仁县");
			project.setPlanning(2l);
			projectService.setType(project,2);
			projectService.createProject(project);
			List<Project> projects=projectService.findByCode(newcode,1l);
			treePath=projects.get(0).getTreePath();
			id=projects.get(0).getId();
			ElementDB elementDB=new ElementDB();
			elementDB.setAlias("23A11");
			elementDB.setValue("103.5,35,2002");
			elementDB.setProject(projects.get(0));
			elementDBService.save(elementDB);	
			elementDBs=elementDBService.findByProject(projects.get(0).getId(), "23A11");
		}else{
			treePath=projectses.get(0).getTreePath();
			id=projectses.get(0).getId();
			elementDBs=elementDBService.findByProject(projectses.get(0).getId(), "23A11");
		}
		if(elementDBs==null){}
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "123.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/123.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile mFile = new CommonsMultipartFile(fileItem);
		String str1="线坐标,供水干管,"+treePath+"/2,"+id;
		String[] fileInfos =str1.split(",");
		String fileName = mFile.getOriginalFilename();
		Message me = coordinateService.uploadCoordinateToData(mFile.getFileItem().getInputStream(),
				project, "102", fileName, Coordinate.WGS84Type.DEGREE);
		Assert.assertTrue(me.getType().toString().equals(Message.Type.FAIL.toString()));
	}
	
	/**
	 * 根据treePath查询坐标表
	 */
	@Test
	public void testFindByTreePath(){
		String newcode="hyswxian34";
		List<Project> projects=projectService.findByCode(newcode,1l);
		String treePath=""+projects.get(0).getTreePath()+"/2/2A/8.xlsx";
		List<String> treePaths=new ArrayList<String>();
		treePaths.add(treePath);
		List<Coordinate> coordinates=coordinateService.findByTreePath(treePaths);
		Assert.assertTrue(coordinates.size()==0);
	}

	/**
	 * 测试上传坐标文件并保存至数据库
	 * @throws IOException
     */
	@Test
	public void testUploadCoordinate() throws IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "8.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/8.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		List<User> user = userService.findAll();
		Project project = new Project();
		String treePath = user.get(0).getId() + new Date().toString();
		treePath = DigestUtils.md5Hex(treePath);
		project.setUser(user.get(0));
		project.setTreePath(treePath);
		projectService.save(project);
//		projectCache.add(project);
		ElementDB elementDB = new ElementDB();
		elementDB.setProject(project);
		elementDB.setValue("102,36,2002");
		elementDB.setAlias("24A11");
		elementDBService.save(elementDB);
		elementDBService.flush();
		String fileInfo = "线坐标,供水支管,"+project.getTreePath()+"/2/2A"+","+project.getId();
		Message message = coordinateService.uploadCoordinate(testFile,project,"102",Coordinate.WGS84Type.DEGREE);
		Assert.assertTrue(message.getType()== Message.Type.OK);
		List<String> treePaths = new ArrayList<>();
		treePaths.add(project.getTreePath() + "/2/2A/" + "8"+".xlsx");
		elementDBService.remove(elementDB);
		coordinateService.removeByTreePaths(treePaths);
//		projectCache.remove(project);
		projectService.remove(project);
	}

	@Test
	public void testBaseType(){
		System.out.println(CommonAttributes.BASETYPEC.length);
		System.out.println(CommonAttributes.BASETYPEE.length);
		for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
			if (!CommonEnum.CommonType.valueOf(i).toString().equals(CommonAttributes.BASETYPEE[i])) {
				System.out.println(CommonEnum.CommonType.valueOf(i)+"==>>"+CommonAttributes.BASETYPEE[i]);
			}
			if ((CommonAttributes.BASETYPEE.length - 1) == i) {
				System.out.println(CommonEnum.CommonType.valueOf(i)+"==>>"+CommonAttributes.BASETYPEE[i]);
			}

		}
	}

	@Test
	public void testReadExcel() throws IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "789.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/789.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		Project project = projectService.find(531l);
		Workbook wb;
		if (fileName.endsWith("xls")) {
			wb = new HSSFWorkbook(testFile.getFileItem().getInputStream());
		}else{
			wb = new XSSFWorkbook(testFile.getFileItem().getInputStream());
		}
		for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
			Sheet sheet = wb.getSheetAt(numSheet);
			if (sheet == null) {
				continue;
			}
			Build build = null;
			for (int i = 0; i < CommonAttributes.BASETYPEC.length; i++) {
				if (sheet.getSheetName().trim().equals(CommonAttributes.BASETYPEC[i])) {
					String s = CommonAttributes.BASETYPEE[i];
					List<BuildGroup> completeBuildGroups = buildGroupService.getCompleteBuildGroups();
					boolean flag=false;
					for (int j = 0; j < completeBuildGroups.size(); j++) {
						BuildGroup buildGroup = completeBuildGroups.get(j);
						for (int k = 0; k < buildGroup.getBuilds().size(); k++) {
							if (buildGroup.getBuilds().get(k).getType().toString().equals(s)) {
								build = buildGroup.getBuilds().get(k);
								flag = true;
								break;
							}
						}
						if(flag){
							break;
						}
					}
					break;
				}
			}
			Build build1 = new Build();
			List<Attribe> attribes = new ArrayList<>();
			Attribe attribe;
			for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);
				if (row != null) {
					String a = null;
					String b = null;
					String c = null;
					String d = null;
					String comment = null;
					if (row.getCell(0) != null) {
						row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
						a = row.getCell(0).getStringCellValue();
					}
					if (row.getCell(1) != null) {
						row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
						b = row.getCell(1).getStringCellValue();
					}
					if (row.getCell(2) != null) {
						row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
						c = row.getCell(2).getStringCellValue();
					}
					if (row.getCell(3) != null) {
						row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
						d = row.getCell(3).getStringCellValue();
						Comment cellComment = row.getCell(3).getCellComment();
						if (cellComment != null) {
							comment = cellComment.getString().getString();
						}

					}
					if (b == null || b.trim().equals("")) {
						continue;
					}
					if (b.equals("名称")) {
						if (rowNum != 0) {
							build1.setAttribeList(attribes);
							buildService.save(build1);
							build1 = new Build();
							attribes = new ArrayList<>();
						}
						build1.setName(build.getName());
						build1.setAlias(build.getAlias());
						build1.setType(build.getType());
						build1.setProject(project);
						continue;
					}
					if (d != null && !d.trim().equals("") && comment != null && !comment.trim().equals("")) {
						if (comment.equals("coor1")) {
							build1.setCenterCoor(d);
						}else if (comment.equals("coor2")) {
							build1.setPositionCoor(d);
						}else {
							attribe = new Attribe();
							attribe.setAlias(comment);
							attribe.setValue(d);
							attribe.setBuild(build1);
							attribes.add(attribe);
						}
					}
					if (rowNum == sheet.getLastRowNum()) {
						build1.setAttribeList(attribes);
						buildService.save(build1);
					}

				}
			}
		}
	}

	/**
	 * 写excel文件类
	 */
	class WriteExecl{
		private int index;
		private int max;

		WriteExecl(){
			this.index = 0;
		}

		public int getIndexAdd() {
			if (max < index) {
				max = index;
			}
			return index++;
		}

		public int getIndex() {
			return index;
		}

		public int getIndexMinus(){
			return index--;
		}

		public int getMax() {
			return max;
		}
	}

	/**
	 * 输出建筑物模板
	 * @throws IOException
	 */
	@Test
	public void testOutputAllModel() throws IOException {
		Workbook wb = new HSSFWorkbook();
		List<Build> buildsDynamic = buildGroupService.getBuildsDynamic();
		Map<CommonEnum.CommonType,List<Build>>map=fieldService.groupBuild(buildsDynamic);
		Row row = null;
		Cell cell = null;
		boolean flag;
		final String[] num = {"一","二","三","四","五","六","七"};
		for (Map.Entry<CommonEnum.CommonType, List<Build>> entry : map.entrySet()) {
			Sheet sheet = null;
			WriteExecl we = new WriteExecl();
			for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
				if (CommonAttributes.BASETYPEE[i].equals(entry.getKey().toString())) {
					sheet = wb.createSheet(CommonAttributes.BASETYPEC[i]);
					break;
				}
			}
			List<Build>  builds= entry.getValue();
			flag = true;
			for (int i = 0; i < builds.size(); i++) {
//				if (builds.get(i).getAttribeList() == null || builds.get(i).getAttribeList().size() == 0) {
//					continue;
//				}
				flag = false;
				CellStyle style1 = wb.createCellStyle();
				style1.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				Font font1 = wb.createFont();
				font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				font1.setFontName("宋体");//设置字体名称
				style1.setFont(font1);
				style1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
				style1.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
				style1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
				style1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
				writeToCell(sheet,row,cell,style1,we,"编号","名称","单位","值",null,true);

				CellStyle style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				Font font = wb.createFont();
				font.setFontName("宋体");//设置字体名称
				style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
				style.setFont(font);
				//坐标头
				writeToCell(sheet,row,cell,style,we,num[0],"坐标",null,null,null,true);

				writeToCell(sheet,row,cell,style,we,"1","中心点","经度,纬度,高程",null,"coor1",true);

				writeToCell(sheet,row,cell,style,we,"2","定位点","经度,纬度,高程",null,"coor2",true);

				int n = 1;
				writeToCell(sheet,row,cell,style,we,num[n++],"描述",null,null,"remark",true);
				int aa = we.getIndex();
				writeToExcel(style, sheet, row, cell, we, builds.get(i).getMaterAttribeGroup(),num[n],false,true);
				if (aa != we.getIndex()) {
					n++;
					aa = we.getIndex();
				}
				writeToExcel(style, sheet, row, cell, we, builds.get(i).getHydraulicsAttribeGroup(),num[n],false,true);
				if (aa != we.getIndex()) {
					n++;
					aa = we.getIndex();
				}
				writeToExcel(style, sheet, row, cell, we, builds.get(i).getDimensionsAttribeGroup(),num[n],false,true);
				if (aa != we.getIndex()) {
					n++;
					aa = we.getIndex();
				}
				writeToExcel(style, sheet, row, cell, we, builds.get(i).getStructureAttribeGroup(),num[n],false,true);
				if (aa != we.getIndex()) {
					n++;
					aa = we.getIndex();
				}

//				writeToExcel(style, sheet, row, cell, we, builds.get(i).getGeologyAttribeGroup(),num[n],false,false);

//                a++;
//                CellStyle style2 = wb.createCellStyle();
//                style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//                Font font2 = wb.createFont();
//                font2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//                font2.setFontName("宋体");//设置字体名称
//                style2.setFont(font2);
//                style2.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
//                writeToCell(sheet,row,cell,style2,we,null,null,null,null,null,false);
			}
			int max = we.getMax();
			while (we.getIndex() <= max) {
				sheet.removeRow(sheet.createRow(we.getIndexAdd()));
			}
			if (flag) {
				wb.removeSheetAt(wb.getSheetIndex(sheet.getSheetName()));
			}
		}
		//        InputStream is = new ByteArrayInputStream(bos.toByteArray());
		OutputStream os = new FileOutputStream(new File("123.xls"));
		wb.write(os);
		os.close();
	}

	boolean writeToExcel(CellStyle style, Sheet sheet, Row row, Cell cell, WriteExecl we, AttribeGroup attribeGroup, String sign, boolean flag, boolean isComment) {
		if (attribeGroup == null) {
			return false;
		}
		writeToCell(sheet, row, cell,style, we, sign, attribeGroup.getName(), null, null, null, true);
		Drawing patriarch = sheet.createDrawingPatriarch();
		boolean flag1 = false;
		boolean flag2 = false;
		int s=1;
		String ss = "";
		String sss = "";
		if (attribeGroup.getAttribes() != null) {
			for (int j = 0; j < attribeGroup.getAttribes().size(); j++) {
//				if (attribeGroup.getAttribes().get(j).getValue() != null && !attribeGroup.getAttribes().get(j).getValue().equals("")) {
					flag = true;
					if (attribeGroup.getName().equals(attribeGroup.getAttribes().get(j).getName())) {
						flag = true;
						we.getIndexMinus();
						writeToCell(sheet, row, cell,style, we, sign, attribeGroup.getName(), null, attribeGroup.getAttribes().get(j).getValue(), null, true);
						if (flag) {
							flag2 = true;
						}
						continue;
					}else{
						row = sheet.createRow(we.getIndexAdd());
						cell = row.createCell(0);
						int e = j + 1;
						if (sign.equals("一") || sign.equals("二") || sign.equals("三") || sign.equals("四") || sign.equals("五") || sign.equals("六") || sign.equals("七")) {
							cell.setCellValue(e);
						} else {
							cell.setCellValue(sign + "." + e);
						}
						if (j == attribeGroup.getAttribes().size() - 1) {
							s = s + e;
						}
						cell.setCellStyle(style);
						cell = row.createCell(1);
						cell.setCellValue(attribeGroup.getAttribes().get(j).getName());
						cell.setCellStyle(style);
						cell = row.createCell(2);
						cell.setCellValue(attribeGroup.getAttribes().get(j).getUnit());
						cell.setCellStyle(style);
						cell = row.createCell(3);
						if (isComment) {
							Comment comment = patriarch.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
							comment.setString(new HSSFRichTextString(attribeGroup.getAttribes().get(j).getAlias()));
							row.getCell(3).setCellComment(comment);
						}
						cell.setCellValue(attribeGroup.getAttribes().get(j).getValue());
						cell.setCellStyle(style);
					}
					if (flag) {
						flag2 = true;
					}
//				}
			}
		}
		if (attribeGroup.getChilds() != null) {
			for (int i = 0; i < attribeGroup.getChilds().size(); i++) {
				if (i == 0) {
					ss = ss + s;
					if (sign.equals("一") || sign.equals("二") || sign.equals("三") || sign.equals("四") || sign.equals("五") || sign.equals("六") || sign.equals("七")) {
						sss = ss;
					} else {
						sss = sign + "." + ss;

					}
				}else{
					s++;
					ss = "";
					ss = ss + s;
					if (sign.equals("一") || sign.equals("二") || sign.equals("三") || sign.equals("四") || sign.equals("五") || sign.equals("六") || sign.equals("七")) {
						sss = ss;
					} else {
						sss = sign + "." + ss;

					}
				}
				flag1=writeToExcel(style, sheet, row, cell, we, attribeGroup.getChilds().get(i),sss,false,isComment);
//                flag = true;
				if (flag1) {
					flag2 = true;
				}else{
					s--;
				}
			}
		}
		if (!(flag||flag2)) {
			we.getIndexMinus();
		}
		if (flag2) {
			s++;
		}
		return flag2;
	}

	void writeToCell(Sheet sheet, Row row, Cell cell, CellStyle style, WriteExecl we, String a, String b, String c, String d, String e, boolean isAdd) {
		if (isAdd) {
			row = sheet.createRow(we.getIndexAdd());
		}else{
			row = sheet.createRow(we.getIndex());
		}
		cell = row.createCell(0);
		if (a != null) {
			cell.setCellValue(a);
		}
		cell.setCellStyle(style);
		cell = row.createCell(1);
		if (b != null) {
			cell.setCellValue(b);
		}
		cell.setCellStyle(style);
		cell = row.createCell(2);
		if (c != null) {
			cell.setCellValue(c);
		}
		cell.setCellStyle(style);
		cell = row.createCell(3);
		if (e != null) {
			Drawing patriarch = sheet.createDrawingPatriarch();
			Comment comment1 = patriarch.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
			comment1.setString(new HSSFRichTextString(e));
			row.getCell(3).setCellComment(comment1);
		}
		if (d != null) {
			cell.setCellValue(d);
		}
		cell.setCellStyle(style);
	}


	/**
	 * 测试线面描述为空
	 * @throws IOException
	 */
	@Test
	public void aaaa1axlsx() throws IOException {
		Project project = projectService.find(580l);
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "0001.xlsx");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/10.xlsx").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		Message list = coordinateService.uploadCoordinateToData(testFile.getFileItem().getInputStream(),
				project, "102", fileName, Coordinate.WGS84Type.DEGREE);
		coordinateService.flush();
		project = projectService.find(580l);
		List<Coordinate> coordinates = coordinateService.findByProject(project);
		Assert.assertTrue(coordinates.size() == 0);
	}

	/**
	 * 测试线面描述为非空
	 * @throws IOException
	 */
	@Test
	public void testUploadCoordinateToData1() throws IOException {
		Project project = projectService.find(818l);
		DiskFileItemFactory factory = new DiskFileItemFactory();
		FileItem fileItem = factory.createItem("file",
				"application/octet-stream", false, "547.xls");
		OutputStream str = fileItem.getOutputStream();
		File str2 = new ClassPathResource("/excelTest/547.xls").getFile();
		IOUtils.copy(new FileInputStream(str2), str);
		CommonsMultipartFile testFile = new CommonsMultipartFile(fileItem);
		String fileName=testFile.getOriginalFilename();
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		coordinateService.uploadCoordinateToData(testFile.getFileItem().getInputStream(),
				project, "102", fileName, Coordinate.WGS84Type.DEGREE);
		coordinateService.flush();
		project = projectService.find(818l);
		List<Coordinate> coordinates = coordinateService.findByProject(project);
		Assert.assertTrue(coordinates.size() == 0);
		coordinateService.flush();
		for (Coordinate coordinate : coordinates) {
			coordinateService.remove(coordinate);
		}
		coordinateService.flush();
		List<Coordinate> coordinates1 = coordinateService.findByProject(project);
		Assert.assertTrue(coordinates1.size() == 0);
	}

	/**
	 * 修改获取中央子午线方法
	 */
	@Test
	public void testGetCoordinateBasedatum(){
		Project project = new Project();
		ElementDB elementDB = new ElementDB();
		elementDB.setAlias("21A1");
		elementDB.setValue("100.49,35.25,0");
		elementDB.setProject(project);
		projectService.save(project);
		elementDBService.save(elementDB);
		projectService.flush();
		elementDBService.flush();
		String s = coordinateService.getCoordinateBasedatum(project);
		Assert.assertTrue(s.equals("99"));
		ElementDB elementDB1 = elementDBService.find(elementDB.getId());
		elementDB1.setValue("100.50,35.25,0");
		elementDBService.save(elementDB1);
		elementDBService.flush();
		s = coordinateService.getCoordinateBasedatum(project);
		Assert.assertTrue(s.equals("99"));
		ElementDB elementDB2 = elementDBService.find(elementDB.getId());
		elementDB2.setValue("100.51,35.25,0");
		elementDBService.save(elementDB2);
		elementDBService.flush();
		s = coordinateService.getCoordinateBasedatum(project);
		Assert.assertTrue(s.equals("102"));
		elementDBService.remove(elementDB);
		projectService.remove(project);
	}

}