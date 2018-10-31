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
//	public void saveCoordinateFromPage(Object line, Object projectId, Object description) {
//		Coordinate coordinate = new Coordinate();
//		JSONObject jsonObject = JSONObject.fromObject(line);
//		String baseType = jsonObject.get("baseType").toString();
//		Project project = projectService.find(Long.valueOf(projectId.toString()));
//		jsonObject.remove("type");
//		jsonObject.remove("baseType");
//		coordinate.setCoordinateStr(String.valueOf(jsonObject));
//		coordinate.setProject(project);
//		coordinate.setCommonType(CommonEnum.CommonType.valueOf(baseType));
//		coordinate.setDescription(description.toString());
//		save(coordinate);
//	}

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
