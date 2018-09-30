package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.CoordinateDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.buildModel.*;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * 读取excel数据并存储进项目中
 *
 * @author Administrator
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
	private FieldWorkService fieldWorkService;
	private Map<String, PLACache> cache = new HashMap<>();




	/**
	 * 根据项目查找项目下所有坐标数据
	 *
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
	 * 根据项目下坐标求出项目的中央子午线
	 *
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
		int last = meridian.length - 1;
		while (begin <= last) {
			int middle = (begin + last) / 2;
			if (Math.abs(Double.valueOf(str[0]) - meridian[middle]) <= 1.5) {
				return String.valueOf(meridian[middle]);
			} else if ((meridian[middle] - Double.valueOf(str[0])) > 1.5) {
				last = middle - 1;
			} else if ((Double.valueOf(str[0]) - meridian[middle]) > 1.5) {
				begin = middle + 1;
			}
		}
		return null;
	}

	/**
	 * 保存设计的线面坐标数据
	 *
	 * @param line
	 * @param projectId
	 * @param description
	 * @return
	 */
	public void saveCoordinateFromPage(Object line, Object projectId, Object description) {
		Coordinate coordinate = new Coordinate();
		JSONObject jsonObject = JSONObject.fromObject(line);
		String baseType = jsonObject.get("baseType").toString();
		Project project = projectService.find(Long.valueOf(projectId.toString()));
		jsonObject.remove("type");
		jsonObject.remove("baseType");
		coordinate.setCoordinateStr(String.valueOf(jsonObject));
		coordinate.setProject(project);
		coordinate.setCommonType(CommonEnum.CommonType.valueOf(baseType));
		coordinate.setDescription(description.toString());
		save(coordinate);
	}

	public List<Coordinate> findByDate() {
		List<Filter> filters = new ArrayList<>();
		Date newDate = new Date();
		Calendar calendar = Calendar.getInstance();  //得到日历
		calendar.setTime(newDate);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, -365 * 3);  //设置为前一天
		Date dBefore = calendar.getTime();   //得到前一天的时间
		filters.add(Filter.between("createDate", dBefore, newDate));
		List<Coordinate> list = coordinateDao.findList(0, null, filters);
		return list;
	}

	/**
	 * 检查坐标格式
	 *
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
//			if (jsonObject.get("lon") == null || jsonObject.get("lat") == null || jsonObject.get("ele") == null) {
//				return false;
//			}
		}
		return true;
	}

	//////////////////////////////////////////////////重写坐标文件上传///////////////////////////////////////////////

	/**
	 * 分析上传坐标文件是否符合要求
	 *
	 * @param entry
	 * @param jsonObject
	 * @param wbs
	 */
	public void uploadCoordinate(Map.Entry<String, MultipartFile> entry, JSONObject jsonObject, Map<String, Workbook> wbs) {
		MultipartFile mFile = entry.getValue();
		String fileName = mFile.getOriginalFilename();
		// 限制上传文件的大小
		if (mFile.getSize() > CommonAttributes.CONVERT_MAX_SZIE) {
			// return "文件过大无法上传";
			logger.debug("文件过大");
			jsonObject.put(entry.getKey(), "文件过大");
			return;
		}
		InputStream is;
		try {
			is = mFile.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("坐标文件或格式异常");
			jsonObject.put(entry.getKey(), "坐标文件或格式异常");
			return;
		}
		String s = fileName.substring(fileName.lastIndexOf(".") + 1);
		try {
			readExcels(is, s, fileName, jsonObject, wbs);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("坐标文件或格式异常");
			jsonObject.put(entry.getKey(), "坐标文件或格式异常");
			return;
		} finally {
			IOUtils.safeClose(is);
		}
	}

	/**
	 * 尝试使用workbook解析
	 *
	 * @param is
	 * @param s
	 * @param fileName
	 * @param jsonObject
	 * @param wbs
	 * @throws Exception
	 */
	public void readExcels(InputStream is, String s, String fileName, JSONObject jsonObject, Map<String, Workbook> wbs) throws Exception {
		Workbook wb = SettingUtils.readExcel(is, s);
		if (wb == null) {
			jsonObject.put(fileName, "坐标文件或格式异常");
			return;
		}
		wbs.put(fileName,wb);
	}


	/**
	 * 分析出所有的sheet，并分类
	 *
	 * @param wbs
	 * @param sheetObject
	 */
	public void getAllSheet(Map<String, Workbook> wbs, SheetObject sheetObject) {
		List<Sheet> sheets;
		for (Map.Entry<String, Workbook> entry : wbs.entrySet()) {
			for (int i = 0; i < entry.getValue().getNumberOfSheets(); i++) {
				boolean flag = true;
				Sheet sheet = entry.getValue().getSheetAt(i);
				for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
					if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
						continue;
					}
					if (sheet.getSheetName().trim().equals(commonType.getTypeC())) {
						if (commonType.getType().equals("buildModel")) {
//							sheetObject.setBuildSheetList(entry.getKey(), sheet);
							flag = false;
						}
						if (commonType.getType().equals("line")) {
							sheetObject.setLineSheetList(entry.getKey(), sheet);
							flag = false;
						}
						if (commonType.getType().equals("area")) {
							sheetObject.setAreaSheetList(entry.getKey(), sheet);
							flag = false;
						}
					}
				}
				for (Build.ChildType childType : Build.ChildType.values()) {
					if (sheet.getSheetName().trim().equals(childType.getTypeC())) {
						if (childType.getType().equals("buildModel")) {
//							sheetObject.setBuildSheetList(entry.getKey(), sheet);
							flag = false;
						}
					}
				}
				if (flag) {
					sheetObject.setUnknowSheetList(entry.getKey(), sheet);
				}
			}
		}
	}

	/**
	 * 解析sheet表
	 * @param sheetObject
	 * @param central
	 * @param wgs84Type
	 * @param project
	 */
	public JSONObject reslove(SheetObject sheetObject, String central, Coordinate.WGS84Type wgs84Type, Project project) {
		CoordinateMap coordinateMap = new CoordinateMap();
		inputExcel(coordinateMap, sheetObject,project);
//		数据分析
		String code = transFromService.checkCode84(central);
		dataAnalysis(coordinateMap,code,wgs84Type,project);
		makeRadomStringAndAddCache(coordinateMap);
		if (coordinateMapAllTrue(coordinateMap)) {
//			saveCoordinateMap(coordinateMap,project);
			return null;
		}
		return msg(coordinateMap,project);
	}

//	private void saveCoordinateMap(CoordinateMap coordinateMap, Project project) {
//		List<Coordinate> coordinates = findByProject(project);
//		List<Build> buildModel = buildService.findByProjectAndSource(project, Build.Source.DESIGN);
//		List<Build> builds1;
//		List<Coordinate> coordinates1;
//		for (Map.Entry<String, List<CoordinateObject>> entry : coordinateMap.getLineMap().entrySet()) {
//			for (CoordinateObject coordinateObject : entry.getValue()) {
//				Object object = cache.get(coordinateObject.getNoticeStr()).getObject();
//				cache.remove(coordinateObject.getNoticeStr());
//				coordinates1 = new ArrayList<>();
//				saveCoordinate((Coordinate) object,coordinates,coordinates1);
//				coordinates.addAll(coordinates1);
//			}
//		}
//		for (Map.Entry<String, List<CoordinateObject>> entry : coordinateMap.getAreaMap().entrySet()) {
//			for (CoordinateObject coordinateObject : entry.getValue()) {
//				Object object = cache.get(coordinateObject.getNoticeStr()).getObject();
//				cache.remove(coordinateObject.getNoticeStr());
//				coordinates1 = new ArrayList<>();
//				saveCoordinate((Coordinate) object,coordinates,coordinates1);
//				coordinates.addAll(coordinates1);
//			}
//		}
//		for (Map.Entry<String, List<Build>> entry : coordinateMap.getBuildMap().entrySet()) {
//			for (Build build : entry.getValue()) {
//				Object object = cache.get(build.getNoticeStr()).getObject();
//				cache.remove(build.getNoticeStr());
//				builds1 = new ArrayList<>();
//				buildService.saveBuild((Build)object,buildModel,coordinates,builds1);
//				buildModel.addAll(builds1);
//			}
//		}
//		for (Map.Entry<String, List<Build>> entry : coordinateMap.getSimpleBuildMap().entrySet()) {
//			for (Build build : entry.getValue()) {
//				Object object = cache.get(build.getNoticeStr()).getObject();
//				cache.remove(build.getNoticeStr());
//				builds1 = new ArrayList<>();
//				buildService.saveBuild((Build)object,buildModel,coordinates,builds1);
//				buildModel.addAll(builds1);
//			}
//		}
//	}

	private void saveCoordinate(Coordinate coordinate, List<Coordinate> coordinates, List<Coordinate> coordinates1) {
		if (coordinates.size() == 0) {
			save(coordinate);
			coordinates1.add(coordinate);
			return;
		}
		boolean flag = true;
		for (Coordinate coordinate1 : coordinates) {
			if (coordinate1.getDescription().equals(coordinate.getDescription())) {
				coordinate1.setCoordinateStr(coordinate.getCoordinateStr());
				coordinate1.setCommonType(coordinate.getCommonType());
				save(coordinate1);
				flag = false;
				break;
			}
		}
		if (flag) {
			save(coordinate);
			coordinates1.add(coordinate);
		}
	}

	private boolean coordinateMapAllTrue(CoordinateMap coordinateMap) {
////		for (Map.Entry<String, List<Build>> entry : coordinateMap.getBuildMap().entrySet()) {
//			for (Build build : entry.getValue()) {
//				if (build.isErrorMsg()) {
//					return false;
//				}
//			}
//		}
//		for (Map.Entry<String, List<CoordinateObject>> entry : coordinateMap.getLineMap().entrySet()) {
//			for (CoordinateObject coordinateObject : entry.getValue()) {
//				if (coordinateObject.getErrorMsg()) {
//					return false;
//				}
//			}
//		}
//		for (Map.Entry<String, List<CoordinateObject>> entry : coordinateMap.getAreaMap().entrySet()) {
//			for (CoordinateObject coordinateObject : entry.getValue()) {
//				if (coordinateObject.getErrorMsg()) {
//					return false;
//				}
//			}
//		}
//		for (Map.Entry<String, List<Build>> entry : coordinateMap.getSimpleBuildMap().entrySet()) {
//			for (Build build : entry.getValue()) {
//				if (build.isErrorMsg()) {
//					return false;
//				}
//			}
//		}
		return true;
	}

	private JSONObject msg(CoordinateMap coordinateMap, Project project) {
		List<Msg> msgs = new ArrayList<>();
//		lineMsg(coordinateMap.getLineMap(),msgs);
//		areaMsg(coordinateMap.getAreaMap(), msgs);
//		buildMsg(coordinateMap.getBuildMap(),msgs);
//		simpleBuildMsg(coordinateMap.getSimpleBuildMap(), msgs);
		return msgsToJson(msgs,project);
	}

	private JSONObject msgsToJson(List<Msg> msgs, Project project) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (Msg msg : msgs) {
			jsonArray.add(msgToJson(msg));
		}
		jsonObject.put("msg", jsonArray);
		jsonObject.put("projectId", project.getId());
		return jsonObject;
	}

	private JSONObject msgToJson(Msg msg) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject(),jsonObject1 = null;
		jsonObject.put("fileName",msg.getFileName());
		jsonObject.put("sheetName",msg.getSheetName());
		jsonObject.put("noticeStr",msg.getNoticeStr());
		for (Msg.ErrorMsgAndRow errorMsgAndRow : msg.getErrorMsgAndRows()) {
			jsonObject1 = new JSONObject();
			jsonObject1.put("errorMsg", errorMsgAndRow.getErrorMsg());
			jsonObject1.put("row", errorMsgAndRow.getRow());
			jsonArray.add(jsonObject1);
		}
		jsonObject.put("errorMsgAndRows", jsonArray);
		return jsonObject;
	}

	private void simpleBuildMsg(Map<String, List<Build>> simpleBuildMap, List<Msg> msgs) {
//		buildMsg(simpleBuildMap, msgs);
	}

	private void areaMsg(Map<String, List<CoordinateObject>> areaMap, List<Msg> msgs) {
		lineMsg(areaMap, msgs);
	}

	private List<Msg> lineMsg(Map<String, List<CoordinateObject>> lineMap, List<Msg> msgs) {
		Msg msg;
		for (Map.Entry<String, List<CoordinateObject>> entry : lineMap.entrySet()) {
			for (CoordinateObject coordinateObject : entry.getValue()) {
				msg = new Msg(entry.getKey(), coordinateObject.getName(),coordinateObject.getNoticeStr());
				for (Map.Entry<Integer, String> stringEntry : coordinateObject.getErrorMsgInfo().entrySet()) {
					msg.setErrorMsgAndRows(stringEntry.getValue(), stringEntry.getKey());
				}
				msgs.add(msg);
			}
		}
		return msgs;
	}

//	private void buildMsg(Map<String, List<Build>> buildMap, List<Msg> msgs) {
//		Msg msg = null;
//		for (Map.Entry<String, List<Build>> entry : buildMap.entrySet()) {
//			for (Build build : entry.getValue()) {
//				if (build.getChildType() != null) {
//					msg = new Msg(entry.getKey(), build.getChildType().getTypeC(),build.getNoticeStr());
//					for (Map.Entry<Integer, String> stringEntry : build.getErrorMsgInfo().entrySet()) {
//						msg.setErrorMsgAndRows(stringEntry.getValue(), stringEntry.getKey());
//					}
//					msgs.add(msg);
//				}else{
//					msg = new Msg(entry.getKey(), build.getType().getTypeC(),build.getNoticeStr());
//					for (Map.Entry<Integer, String> stringEntry : build.getErrorMsgInfo().entrySet()) {
//						msg.setErrorMsgAndRows(stringEntry.getValue(), stringEntry.getKey());
//					}
//					msgs.add(msg);
//				}
//			}
//		}
//	}

	/**
	 * 生成随机串并加入缓存
	 * @param coordinateMap
	 */
	private void makeRadomStringAndAddCache(CoordinateMap coordinateMap) {
//		buildRadomAndAddCache(coordinateMap.getBuildMap());
//		lineRadomAndAddCache(coordinateMap.getLineMap());
//		areaRadomAndAddCache(coordinateMap.getAreaMap());
//		simpleBuildRadomAndAddCache(coordinateMap.getSimpleBuildMap());
	}

	private void simpleBuildRadomAndAddCache(Map<String, List<Build>> simpleBuildMap) {
		buildRadomAndAddCache(simpleBuildMap);
	}

	private void areaRadomAndAddCache(Map<String, List<CoordinateObject>> areaMap) {
		lineRadomAndAddCache(areaMap);
	}

	private void lineRadomAndAddCache(Map<String, List<CoordinateObject>> lineMap) {
		JSONArray jsonArray;
		JSONObject jsonObject;
		for (Map.Entry<String, List<CoordinateObject>> entry : lineMap.entrySet()) {
			for (CoordinateObject coordinateObject : entry.getValue()) {
				coordinateObject.setNoticeStr(UUID.randomUUID().toString().replaceAll("-",""));
				Coordinate coordinate = new Coordinate();
				jsonArray = new JSONArray();
				for (CoordinateBase1 coordinateBase1 : coordinateObject.getCoordinateBase1s()) {
					jsonObject = new JSONObject();
					jsonObject.put("lon", coordinateBase1.getLon().getValue());
					jsonObject.put("lat", coordinateBase1.getLat().getValue());
					jsonObject.put("ele", coordinateBase1.getEle().getValue());
					jsonArray.add(jsonObject);
				}
				jsonObject = new JSONObject();
				jsonObject.put("coordinate", jsonArray);
				coordinate.setCoordinateStr(jsonObject.toString());
				for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
					if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
						continue;
					}
					if (commonType.getTypeC().equals(coordinateObject.getName())) {
						coordinate.setCommonType(commonType);
						break;
					}
				}
				coordinate.setDescription(coordinateObject.getRemark());
				coordinate.setProject(coordinateObject.getProject());
				coordinate.setErrorMsg(coordinateObject.getErrorMsg());
//				PLACache plaCache = new PLACache(new Date(),coordinate);
//				cache.put(coordinateObject.getNoticeStr(), plaCache);
			}
		}
	}

	private void buildRadomAndAddCache(Map<String, List<Build>> buildMap) {
		for (Map.Entry<String, List<Build>> entry : buildMap.entrySet()) {
			for (Build build : entry.getValue()) {
				build.setNoticeStr(UUID.randomUUID().toString().replaceAll("-",""));
//				PLACache plaCache = new PLACache(new Date(),build);
//				cache.put(build.getNoticeStr(), plaCache);
			}
		}
	}


	/**
	 * 错误处理
	 * @param unknowWBs
	 */
	public JSONArray errorMsg(Map<String, List<Sheet>> unknowWBs) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;
		StringBuffer sb = null;
		for (Map.Entry<String, List<Sheet>> entry : unknowWBs.entrySet()) {
			List<Sheet> sheets = entry.getValue();
			sb = new StringBuffer("sheet表：");
			for (Sheet sheet : sheets) {
				sb.append(sheet.getSheetName());
				sb.append("，");
			}
			sb.replace(sb.length() - 1, sb.length(), "：");
			sb.append("未知类型");
			jsonObject = new JSONObject();
			jsonObject.put(entry.getKey(), sb.toString());
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}

	/**
	 * 数据分析
	 * @param coordinateMap
	 * @param code
	 * @param wgs84Type
	 * @param project
	 */
	private void dataAnalysis(CoordinateMap coordinateMap, String code, Coordinate.WGS84Type wgs84Type, Project project) {
//		数据有效性检查
		dataValidityCheck(coordinateMap,code,wgs84Type,project);
	}

	/**
	 * 数据有效性检查
	 * @param coordinateMap
	 * @param code
	 * @param wgs84Type
	 * @param project
	 */
	private void dataValidityCheck(CoordinateMap coordinateMap, String code, Coordinate.WGS84Type wgs84Type, Project project) {
		dataValidityCheckBuild(coordinateMap,code,wgs84Type);
//		dataValidityCheckLine(coordinateMap,code,wgs84Type,project);
//		dataValidityCheckArea(coordinateMap,code,wgs84Type);
	}

//	/**
//	 * 检查面类型数据有效性
//	 * @param coordinateMap
//	 * @param code
//	 * @param wgs84Type
//	 */
//	private void dataValidityCheckArea(CoordinateMap coordinateMap, String code, Coordinate.WGS84Type wgs84Type) {
//		for (Map.Entry<String, List<CoordinateObject>> entry : coordinateMap.getAreaMap().entrySet()) {
//			for (CoordinateObject coordinateObject : entry.getValue()) {
//				for (CoordinateBase1 coordinateBase1 : coordinateObject.getCoordinateBase1s()) {
////					坐标格式转换
//					formatConversion(coordinateObject, coordinateBase1, code, wgs84Type);
//				}
//			}
//		}
//	}

//	/**
//	 * 检查线类型数据有效性并检出简单建筑物
//	 * @param coordinateMap
//	 * @param code
//	 * @param wgs84Type
//	 * @param project
//	 */
//	private void dataValidityCheckLine(CoordinateMap coordinateMap, String code, Coordinate.WGS84Type wgs84Type, Project project) {
//		Map<String, List<Build>> simpleBuildMap = new HashMap<>();
//		for (Map.Entry<String, List<CoordinateObject>> entry : coordinateMap.getLineMap().entrySet()) {
//			for (CoordinateObject coordinateObject : entry.getValue()) {
//				for (CoordinateBase1 coordinateBase1 : coordinateObject.getCoordinateBase1s()) {
////					坐标格式转换
//					formatConversion(coordinateObject, coordinateBase1, code, wgs84Type);
////					构建简单建筑物
//					makeSimpleBuild(entry,simpleBuildMap,coordinateObject,coordinateBase1,project);
//				}
//			}
//		}
//		coordinateMap.setSimpleBuildMap(simpleBuildMap);
//	}

	private void makeSimpleBuild(Map.Entry<String, List<CoordinateObject>> entry, Map<String, List<Build>> simpleBuildMap, CoordinateObject coordinateObject, CoordinateBase1 coordinateBase1, Project project) {
		List<Build> builds;
		Build build;
		JSONObject jsonObject;
		if (coordinateBase1.getType() != null && coordinateBase1.getType().getValue() != null && !coordinateBase1.getType().getValue().equals("")) {
			jsonObject = new JSONObject();
			jsonObject.put("lon", coordinateBase1.getLon().getValue());
			jsonObject.put("lat", coordinateBase1.getLat().getValue());
			jsonObject.put("ele", coordinateBase1.getEle().getValue());
			if (simpleBuildMap.get(entry.getKey()) == null) {
				build = buildService.pickedBuild(coordinateBase1.getType().getValue().trim());
				if (build == null) {
					coordinateBase1.setTypeErrorMsgTrue();
					coordinateObject.setErrorMsgTrue();
					coordinateObject.setErrorMsgInfo(coordinateBase1.getNum(),coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())==null?"建筑物类型未知":(coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())+",建筑物类型未知"));
					return;
				}
				if (coordinateBase1.getLon().getErrorMsg() || coordinateBase1.getLat().getErrorMsg() || coordinateBase1.getEle().getErrorMsg()) {
					build.setErrorMsgTrue();
					coordinateObject.setErrorMsgInfo(coordinateBase1.getNum(), coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())==null?"建筑物坐标格式错误":(coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())+",建筑物坐标格式错误"));
				}
				build.setCenterCoor(jsonObject.toString());
				build.setProject(project);
				build.setRemark(coordinateBase1.getDescription()==null?null:coordinateBase1.getDescription().getValue());
				builds = new ArrayList<>();
				builds.add(build);
				simpleBuildMap.put(entry.getKey(), builds);
			} else {
				build = buildService.pickedBuild(coordinateBase1.getType().getValue().trim());
				if (build == null) {
					coordinateBase1.setTypeErrorMsgTrue();
					coordinateObject.setErrorMsgTrue();
					coordinateObject.setErrorMsgInfo(coordinateBase1.getNum(),coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())==null?"建筑物类型未知":(coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())+",建筑物类型未知"));
					return;
				}
				if (coordinateBase1.getLon().getErrorMsg() || coordinateBase1.getLat().getErrorMsg() || coordinateBase1.getEle().getErrorMsg()) {
					build.setErrorMsgTrue();
					coordinateObject.setErrorMsgInfo(coordinateBase1.getNum(), coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())==null?"建筑物坐标格式错误":(coordinateObject.getErrorMsgInfo().get(coordinateBase1.getNum())+",建筑物坐标格式错误"));
				}
				build.setCenterCoor(jsonObject.toString());
				build.setProject(project);
				build.setRemark(coordinateBase1.getDescription().getValue());
				builds = simpleBuildMap.get(entry.getKey());
				builds.add(build);
				simpleBuildMap.put(entry.getKey(), builds);
			}
		}
	}

	/**
	 * 数据格式转换
	 * @param coordinateObject
	 * @param coordinateBase1
	 * @param code
	 * @param wgs84Type
	 */
	private void formatConversion(CoordinateObject coordinateObject, CoordinateBase1 coordinateBase1, String code, Coordinate.WGS84Type wgs84Type) {
		JSONObject jsonObject = null;
		try {
//			jsonObject = coordinateXYZToBLH(coordinateBase1.getLon().getValue(), coordinateBase1.getLat().getValue(), code, wgs84Type);
		} catch (Exception e) {
			coordinateBase1.setLonErrorMsgTrue();
			coordinateBase1.setLatErrorMsgTrue();
			coordinateObject.setErrorMsgTrue();
			coordinateObject.setErrorMsgInfo(coordinateBase1.getNum(), "坐标格式错误");
			return;
		}
		if (jsonObject == null) {
			coordinateBase1.setLonErrorMsgTrue();
			coordinateBase1.setLatErrorMsgTrue();
			coordinateObject.setErrorMsgTrue();
			coordinateObject.setErrorMsgInfo(coordinateBase1.getNum(), "平面坐标转换失败");
			return;
		}
		coordinateBase1.setLon(jsonObject.get("lon").toString());
		coordinateBase1.setLat(jsonObject.get("lat").toString());
	}

	/**
	 * 检查build数据有效性
	 * @param coordinateMap
	 * @param code
	 * @param wgs84Type
	 */
	private void dataValidityCheckBuild(CoordinateMap coordinateMap, String code, Coordinate.WGS84Type wgs84Type) {
//		for (Map.Entry<String, List<Build>> entry : coordinateMap.getBuildMap().entrySet()) {
//			List<Build> buildModel = entry.getValue();
//			Iterator<Build> it = buildModel.iterator();
			Build build;
//			while (it.hasNext()) {
//				build = it.next();
//				if (build.getCenterCoorNum() == null || build.getPositionCoorNum() == null || build.getDesignElevationNum() == null || build.getRemarkNum() == null) {
//					build.setErrorMsgInfo(0,"sheet模板被修改，无法读取");
//					build.setErrorMsgTrue();
//					continue;
//				}
//				if ((build.getCenterCoor() == null || build.getCenterCoor().equals("")) && (build.getDesignElevation() == null || build.getDesignElevation().equals("")) && (build.getRemark() == null || build.getRemark().equals(""))) {
//					it.remove();
//				}
//				checkCenterCoordinateValueFormat(build,code,wgs84Type);
//				checkPositionCoordinateValueFormat(build,code,wgs84Type);
//				if (build.getDesignElevation() == null || build.getDesignElevation().equals("")) {
//					build.setDesignElevation(null);
//					build.setErrorMsgInfo(build.getDesignElevationNum(), "设计标高为空");
//					build.setErrorMsgTrue();
//				}
//				if (build.getRemark() == null || build.getRemark().equals("")) {
//					build.setRemark(null);
//					build.setErrorMsgInfo(build.getRemarkNum(), "描述为空");
//					build.setErrorMsgTrue();
//				}
//			}
//		}
	}

	private void checkPositionCoordinateValueFormat(Build build, String code, Coordinate.WGS84Type wgs84Type) {
		JSONObject jsonObject,jsonObject1;
		if (build.getPositionCoor() == null||build.getPositionCoor().equals("")) {
			build.setPositionCoor(null);
			return;
		}
		String[] split = build.getPositionCoor().split(",");
		jsonObject = new JSONObject();
		jsonObject.put("lon", split[0]);
		jsonObject.put("lat", split[1]);
		jsonObject.put("ele", split[2]);
		try {
//			jsonObject1 = coordinateXYZToBLH(jsonObject.get("lon").toString(), jsonObject.get("lat").toString(), code, wgs84Type);
		} catch (Exception e) {
			build.setErrorMsgTrue();
//			build.setErrorMsgInfo(build.getPositionCoorNum(), "坐标格式错误");
			return;
		}
//		if (jsonObject1 == null) {
//			build.setErrorMsgTrue();
//			build.setErrorMsgInfo(build.getPositionCoorNum(), "平面坐标转换失败");
//			return;
//		}
//		jsonObject1.put("ele", jsonObject.get("ele"));
//		build.setPositionCoor(jsonObject1.toString());
	}

	private void checkCenterCoordinateValueFormat(Build build, String code, Coordinate.WGS84Type wgs84Type) {
		JSONObject jsonObject,jsonObject1;
		if (build.getCenterCoor() == null || build.getCenterCoor().equals("")) {
			build.setCenterCoor(null);
			build.setErrorMsgTrue();
//			build.setErrorMsgInfo(build.getCenterCoorNum(), "中心坐标为空");
			return;
		}
		String[] split = build.getCenterCoor().split(",");
		jsonObject = new JSONObject();
		jsonObject.put("lon", split[0]);
		jsonObject.put("lat", split[1]);
		jsonObject.put("ele", split[2]);
		try {
//			jsonObject1 = coordinateXYZToBLH(jsonObject.get("lon").toString(), jsonObject.get("lat").toString(), code, wgs84Type);
		} catch (Exception e) {
			build.setErrorMsgTrue();
//			build.setErrorMsgInfo(build.getCenterCoorNum(), "坐标格式错误");
			return;
		}
//		if (jsonObject1 == null) {
//			build.setErrorMsgTrue();
//			build.setErrorMsgInfo(build.getCenterCoorNum(), "平面坐标转换失败");
//			return;
//		}
//		jsonObject1.put("ele", jsonObject.get("ele"));
//		build.setCenterCoor(jsonObject1.toString());
	}

	/**
	 * 导入excel
	 * @param coordinateMap
	 * @param sheetObject
	 * @param project
	 */
	private void inputExcel(CoordinateMap coordinateMap, SheetObject sheetObject, Project project) {
//		coordinateMap.setBuildMap(buildService.inputBuilds(sheetObject.getBuildWBs(),project));
//		coordinateMap.setLineMap(inputLine(sheetObject.getLineWBs(),project));
//		coordinateMap.setAreaMap(inputArea(sheetObject.getAreaWBs(),project));
	}

//	/**
//	 * 导入面
//	 * @param areaWBs
//	 */
//	private Map<String, List<CoordinateObject>> inputArea(Map<String, List<Sheet>> areaWBs,Project project) {
//		return inputLine(areaWBs,project);
//	}

//	/**
//	 * 导入线
//	 * @param lineWBs
//	 */
//	private Map<String, List<CoordinateObject>> inputLine(Map<String, List<Sheet>> lineWBs,Project project) {
//		Row row;
//		String a = null,b = null;
//		Map<String, List<CoordinateObject>> lineMap = new HashMap<>();
//		List<CoordinateBase1> coordinateBase1s = null;
//		CoordinateBase1 coordinateBase1;
//		List<CoordinateObject> jsonObjects;
//		CoordinateObject coordinateObject = null;
//		for (Map.Entry<String, List<Sheet>> entry : lineWBs.entrySet()) {
//			for (Sheet sheet : entry.getValue()) {
//				if (sheet == null) {
//					continue;
//				}
//				for (int j = 0; j <= sheet.getLastRowNum(); j++) {
//					row = sheet.getRow(j);
//					if (row == null) {
//						continue;
//					}
//					if (row.getCell(0) != null) {
//						row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
//						a = row.getCell(0).getStringCellValue();
//					}
//					if (row.getCell(1) != null) {
//						row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
//						b = row.getCell(1).getStringCellValue();
//					}
//					if (a.trim().equals("描述")) {
//						if (j == 0) {
//							coordinateObject = new CoordinateObject();
//							coordinateObject.setRemark(b.trim());
//							coordinateObject.setName(sheet.getSheetName().trim());
//							coordinateObject.setProject(project);
//							coordinateBase1s = new ArrayList<>();
//							continue;
//						}else{
//							coordinateObject.setCoordinateBase1s(coordinateBase1s);
//							if (lineMap.get(entry.getKey()) == null) {
//								jsonObjects = new ArrayList<>();
//								jsonObjects.add(coordinateObject);
//								lineMap.put(entry.getKey(), jsonObjects);
//							} else {
//								jsonObjects = lineMap.get(entry.getKey());
//								jsonObjects.add(coordinateObject);
//								lineMap.put(entry.getKey(), jsonObjects);
//							}
//							coordinateObject = new CoordinateObject();
//							coordinateObject.setRemark(b.trim());
//							coordinateObject.setName(sheet.getSheetName().trim());
//							coordinateObject.setProject(project);
//							coordinateBase1s = new ArrayList<>();
//							continue;
//						}
//					}
//					if (a.trim().equals("")||a.trim().equals("经度")) {
//						continue;
//					}
//					coordinateBase1 = new CoordinateBase1();
//					coordinateBase1.setLon(a);
//					if (row.getCell(1) != null) {
//						row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
//						coordinateBase1.setLat(row.getCell(1).getStringCellValue().equals("")?null:row.getCell(1).getStringCellValue());
//					}
//					if (row.getCell(2) != null) {
//						row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
//						coordinateBase1.setEle(row.getCell(2).getStringCellValue().equals("")?null:row.getCell(2).getStringCellValue());
//					}
//					if (row.getCell(3) != null) {
//						row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
//						coordinateBase1.setType(row.getCell(3).getStringCellValue().equals("")?null:row.getCell(3).getStringCellValue());
//					}
//					if (row.getCell(4) != null) {
//						row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
//						coordinateBase1.setDescription(row.getCell(4).getStringCellValue().equals("")?null:row.getCell(4).getStringCellValue());
//					}
//					coordinateBase1.setNum(j+1);
//					coordinateBase1s.add(coordinateBase1);
//					if (j == sheet.getLastRowNum()) {
//						coordinateObject.setCoordinateBase1s(coordinateBase1s);
//						if (lineMap.get(entry.getKey()) == null) {
//							jsonObjects = new ArrayList<>();
//							jsonObjects.add(coordinateObject);
//							lineMap.put(entry.getKey(), jsonObjects);
//						} else {
//							jsonObjects = lineMap.get(entry.getKey());
//							jsonObjects.add(coordinateObject);
//							lineMap.put(entry.getKey(), jsonObjects);
//						}
//					}
//				}
//			}
//		}
//		return lineMap;
//	}

	/**
	 * 删除cache缓存中时间超多3小时的记录
	 */
	public void deteteCache() {
		Iterator<Map.Entry<String, PLACache>> iterator = cache.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, PLACache> next = iterator.next();
			PLACache value = next.getValue();
//			if (value.getDate().getTime() + 3 * 3600 * 1000l < System.currentTimeMillis()) {
//				iterator.remove();
//			}
		}
	}

	public void saveObject(String noticeStr, List<Build> builds, List<Coordinate> coordinates) {
		List<Coordinate> coordinates1;
		List<Build> builds1;
		PLACache plaCache = cache.get(noticeStr);
//		Object object = plaCache.getObject();
//		if (object instanceof Coordinate) {
//			cache.remove(noticeStr);
//			if (((Coordinate) object).getErrorMsg()) {
//				return;
//			}
//			coordinates1 = new ArrayList<>();
//			saveCoordinate((Coordinate) object,coordinates,coordinates1);
//			coordinates.addAll(coordinates1);
//		} else if (object instanceof Build) {
//			cache.remove(noticeStr);
//			if (((Build) object).isErrorMsg()) {
//				return;
//			}
//			builds1 = new ArrayList<>();
//			buildService.saveBuild((Build)object,buildModel,coordinates,builds1);
//			buildModel.addAll(builds1);
//		}
	}

	public JSONArray removeNoticeStr(Object msg) {
		JSONArray jsonArray = JSONArray.fromObject(msg),jsonArray1;
		Iterator iterator = jsonArray.iterator();
		while (iterator.hasNext()) {
			Object o = iterator.next();
			JSONObject jsonObject = (JSONObject) o;
			jsonObject.remove("noticeStr");
			jsonArray1= (JSONArray) jsonObject.get("errorMsgAndRows");
			if (jsonArray1.isEmpty()) {
				iterator.remove();
			}
		}
		return jsonArray;
	}

	void writeToCell(int i, Sheet sheet, Row row, Cell cell, CellStyle style, String a, String b, String c, String d, String e, String type, List<String> selects, Workbook wb, boolean locked) {
		row = sheet.createRow(i);
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
		if (d != null) {
			cell.setCellValue(d);
		}
		cell.setCellStyle(style);
		cell = row.createCell(4);
		if (e != null) {
			cell.setCellValue(e);
		}
		cell.setCellStyle(style);
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		if (type != null) {
			buildService.setXSSFValidation1((XSSFSheet) sheet, selects.toArray(new String[selects.size()]), i, i, 3, 3);
		}
	}
}
