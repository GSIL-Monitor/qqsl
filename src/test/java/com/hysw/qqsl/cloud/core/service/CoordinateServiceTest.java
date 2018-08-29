package com.hysw.qqsl.cloud.core.service;

import java.io.*;
import java.util.*;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.builds.*;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.hysw.qqsl.cloud.BaseTest;
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
	private FieldWorkService fieldWorkService;
	String str = "泉室,QS,截水廊道,JSLD,大口井,DKJ,土井,TJ,机井,JJ,涝池,LC,闸,FSZ,倒虹吸,DHX,跌水,DS,消力池,XIAOLC,护坦,HUT,海漫,HAIM,渡槽,DC,涵洞,HD,隧洞,SD,农口,NK,斗门,DM,公路桥,GLQ,车便桥,CBQ,各级渠道,GJQD,检查井,JCJ,分水井,FSJ,供水井,GSJ,减压井,JYJ,减压池,JYC,排气井,PAIQJ,放水井,FANGSJ,蓄水池,XSC,各级管道,GJGD,防洪堤,FHD,排洪渠,PHQ,挡墙,DANGQ,淤地坝,YDB,谷坊,GF,滴灌,DG,喷头,PT,给水栓,JSS,施肥设施,SFSS,过滤系统,GLXT,林地,LD,耕地,GD,草地,CD,居民区,JMQ,工矿区,GKQ,电力,DL,次级交通,CJJT,河床,HEC,水面,SHUIM,水位,SHUIW,水文,SHUIWEN,雨量,YUL,水质,SHUIZ,泵站,BZ,电站厂房,DZCF,地质点,DIZD,其他,TSD,普通点,POINT,供水干管,GSGG,供水支管,GSZG,供水斗管,GSDG,供水干渠,GSGQ,供水支渠,GSZQ,供水斗渠,GSDQ,排水干管,PSGG,排水支管,PSZG,排水斗管,PSDG,排水干渠,PSGQ,排水支渠,PSZQ,排水斗渠,PSDQ,灌溉范围,GGFW,保护范围,BHFW,供水区域,GSQY,治理范围,ZLFW,库区淹没范围,KQYMFW,水域,SHUIY,公共线面,GONGGXM";

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
		for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
			if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
				continue;
			}
			String value = map.get(commonType.getTypeC());
			if (!value.equals(commonType.name())) {
				logger.info(commonType.getTypeC() + ":"
						+ commonType.name());
				flag = false;
			}
		}
//		for (int j = 0; j < CommonAttributes.BASETYPEC.length; j++) {
//			String value = map.get(CommonAttributes.BASETYPEC[j]);
//			if (!value.equals(CommonAttributes.BASETYPEE[j])) {
//				logger.info(CommonAttributes.BASETYPEC[j] + ":"
//						+ CommonAttributes.BASETYPEE[j]);
//				flag = false;
//			}
//		}
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
		JSONObject jsonObject = new JSONObject();
//		coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.DEGREE,fileName,jsonObject);
//		Assert.assertTrue(jsonObject.get(fileName).equals(Message.Type.OK.getStatus()));
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
			JSONObject jsonObject = new JSONObject();
//			coordinateService.readExcels(testFile.getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.DEGREE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
        try {
//        	coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE, fileName, jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(!flag);
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
		JSONObject jsonObject = new JSONObject();
		try {
//			coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
		} catch (Exception e) {
			flag = true;
		}
		Assert.assertTrue(!flag);
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
		JSONObject jsonObject = new JSONObject();
//		coordinateService.readExcels(testFile.getFileItem().getInputStream(), "102", s, projectService.find(222l), Coordinate.WGS84Type.PLANE_COORDINATE,fileName,jsonObject);
		Assert.assertTrue(jsonObject.get(fileName).equals(Message.Type.COOR_FORMAT_ERROR.getStatus()));
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