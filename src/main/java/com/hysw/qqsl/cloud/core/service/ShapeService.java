package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.ShapeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.buildModel.*;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.osgeo.proj4j.ProjCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 外业测量service
 * Created by chenl on 17-4-13.
 */
@Service("shapeService")
public class ShapeService extends BaseService<Shape, Long> {
    private static final long serialVersionUID = -9100968677794664521L;
    @Autowired
    private ShapeDao shapeDao;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private TransFromService transFromService;
    @Autowired
    private ElementDBService elementDBService;
    @Autowired
    private LineSectionPlaneModelService lineSectionPlaneModelService;
    @Autowired
    private LineService lineService;
    @Autowired
    private ShapeAttributeService shapeAttributeService;
    @Autowired
    private ShapeCoordinateService shapeCoordinateService;
    @Autowired
    public void setBaseDao(ShapeDao shapeDao) {
        super.setBaseDao(shapeDao);
    }

    Log logger = LogFactory.getLog(getClass());

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

    public void uploadCoordinate(MultipartFile mFile, JSONObject jsonObject, Map<String, Workbook> wbs) {
        String fileName = mFile.getOriginalFilename();
        // 限制上传文件的大小
        if (mFile.getSize() > CommonAttributes.CONVERT_MAX_SZIE) {
            // return "文件过大无法上传";
            logger.debug("文件过大");
            jsonObject.put(fileName, "文件过大");
            return;
        }
        InputStream is;
        try {
            is = mFile.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("坐标文件或格式异常");
            jsonObject.put(fileName, "坐标文件或格式异常");
            return;
        }
        String s = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
            readExcels(is, s, fileName, jsonObject, wbs);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("坐标文件或格式异常");
            jsonObject.put(fileName, "坐标文件或格式异常");
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
        for (Map.Entry<String, Workbook> entry : wbs.entrySet()) {
            for (int i = 0; i < entry.getValue().getNumberOfSheets(); i++) {
                boolean flag = true;
                Sheet sheet = entry.getValue().getSheetAt(i);
                for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
                    if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                        continue;
                    }
                    if (sheet.getSheetName().trim().equals(commonType.getTypeC())) {
                        if (commonType.getType().equals("line")) {
                            sheetObject.setLineSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                        if (commonType.getType().equals("area")) {
                            sheetObject.setAreaSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                        if (commonType.getType().equals("buildModel")) {
                            sheetObject.setBuildSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                    }
                }
                for (LineSectionPlaneModel.Type type : LineSectionPlaneModel.Type.values()) {
                    if (sheet.getSheetName().trim().equals(type.getTypeC())) {
                        if (type.getType().equals("sectionPlane")) {
                            sheetObject.setScetionPlaneModelList(entry.getKey(), sheet);
                            flag = false;
                        }
                    }
                }
                for (Build.ChildType childType : Build.ChildType.values()) {
                    if (sheet.getSheetName().trim().equals(childType.getTypeC())) {
                        if (childType.getType().equals("buildModel")) {
							sheetObject.setBuildSheetList(entry.getKey(), sheet);
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
    public PLACache reslove(SheetObject sheetObject, String central, Coordinate.WGS84Type wgs84Type, Project project) {
        CoordinateMap coordinateMap = new CoordinateMap();
        inputExcel(coordinateMap, sheetObject,project);
//		数据分析
        String code = transFromService.checkCode84(central);
        PLACache plaCache = new PLACache();
        makePLA(coordinateMap, code, wgs84Type, project, plaCache);
        if (isAllFalse(plaCache)) {
            saveEvery(plaCache);
            return null;
        }
        return plaCache;
    }

    public JSONArray pickedErrorMsg(PLACache plaCache) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        if (plaCache.getLineShape() != null) {
            for (Map.Entry<String, List<Shape>> entry : plaCache.getLineShape().entrySet()) {
                for (Shape shape : entry.getValue()) {
                    for (ShapeCoordinate shapeCoordinate : shape.getShapeCoordinates()) {
                        if (shapeCoordinate.isErrorMsg()) {
                            jsonObject = new JSONObject();
                            jsonObject.put("rowNum", shapeCoordinate.getCellNum());
                            jsonObject.put("sheetName", shapeCoordinate.getShape().getCommonType().getTypeC());
                            jsonObject.put("excel", entry.getKey());
                            jsonArray.add(jsonObject);
                        }
                    }
                }
            }
        }
        if (plaCache.getAreaShape() != null) {
            for (Map.Entry<String, List<Shape>> entry : plaCache.getAreaShape().entrySet()) {
                for (Shape shape : entry.getValue()) {
                    for (ShapeCoordinate shapeCoordinate : shape.getShapeCoordinates()) {
                        if (shapeCoordinate.isErrorMsg()) {
                            jsonObject = new JSONObject();
                            jsonObject.put("rowNum", shapeCoordinate.getCellNum());
                            jsonObject.put("sheetName", shapeCoordinate.getShape().getRemark());
                            jsonObject.put("excel", entry.getKey());
                            jsonArray.add(jsonObject);
                        }
                    }
                }
            }
        }
        return jsonArray;
    }

    public Shape findByProjectAndRemark(Project project, String remark) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        filters.add(Filter.eq("remark", remark));
        List<Shape> list = shapeDao.findList(0, null, filters);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 保存所有建筑物
     * @param plaCache
     */
    private void saveEvery(PLACache plaCache) {
        Shape shape1;
        if (plaCache.getLineShape() != null) {
            for (Map.Entry<String, List<Shape>> entry : plaCache.getLineShape().entrySet()) {
                for (Shape shape : entry.getValue()) {
                    shape1 = findByProjectAndRemark(shape.getProject(), shape.getRemark());
                    if (shape1 == null) {
                        save(shape);
                    } else {
                        remove(shape1);
                        save(shape);
                    }
                }
            }
        }
        if (plaCache.getAreaShape() != null) {
            for (Map.Entry<String, List<Shape>> entry : plaCache.getAreaShape().entrySet()) {
                for (Shape shape : entry.getValue()) {
                    shape1 = findByProjectAndRemark(shape.getProject(), shape.getRemark());
                    if (shape1 == null) {
                        save(shape);
                    } else {
                        remove(shape1);
                        save(shape);
                    }
                }
            }
        }
        if (plaCache.getBuildsMap() != null) {
            for (Map.Entry<String, List<Build>> entry : plaCache.getBuildsMap().entrySet()) {
                for (Build build : entry.getValue()) {
                    buildService.save(build);
                }
            }
        }
    }

    /**
     * 是否所有建筑物都没有报错
     * @param plaCache
     * @return
     */
    private boolean isAllFalse(PLACache plaCache) {
        if (plaCache.getLineShape() != null) {
            for (Map.Entry<String, List<Shape>> entry : plaCache.getLineShape().entrySet()) {
                for (Shape shape : entry.getValue()) {
                    if (shape.isErrorMsg()) {
                        return false;
                    }
                }
            }
        }
        if (plaCache.getAreaShape() != null) {
            for (Map.Entry<String, List<Shape>> entry : plaCache.getAreaShape().entrySet()) {
                for (Shape shape : entry.getValue()) {
                    if (shape.isErrorMsg()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void makePLA(CoordinateMap coordinateMap, String code, Coordinate.WGS84Type wgs84Type, Project project, PLACache plaCache) {
        makeLineShape(coordinateMap.getLineMap(), code, wgs84Type, project, plaCache);
        makeAreaShape(coordinateMap.getAreaMap(), code, wgs84Type, project, plaCache);
    }

    private void makeAreaShape(Map<String, List<ShapeCache>> areaMap, String code, Coordinate.WGS84Type wgs84Type, Project project, PLACache plaCache) {
        List<Line> lines = lineService.getLines();
        Elevation elevation;
        Line line1 = null;
        Shape shape;
        JSONObject jsonObject = null;
        ShapeCoordinate shapeCoordinate;
        List<Shape> shapes = new LinkedList<>();
        List<ShapeCoordinate> shapeCoordinates;
        for (Map.Entry<String, List<ShapeCache>> entry : areaMap.entrySet()) {
            for (ShapeCache shapeCache : entry.getValue()) {
                for (Line line : lines) {
                    if (line.getCommonType().getTypeC().equals(shapeCache.getName())) {
                        line1 = (Line) SettingUtils.objectCopy(line);
                        break;
                    }
                }
                shape = new Shape();
                shape.setCommonType(line1.getCommonType());
                shape.setProject(project);
                shape.setRemark(shapeCache.getRemark());
                shapeCoordinates = new LinkedList<>();
                for (List<String> list : shapeCache.getList()) {
                    shapeCoordinate = new ShapeCoordinate();
                    try {
                        jsonObject = coordinateXYZToBLH(list.get(1), list.get(2), code, wgs84Type);
                    } catch (Exception e) {
                        e.printStackTrace();
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                        shapeCoordinate.setCellNum(Integer.valueOf(list.get(0)));
                    }
                    if (jsonObject == null) {
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                        shapeCoordinate.setCellNum(Integer.valueOf(list.get(0)));
                    }
                    String lon = jsonObject==null?list.get(1):jsonObject.get("lon").toString();
                    String lat = jsonObject==null?list.get(2):jsonObject.get("lat").toString();
                    if (lon.equals("")||Float.valueOf(lon) > 180 || Float.valueOf(lon) < 0) {
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                    }
                    if (lat.equals("")||Float.valueOf(lat) > 180 || Float.valueOf(lat) < 0) {
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                    }
                    shapeCoordinate.setLon(lon);
                    shapeCoordinate.setLat(lat);
                    shapeCoordinate.setShape(shape);
                    shapeCoordinate.setCellNum(Integer.valueOf(list.get(0)));
                    for (int i = 3; i <= line1.getCellProperty().split(",").length; i++) {
                        elevation = new Elevation(list, line1.getCellProperty(), i, shape,shapeCoordinate);
                        shapeCoordinate.setElevationList(elevation);
                        shapeCoordinate.setElevation(elevation);
                    }
                    if (shapeCoordinates.size() != 0) {
                        shapeCoordinates.get(shapeCoordinates.size() - 1).setNext(shapeCoordinate);
                        shapeCoordinate.setParent(shapeCoordinates.get(shapeCoordinates.size()-1));
                    }
                    shapeCoordinates.add(shapeCoordinate);
                }
                shape.setShapeCoordinates(shapeCoordinates);
                shapes.add(shape);
            }
            Map<String, List<Shape>> areaShape = new HashMap<>();
            areaShape.put(entry.getKey(), shapes);
            plaCache.setAreaShape(areaShape);
        }
    }

    private void makeLineShape(Map<String, List<ShapeCache>> lineMap, String code, Coordinate.WGS84Type wgs84Type, Project project, PLACache plaCache) {
        List<Line> lines = lineService.getLines();
        Elevation elevation;
        Line line1 = null;
        Shape shape;
        ShapeCoordinate shapeCoordinate;
        JSONObject jsonObject = null;
        List<Build> builds = new LinkedList<>();
        List<Shape> shapes = new LinkedList<>();
        List<ShapeCoordinate> shapeCoordinates;
        for (Map.Entry<String, List<ShapeCache>> entry : lineMap.entrySet()) {
            for (ShapeCache shapeCache : entry.getValue()) {
                for (Line line : lines) {
                    if (line.getCommonType().getTypeC().equals(shapeCache.getName())) {
                        line1 = (Line) SettingUtils.objectCopy(line);
                        break;
                    }
                }
                shape = new Shape();
                shape.setCommonType(line1.getCommonType());
                shape.setProject(project);
                shape.setRemark(shapeCache.getRemark());
                shapeCoordinates = new LinkedList<>();
                for (List<String> list : shapeCache.getList()) {
                    shapeCoordinate = new ShapeCoordinate();
                    try {
                        jsonObject = coordinateXYZToBLH(list.get(1), list.get(2), code, wgs84Type);
                    } catch (Exception e) {
                        e.printStackTrace();
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                        shapeCoordinate.setCellNum(Integer.valueOf(list.get(0)));
                    }
                    if (jsonObject == null) {
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                        shapeCoordinate.setCellNum(Integer.valueOf(list.get(0)));
                    }
                    String lon = jsonObject==null?list.get(1):jsonObject.get("lon").toString();
                    String lat = jsonObject==null?list.get(2):jsonObject.get("lat").toString();
                    if (lon.equals("")||Float.valueOf(lon) > 180 || Float.valueOf(lon) < 0) {
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                    }
                    if (lat.equals("")||Float.valueOf(lat) > 180 || Float.valueOf(lat) < 0) {
                        shape.setErrorMsg(true);
                        shapeCoordinate.setErrorMsg(true);
                    }
                    shapeCoordinate.setLon(lon);
                    shapeCoordinate.setLat(lat);
                    shapeCoordinate.setShape(shape);
                    shapeCoordinate.setCellNum(Integer.valueOf(list.get(0)));
                    for (int i = 3; i <= line1.getCellProperty().split(",").length-2; i++) {
                        elevation = new Elevation(list, line1.getCellProperty(), i, shape,shapeCoordinate);
                        shapeCoordinate.setElevationList(elevation);
                        shapeCoordinate.setElevation(elevation);
                    }
                    makeBuild(list, line1,builds,shapeCoordinate,project);
                    if (shapeCoordinates.size() != 0) {
                        shapeCoordinates.get(shapeCoordinates.size() - 1).setNext(shapeCoordinate);
                        shapeCoordinate.setParent(shapeCoordinates.get(shapeCoordinates.size()-1));
                    }
                    shapeCoordinates.add(shapeCoordinate);
                }
                shape.setShapeCoordinates(shapeCoordinates);
                shapes.add(shape);
            }
            Map<String, List<Build>> buildMap = new HashMap<>();
            buildMap.put(entry.getKey(), builds);
            Map<String, List<Shape>> lineShape = new HashMap<>();
            lineShape.put(entry.getKey(), shapes);
            plaCache.setLineShape(lineShape);
            plaCache.setBuildsMap(buildMap);
        }
    }

    private void makeBuild(List<String> list, Line line1, List<Build> builds, ShapeCoordinate shapeCoordinate,Project project) {
        Build build = null;
        for (int i = line1.getCellProperty().split(",").length - 2; i < line1.getCellProperty().split(",").length - 1; i++) {
            if (list.get(i+1).equals("")) {
                continue;
            }
            List<Build> builds1 = buildService.getBuilds();
            for (Build build1 : builds1) {
                if (build1.getType().getTypeC().equals(list.get(i+1))) {
                    build = (Build) SettingUtils.objectCopy(build1);
                    break;
                }
            }
            if (build == null) {
                shapeCoordinate.setErrorMsg(true);
                shapeCoordinate.getShape().setErrorMsg(true);
                return;
            }
            build.setRemark(list.get(i + 2));
            build.setShapeCoordinate(shapeCoordinate);
            build.setProjectId(project.getId());
            builds.add(build);
        }
    }


    private void inputExcel(CoordinateMap coordinateMap, SheetObject sheetObject, Project project) {
        coordinateMap.setLineMap(inputLine(sheetObject.getLineWBs(), project));
        coordinateMap.setAreaMap(inputArea(sheetObject.getAreaWBs(),project));
    }

    private Map<String, List<ShapeCache>> inputSectionPlaneModel(Map<String, List<Sheet>> scetionPlaneModelWBs) {
        Row row;
        Map<String, List<ShapeCache>> shapeCacheMap = new HashMap<>();
        ShapeCache shapeCache = null;
        List<ShapeCache> shapeCaches = null;
        for (Map.Entry<String, List<Sheet>> entry : scetionPlaneModelWBs.entrySet()) {
            for (Sheet sheet : entry.getValue()) {
                if (sheet == null) {
                    continue;
                }
                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                    row = sheet.getRow(j);
                    if (row == null) {
                        continue;
                    }
                    List<String> list = getCellSectionPlaneModel(row);
                    if (list.get(1).trim().equals("编号")) {
                        if (j == 0) {
                            shapeCache = new ShapeCache();
                            shapeCache.setName(sheet.getSheetName());
                            continue;
                        }else{
                            if (shapeCacheMap.get(entry.getKey()) == null) {
                                shapeCaches = new ArrayList<>();
                                shapeCaches.add(shapeCache);
                                shapeCacheMap.put(entry.getKey(), shapeCaches);
                            } else {
                                shapeCaches = shapeCacheMap.get(entry.getKey());
                                shapeCaches.add(shapeCache);
                                shapeCacheMap.put(entry.getKey(), shapeCaches);
                            }
                            shapeCache = new ShapeCache();
                            shapeCache.setName(sheet.getSheetName());
                            continue;
                        }
                    }
                    if (!list.get(2).equals("")) {
                        shapeCache.setList(list);
                    }
                    if (j == sheet.getLastRowNum()) {
                        if (shapeCacheMap.get(entry.getKey()) == null) {
                            shapeCaches = new ArrayList<>();
                            shapeCaches.add(shapeCache);
                            shapeCacheMap.put(entry.getKey(), shapeCaches);
                        } else {
                            shapeCaches = shapeCacheMap.get(entry.getKey());
                            shapeCaches.add(shapeCache);
                            shapeCacheMap.put(entry.getKey(), shapeCaches);
                        }
                    }
                }
            }
        }
        return shapeCacheMap;
    }

    private List<String> getCellSectionPlaneModel(Row row) {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(row.getRowNum()+1));
        if (row.getCell(0) != null) {
            row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
            list.add(row.getCell(0).getStringCellValue());
        }else{
            list.add("");
        }
        if (row.getCell(2) != null) {
            row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
            Comment cellComment = row.getCell(2).getCellComment();
            if (cellComment != null) {
                list.add(cellComment.getString().getString());
            } else {
                list.add("");
            }
        }else{
            list.add("");
        }
        if (row.getCell(3) != null) {
            row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
            list.add(row.getCell(3).getStringCellValue());
        } else {
            list.add("");
        }
        return list;
    }

    /**
     * 导入面
     * @param areaWBs
     */
    private Map<String, List<ShapeCache>> inputArea(Map<String, List<Sheet>> areaWBs, Project project) {
        return inputLine(areaWBs, project);
    }


    /**
     * 导入线
     * @param lineWBs
     */
    private Map<String, List<ShapeCache>> inputLine(Map<String, List<Sheet>> lineWBs, Project project) {
        Row row;
        Map<String, List<ShapeCache>> shapeCacheMap = new HashMap<>();
        ShapeCache shapeCache = null;
        List<ShapeCache> shapeCaches = null;
        for (Map.Entry<String, List<Sheet>> entry : lineWBs.entrySet()) {
            for (Sheet sheet : entry.getValue()) {
                if (sheet == null) {
                    continue;
                }
                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                    row = sheet.getRow(j);
                    if (row == null) {
                        continue;
                    }
                    List<String> list = getCell(row);
                    if (list.get(1).trim().equals("描述")) {
                        if (j == 0) {
                            shapeCache = new ShapeCache();
                            shapeCache.setName(sheet.getSheetName());
                            shapeCache.setRemark(list.get(2));
                            continue;
                        }else{
                            if (shapeCacheMap.get(entry.getKey()) == null) {
                                shapeCaches = new ArrayList<>();
                                shapeCaches.add(shapeCache);
                                shapeCacheMap.put(entry.getKey(), shapeCaches);
                            } else {
                                shapeCaches = shapeCacheMap.get(entry.getKey());
                                shapeCaches.add(shapeCache);
                                shapeCacheMap.put(entry.getKey(), shapeCaches);
                            }
                            shapeCache = new ShapeCache();
                            shapeCache.setName(sheet.getSheetName());
                            shapeCache.setRemark(list.get(2));
                            continue;
                        }
                    }
                    if (list.get(1).trim().equals("")||list.get(1).trim().equals("经度")) {
                        continue;
                    }
                    shapeCache.setList(list);
                    if (j == sheet.getLastRowNum()) {
                        if (shapeCacheMap.get(entry.getKey()) == null) {
                            shapeCaches = new ArrayList<>();
                            shapeCaches.add(shapeCache);
                            shapeCacheMap.put(entry.getKey(), shapeCaches);
                        } else {
                            shapeCaches = shapeCacheMap.get(entry.getKey());
                            shapeCaches.add(shapeCache);
                            shapeCacheMap.put(entry.getKey(), shapeCaches);
                        }
                    }
                }
            }
        }
        return shapeCacheMap;
    }

    private List<String> getCell(Row row) {
        List<String> list = new LinkedList<>();
        list.add(String.valueOf(row.getRowNum()+1));
        for (int i = 0; i < 10; i++) {
            if (row.getCell(i) != null) {
                row.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
                list.add(row.getCell(i).getStringCellValue());
            } else {
                list.add("");
            }
        }
        return list;
    }


    /**
     * 将各类坐标转换为大地坐标
     *
     * @param longitude
     * @param latitude
     * @param code
     * @return
     */
    private JSONObject coordinateXYZToBLH(String longitude, String latitude, String code, Coordinate.WGS84Type wgs84Type) throws Exception {
        if (wgs84Type == null) {
            return null;
        }
        switch (wgs84Type) {
            case DEGREE:
                return degree(longitude, latitude);
            case DEGREE_MINUTE_1:
                return degreeMinute1(longitude, latitude);
            case DEGREE_MINUTE_2:
                return degreeMinute2(longitude, latitude);
            case DEGREE_MINUTE_SECOND_1:
                return degreeMinuteSecond1(longitude, latitude);
            case DEGREE_MINUTE_SECOND_2:
                return degreeMinuteSecond2(longitude, latitude);
            case PLANE_COORDINATE:
                return planeCoordinate(longitude, latitude, code);
        }
        return null;
    }

    private JSONObject degreeMinuteSecond2(String longitude, String latitude) {
        if (longitude.length() == 0 || latitude.length() == 0) {
            return null;
        }
        String a = longitude.substring(0, longitude.indexOf("°"));
        String b = longitude.substring(longitude.indexOf("°") + 1, longitude.indexOf("'"));
        String c = longitude.substring(longitude.indexOf("'") + 1, longitude.length() - 1);
        longitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60 + Double.valueOf(c) / 3600);
        a = latitude.substring(0, latitude.indexOf("°"));
        b = latitude.substring(latitude.indexOf("°") + 1, latitude.indexOf("'"));
        c = latitude.substring(latitude.indexOf("'") + 1, latitude.length() - 1);
        latitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60 + Double.valueOf(c) / 3600);
        if (Float.valueOf(longitude) > 180
                || Float.valueOf(longitude) < 0) {
            return null;
        }
        if (Float.valueOf(latitude) > 90
                || Float.valueOf(latitude) < 0) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinuteSecond1(String longitude, String latitude) {
        if (longitude.length() == 0 || latitude.length() == 0) {
            return null;
        }
        String[] str = longitude.split(":");
        longitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60 + Double.valueOf(str[2]) / 3600);
        str = latitude.split(":");
        latitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60 + Double.valueOf(str[2]) / 3600);
        if (Float.valueOf(longitude) > 180
                || Float.valueOf(longitude) < 0) {
            return null;
        }
        if (Float.valueOf(latitude) > 90
                || Float.valueOf(latitude) < 0) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinute2(String longitude, String latitude) {
        if (longitude.length() == 0 || latitude.length() == 0) {
            return null;
        }
        String a = longitude.substring(0, longitude.indexOf("°"));
        String b = longitude.substring(longitude.indexOf("°") + 1, longitude.indexOf("'"));
        longitude = String.valueOf(Double.valueOf(a) + Double.valueOf(b) / 60);
        a = latitude.substring(0, latitude.indexOf("°"));
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
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinute1(String longitude, String latitude) {
        if (longitude.length() == 0 || latitude.length() == 0) {
            return null;
        }
        String[] str = longitude.split(":");
        longitude = String.valueOf(Double.valueOf(str[0]) + Double.valueOf(str[1]) / 60);
        str = latitude.split(":");
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
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    /**
     * 平面坐标转经纬度
     *
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
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    /**
     * 度
     *
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
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    public PLACache reslove(SheetObject sheetObject, String central, Coordinate.WGS84Type wgs84Type, Project project, Shape shape) {
        Map<String, List<ShapeCache>> map = inputSectionPlaneModel(sheetObject.getBuildWBs());
        //		数据分析
        String code = transFromService.checkCode84(central);
        Map<PLACache, List<Build>> map1 = saveBuildAttribute(map, code, wgs84Type, project, shape);
        boolean flag = false;
        PLACache plaCache = null;
        for (Map.Entry<PLACache, List<Build>> entry : map1.entrySet()) {
            flag=true;
            for (Map.Entry<String, List<Build>> entity1 : entry.getKey().getBuildsMap().entrySet()) {
                for (Build build : entity1.getValue()) {
                    if (build.isErrorMsg()) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    break;
                }
            }
            plaCache = entry.getKey();
            if (flag) {
                saveBuildAttributes(entry.getKey(), entry.getValue());
                plaCache = null;
            }
            break;
        }
        return plaCache;
    }

    public PLACache reslove(SheetObject sheetObject, String central, Coordinate.WGS84Type wgs84Type, Project project, ShapeCoordinate shapeCoordinate) {
        Map<String, List<ShapeCache>> map = inputSectionPlaneModel(sheetObject.getBuildWBs());
        //		数据分析
        String code = transFromService.checkCode84(central);
        Map<PLACache, List<Build>> map1 = saveBuildAttribute(map, code, wgs84Type, project, shapeCoordinate);
        boolean flag = false;
        PLACache plaCache = null;
        for (Map.Entry<PLACache, List<Build>> entry : map1.entrySet()) {
            flag=true;
            for (Map.Entry<String, List<Build>> entity1 : entry.getKey().getBuildsMap().entrySet()) {
                for (Build build : entity1.getValue()) {
                    if (build.isErrorMsg()) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    break;
                }
            }
            plaCache = entry.getKey();
            if (flag) {
                saveBuildAttributes(entry.getKey(), entry.getValue());
                plaCache = null;
            }
            break;
        }
        return plaCache;
    }

    private void saveBuildAttributes(PLACache plaCache, List<Build> deletes) {
        Iterator<Build> iterator = deletes.iterator();
        Build build1;
        while (iterator.hasNext()) {
            build1 = iterator.next();
            buildService.remove(build1);
            buildService.flush();
        }
        for (Map.Entry<String, List<Build>> entry : plaCache.getBuildsMap().entrySet()) {
            for (Build build : entry.getValue()) {
                buildService.save(build);
            }
        }
    }

    private Map<PLACache, List<Build>> saveBuildAttribute(Map<String, List<ShapeCache>> map, String code, Coordinate.WGS84Type wgs84Type, Project project, Shape shape) {
        List<Build> builds;
        Build build1 = null,build2;
        BuildAttribute buildAttribute;
        List<BuildAttribute> buildAttributes;
        JSONObject jsonObject;
        List<Build> builds1 = new ArrayList<>();
        List<Build> deleteBuilds = new ArrayList<>();
        PLACache plaCache = new PLACache();
        Map<String, List<Build>> map2 = new HashMap<>();
        Map<PLACache, List<Build>> map1 = new HashMap<>();
        for (Map.Entry<String, List<ShapeCache>> entry : map.entrySet()) {
            for (ShapeCache shapeCache : entry.getValue()) {
                builds = buildService.getBuilds();
                for (Build build : builds) {
                    if (build.getType().getTypeC().equals(shapeCache.getName())) {
                        build1 = (Build) SettingUtils.objectCopy(build);
                        break;
                    }
                    if (build.getChildType() != null) {
                        if (build.getChildType().getTypeC().equals(shapeCache.getName())) {
                            build1 = (Build) SettingUtils.objectCopy(build);
                            break;
                        }
                    }
                }
                buildAttributes = new ArrayList<>();
                for (List<String> list : shapeCache.getList()) {
                    buildAttribute = new BuildAttribute();
                    buildAttribute.setValue(list.get(3));
                    buildAttribute.setAlias(list.get(2));
                    buildAttribute.setRow(Integer.valueOf(list.get(0)));
                    buildAttribute.setBuild(build1);
                    buildAttributes.add(buildAttribute);
                }
                writeCoordinateToBuild(build1,buildAttributes,code,wgs84Type);
                build1.setBuildAttributes(buildAttributes);
                build1.setProjectId(project.getId());
                Iterator<Build> iterator = builds1.iterator();
                while (iterator.hasNext()) {
                    build2 = iterator.next();
                    if (build2.getCenterCoor() != null && build1.getCenterCoor() != null) {
                        if (build2.getCenterCoor().equals(build1.getCenterCoor())) {
                            iterator.remove();
                        }
                    }
                }
                builds1.add(build1);
                if (build1.isErrorMsg()) {
                    continue;
                }
                List<ShapeCoordinate> shapeCoordinates = shapeCoordinateService.findByShape(shape);
                for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
                    jsonObject = JSONObject.fromObject(build1.getCenterCoor());
                    if (shapeCoordinate.getLon().equals(jsonObject.get("lon").toString()) && shapeCoordinate.getLat().equals(jsonObject.get("lat").toString())) {
                        if (shapeCoordinate.getBuild() != null) {
                            if (!deleteBuilds.contains(shapeCoordinate.getBuild())) {
                                deleteBuilds.add(shapeCoordinate.getBuild());
                            }
                        }
                        build1.setShapeCoordinate(shapeCoordinate);
                        break;
                    }
                }
                if (build1.getShapeCoordinate() == null) {
                    build1.setErrorMsgTrue();
                }
            }
            map2.put(entry.getKey(), builds1);
        }
        plaCache.setBuildsMap(map2);
        map1.put(plaCache, deleteBuilds);
        return map1;
    }

    private Map<PLACache, List<Build>> saveBuildAttribute(Map<String, List<ShapeCache>> map, String code, Coordinate.WGS84Type wgs84Type, Project project, ShapeCoordinate shapeCoordinate) {
        List<Build> builds;
        Build build1 = null, build2;
        BuildAttribute buildAttribute;
        List<BuildAttribute> buildAttributes;
        JSONObject jsonObject;
        List<Build> builds1 = new ArrayList<>();
        List<Build> deleteBuilds = new ArrayList<>();
        PLACache plaCache = new PLACache();
        Map<String, List<Build>> map2 = new HashMap<>();
        Map<PLACache, List<Build>> map1 = new HashMap<>();
        for (Map.Entry<String, List<ShapeCache>> entry : map.entrySet()) {
            for (ShapeCache shapeCache : entry.getValue()) {
                builds = buildService.getBuilds();
                for (Build build : builds) {
                    if (build.getType().getTypeC().equals(shapeCache.getName())) {
                        build1 = (Build) SettingUtils.objectCopy(build);
                        break;
                    }
                    if (build.getChildType() != null) {
                        if (build.getChildType().getTypeC().equals(shapeCache.getName())) {
                            build1 = (Build) SettingUtils.objectCopy(build);
                            break;
                        }
                    }
                }
                buildAttributes = new ArrayList<>();
                for (List<String> list : shapeCache.getList()) {
                    buildAttribute = new BuildAttribute();
                    buildAttribute.setValue(list.get(3));
                    buildAttribute.setAlias(list.get(2));
                    buildAttribute.setRow(Integer.valueOf(list.get(0)));
                    buildAttribute.setBuild(build1);
                    buildAttributes.add(buildAttribute);
                }
                writeCoordinateToBuild(build1, buildAttributes, code, wgs84Type);
                build1.setBuildAttributes(buildAttributes);
                build1.setProjectId(project.getId());
                Iterator<Build> iterator = builds1.iterator();
                while (iterator.hasNext()) {
                    build2 = iterator.next();
                    if (build2.getCenterCoor() != null && build1.getCenterCoor() != null) {
                        if (build2.getCenterCoor().equals(build1.getCenterCoor())) {
                            iterator.remove();
                        }
                    }
                }
                builds1.add(build1);
                if (build1.isErrorMsg()) {
                    continue;
                }
//                jsonObject = JSONObject.fromObject(build1.getCenterCoor());
////                if (shapeCoordinate.getLon().equals(jsonObject.get("lon").toString()) && shapeCoordinate.getLat().equals(jsonObject.get("lat").toString())) {
                if (shapeCoordinate.getBuild() != null) {
                    if (!deleteBuilds.contains(shapeCoordinate.getBuild())) {
                        deleteBuilds.add(shapeCoordinate.getBuild());
                    }
                }
////                }
                build1.setShapeCoordinate(shapeCoordinate);
//                if (build1.getShapeCoordinate() == null) {
//                    build1.setErrorMsgTrue();
//                }
            }
            map2.put(entry.getKey(), builds1);
        }
        plaCache.setBuildsMap(map2);
        map1.put(plaCache, deleteBuilds);
        return map1;
    }

    private void writeCoordinateToBuild(Build build1, List<BuildAttribute> buildAttributes, String code, Coordinate.WGS84Type wgs84Type) {
        Iterator<BuildAttribute> iterator = buildAttributes.iterator();
        BuildAttribute buildAttribute;
        JSONObject jsonObject = null;
        while (iterator.hasNext()) {
            buildAttribute = iterator.next();
            if (buildAttribute.getAlias().equals("center")) {
                String[] split = buildAttribute.getValue().split(",").length==1?buildAttribute.getValue().split("，"):buildAttribute.getValue().split(",");
                try {
                    jsonObject = coordinateXYZToBLH(split[0], split[1], code, wgs84Type);
                } catch (Exception e) {
//                    e.printStackTrace();
                    build1.setErrorMsgTrue();
                }
                if (jsonObject == null) {
                    build1.setErrorMsgTrue();
                }
                build1.setCenterCoor(jsonObject==null?null:jsonObject.toString());
                build1.setCenterCoorNum(buildAttribute.getRow());
                iterator.remove();
            }
            if (buildAttribute.getAlias().equals("position")) {
                String[] split = buildAttribute.getValue().split(",").length==1?buildAttribute.getValue().split("，"):buildAttribute.getValue().split(",");
                try {
                    jsonObject = coordinateXYZToBLH(split[0], split[1], code, wgs84Type);
                } catch (Exception e) {
//                    e.printStackTrace();
                    continue;
                }
                build1.setPositionCoor(jsonObject==null?null:jsonObject.toString());
                build1.setPositionCoorNum(buildAttribute.getRow());
                iterator.remove();
            }
            if (buildAttribute.getAlias().equals("remark")) {
                build1.setRemark(buildAttribute.getValue());
                build1.setRemarkNum(buildAttribute.getRow());
                iterator.remove();
            }
            if (buildAttribute.getAlias().equals("designElevation")) {
                build1.setDesignElevation(buildAttribute.getValue());
                build1.setDesignElevationNum(buildAttribute.getRow());
                iterator.remove();
            }
        }
    }

    public JSONArray pickedErrorMsg1(PLACache plaCache) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (Map.Entry<String, List<Build>> entry : plaCache.getBuildsMap().entrySet()) {
            for (Build build : entry.getValue()) {
                if (build.isErrorMsg()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("excel", entry.getKey());
                    jsonObject.put("sheetName", build.getChildType() == null ? build.getType().getTypeC() : build.getChildType().getTypeC());
                    if (build.getCenterCoor() == null || build.getCenterCoor().equals("")) {
                        jsonObject.put("rowNum-center", build.getCenterCoorNum());
                    }
                    if (build.getDesignElevation() == null || build.getDesignElevation().equals("")) {
                        jsonObject.put("rowNum-design", build.getDesignElevationNum());
                    }
                    if (build.getRemark() == null || build.getRemark().equals("")) {
                        jsonObject.put("rowNum-remark", build.getDesignElevationNum());
                    }
                    if (build.getShapeCoordinate() == null) {
                        jsonObject.put("errorMsg", "未找到匹配坐标");
                    }
                    jsonArray.add(jsonObject);
                }
            }
        }
        return jsonArray;
    }

    public Workbook downloadShapeModel(List<String> list) {
        Workbook wb = new XSSFWorkbook();
        List<Line> lines1 = new ArrayList<>();
        List<Line> lines = lineService.getLines();
        for (Line line : lines) {
            for (String s : list) {
                if (line.getCommonType().name().equals(s.toUpperCase())&&(line.getCommonType().getType().equals("area")||line.getCommonType().getType().equals("line"))&&line.getCommonType().isModel()) {
                    lines1.add(line);
                    break;
                }
            }
        }
        List<String> selects = new ArrayList<>();
        for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
            if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                continue;
            }
            if (commonType.getType().equals("buildModel")) {
                selects.add(commonType.getTypeC());
            }
        }
        if (lines1.size() == 0) {
            return null;
        }
        outputShapeModel(wb, lines1,selects);
        return wb;
    }

    private void outputShapeModel(Workbook wb, List<Line> lines, List<String> selects) {
        for (Line line : lines) {
            int i = 0;
            Row row = null;
            Cell cell = null;
            Sheet sheet = wb.createSheet(line.getCommonType().getTypeC());
            List<String> list = new ArrayList<>();
            String[] split = line.getCellProperty().split(",");
            list.add("描述");
            for (int i1 = 1; i1 < split.length; i1++) {
                list.add("");
            }
            writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundSkyBlue(wb), list, null, null, wb, true);
            list = new ArrayList<>();
            for (String s : split) {
                list.add(s.split(":")[0]);
            }
            if (line.getCommonType().getType().equals("line")) {
                writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundYellow(wb), list, "select", selects, wb, true);
            } else {
                writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundYellow(wb), list, null, null, wb, true);
            }
        }
    }

    void writeToCell(int i, Sheet sheet, Row row, Cell cell, CellStyle style, List<String> list, String type, List<String> selects, Workbook wb, boolean locked) {
        row = sheet.createRow(i);
        for (int i1 = 0; i1 < list.size(); i1++) {
            cell = row.createCell(i1);
            if (list.get(i1) != null) {
                cell.setCellValue(list.get(i1));
            }
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i1);
        }
        if (type != null) {
            buildService.setXSSFValidation1((XSSFSheet) sheet, selects.toArray(new String[selects.size()]), i, i, list.size()-2, list.size()-2);
        }
    }

    public List<Shape> findByProject(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        return shapeDao.findList(0, null, filters);
    }

    public Workbook downloadShape(List<Shape> shapes) {
        Workbook wb = new XSSFWorkbook();
        Map<String, List<Shape>> map = new HashMap<>();
        List<Shape> shapes1;
        for (Shape shape : shapes) {
            if (shape.getChildType() == null) {
                if (map.get(shape.getCommonType().toString()) == null) {
                    shapes1 = new ArrayList<>();
                    shapes1.add(shape);
                    map.put(shape.getCommonType().toString(), shapes1);
                } else {
                    shapes1 = map.get(shape.getCommonType().toString());
                    shapes1.add(shape);
                    map.put(shape.getCommonType().toString(), shapes1);
                }
            } else {
                if (map.get(shape.getChildType().toString()) == null) {
                    shapes1 = new ArrayList<>();
                    shapes1.add(shape);
                    map.put(shape.getChildType().toString(), shapes1);
                } else {
                    shapes1 = map.get(shape.getChildType().toString());
                    shapes1.add(shape);
                    map.put(shape.getChildType().toString(), shapes1);
                }
            }
        }
        outputShape(wb, map);
        return wb;
    }

    private void outputShape(Workbook wb, Map<String, List<Shape>> map) {
        Line line;
        List<String> list;
        Row row = null;
        Cell cell = null;
        Sheet sheet;
        JSONObject jsonObject = null;
        JSONArray jsonArray;
        int size;
        for (Map.Entry<String, List<Shape>> entry : map.entrySet()) {
            try {
                sheet = wb.createSheet(CommonEnum.CommonType.valueOf(entry.getKey()).getTypeC());
            } catch (Exception e) {
                sheet = wb.createSheet(LineSectionPlaneModel.Type.valueOf(entry.getKey()).getTypeC());
            }
            int i = 0;
            for (Shape shape : entry.getValue()) {
                line = getLine(shape);
                String[] split = line.getCellProperty().split(",");
                list = new ArrayList<>();
                list.add("描述");
                list.add(shape.getRemark());
                size = list.size();
                for (int i1 = 0; i1 < split.length - size; i1++) {
                    list.add("");
                }
                writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundSkyBlue(wb), list, null, null, wb, true);
                list = new ArrayList<>();
                for (String s : split) {
                    list.add(s.split(":")[0]);
                }
                writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundYellow(wb), list, null, null, wb, true);
                List<ShapeCoordinate> shapeCoordinates = shapeCoordinateService.findByShape(shape);
                for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
                    list = new ArrayList<>();
                    list.add(shapeCoordinate.getLon());
                    list.add(shapeCoordinate.getLat());
                    jsonArray = JSONArray.fromObject(shapeCoordinate.getElevations());
                    for (int i1 = 2; i1 < split.length; i1++) {
                        for (Object o : jsonArray) {
                            jsonObject = (JSONObject) o;
                            if (jsonObject.get("alias").toString().equals(split[i1].split(":")[1])) {
                                list.add(String.valueOf(jsonObject.get("ele")));
                                break;
                            }
                        }
                    }
                    if (shapeCoordinate.getBuild() != null) {
                        list.add(shapeCoordinate.getBuild().getType().getTypeC());
                        list.add(shapeCoordinate.getBuild().getRemark());
                    } else {
                        if (split.length > 3) {
                            list.add("");
                            list.add("");
                        }
                    }
                    writeToCell(i++, sheet, row, cell, buildService.noBold(wb), list, null, null, wb, true);
                }
            }
        }
    }

    private Line getLine(Shape shape){
        List<Line> lines = lineService.getLines();
        for (Line line : lines) {
            if (shape.getCommonType() == line.getCommonType()) {
                return line;
            }
        }
        return null;
    }


    public LineSectionPlaneModel pickedShapeAndSetProperty(Shape shape) {
        List<ShapeAttribute> shapeAttributes = shapeAttributeService.findByShape(shape);
        LineSectionPlaneModel lineSectionPlaneModel1 = null;
        List<LineSectionPlaneModel> lineSectionPlaneModels = lineSectionPlaneModelService.getLineSectionPlaneModel();
        for (LineSectionPlaneModel lineSectionPlaneModel : lineSectionPlaneModels) {
            if (shape.getChildType() != null) {
                if (lineSectionPlaneModel.getType() == shape.getChildType()) {
                    lineSectionPlaneModel1 = (LineSectionPlaneModel) SettingUtils.objectCopy(lineSectionPlaneModel);
                    break;
                }
            } else {
                if (lineSectionPlaneModel.getType().getCommonType() == shape.getCommonType()) {
                    lineSectionPlaneModel1 = (LineSectionPlaneModel) SettingUtils.objectCopy(lineSectionPlaneModel);
                    break;
                }
            }
        }
        if (lineSectionPlaneModel1 == null) {
            return null;
        }
        lineSectionPlaneModel1.setShapeAttribute(shapeAttributes);
        setProperty(lineSectionPlaneModel1);
        return lineSectionPlaneModel1;
    }

    /**
     * 匹配建筑物数据
     * sign=true 匹配属性
     *
     * @param lineSectionPlaneModel
     */
    public void setProperty(LineSectionPlaneModel lineSectionPlaneModel) {
        attributeGroupNotNuLL(lineSectionPlaneModel.getRemark(), lineSectionPlaneModel.getShapeAttribute());
        attributeGroupNotNuLL(lineSectionPlaneModel.getLineWaterResource(), lineSectionPlaneModel.getShapeAttribute());
        attributeGroupNotNuLL(lineSectionPlaneModel.getLineControlSize(), lineSectionPlaneModel.getShapeAttribute());
        attributeGroupNotNuLL(lineSectionPlaneModel.getLineGroundStress(), lineSectionPlaneModel.getShapeAttribute());
        attributeGroupNotNuLL(lineSectionPlaneModel.getLineComponent(), lineSectionPlaneModel.getShapeAttribute());
    }

    private void attributeGroupNotNuLL(LineAttributeGroup attributeGroup, List<ShapeAttribute> attributeList) {
        if (attributeGroup == null) {
            return;
        }
        setAttribute(attributeGroup.getShapeAttributes(), attributeList);
        setChild(attributeGroup.getChilds(), attributeList);
    }

    /**
     * 匹配属性
     *
     * @param shapeAttributes    输出的
     * @param attributeList 数据库中的
     */
    private void setAttribute(List<ShapeAttribute> shapeAttributes, List<ShapeAttribute> attributeList) {
        if (shapeAttributes == null) {
            return;
        }
        for (ShapeAttribute shapeAttribute : shapeAttributes) {
            for (ShapeAttribute shapeAttribute1 : attributeList) {
                if (shapeAttribute.getAlias().equals(shapeAttribute1.getAlias())) {
                    shapeAttribute.setValue(shapeAttribute1.getValue());
                    shapeAttribute.setId(shapeAttribute1.getId());
                    break;
                }
            }
        }
    }

    /**
     * 子节点匹配属性
     *
     * @param attributeGroups
     * @param attributeList
     */
    private void setChild(List<LineAttributeGroup> attributeGroups, List<ShapeAttribute> attributeList) {
        if (attributeGroups == null) {
            return;
        }
        for (LineAttributeGroup attributeGroup : attributeGroups) {
            setAttribute(attributeGroup.getShapeAttributes(), attributeList);
            setChild(attributeGroup.getChilds(), attributeList);
        }
    }

    public JSONObject buildJson(Shape shape) {
        JSONObject jsonObject = new JSONObject(),jsonObject1;
        jsonObject.put("remark", shape.getRemark());
        jsonObject.put("type", shape.getCommonType().getTypeC());
        jsonObject.put("id", shape.getId());
        jsonObject.put("createDate", shape.getCreateDate().getTime());
        List<ShapeCoordinate> shapeCoordinates = shapeCoordinateService.findByShape(shape);
        JSONArray jsonArray = new JSONArray();
        for (ShapeCoordinate shapeCoordinate : sortShapeCoordinate(shapeCoordinates)) {
            jsonObject1 = new JSONObject();
            jsonObject1.put("id", shapeCoordinate.getId());
            jsonObject1.put("lon", shapeCoordinate.getLon());
            jsonObject1.put("lat", shapeCoordinate.getLat());
            jsonObject1.put("elevations", JSONArray.fromObject(shapeCoordinate.getElevations()));
            if (shapeCoordinate.getBuild() != null) {
                jsonObject1.put("buildName", shapeCoordinate.getBuild().getType().getTypeC());
            }
//            if (shapeCoordinate.getNext() != null) {
//                jsonObject1.put("next", shapeCoordinate.getNext().getId());
//            }
            jsonArray.add(jsonObject1);
        }
        jsonObject.put("coors", jsonArray);
        if (shape.getChildType() == null) {
            jsonObject.put("childTypes", shapeAttributeService.getModelType(shape.getCommonType()));
        } else {
            jsonObject.put("childType", shape.getChildType().getTypeC());
            jsonObject.put("attribute", shapeAttributeService.buildJson(shape));
        }
        return jsonObject;
    }

    /**
     * 对查出的坐标进行排序
     * @param shapeCoordinates
     * @return
     */
    private List<ShapeCoordinate> sortShapeCoordinate(List<ShapeCoordinate> shapeCoordinates) {
        List<ShapeCoordinate> list = new ArrayList<>();
        for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
            if (shapeCoordinate.getParent() == null) {
                list.add(shapeCoordinate);
                break;
            }
        }
        if (list.size() == 0) {
            return null;
        }
        while (list.get(list.size() - 1).getNext() != null) {
            ShapeCoordinate next = list.get(list.size() - 1).getNext();
            for (ShapeCoordinate shapeCoordinate : shapeCoordinates) {
                if (next.getId().equals(shapeCoordinate.getId())) {
                    list.add(shapeCoordinate);
                    break;
                }
            }
        }
        return list;
    }

    public JSONArray getModelType() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
            if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                continue;
            }
            if (!commonType.isModel()||commonType.getType().equals("buildModel")) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("typeC", commonType.getTypeC());
            jsonObject.put("commonType", commonType.name());
            jsonObject.put("type", commonType.getType());
            jsonObject.put("buildType", commonType.getBuildType());
            jsonObject.put("abbreviate", commonType.getAbbreviate());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
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

    public Shape editShape(Object coors, Object shapeId, Object remark) {
        List<ShapeCoordinate> shapeCoordinates = new ArrayList<>();
        Shape shape = find(Long.valueOf(shapeId.toString()));
        Line line = null;
        Elevation elevation;
        ShapeCoordinate shapeCoordinate;
        JSONArray jsonArray = JSONArray.fromObject(coors);
        JSONObject jsonObject;
        ShapeCoordinate next = null;
        for (Object list1 : jsonArray) {
            jsonObject = JSONObject.fromObject(list1);
            Object id = jsonObject.get("id");
            if (id != null) {
                if (shapeCoordinates.size() != 0) {
                    shapeCoordinates.get(shapeCoordinates.size() - 1).setNext(next);     if (next != null) {
                        next.setParent(shapeCoordinates.get(shapeCoordinates.size() - 1));
                        shapeCoordinates.add(next);
                    }
                }
                shapeCoordinate = shapeCoordinateService.find(Long.valueOf(id.toString()));
                if (!shapeCoordinate.getShape().getId().equals(shape.getId())) {
                    return null;
                }
                next = shapeCoordinate.getNext();
                shapeCoordinate.setLon(jsonObject.get("lon").toString());
                shapeCoordinate.setLat(jsonObject.get("lat").toString());
                shapeCoordinateService.save(shapeCoordinate);
                shapeCoordinates.add(shapeCoordinate);
                continue;
            }
            shapeCoordinate = new ShapeCoordinate();
            shapeCoordinate.setLat(jsonObject.get("lat").toString());
            shapeCoordinate.setLon(jsonObject.get("lon").toString());
            for (Line line1 : lineService.getLines()) {
                if (line1.getCommonType() == shape.getCommonType()) {
                    line = (Line) SettingUtils.objectCopy(line1);
                }
            }
            if (line.getCommonType().getType().equals("line")) {
                for (int i = 3; i <= line.getCellProperty().split(",").length-2; i++) {
                    elevation = new Elevation("0", line.getCellProperty(), i, shape,shapeCoordinate);
                    shapeCoordinate.setElevation(elevation);
                }
            }
            if (line.getCommonType().getType().equals("area")) {
                for (int i = 3; i <= line.getCellProperty().split(",").length; i++) {
                    elevation = new Elevation("0", line.getCellProperty(), i, shape,shapeCoordinate);
                    shapeCoordinate.setElevation(elevation);
                }
            }
            shapeCoordinate.setShape(shape);
            shapeCoordinateService.save(shapeCoordinate);
            if (shapeCoordinates.size() != 0) {
                shapeCoordinate.setParent(shapeCoordinates.get(shapeCoordinates.size()-1));
                shapeCoordinates.get(shapeCoordinates.size()-1).setNext(shapeCoordinate);
            }
            shapeCoordinates.add(shapeCoordinate);
        }
        shapeCoordinates.get(shapeCoordinates.size() - 1).setNext(next);
        if (next != null) {
            next.setParent(shapeCoordinates.get(shapeCoordinates.size() - 1));
            shapeCoordinates.add(next);
        }
        for (ShapeCoordinate shapeCoordinate1 : shapeCoordinates) {
            shapeCoordinateService.save(shapeCoordinate1);
        }
        if (remark != null) {
            shape.setRemark(remark.toString());
            save(shape);
        }
        return shape;
    }

    public Shape newShape(Object coors, Object type, Object remark, Object projectId) {
        List<ShapeCoordinate> shapeCoordinates = new ArrayList<>();
        Shape shape1 = new Shape();
        shape1.setCommonType(CommonEnum.CommonType.valueOf(type.toString().toUpperCase()));
        shape1.setRemark(remark.toString());
        shape1.setProject(projectService.find(Long.valueOf(projectId.toString())));
        Line line = null;
        Elevation elevation;
        ShapeCoordinate shapeCoordinate;
        JSONArray jsonArray = JSONArray.fromObject(coors);
        JSONObject jsonObject;
        ShapeCoordinate next = null;
        for (Object list1 : jsonArray) {
            jsonObject = JSONObject.fromObject(list1);
            shapeCoordinate = new ShapeCoordinate();
            shapeCoordinate.setLat(jsonObject.get("lat").toString());
            shapeCoordinate.setLon(jsonObject.get("lon").toString());
            for (Line line1 : lineService.getLines()) {
                if (line1.getCommonType() == shape1.getCommonType()) {
                    line = (Line) SettingUtils.objectCopy(line1);
                    break;
                }
            }
            if (line.getCommonType().getType().equals("line")) {
                for (int i = 3; i <= line.getCellProperty().split(",").length-2; i++) {
                    elevation = new Elevation("0", line.getCellProperty(), i, shape1,shapeCoordinate);
                    shapeCoordinate.setElevation(elevation);
                }
            }
            if (line.getCommonType().getType().equals("area")) {
                for (int i = 3; i <= line.getCellProperty().split(",").length; i++) {
                    elevation = new Elevation("0", line.getCellProperty(), i, shape1,shapeCoordinate);
                    shapeCoordinate.setElevation(elevation);
                }
            }
            shapeCoordinate.setShape(shape1);
            if (shapeCoordinates.size() != 0) {
                shapeCoordinate.setParent(shapeCoordinates.get(shapeCoordinates.size()-1));
                shapeCoordinates.get(shapeCoordinates.size()-1).setNext(shapeCoordinate);
            }
            shapeCoordinates.add(shapeCoordinate);
        }
        shapeCoordinates.get(shapeCoordinates.size()-1).setNext(next);
        shape1.setShapeCoordinates(shapeCoordinates);
        save(shape1);
        return shape1;
    }

    /**
     * 获取图形线面列表
     * @param project
     * @return
     */
    public JSONArray getLists(Project project) {
        List<Shape> shapes = findByProject(project);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (Shape shape : shapes) {
            jsonObject = new JSONObject();
            jsonObject.put("id", shape.getId());
            jsonObject.put("remark", shape.getRemark());
            jsonObject.put("type", shape.getCommonType().getTypeC());
            jsonObject.put("childType", shape.getChildType());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
