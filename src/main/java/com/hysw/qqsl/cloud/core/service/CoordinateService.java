package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.CoordinateDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.Message;
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
//	// 转换文件大小
//	private final long CONVERT_MAX_SZIE = 36 * 1024 * 1024;

	/**
	 * 读取excel 文件抛出异常
	 *
	 * @param is
	 * @param central
	 * @param wgs84Type
	 * @return
	 * @throws IOException
	 */
	public void readExcels(InputStream is, String central, String s, Project project, Coordinate.WGS84Type wgs84Type, String fileName, JSONObject jsonObject) throws Exception {
		Workbook wb = SettingUtils.readExcel(is, s);
		if (wb == null) {
			jsonObject.put(fileName, Message.Type.COOR_FORMAT_ERROR.getStatus());
			return;
		}
		readExcel(central, wb, project, wgs84Type, fileName, jsonObject);
	}

	/**
	 * Read the Excel 2010
	 *
	 * @param central   the path of the excel file
	 * @param wgs84Type
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void readExcel(String central, Workbook wb, Project project, Coordinate.WGS84Type wgs84Type, String fileName, JSONObject jsonObject) throws Exception {
		String code = transFromService.checkCode84(central);
		List<Graph> graphs = new ArrayList<>();
		List<Build> builds = new ArrayList<>();
		// Read the Sheet
		for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
			Sheet sheet = wb.getSheetAt(numSheet);
			if (sheet == null) {
				continue;
			}
//			读取建筑物及其属性数据
			boolean b = readBuild(sheet, code, builds, project, wgs84Type);
			if (b) {
				continue;
			}
			try {
//				读取线面数据
				if (!readLineOrAera(sheet, code, graphs, wb, numSheet, wgs84Type)) {
					logger.info("数据格式与所选格式不一致");
					jsonObject.put(fileName, Message.Type.COOR_TYPE_ERROR.getStatus());
					return;
				}
			} catch (OfficeXmlFileException e) {
				e.printStackTrace();
				logger.info("坐标文件03-07相互拷贝异常");
			} catch (NumberFormatException e) {
				e.printStackTrace();
				logger.info("经纬度有多余小数");
			}
		}
		if (graphs.size() != 0) {
			saveCoordinate(graphs, builds, project);
		}
		flush();
		if (builds.size() != 0) {
			matchLineAndPoint(builds,project);
			saveBuild(builds,project);
		}
		buildService.flush();
		jsonObject.put(fileName, Message.Type.OK.getStatus());
	}

	/**
	 * 匹配点坐标到线面
	 * @param builds
	 * @param project
	 */
	private void matchLineAndPoint(List<Build> builds, Project project) {
		List<Coordinate> coordinates = findByProjectAndSource(project, Build.Source.DESIGN);
		for (Coordinate coordinate : coordinates) {
			JSONObject jsonObject = JSONObject.fromObject(coordinate.getCoordinateStr());
			JSONArray coordinate1 = (JSONArray) jsonObject.get("coordinate");
			for (Object o : coordinate1) {
				JSONObject jsonObject1 = JSONObject.fromObject(o);
				String longitude = jsonObject1.get("longitude").toString();
				String latitude = jsonObject1.get("latitude").toString();
				String elevation = jsonObject1.get("elevation").toString();
				Build build = fieldService.allEqual(builds, longitude, latitude, elevation);
				if (build != null && build.getCoordinateId() == null) {
					build.setCoordinateId(coordinate.getId());
				}
			}
		}
	}

	/**
	 * 保存建筑物
	 * @param builds
	 * @param project
	 */
	private void saveBuild(List<Build> builds, Project project) {
		List<Build> builds1 = buildService.findByProjectAndSource(project, Build.Source.DESIGN);
		for (Build build : builds) {
			JSONObject centerCoor = JSONObject.fromObject(build.getCenterCoor());
			String longitude = centerCoor.get("longitude").toString();
			String latitude = centerCoor.get("latitude").toString();
			String elevation = centerCoor.get("elevation").toString();
			Build build1 = fieldService.allEqual(builds1, longitude, latitude, elevation);
			if (build1 != null) {
				if ((build.getAttribeList()==null||build.getAttribeList().size() == 0) && (build1.getAttribeList()==null||build1.getAttribeList().size() != 0)) {
				} else {
					buildService.remove(build1);
					builds1.remove(build1);
					buildService.save(build);
					builds1.add(build);
				}
			} else {
				buildService.save(build);
				builds1.add(build);
			}
		}

	}

	/**
	 * 读取建筑物及其属性数据
	 * @param sheet
	 * @param code
	 * @param builds
	 * @param project
	 * @param wgs84Type
	 */
	private boolean readBuild(Sheet sheet, String code, List<Build> builds, Project project, Coordinate.WGS84Type wgs84Type) throws Exception {
		Build build;
		build = pickBuildFromBuilds(sheet);
		if (build == null) {
			return false;
		}
		Build build1 = new Build();
		List<Attribe> attribes = new ArrayList<>();
		for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
			Row row = sheet.getRow(rowNum);
			if (row != null) {
				String b = null;
				String d = null;
				String comment = null;
				if (row.getCell(1) != null) {
					row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
					b = row.getCell(1).getStringCellValue();
				}
				if (row.getCell(3) != null) {
					row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
					d = row.getCell(3).getStringCellValue();
					Comment cellComment = row.getCell(3).getCellComment();
					if (cellComment != null) {
						comment = cellComment.getString().getString();
					}
				}
                if ((b == null || b.trim().equals("")) /*&& rowNum != sheet.getLastRowNum()*/) {
                    continue;
                }
                if (b.trim().equals("名称")) {
					if (rowNum != 0 && build1.getCenterCoor() != null) {
						setBuildPropertyAndAddCache(build1,attribes,builds);
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
				if (!resolveExcelRow(d, comment, code, wgs84Type, build1, attribes)) {
					return false;
				}
				if (rowNum == sheet.getLastRowNum() && build1.getCenterCoor() != null) {
					setBuildPropertyAndAddCache(build1,attribes,builds);
				}
			}
		}
        return true;
    }

	/**
	 * 解析excel各行信息
	 * @return
	 * @param d
	 * @param comment
	 * @param code
	 * @param wgs84Type
	 * @param build1
	 * @param attribes
	 */
	private boolean resolveExcelRow(String d, String comment, String code, Coordinate.WGS84Type wgs84Type, Build build1, List<Attribe> attribes) throws Exception {
		JSONObject jsonObject,jsonObject1;
		Attribe attribe;
		if (d != null && !d.trim().equals("") && comment != null && !comment.trim().equals("")) {
			if (comment.equals("coor1")) {
				String[] split = d.split(",");
				if (split.length != 3) {
					return false;
				}
				jsonObject1 = coordinateXYZToBLH(split[0], split[1], code,wgs84Type);
				if (jsonObject1 == null) {
					return false;
				}
				jsonObject = new JSONObject();
				jsonObject.put("longitude", String.valueOf(jsonObject1.get("longitude")));
				jsonObject.put("latitude", String.valueOf(jsonObject1.get("latitude")));
				jsonObject.put("elevation", String.valueOf(split[2]));
				build1.setCenterCoor(jsonObject.toString());
			}else if (comment.equals("coor2")) {
				String[] split = d.split(",");
				if (split.length != 3) {
					return false;
				}
				jsonObject1 = coordinateXYZToBLH(split[0], split[1], code,wgs84Type);
				if (jsonObject1 == null) {
					return false;
				}
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
		return true;
	}

	/**
	 * 写build属性，并将其加入builds2的缓存中
	 * @param build1
	 * @param attribes
	 * @param builds2
	 */
	private void setBuildPropertyAndAddCache(Build build1, List<Attribe> attribes, List<Build> builds2) {
		build1.setAttribeList(attribes);
		if (build1.getRemark() == null) {
			for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
				if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
					continue;
				}
				if (commonType.name().equals(build1.getType().toString())) {
					build1.setRemark(commonType.getTypeC());
					break;
				}
			}
		}
//		buildService.save(build1);
		builds2.add(build1);
	}

	/**
	 * 从builds中根据类型挑拣相应的build
	 * @param sheet
	 */
	private Build pickBuildFromBuilds(Sheet sheet) {
		Build build = null;
		for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
			if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
				continue;
			}
			if (sheet.getSheetName().trim().equals(commonType.getTypeC())) {
				if (!commonType.getType().equals("build")) {
					return null;
				}
				List<Build> builds1 = buildGroupService.getBuilds();
				for (Build build1 : builds1) {
					if (build1.getType().toString().equals(commonType.name())) {
						build = build1;
						break;
					}
				}
				break;
			}
		}
		return build;
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
	private boolean readLineOrAera(Sheet sheet, String code, List<Graph> graphs, Workbook wb, int numSheet, Coordinate.WGS84Type wgs84Type) throws Exception {
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
						|| elevation == null||longitude.trim().equals("经度")||longitude.equals("")||latitude.equals("")||elevation.equals("")) {
					continue;
				}
				JSONObject jsonObject = coordinateXYZToBLH(longitude, latitude, code,wgs84Type);
				if (jsonObject == null) {
					return false;
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
					for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
						if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
							continue;
						}
                        graph.setBaseType(CommonEnum.CommonType.TSD);
                        if (commonType.getTypeC().equals(type.trim())) {
                            graph.setBaseType(commonType);
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
		return true;
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
		if (longitude.length() == 0 || latitude.length() == 0) {
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
		if (longitude.length() == 0 || latitude.length() == 0) {
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
		if (longitude.length() == 0 || latitude.length() == 0) {
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
		if (longitude.length() == 0 || latitude.length() == 0) {
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
		if (longitude.length() == 0 || latitude.length() == 0) {
			return null;
		}
		String str = longitude;
		if (str.contains(".")) {
			str = str.substring(0, str.indexOf("."));
		}
		if (str.length() == 6) {
			lon = Double.valueOf(str);
		} else {
			throw new Exception("");
		}
		str = latitude;
		if (str.contains(".")) {
			str = str.substring(0, str.indexOf("."));
		}
		if (str.length() == 7) {
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
		if (longitude.length() == 0 || latitude.length() == 0) {
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
				for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
					if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
						continue;
					}
					if (!commonType.getType().equals("line")) {
						continue;
					}
					if (commonType.getTypeC().trim().equals(list.get(i).getCoordinates().get(0).getElevation().trim())) {
						type = Coordinate.Type.LINE;
						break;
					}
				}
				for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
					if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
						continue;
					}
					if (!commonType.getType().equals("area")) {
						continue;
					}
					if (commonType.getTypeC().trim().equals(list.get(i).getCoordinates().get(0).getElevation().trim())) {
						type = Coordinate.Type.AREA;
						break;
					}
				}
				baseType = CommonEnum.CommonType.GONGGXM;
				for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
					if (SettingUtils.changeDeprecatedEnum(commonType,commonType.name())) {
						continue;
					}
					if (commonType.getType().equals("build")) {
						continue;
					}
					if (commonType.getTypeC().equals(list.get(i).getCoordinates().get(0).getElevation().trim())) {
						baseType = commonType;
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
	 * 保存坐标数据
	 * @param list
	 * @param builds
	 * @param project  @return
	 */
	public void saveCoordinate(List<Graph> list, List<Build> builds, Project project) {
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
			if (description != null && !description.trim().equals("")&&((JSONArray) JSONObject.fromObject(jsonCoordinates).get("coordinate")).size()!=0) {
				save(coordinate);
				//		保存筛选出的建筑简单数据
				pointToBuild(entry.getValue(),builds,project,coordinate.getId());
			}
		}
	}

	/**
	 * 保存建筑物简单数据
	 * @param pointList
	 * @param builds
	 * @param project
	 * @param coordinateId
	 */
	private void pointToBuild(List<Graph> pointList, List<Build> builds, Project project, Long coordinateId) {
		List<Build> builds1 = buildGroupService.getBuilds();
		Build build1 = null;
		for (Graph graph : pointList) {
			CoordinateBase coordinateBase = graph.getCoordinates().get(0);
			for (Build build : builds1) {
				if (build.getType().toString().equals(graph.getBaseType().toString())) {
					build1 = (Build) SettingUtils.objectCopy(build);
					break;
				}
			}
			if (graph.getDescription() != null) {
				build1.setRemark(graph.getDescription());
			}else{
			    build1.setRemark(build1.getType().getTypeC());
            }
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("longitude", coordinateBase.getLongitude());
			jsonObject.put("latitude", coordinateBase.getLatitude());
			jsonObject.put("elevation", coordinateBase.getElevation());
			build1.setCenterCoor(jsonObject.toString());
			build1.setProject(project);
			build1.setSource(Build.Source.DESIGN);
			build1.setCoordinateId(coordinateId);
			builds.add(build1);
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
	 * 保存设计的线面坐标数据
	 * @param line
	 * @param projectId
	 * @param description
	 * @return
	 */
	public void saveCoordinateFromPage(Object line, Object projectId, Object description) {
		Coordinate coordinate = new Coordinate();
		JSONObject jsonObject = JSONObject.fromObject(line);
		Project project = projectService.find(Long.valueOf(projectId.toString()));
		coordinate.setCoordinateStr(String.valueOf(jsonObject));
		coordinate.setProject(project);
		coordinate.setDescription(description.toString());
		save(coordinate);
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
	public boolean checkCoordinateFormat(Object line) {
		Map<Object, Object> map = (Map<Object, Object>) line;
		JSONArray coordinate = JSONArray.fromObject(map.get("coordinate"));
		JSONObject jsonObject;
		for (Object o : coordinate) {
			jsonObject = JSONObject.fromObject(o);
			if (jsonObject.size() != 3) {
				return false;
			}
		}
		return true;
	}

    /**
     * 上传坐标文件
     * @param entry
     * @param jsonObject
     * @param project
     * @param central
     * @param wgs84Type
     */
    public void uploadCoordinate(Map.Entry<String, MultipartFile> entry, JSONObject jsonObject, Project project, String central, Coordinate.WGS84Type wgs84Type) {
        MultipartFile mFile = entry.getValue();
        String fileName = mFile.getOriginalFilename();
        // 限制上传文件的大小
        if (mFile.getSize() > CommonAttributes.CONVERT_MAX_SZIE) {
            // return "文件过大无法上传";
            logger.debug("文件过大");
            jsonObject.put(entry.getKey(), Message.Type.FILE_TOO_MAX.getStatus());
            return;
        }
        InputStream is;
        try {
            is = mFile.getInputStream();
        } catch (IOException e) {
			e.printStackTrace();
            logger.info("坐标文件或格式异常");
            jsonObject.put(entry.getKey(),Message.Type.COOR_FORMAT_ERROR.getStatus());
            return;
        }
        String s = fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length());
        try {
			readExcels(is, central, s, project, wgs84Type, fileName, jsonObject);
        } catch (Exception e) {
			e.printStackTrace();
            logger.info("坐标文件或格式异常");
            jsonObject.put(entry.getKey(),Message.Type.COOR_FORMAT_ERROR.getStatus());
            return;
        }finally {
            IOUtils.safeClose(is);
        }
    }
}
