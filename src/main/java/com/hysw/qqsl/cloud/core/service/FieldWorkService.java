package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.FieldWorkDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;
import com.hysw.qqsl.cloud.core.entity.buildModel.Graph;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgeo.proj4j.ProjCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 外业测量service
 * Created by chenl on 17-4-13.
 */
@Service("fieldWorkService")
public class FieldWorkService extends BaseService<FieldWork, Long> {
    private static final long serialVersionUID = -9100968677794664521L;
    @Autowired
    private FieldWorkDao fieldWorkDao;
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
    public void setBaseDao(FieldWorkDao fieldWorkDao) {
        super.setBaseDao(fieldWorkDao);
    }

    /**
     * @param builds
     * @param longitude1
     * @param latitude1
     * @return
     */
    public Build isSameBuild(List<Build> builds, String longitude1, String latitude1) {
        for (Build build : builds) {
            JSONObject centerCoor = JSONObject.fromObject(build.getCenterCoor());
            String longitude = centerCoor.get("longitude").toString();
            String latitude = centerCoor.get("latitude").toString();
            double distance = SettingUtils.distance(Double.valueOf(longitude), Double.valueOf(latitude), Double.valueOf(longitude1), Double.valueOf(latitude1));
            if (distance < 1.0) {
                return build;
            }
        }
        return null;
    }

    /**
     * 判断经度、维度、高程是否全等
     *
     * @param builds
     * @param longitude1
     * @param latitude1
     * @param elevation1
     * @return
     */
    public Build allEqual(List<Build> builds, String longitude1, String latitude1, String elevation1) {
        for (Build build : builds) {
            JSONObject centerCoor = JSONObject.fromObject(build.getCenterCoor());
            String longitude = centerCoor.get("lon").toString();
            String latitude = centerCoor.get("lat").toString();
            String elevation = centerCoor.get("ele").toString();
            if (longitude1.equals(longitude) && latitude1.equals(latitude) && elevation1.equals(elevation)) {
                return build;
            }
        }
        return null;
    }

    /**
     * 点线面坐标文件转字符串
     *
     * @param list
     * @return
     */
    private String listToString(List<Graph> list, JSONArray coordinates) {
        List<JSONObject> objects = new ArrayList<>();
        JSONObject jsonObject;
        JSONObject coordinate;
//        Coordinate.Type type = null;
        CommonEnum.CommonType baseType = null;
        String description = null;
        for (int i = 0; i < list.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("longitude", list.get(i).getCoordinates().get(0).getLongitude());
            jsonObject.put("latitude", list.get(i).getCoordinates().get(0).getLatitude());
            jsonObject.put("elevation", list.get(i).getCoordinates().get(0).getElevation());
//            type= Coordinate.Type.FIELD;
            baseType = list.get(i).getBaseType();
            if (list.get(i).getDescription() == null) {
                continue;
            } else {
                description = list.get(i).getDescription();
            }
            objects.add(jsonObject);
            coordinate = new JSONObject();
            coordinate.put("coordinate", objects);
            if (baseType != null) {
//                coordinate.put("type",type.toString());
                coordinate.put("baseType", baseType.toString());
                coordinate.put("alias", list.get(i).getAlias());
                if (description != null) {
                    coordinate.put("description", description);
                }
            }
            coordinates.add(coordinate);
            objects = new ArrayList<>();
        }
        return coordinates.toString();
    }

    /**
     * 根据项目以及保存时的来源（设计、外业）返回坐标和建筑物简单数据Json
     *
     * @param project
     * @param source
     * @return
     */
    public JSONObject field(Project project, Build.Source source) {
        List<Coordinate> coordinates = coordinateService.findByProject(project);
        List<Build> builds = buildService.findByProjectAndSource(project, source);
        JSONArray jsonArray = matchCoordinate(coordinates);
        JSONArray jsonArray1 = returnSimpleBuildJson(builds);
        JSONObject jsonObject1 = new JSONObject();
        if (!jsonArray.isEmpty()) {
            jsonObject1.put("coordinates", jsonArray);
        }
        if (!jsonArray1.isEmpty()) {
            jsonObject1.put("buildModel", jsonArray1);
        }
        return jsonObject1;
    }

    private void writeProperty(List<Build> list, JSONArray jsonArray) {
        JSONObject jsonObject;
        for (Build build : list) {
            jsonObject = new JSONObject();
            jsonObject.put("id", build.getId());
            jsonObject.put("name", build.getName());
            jsonObject.put("alias", build.getAlias());
            jsonObject.put("type", build.getType());
            jsonObject.put("centerCoor", build.getCenterCoor());
            jsonObject.put("positionCoor", build.getPositionCoor());
            jsonObject.put("remark", build.getRemark());
            jsonArray.add(jsonObject);
        }
    }

    /**
     * 匹配建筑物数据
     * sign=true 匹配属性
     *
     * @param build
     * @param build1
     * @param sign
     */
    public void setProperty(Build build, Build build1, boolean sign) {
        build.setCenterCoor(build1.getCenterCoor());
        build.setPositionCoor(build1.getPositionCoor());
        build.setDesignElevation(build1.getDesignElevation());
        build.setId(build1.getId());
        build.setRemark(build1.getRemark());
        if (!sign) {
            return;
        }
        build.setBuildAttributes((List<BuildAttribute>) SettingUtils.objectCopy(build1.getBuildAttributes()));
//        attributeGroupNotNuLL(build.getCoordinate(), build1.getAttribeList());

        attributeGroupNotNuLL(build.getWaterResources(), build1.getBuildAttributes());

        attributeGroupNotNuLL(build.getControlSize(), build1.getBuildAttributes());

        attributeGroupNotNuLL(build.getGroundStress(), build1.getBuildAttributes());

        attributeGroupNotNuLL(build.getComponent(), build1.getBuildAttributes());
    }

    private void attributeGroupNotNuLL(AttributeGroup attributeGroup, List<BuildAttribute> attributeList) {
        if (attributeGroup == null) {
            return;
        }
        setAttribute(attributeGroup.getBuildAttributes(), attributeList);
        setChild(attributeGroup.getChilds(), attributeList);
    }

    /**
     * 匹配属性
     *
     * @param buildAttributes    输出的
     * @param attributeList 数据库中的
     */
    private void setAttribute(List<BuildAttribute> buildAttributes, List<BuildAttribute> attributeList) {
        if (buildAttributes == null) {
            return;
        }
        for (BuildAttribute buildAttribute : buildAttributes) {
            for (BuildAttribute buildAttribute1 : attributeList) {
                if (buildAttribute.getAlias().equals(buildAttribute1.getAlias())) {
                    buildAttribute.setValue(buildAttribute1.getValue());
                    buildAttribute.setId(buildAttribute1.getId());
                    buildAttribute.setCreateDate(buildAttribute1.getCreateDate());
                    attributeList.remove(buildAttribute1);
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
    private void setChild(List<AttributeGroup> attributeGroups, List<BuildAttribute> attributeList) {
        if (attributeGroups == null) {
            return;
        }
        for (AttributeGroup attributeGroup : attributeGroups) {
            setAttribute(attributeGroup.getBuildAttributes(), attributeList);
            setChild(attributeGroup.getChilds(), attributeList);
        }
    }

    /**
     * 对比差异数据并挑选未改变的数据组成新的list
     *
     * @param attributeList
     * @param attributes
     * @return
     */
    public List<BuildAttribute> contrastEditAttribute(List<BuildAttribute> attributeList, List<BuildAttribute> attributes) {
        List<BuildAttribute> list = new ArrayList<>();
        if (attributeList == null || attributeList.size() == 0) {
            list.addAll(attributes);
            return list;
        }
        if (attributes.size() == 0) {
            return null;
        }
        boolean flag;
        BuildAttribute attribute1 = null;
        for (BuildAttribute attribute : attributeList) {
            flag = false;
            for (int i = 0; i < attributes.size(); i++) {
                attribute1 = attributes.get(i);
                if (attribute.getAlias().equals(attribute1.getAlias())) {
                    attribute.setValue(attribute1.getValue());
                    list.add(attribute);
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                list.add(attribute1);
            }
        }
        return list;
    }

    /**
     * 根据项目以及保存时的来源（设计、外业）输出数据到excel
     *
     * @param project
     * @return
     */
    public Workbook writeExcelByDesign(Project project, Coordinate.WGS84Type wgs84Type) {
        List<Coordinate> coordinates = coordinateService.findByProject(project);
        JSONArray jsonArray = matchCoordinate(coordinates);
        List<Build> builds2 = buildService.findByProjectAndSource(project, Build.Source.DESIGN);
        List<Build> list = matchBuild(builds2, true);
        putBuildOnline(jsonArray, list);
        Workbook wb = new XSSFWorkbook();
        String central = coordinateService.getCoordinateBasedatum(project);
        if (central == null || central.equals("null") || central.equals("")) {
            return null;
        }
        String code = transFromService.checkCode84(central);
        writeCoordinateToExcelDesign(jsonArray, wb, code, wgs84Type);
        buildService.outputBuilds(list, wb, code, wgs84Type);
        return wb;
    }

    public Workbook writeExcelByFieldWork(Project project, Coordinate.WGS84Type wgs84Type) {
        List<FieldWork> fieldWorks =findByProject(project);
        JSONArray jsonArray = matchFieldWork(fieldWorks);
        List<Build> builds2 = buildService.findByProjectAndSource(project, Build.Source.FIELD);
        List<Build> list = matchBuild(builds2, true);
        putBuildOnline(jsonArray, list);
        Workbook wb = new XSSFWorkbook();
        String central = coordinateService.getCoordinateBasedatum(project);
        if (central == null || central.equals("null") || central.equals("")) {
            return null;
        }
        String code = transFromService.checkCode84(central);
        writeCoordinateToExcelField(jsonArray, wb, code, wgs84Type);
        return wb;
    }

    private List<FieldWork> findByProject(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        return fieldWorkDao.findList(0, null, filters);
    }

    /**
     * 将外业坐标数据输出到excel
     *
     * @param jsonArray1
     * @param wb
     * @param code
     * @param wgs84Type
     */
    private void writeCoordinateToExcelField(JSONArray jsonArray1, Workbook wb, String code, Coordinate.WGS84Type wgs84Type) {
        JSONArray jsonArray2 = new JSONArray();
        for (Object o : jsonArray1) {
            JSONObject jsonObject = JSONObject.fromObject(o);
            JSONArray jsonArray = (JSONArray) jsonObject.get("coordinate");
            for (Object o1 : jsonArray) {
                JSONObject jsonObject1 = JSONObject.fromObject(o1);
                jsonArray2.add(jsonObject1);
            }
        }
        Sheet sheet = wb.createSheet("外业勘测数据");
        Row row = null;
        Cell cell = null;
        int i = 0;
        coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundYellow(wb), "经度", "纬度", "高程", "类型", "描述", null, null, wb, true);
        JSONObject jsonObject, jsonObject1;
        for (Object o : jsonArray2) {
            jsonObject = JSONObject.fromObject(o);
            jsonObject1 = coordinateBLHToXYZ(jsonObject.get("lon").toString(), jsonObject.get("lat").toString(), code, wgs84Type);
            coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundSkyBlue(wb), jsonObject1.get("lon").toString(), jsonObject1.get("lat").toString(), jsonObject.get("ele").toString(), jsonObject.get("baseType") == null ? null : jsonObject.get("baseType").toString(), jsonObject.get("description") == null ? null : jsonObject.get("description").toString(), null, null, wb, true);
        }
    }

    /**
     * 设计数据输出到excel将
     * @param code
     * @param jsonArray1
     * @param wb
     * @param wgs84Type
     */
    private void writeCoordinateToExcelDesign(JSONArray jsonArray1, Workbook wb, String code, Coordinate.WGS84Type wgs84Type) {
        Map<CommonEnum.CommonType, JSONArray> map = new LinkedHashMap<>();
        JSONObject jsonObject;
        for (Object o : jsonArray1) {
            jsonObject = JSONObject.fromObject(o);
            String baseType = jsonObject.get("baseType").toString();
            JSONArray jsonArray2 = map.get(CommonEnum.CommonType.valueOf(baseType));
            if (jsonArray2 == null) {
                jsonArray2 = new JSONArray();
                jsonArray2.add(jsonObject);
                map.put(CommonEnum.CommonType.valueOf(baseType), jsonArray2);
            } else {
                jsonArray2.add(jsonObject);
                map.put(CommonEnum.CommonType.valueOf(baseType), jsonArray2);
            }
        }
        writeDesignCoordinateToExcel(map, wb, code, wgs84Type);
    }

    /**
     * 设计数据写入excel
     *
     * @param code
     * @param map
     * @param wb
     * @param wgs84Type
     */
    private void writeDesignCoordinateToExcel(Map<CommonEnum.CommonType, JSONArray> map, Workbook wb, String code, Coordinate.WGS84Type wgs84Type) {
        Sheet sheet;
        Row row = null;
        Cell cell = null;
        JSONObject jsonObject,jsonObject1;
        JSONArray jsonArray;
        for (Map.Entry<CommonEnum.CommonType, JSONArray> entry : map.entrySet()) {
            sheet = wb.createSheet(entry.getKey().toString());
            int i = 0;
            for (Object o : entry.getValue()) {
                jsonObject = (JSONObject) o;
                coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundSkyBlue(wb), "描述", jsonObject.get("description").toString(), null, null, null, null, null, wb, true);
                coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundYellow(wb), "经度", "纬度", "高程", "类型", "描述", null, null, wb, true);
                jsonArray = (JSONArray) jsonObject.get("coordinate");
                for (Object o1 : jsonArray) {
                    jsonObject = (JSONObject) o1;
                    jsonObject1 = coordinateBLHToXYZ(jsonObject.get("lon").toString(), jsonObject.get("lat").toString(), code, wgs84Type);
                    coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundSkyBlue(wb), jsonObject1.get("lon").toString(), jsonObject1.get("lat").toString(), jsonObject.get("ele").toString(), jsonObject.get("baseType") == null?null:jsonObject.get("baseType").toString(), jsonObject.get("description") == null?null:jsonObject.get("description").toString(), null, null, wb, true);
                }
            }

        }
    }

    /**
     * 将建筑物匹配到线上用于输出
     *
     * @param jsonArray
     * @param builds
     */
    private void putBuildOnline(JSONArray jsonArray, List<Build> builds) {
        JSONObject coordinates, jsonObject1;
        String longitude, latitude, elevation;
        Build build;
        for (Object o : jsonArray) {
            coordinates = (JSONObject) o;
            JSONArray coordinate1 = (JSONArray) coordinates.get("coordinate");
            for (Object o1 : coordinate1) {
                jsonObject1 = (JSONObject) o1;
                longitude = jsonObject1.get("lon").toString();
                latitude = jsonObject1.get("lat").toString();
                elevation = jsonObject1.get("ele").toString();
                build = allEqual(builds, longitude, latitude, elevation);
                if (build != null) {
                    jsonObject1.put("baseType", build.getType().getTypeC());
                    jsonObject1.put("description", build.getRemark());
                }
            }
        }
    }

    /**
     * 筛选坐标数据，并归类
     *
     * @param coordinates
     * @return
     */
    public JSONArray matchCoordinate(List<Coordinate> coordinates) {
        JSONArray jsonArray = new JSONArray();
        for (Coordinate coordinate : coordinates) {
            putIdInJson(coordinate, jsonArray, coordinate.getCoordinateStr());
        }
        return jsonArray;
    }

    public JSONArray matchFieldWork(List<FieldWork> fieldWorks) {
        JSONArray jsonArray = new JSONArray();
        for (FieldWork fieldWork : fieldWorks) {
            JSONArray jsonArray1 = JSONArray.fromObject(fieldWork.getCoordinateStr());
            for (Object o : jsonArray1) {
                putIdInJson(fieldWork,jsonArray,o);
            }
        }
        return jsonArray;
    }

    /**
     * 将id放入Json中
     *
     * @param object
     * @param jsonArray
     * @param str
     */
    private void putIdInJson(Object object, JSONArray jsonArray, Object str) {
        JSONObject jsonObject = JSONObject.fromObject(str);
        JSONObject jsonObject11 = jsonObject;
        if (object instanceof Coordinate) {
            jsonObject11.put("id", ((Coordinate)object).getId());
            jsonObject11.put("baseType", ((Coordinate)object).getCommonType());
            jsonObject11.put("type", ((Coordinate)object).getCommonType().getType().toUpperCase());
            jsonObject11.put("description", ((Coordinate)object).getDescription());
        }
        if (object instanceof FieldWork) {
            jsonObject11.put("id", ((FieldWork)object).getId());
            if (((FieldWork)object).getName() != null) {
                jsonObject11.put("name", ((FieldWork)object).getName());
                jsonObject11.put("modifyDate", ((FieldWork)object).getModifyDate());
                jsonObject11.put("deviceMac", ((FieldWork)object).getDeviceMac());
            }
        }
        jsonArray.add(jsonObject11);
    }

    /**
     * 匹配建筑
     * sign=true同时匹配属性
     * sign=false不匹配属性
     *
     * @param builds2
     * @param sign
     * @return
     */
    private List<Build> matchBuild(List<Build> builds2, boolean sign) {
        Build build = null;
        List<Build> list = new ArrayList<>();
        List<Build> builds = buildService.getBuilds();
        for (Build build2 : builds2) {
            for (Build build1 : builds) {
                if (build2.getType().equals(build1.getType())) {
                    build = (Build) SettingUtils.objectCopy(build1);
                    setProperty(build, build2, sign);
                    list.add(build);
                    break;
                }
            }
        }
        return list;
    }

    public JSONArray getModelType() {
        JSONArray jsonArray = new JSONArray(), jsonArray1;
        JSONObject jsonObject, jsonObject1;
        for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
            if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("baseType", commonType.getType());
            jsonArray1 = new JSONArray();
            for (Build.ChildType value : Build.ChildType.values()) {
                if (value.getCommonType() == commonType) {
                    jsonObject1 = new JSONObject();
                    jsonObject1.put("baseType", value.getType());
                    jsonObject1.put("type", value.name());
                    jsonObject1.put("name", value.getTypeC());
                    jsonObject1.put("abbreviate", value.getAbbreviate());
                    jsonArray1.add(jsonObject1);
                }
            }
            if (!jsonArray1.isEmpty()) {
                jsonObject.put("childTypes", jsonArray1);
            }
            jsonObject.put("type", commonType.name());
            jsonObject.put("name", commonType.getTypeC());
            jsonObject.put("abbreviate", commonType.getAbbreviate());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * 构建模板
     *
     * @param list
     * @return
     */
    public Workbook downloadModel(List<String> list) {
        Workbook wb = new XSSFWorkbook();
        List<Build> builds = buildService.getBuilds();
        List<Build> builds1 = new LinkedList<>();
        List<String> lineAera = new LinkedList<>();
        for (String s : list) {
            for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
                if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                    continue;
                }
                if (commonType.name().equals(s)) {
                    if (commonType.getType().equals("line") || commonType.getType().equals("area")) {
                        lineAera.add(s);
                    } else {
                        for (Build build : builds) {
                            if (build.getType().toString().equals(s)) {
                                builds1.add((Build) SettingUtils.objectCopy(build));
                                break;
                            }
                        }
                    }
                }
            }
        }
        writeLineAreaModel(wb, lineAera);
        buildService.outBuildModel(wb, builds1);
        return wb;
    }

    /**
     * 构建线面模板
     *
     * @param wb
     * @param lineArea
     */
    private void writeLineAreaModel(Workbook wb, List<String> lineArea) {
        for (String s : lineArea) {
            int i = 0;
            Sheet sheet = null;
            Row row = null;
            Cell cell = null;
            for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
                if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                    continue;
                }
                if (commonType.name().equals(s)) {
                    sheet = wb.createSheet(commonType.getTypeC());
                    break;
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
            coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundSkyBlue(wb), "描述", null, null, null, null, null, null, wb, true);
            coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldHaveBackgroundYellow(wb), "经度", "纬度", "高程", "类型", "描述", "select", selects, wb, true);
//            for (int j = 0; j < 10000; j++) {
//                coordinateService.writeToCell(i++, sheet, row, cell, buildService.noBoldAndLocked(wb), null, null, null, null, null, "select", selects, wb, false);
//            }
//            sheet.protectSheet("hyswqqsl");
        }
    }

    /**
     * 新建建筑物
     *
     * @param type
     * @param centerCoor
     * @param remark
     * @param projectId
     */
    public boolean newBuild(Object type, Object centerCoor, Object remark, Object projectId,Object commonId) {
        Build build1 = null;
        List<Build> builds = buildService.getBuilds();
        for (Build build : builds) {
            if (build.getType().toString().equals(type.toString())) {
                build1 = (Build) SettingUtils.objectCopy(build);
                break;
            }
        }
        if (build1 == null) {
            return false;
        }
        build1.setSource(Build.Source.DESIGN);
        JSONObject jsonObject = JSONObject.fromObject(centerCoor);
//        if (jsonObject.get("lon") == null || jsonObject.get("lat") == null || jsonObject.get("ele") == null) {
//            return false;
//        }
        build1.setCenterCoor(String.valueOf(jsonObject));
        build1.setRemark(remark.toString());
        Project project = projectService.find(Long.valueOf(projectId.toString()));
        build1.setProjectId(project.getId());
//        build1.setCommonId(Long.valueOf(commonId.toString()));
        buildService.save(build1);
        return true;
    }


    /**
     * 编辑建筑物
     *
     * @param build
     * @param remark
     * @param type
     * @param attributes
     * @return
     */
    public boolean editBuild(Build build, Object remark, Object type, Object attributes) {
        if ((build.getBuildAttributes() == null || build.getBuildAttributes().size() == 0) && type != null) {
            build.setType(CommonEnum.CommonType.valueOf(type.toString()));
        }
        BuildAttribute buildAttribute;
        List<BuildAttribute> buildAttributes = new ArrayList<>();
        if (attributes != null) {
            for (Map<String, Object> map : (List<Map<String, Object>>) attributes) {
                if (map.get("alias") == null || map.get("value") == null) {
                    return false;
                }
                buildAttribute = new BuildAttribute();
                buildAttribute.setAlias(map.get("alias").toString());
                buildAttribute.setValue(map.get("value").toString());
                buildAttributes.add(buildAttribute);
            }
        }
        List<BuildAttribute> list = contrastEditAttribute(build.getBuildAttributes(), buildAttributes);
        build.setBuildAttributes(list);
        if (remark != null) {
            build.setRemark(remark.toString());
        }
        buildService.save(build);
        return true;
    }


    /**
     * 根据source类型传回相应的类型的坐标数据与简单的建筑物信息
     *
     * @param builds2
     * @return
     */
    private JSONArray returnSimpleBuildJson(List<Build> builds2) {
        JSONArray jsonArray1 = new JSONArray();
        JSONObject jsonObject;
        for (Build build : builds2) {
            jsonObject = new JSONObject();
            jsonObject.put("id", build.getId());
            jsonObject.put("centerCoor", build.getCenterCoor());
//            if (build.getCommonId() != null) {
//                jsonObject.put("commonId", build.getCommonId());
//            }
            if (build.getPositionCoor() != null) {
                jsonObject.put("positionCoor", build.getPositionCoor());
            }
            if (build.getBuildAttributes().size() > 0) {
                jsonObject.put("simpleFlag", false);
            } else {
                jsonObject.put("simpleFlag", true);
            }
            jsonObject.put("type", build.getType());
            jsonObject.put("remark", build.getRemark() == null ? "" : build.getRemark());
            if (build.getChildType() != null) {
                jsonObject.put("childType", build.getChildType());
            }
            jsonArray1.add(jsonObject);
        }
        return jsonArray1;
    }

    /**
     * 将大地坐标转换为各类坐标
     *
     * @param longitude
     * @param latitude
     * @param code
     * @return
     */
    public JSONObject coordinateBLHToXYZ(String longitude, String latitude, String code, Coordinate.WGS84Type wgs84Type) {
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

    private JSONObject planeCoordinate(String longitude, String latitude, String code) {
        ProjCoordinate projCoordinate = transFromService.BLHToXYZ(code, Double.valueOf(longitude), Double.valueOf(latitude));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", projCoordinate.x);
        jsonObject.put("lat", projCoordinate.y);
        return jsonObject;
    }

    private JSONObject degreeMinuteSecond2(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        String c = b.substring(0, b.indexOf("."));
        String d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        longitude = a + "°" + c + "'" + d + "\"";
        jsonObject.put("lon", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        c = b.substring(0, b.indexOf("."));
        d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        latitude = a + "°" + c + "'" + d + "\"";
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinuteSecond1(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        String c = b.substring(0, b.indexOf("."));
        String d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        longitude = a + ":" + c + ":" + d;
        jsonObject.put("lon", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        c = b.substring(0, b.indexOf("."));
        d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        latitude = a + ":" + c + ":" + d;
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinute2(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        longitude = a + "°" + b + "'";
        jsonObject.put("lon", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        latitude = a + "°" + b + "'";
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinute1(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        longitude = a + ":" + b;
        jsonObject.put("lon", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        latitude = a + ":" + b;
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private JSONObject degree(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }
}
