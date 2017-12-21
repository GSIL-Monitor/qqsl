package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.CoordinateDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.build.CoordinateBase;
import com.hysw.qqsl.cloud.core.entity.build.Graph;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.aspectj.util.LangUtil;
import org.osgeo.proj4j.ProjCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * 读取excel数据并存储进项目中
 * 
 * @author Administrator
 *
 */
@Service("coordinateService")
public class CoordinateService extends BaseService<Coordinate, Long> {

	@Autowired
	public void setBaseDao(CoordinateDao coordinateDao) {
		super.setBaseDao(coordinateDao);
	}

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private TransFromService transFromService;
	@Autowired
	private CoordinateDao coordinateDao;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ElementDBService elementDBService;
	@Autowired
	private BuildService buildService;
	@Autowired
	private BuildGroupService buildGroupService;
	@Autowired
	private FieldService fieldService;
	// 转换文件大小
	private final long CONVERT_MAX_SZIE = 36 * 1024 * 1024;

	/**
	 * 读取excel 文件抛出异常
	 *
	 * @param is
	 * @param central
	 * @param wgs84Type
	 * @return
	 * @throws IOException
	 */
	public Message readExcels(InputStream is, String central, String s, Project project, Coordinate.WGS84Type wgs84Type) throws Exception {
		Workbook wb = SettingUtils.readExcel(is,s);
		if(wb==null){
			return new Message(Message.Type.FAIL);
		}
		return readExcel(central, wb, project,wgs84Type);
	}

	/**
	 * Read the Excel 2010
	 *
	 * @param central the path of the excel file
	 * @param wgs84Type
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public Message readExcel(String central, Workbook wb, Project project, Coordinate.WGS84Type wgs84Type) throws Exception {
		String code = transFromService.checkCode84(central);
		List<Graph> graphs = new ArrayList<Graph>();
		List<Build> builds = buildService.findByProjectAndSource(project, Build.Source.DESIGN);
		List<Build> builds1 = new ArrayList<>();
		// Read the Sheet
		for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
			Sheet sheet = wb.getSheetAt(numSheet);
			if (sheet == null) {
				continue;
			}
//			读取建筑物及其属性数据
			Message me = readBuild(sheet, code, builds, builds1, project,wgs84Type);
			if (me != null) {
                return me;
            }
            try {
//				读取线面数据
				readLineOrAera(sheet, code, graphs, wb, numSheet,wgs84Type);
			} catch (OfficeXmlFileException e) {
				logger.info("坐标文件03-07相互拷贝异常");
				continue;
			}catch(NumberFormatException e){
//				logger.info("经纬度有多余小数");
				continue;
			}
		}
		Map<List<Graph>, List<Build>> map = new LinkedHashMap<>();
		map.put(graphs, builds1);
        return new Message(Message.Type.OK, map);
    }

	/**
	 * 读取建筑物及其属性数据
	 * @param sheet
	 * @param code
	 * @param builds
	 * @param builds2
	 * @param project
	 * @param wgs84Type
	 */
	private Message readBuild(Sheet sheet, String code, List<Build> builds, List<Build> builds2, Project project, Coordinate.WGS84Type wgs84Type) throws Exception {
		Build build = null;
		Build build2 = null;
		JSONObject jsonObject1;
		JSONObject jsonObject;
		for (int i = 0; i < CommonAttributes.BASETYPEC.length; i++) {
			if (sheet.getSheetName().trim().equals(CommonAttributes.BASETYPEC[i])) {
				String s = CommonAttributes.BASETYPEE[i];
				List<Build> builds1 = buildGroupService.getBuilds();
				for (int k = 0; k < builds1.size(); k++) {
					if (builds1.get(k).getType().toString().equals(s)) {
						build = builds1.get(k);
						break;
					}
				}
				break;
			}
		}
		if (build == null) {
			return null;
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
                if ((b == null || b.trim().equals("")) && rowNum != sheet.getLastRowNum()) {
                    continue;
                }
                if (b.trim().equals("名称")) {
					if (rowNum != 0) {
						if (build1 != null && build1.getCenterCoor() != null) {
							build1.setAttribeList(attribes);
//							if (build1.getAttribeList()!=null&&build1.getAttribeList().size()!=0) {
							if (build1.getRemark() == null) {
								for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
									if (CommonAttributes.BASETYPEE[i].equals(build1.getType().toString())) {
										build1.setRemark(CommonAttributes.BASETYPEC[i]);
										break;
									}
								}
							}
							buildService.save(build1);
							builds2.add(build1);
//							}
						}
						build1 = new Build();
						attribes = new ArrayList<>();
					}
					build1.setName(build.getName());
					build1.setAlias(build.getAlias());
					build1.setType(build.getType());
					build1.setProject(project);
					build1.setSource(Build.Source.DESIGN);
					continue;
				}
//				if (build1 == null) {
//					continue;
//				}
				if (d != null && !d.trim().equals("") && comment != null && !comment.trim().equals("")) {
					if (comment.equals("coor1")) {
						String[] split = d.split(",");
						if (split.length != 3) {
                            return new Message(Message.Type.OTHER);
						}
						jsonObject1 = coordinateXYZToBLH(split[0], split[1], code,wgs84Type);
						if (jsonObject1 == null) {
							return new Message(Message.Type.OTHER);
						}
//						if (!SettingUtils.coordinateParameterCheck(split[0], split[1], split[2])) {
//							return new Message(Message.Type.OTHER);
//						}
						build2 = fieldService.allEqual(builds, String.valueOf(jsonObject1.get("longitude")), String.valueOf(jsonObject1.get("latitude")),split[2]);
						if (build2!= null) {
							buildService.remove(build2);
						}
						jsonObject = new JSONObject();
						jsonObject.put("longitude", String.valueOf(jsonObject1.get("longitude")));
						jsonObject.put("latitude", String.valueOf(jsonObject1.get("latitude")));
						jsonObject.put("elevation", String.valueOf(split[2]));
						build1.setCenterCoor(jsonObject.toString());
					}else if (comment.equals("coor2")) {
						String[] split = d.split(",");
						if (split.length != 3) {
                            return new Message(Message.Type.OTHER);
						}
						jsonObject1 = coordinateXYZToBLH(split[0], split[1], code,wgs84Type);
						if (jsonObject1 == null) {
							return new Message(Message.Type.OTHER);
						}
//						if (!SettingUtils.coordinateParameterCheck(split[0], split[1], split[2])) {
//                            return new Message(Message.Type.OTHER);
//						}
						jsonObject = new JSONObject();
						jsonObject.put("longitude", String.valueOf(jsonObject1.get("longitude")));
						jsonObject.put("latitude", String.valueOf(jsonObject1.get("latitude")));
						jsonObject.put("elevation", String.valueOf(split[2]));
						build1.setPositionCoor(jsonObject.toString());
					}else if(comment.equals("remark")){
						build1.setRemark(d);
					}else{
						attribe = new Attribe();
						attribe.setAlias(comment);
						attribe.setValue(d);
						attribe.setBuild(build1);
						attribes.add(attribe);
					}
				}
				if (rowNum == sheet.getLastRowNum() && build1 != null && build1.getCenterCoor() != null) {
					build1.setAttribeList(attribes);
//					if (build1.getAttribeList()!=null&&build1.getAttribeList().size()!=0) {
					if (build1.getRemark() == null) {
						for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
							if (CommonAttributes.BASETYPEE[i].equals(build1.getType().toString())) {
								build1.setRemark(CommonAttributes.BASETYPEC[i]);
								break;
							}
						}
					}
					buildService.save(build1);
					builds2.add(build1);
//					}
				}
			}
		}
        return null;
    }

	/**
	 * 读取线面数据
	 * @param sheet
	 * @param code
	 * @param graphs
	 * @param wb
	 * @param numSheet
	 * @param wgs84Type
	 */
	private void readLineOrAera(Sheet sheet, String code, List<Graph> graphs, Workbook wb, int numSheet, Coordinate.WGS84Type wgs84Type) throws Exception {
		CoordinateBase coordinateBase;
		Graph graph;
		List<CoordinateBase> list;
		// Read the Row
		for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row != null) {
				coordinateBase = new CoordinateBase();
				graph = new Graph();
				list = new ArrayList<>();
				String longitude = null;
				String latitude = null;
				String elevation = null;
				String type = null;
				String describtion = null;
				if (row.getCell(0) != null) {
					row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
					longitude = row.getCell(0).getStringCellValue();
				}
				if (row.getCell(1) != null) {
					row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
					latitude = row.getCell(1).getStringCellValue();
				}
				if (row.getCell(2) != null) {
					row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
					elevation = row.getCell(2).getStringCellValue();
				}
				if (row.getCell(3) != null) {
					row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
					type = row.getCell(3).getStringCellValue();
				}
				if (row.getCell(4) != null) {
					row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
					describtion = row.getCell(4).getStringCellValue();
				}
				if (null!=longitude&&longitude.trim().equals("描述")) {
					coordinateBase.setElevation(wb.getSheetName(numSheet));
					coordinateBase.setLatitude(latitude);
					coordinateBase.setLongitude("0");
					list.add(coordinateBase);
					graph.setCoordinates(list);
					graphs.add(graph);
					continue;
				}
				if (longitude == null || latitude == null
						|| elevation == null||longitude.trim().equals("经度")) {
					continue;
				}
				JSONObject jsonObject = coordinateXYZToBLH(longitude, latitude, code,wgs84Type);
				if (jsonObject == null) {
					continue;
				}
				if (Float.valueOf(elevation) < 0) {
					continue;
				}
				coordinateBase.setLongitude(String.valueOf(jsonObject.get("longitude")));
				coordinateBase.setLatitude(String.valueOf(jsonObject.get("latitude")));
				coordinateBase.setElevation(elevation);
				list.add(coordinateBase);
				graph.setCoordinates(list);
				if (type != null&&!type.trim().equals("")) {
					for (int i = 0; i < CommonAttributes.BASETYPEC.length; i++) {
						graph.setBaseType(CommonEnum.CommonType.TSD);
						if (CommonAttributes.BASETYPEC[i]
								.equals(type.trim())) {
							graph.setBaseType(CommonEnum.CommonType.valueOf(CommonAttributes.BASETYPEE[i]));
							break;
						}
					}
				}
				if (describtion != null) {
					graph.setDescription(describtion);
				}
				graphs.add(graph);
			}
		}
	}

	/**
	 * 将各类坐标转换为大地坐标
	 * @param longitude
	 * @param latitude
	 * @param code
	 * @return
	 */
	private JSONObject coordinateXYZToBLH(String longitude, String latitude, String code,Coordinate.WGS84Type wgs84Type) throws Exception {
		if (wgs84Type == null) {
			return null;
		}
		switch (wgs84Type) {
			case DEGREE:
				return degree(longitude,latitude);
			case DEGREE_MINUTE_1:
				return degreeMinute1(longitude,latitude);
			case DEGREE_MINUTE_2:
				return degreeMinute2(longitude,latitude);
			case DEGREE_MINUTE_SECOND_1:
				return degreeMinuteSecond1(longitude, latitude);
			case DEGREE_MINUTE_SECOND_2:
				return degreeMinuteSecond2(longitude, latitude);
			case PLANE_COORDINATE:
				return planeCoordinate(longitude,latitude,code);
		}
		return null;
	}

	private JSONObject degreeMinuteSecond2(String longitude, String latitude) {
		if (longitude.length() == 0 || latitude.length() == 0
				|| SettingUtils.parameterRegex(longitude)
				|| SettingUtils.parameterRegex(latitude)) {
			return null;
		}
		String a = longitude.substring(0,longitude.indexOf("°"));
		String b = longitude.substring(longitude.indexOf("°") + 1, longitude.indexOf("'"));
		String c = longitude.substring(longitude.indexOf("'") + 1, longitude.length() - 1);
		longitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60+Double.valueOf(c) / 3600);
		a = latitude.substring(0,latitude.indexOf("°"));
		b = latitude.substring(latitude.indexOf("°") + 1, latitude.indexOf("'"));
		c = latitude.substring(latitude.indexOf("'") + 1, latitude.length() - 1);
		latitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60+Double.valueOf(c) / 3600);
		if (Float.valueOf(longitude) > 180
				|| Float.valueOf(longitude) < 0) {
			return null;
		}
		if (Float.valueOf(latitude) > 90
				|| Float.valueOf(latitude) < 0) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", longitude);
		jsonObject.put("latitude", latitude);
		return jsonObject;
	}

	private JSONObject degreeMinuteSecond1(String longitude, String latitude) {
		if (longitude.length() == 0 || latitude.length() == 0
				|| SettingUtils.parameterRegex(longitude)
				|| SettingUtils.parameterRegex(latitude)) {
			return null;
		}
		String[] str=longitude.split(":");
		longitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60+Double.valueOf(str[2]) / 3600);
		str=latitude.split(":");
		latitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60+Double.valueOf(str[2])/3600);
		if (Float.valueOf(longitude) > 180
				|| Float.valueOf(longitude) < 0) {
			return null;
		}
		if (Float.valueOf(latitude) > 90
				|| Float.valueOf(latitude) < 0) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", longitude);
		jsonObject.put("latitude", latitude);
		return jsonObject;
	}

	private JSONObject degreeMinute2(String longitude, String latitude) {
		if (longitude.length() == 0 || latitude.length() == 0
				|| SettingUtils.parameterRegex(longitude)
				|| SettingUtils.parameterRegex(latitude)) {
			return null;
		}
		String a = longitude.substring(0,longitude.indexOf("°"));
		String b = longitude.substring(longitude.indexOf("°") + 1, longitude.indexOf("'"));
		longitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60);
		a = latitude.substring(0,latitude.indexOf("°"));
		b = latitude.substring(latitude.indexOf("°") + 1, latitude.indexOf("'"));
		latitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60);
		if (Float.valueOf(longitude) > 180
				|| Float.valueOf(longitude) < 0) {
			return null;
		}
		if (Float.valueOf(latitude) > 90
				|| Float.valueOf(latitude) < 0) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", longitude);
		jsonObject.put("latitude", latitude);
		return jsonObject;
	}

	private JSONObject degreeMinute1(String longitude, String latitude) {
		if (longitude.length() == 0 || latitude.length() == 0
				|| SettingUtils.parameterRegex(longitude)
				|| SettingUtils.parameterRegex(latitude)) {
			return null;
		}
		String[] str=longitude.split(":");
		longitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60);
		str=latitude.split(":");
		latitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60);
		if (Float.valueOf(longitude) > 180
				|| Float.valueOf(longitude) < 0) {
			return null;
		}
		if (Float.valueOf(latitude) > 90
				|| Float.valueOf(latitude) < 0) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", longitude);
		jsonObject.put("latitude", latitude);
		return jsonObject;
	}

	/**
	 * 平面坐标转经纬度
	 * @param longitude
	 * @param latitude
	 * @param code
	 * @return
	 */
	private JSONObject planeCoordinate(String longitude, String latitude, String code) throws Exception {
		double lon = 0.0f;
		double lat = 0.0f;
		if (longitude.length() == 0 || latitude.length() == 0
				|| SettingUtils.parameterRegex(longitude)
				|| SettingUtils.parameterRegex(latitude)) {
			return null;
		}
		String str = longitude;
		String str1 = str.substring(0, str.indexOf("."));
		if (str1.length() == 6) {
			lon = Double.valueOf(str);
		} else {
			throw new Exception("");
		}
		str = latitude;
		str1 = str.substring(0, str.indexOf("."));
		if (str1.length() == 7) {
			lat = Double.valueOf(str);
		} else {
			throw new Exception("");
		}
		ProjCoordinate pc = transFromService.XYZToBLH(code,
				lon, lat);
		longitude = String.valueOf(pc.x);
		latitude = String.valueOf(pc.y);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", longitude);
		jsonObject.put("latitude", latitude);
		return jsonObject;
	}

	/**
	 * 度
	 * @param longitude
	 * @param latitude
	 * @return
	 */
	private JSONObject degree(String longitude, String latitude) {
		if (longitude.length() == 0 || latitude.length() == 0
				|| SettingUtils.parameterRegex(longitude)
				|| SettingUtils.parameterRegex(latitude)) {
			return null;
		}
		if (Float.valueOf(longitude) > 180
				|| Float.valueOf(longitude) < 0) {
			return null;
		}
		if (Float.valueOf(latitude) > 90
				|| Float.valueOf(latitude) < 0) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("longitude", longitude);
		jsonObject.put("latitude", latitude);
		return jsonObject;
	}


	/**
	 * 根据项目查找项目下所有坐标数据
	 * @param project
	 * @return
	 */
	public List<Coordinate> findByProject(Project project) {
		List<Filter> filters = new ArrayList<>();
		filters.add(Filter.eq("project", project));
		List<Coordinate> coordinates = coordinateDao.findList(0, null, filters);
//		for (int i = 0; i < coordinates.size(); i++) {
//			for (int j = 0; j < coordinates.get(i).getBuilds().size(); j++) {
//				for (int k = 0; k < coordinates.get(i).getBuilds().get(j).getAttribeList().size(); k++) {
//					System.out.println(coordinates.get(i).getBuilds().get(j).getAttribeList().get(k).getId());
//				}
//			}
//		}
		return coordinates;
	}


	/**
	 * 根据项目以及保存时来源（外业、数据）查找坐标数据
	 * @param project
	 * @param source
	 * @return
	 */
	public List<Coordinate> findByProjectAndSource(Project project, Build.Source source) {
		List<Filter> filters = new ArrayList<>();
		filters.add(Filter.eq("project", project));
		filters.add(Filter.eq("source", source));
		List<Coordinate> coordinates = coordinateDao.findList(0, null, filters);
//		for (int i = 0; i < coordinates.size(); i++) {
//			for (int j = 0; j < coordinates.get(i).getBuilds().size(); j++) {
//				for (int k = 0; k < coordinates.get(i).getBuilds().get(j).getAttribeList().size(); k++) {
//					System.out.println(coordinates.get(i).getBuilds().get(j).getAttribeList().get(k).getId());
//				}
//			}
//		}
		return coordinates;
	}


	/**
	 * 点线面坐标文件转字符串
	 *
	 * @param list
	 * @return
	 */
	public String listToString(List<Graph> list, JSONObject coordinate) {
		List<JSONObject> objects=new ArrayList<>();
		JSONObject jsonObject;
		Coordinate.Type type = null;
		CommonEnum.CommonType baseType = null;
		String description = null;
		for (int i = 0; i < list.size(); i++) {
			jsonObject=new JSONObject();
			jsonObject.put("longitude",list.get(i).getCoordinates().get(0).getLongitude());
			jsonObject.put("latitude",list.get(i).getCoordinates().get(0).getLatitude());
			jsonObject.put("elevation",list.get(i).getCoordinates().get(0).getElevation());
			if(list.get(i).getCoordinates().get(0).getLongitude().equals("0")){
				type = Coordinate.Type.LINE;
				for (int i1 = 0; i1 < CommonAttributes.TYPELINEC.length; i1++) {
					if (CommonAttributes.TYPELINEC[i1].trim().equals(list.get(i).getCoordinates().get(0).getElevation().trim())) {
						type = Coordinate.Type.LINE;
						break;
					}
				}
				for (int i1 = 0; i1 < CommonAttributes.TYPEAREAC.length; i1++) {
					if (CommonAttributes.TYPEAREAC[i1].trim().equals(list.get(i).getCoordinates().get(0).getElevation().trim())) {
						type = Coordinate.Type.AREA;
						break;
					}
				}
				baseType = CommonEnum.CommonType.GONGGXM;
				for (int i1 = 0; i1 < CommonAttributes.TYPELINEAREAC.length; i1++) {
					if (CommonAttributes.TYPELINEAREAC[i1].trim().equals(list.get(i).getCoordinates().get(0).getElevation().trim())) {
						baseType = CommonEnum.CommonType.valueOf(CommonAttributes.TYPELINEAREAE[i1]);
						break;
					}
				}
				description = list.get(i).getCoordinates().get(0).getLatitude().trim();
				continue;
			}
			objects.add(jsonObject);
		}
		coordinate.put("coordinate",objects);
		if(baseType!=null){
			coordinate.put("type",type.toString());
			coordinate.put("baseType",baseType.toString());
			if(description!=null){
				coordinate.put("description",description);
			}
//				coordinate.put("treePath", treePath);
		}
		return description;
	}


	/**
	 * 坐标格式文件处理数据并存入数据库
	 *
	 * @param is
	 * @param central
	 * @param fileName
	 * @param wgs84Type
	 * @return
	 * @throws IOException
	 */
	public Message uploadCoordinateToData(InputStream is,
										  Project project, String central, String fileName, Coordinate.WGS84Type wgs84Type) {
		String s = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
		Message me;
		try {
			me = readExcels(is, central, s, project,wgs84Type);
		} catch (Exception e) {
			logger.info("坐标文件或格式异常");
			return new Message(Message.Type.FAIL);
		}finally {
			IOUtils.safeClose(is);
		}
		if (me.getType()== Message.Type.OTHER) {
		    return me;
        }
        Map<List<Graph>, List<Build>> map = (Map<List<Graph>, List<Build>>) me.getData();
        List<Graph> list = null;
		List<Build> builds = null;
		for (Map.Entry<List<Graph>, List<Build>> entry : map.entrySet()) {
			list = entry.getKey();
			builds = entry.getValue();
			break;
		}
		if (list == null || list.size() == 0) {
			return new Message(Message.Type.OK);
		}
		return saveCoordinate(list, builds, project);
	}

	/**
	 * 保存坐标数据
	 * @param list
	 * @param builds
	 *@param project  @return
	 */
	public Message saveCoordinate(List<Graph> list, List<Build> builds, Project project) {
		List<Graph> pointList = new ArrayList<>();
		List<Graph> areaList = new ArrayList<>();
		Coordinate coordinate = null;
		JSONObject jsonCoordinates;
		Map<List<Graph>, List<Graph>> map = new LinkedHashMap<>();
		List<Coordinate> coordinates = findByProjectAndSource(project, Build.Source.DESIGN);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getBaseType() != null) {
				pointList.add(list.get(i));
			}
			if (list.get(i).getCoordinates().get(0).getLongitude().equals("0")) {
				if (i != 0) {
					map.put(areaList, pointList);
				}
				areaList = new ArrayList<>();
				pointList = new ArrayList<>();
			}
			areaList.add(list.get(i));
			if (i == list.size() - 1) {
				map.put(areaList, pointList);
			}
		}
		for (Map.Entry<List<Graph>, List<Graph>> entry : map.entrySet()) {
			jsonCoordinates=new JSONObject();
			String description = listToString(entry.getKey(), jsonCoordinates);
			boolean flag = false;
			for (Coordinate coordinate1 : coordinates) {
				if (coordinate1.getDescription().trim().equals(description.trim())) {
					coordinate = coordinate1;
					flag = true;
					break;
				}
			}
			if (!flag) {
				coordinate = new Coordinate();
			}
			coordinate.setProject(project);
			coordinate.setCoordinateStr(jsonCoordinates.toString());
			coordinate.setDescription(description);
			if (description != null && !description.trim().equals("")) {
				save(coordinate);
				//		保存筛选出的建筑简单数据
				pointToBuild(entry.getValue(),builds,project,coordinate.getId());
			}
		}
		return new Message(Message.Type.OK);
	}

	/**
	 * 保存建筑物简单数据
	 * @param pointList
	 * @param builds3
	 * @param project
	 * @param coordinateId
	 */
	private void pointToBuild(List<Graph> pointList, List<Build> builds3, Project project, Long coordinateId) {
		List<Build> builds = buildService.findByProjectAndSourceCoordinateId(project, Build.Source.DESIGN,coordinateId);
		List<Build> builds1 = buildGroupService.getBuilds();
		List<Build> builds2 = new ArrayList<>();
//		挑出上一次上传的同条线上的建筑物
		for (Build build : builds) {
			if (build.getAttribeList() == null || build.getAttribeList().size() == 0) {
				build.setCut(true);
				buildService.save(build);
			} else {
				builds2.add(build);
			}
		}
//		将查询数据库的结果与新上传的建筑物进行合并，剔除id相同的建筑物
		boolean flag;
		for (Build build : builds3) {
			flag = true;
			for (Build build1 : builds2) {
				if (build.getId().equals(build1.getId())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				builds2.add(build);
			}
		}
		Build build1 = null;
		for (Graph graph : pointList) {
			CoordinateBase coordinateBase = graph.getCoordinates().get(0);
			String longitude = coordinateBase.getLongitude();
			String latitude = coordinateBase.getLatitude();
			String elevation = coordinateBase.getElevation();
			flag = false;
			for (Build build : builds2) {
				JSONObject jsonObject = JSONObject.fromObject(build.getCenterCoor());
				String longitude1 = jsonObject.get("longitude").toString();
				String latitude1 = jsonObject.get("latitude").toString();
				String elevation1 = jsonObject.get("elevation").toString();
				if (longitude.equals(longitude1) && latitude.equals(latitude1) && elevation.equals(elevation1)) {
					builds2.remove(build);
					flag = true;
					break;
				}
			}
			if (!flag) {
				for (Build build : builds1) {
					if (build.getType().toString().equals(graph.getBaseType().toString())) {
						build1 = (Build) SettingUtils.objectCopy(build);
						break;
					}
				}
				if (graph.getDescription() != null) {
					build1.setRemark(graph.getDescription());
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("longitude", longitude);
				jsonObject.put("latitude", latitude);
				jsonObject.put("elevation", elevation);
				build1.setCenterCoor(jsonObject.toString());
				build1.setProject(project);
				build1.setSource(Build.Source.DESIGN);
				build1.setCoordinateId(String.valueOf(coordinateId));
				buildService.save(build1);
			}
		}
	}

	/**
	 * 根据项目下坐标求出项目的中央子午线
	 * @param project
	 * @return
	 */
	public String getCoordinateBasedatum(Project project) {
		List<ElementDB> elementDBs = elementDBService.findByProjectAndAlias(project);
		if (elementDBs.size() == 0 || elementDBs.get(0).getValue() == null) {
			return null;
		}
		String[] str = elementDBs.get(0).getValue().split(",");
		int[] meridian = {75, 78, 81, 84, 87, 90, 93, 96, 99, 102, 105, 108, 111, 114, 117, 120, 123, 126, 129, 132, 135};
		int begin = 0;
		int last = meridian.length-1;
		while(begin <= last) {
			int middle = (begin + last)/2;
			if(Math.abs(Double.valueOf(str[0]) - meridian[middle])<=1.5) {
				return String.valueOf(meridian[middle]);
			}else if((meridian[middle]-Double.valueOf(str[0]))>1.5) {
				last = middle - 1;
			}else if((Double.valueOf(str[0])-meridian[middle])>1.5){
				begin = middle + 1;
			}
		}
		return null;
	}


	/**
	 * 根据treePath查询坐标属性
	 *
	 * @param treePaths
	 * @return
	 */
	public List<Coordinate> findByTreePath(List<String> treePaths) {
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.in("treePath", treePaths));
		List<Coordinate> coordinates = coordinateDao.findList(0, null, filters);
		return coordinates;
	}

	/**
	 * 根据treePath删除坐标表单
	 *
	 * @param treePaths
	 */
	public void removeByTreePaths(List<String> treePaths) {
		List<Coordinate> coordinates = findByTreePath(treePaths);
		if (coordinates != null && coordinates.size() != 0) {
			Coordinate coordinate = coordinates.get(0);
			super.remove(coordinate);
		}
	}


	/**
	 * 上传坐标文件并保存至数据库
	 * @param mFile
     * @param project
* @param central
* @param wgs84Type
	 * @return
     */
	public Message uploadCoordinate(MultipartFile mFile, Project project, String central, Coordinate.WGS84Type wgs84Type) {
		Message me;
		String fileName;
		fileName = mFile.getOriginalFilename();
		// 限制上传文件的大小
		if (mFile.getSize() > CONVERT_MAX_SZIE) {
			// return "文件过大无法上传";
			logger.debug("文件过大");
			return new Message(Message.Type.FAIL);
		}
		InputStream is;
		try {
			is = mFile.getInputStream();
		} catch (IOException e) {
			logger.info("坐标文件或格式异常");
			return new Message(Message.Type.FAIL);
		}
		me = uploadCoordinateToData(is,project,central,fileName,wgs84Type);
		if (me != null) {
			return me;
		}
		return new Message(Message.Type.OK);
	}

	/**
	 * 保存设计的线面坐标数据
	 * @param objectMap
	 * @return
	 */
	public Message saveCoordinateFromPage(Map<String, Object> objectMap) {
		Object line = objectMap.get("line");
		Object projectId = objectMap.get("projectId");
		Object description = objectMap.get("description");
		if (line == null || projectId == null || description == null) {
			return new Message(Message.Type.FAIL);
		}
		Message message=checkCoordinateFormat(line);
		if (message.getType() == Message.Type.OTHER) {
			return message;
		}
		Coordinate coordinate = new Coordinate();
		JSONObject jsonObject = JSONObject.fromObject(line);
		Project project = projectService.find(Long.valueOf(projectId.toString()));
		coordinate.setCoordinateStr(String.valueOf(jsonObject));
		coordinate.setProject(project);
		coordinate.setDescription(description.toString());
		save(coordinate);
		return new Message(Message.Type.OK);
	}

	public List<Coordinate> findByDate(){
		List<Filter> filters = new ArrayList<>();
		Date newDate=new Date();
		Calendar calendar = Calendar.getInstance();  //得到日历
		calendar.setTime(newDate);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, -365*3);  //设置为前一天
		Date dBefore = calendar.getTime();   //得到前一天的时间
		filters.add(Filter.between("createDate", dBefore,newDate));
		List<Coordinate> list = coordinateDao.findList(0, null, filters);
		return list;
	}

	/**
	 * 检查坐标格式
	 * @param line
	 */
	public Message checkCoordinateFormat(Object line) {
		Map<Object, Object> map = (Map<Object, Object>) line;
		JSONArray coordinate = JSONArray.fromObject(map.get("coordinate"));
		JSONObject jsonObject;
		for (Object o : coordinate) {
			jsonObject = JSONObject.fromObject(o);
			if (jsonObject.size() != 3) {
				return new Message(Message.Type.OTHER);
			}
		}
		return new Message(Message.Type.OK);
	}
}
