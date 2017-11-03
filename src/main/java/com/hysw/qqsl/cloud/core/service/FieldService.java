package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.build.CoordinateBase;
import com.hysw.qqsl.cloud.core.entity.build.Graph;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.entity.element.Position;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.osgeo.proj4j.ProjCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *
 * 外业测量service
 * Created by chenl on 17-4-13.
 */
@Service("fieldService")
public class FieldService {
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private AttribeService attribeService;
    @Autowired
    private TransFromService transFromService;


    public Message saveField(Map<String, Object> objectMap) {
        Build build;
        Attribe attribe;
        CoordinateBase coordinateBase;
        List<CoordinateBase> coordinateBases;
        JSONObject jsonObject;
        Graph graph;
        Object code;
        List<Graph> graphs = new ArrayList<>();
        Coordinate coordinate2=null;
        Object projectId = objectMap.get("projectId");
        Object userId = objectMap.get("userId");
        Object name = objectMap.get("name");
        Object deviceMac = objectMap.get("deviceMac");
        List<Object> coordinates = (List<Object>) objectMap.get("coordinates");
        if (projectId == null || userId == null || name == null || deviceMac == null) {
            return new Message(Message.Type.FAIL);
        }
        Project project = projectService.find(Long.valueOf(projectId.toString()));
        List<Build> builds1 = buildService.findByProjectAndSource(project, Build.Source.FIELD);
//        List<Attribe> attribes1=attribeService.findByBuilds(builds1);
        List<Coordinate> coordinates1 = coordinateService.findByProjectAndSource(project, Build.Source.FIELD);
        for (int i = 0; i < coordinates.size(); i++) {
            boolean flag = false;
            Map<String,Object> coordinate= (Map<String, Object>) coordinates.get(i);
            Object type = coordinate.get("type");
            if (type == null) {
                return new Message(Message.Type.FAIL);
            }
            Object center = coordinate.get("center");
            Object remark = coordinate.get("description");
            Object delete = coordinate.get("delete");
            if (remark == null) {
                return new Message(Message.Type.FAIL);
            }
            if (center == null) {
                return new Message(Message.Type.FAIL);
            }
            Object longitude = ((Map<String,String>)center).get("longitude");
            Object latitude = ((Map<String,String>)center).get("latitude");
            Object elevation = ((Map<String,String>)center).get("elevation");
            Object attribes = coordinate.get("attribes");
            Object description = coordinate.get("description");
            Object alias = coordinate.get("alias");
            if (longitude == null || latitude == null || elevation == null || description == null||alias==null) {
                return new Message(Message.Type.FAIL);
            }
            if (!SettingUtils.coordinateParameterCheck(longitude, latitude, elevation)) {
                return new Message(Message.Type.FAIL);
            }
            coordinateBase = new CoordinateBase();
            coordinateBases = new ArrayList<>();
            coordinateBase.setLongitude(longitude.toString());
            coordinateBase.setLatitude(latitude.toString());
            coordinateBase.setElevation(elevation.toString());
            coordinateBases.add(coordinateBase);
            graph = new Graph();
            graph.setCoordinates(coordinateBases);
            graph.setBaseType(CommonEnum.CommonType.valueOf(type.toString()));
            graph.setDescription(description.toString());
            graph.setAlias(alias.toString());
            graphs.add(graph);
            if (attribes != null) {
                build = isSameBuild(builds1, longitude.toString(), latitude.toString());
                if (delete != null && Boolean.valueOf(delete.toString()) == true) {
                    deleteBuildAndAttribes(build);
                    continue;
                }
                if (build == null) {
                    flag = true;
                    build = new Build();
                    build.setProject(project);
                    build.setType(CommonEnum.CommonType.valueOf(type.toString()));
                    build.setSource(Build.Source.FIELD);
                    build.setRemark(remark.toString());
                }
                Object position = coordinate.get("position");
                if (position != null) {
                    jsonObject = new JSONObject();
                    Object longitude1 = ((Map<String,String>)position).get("longitude");
                    Object latitude1 = ((Map<String,String>)position).get("latitude");
                    Object elevation1 = ((Map<String,String>)position).get("elevation");
                    jsonObject.put("longitude", longitude1.toString());
                    jsonObject.put("latitude", latitude1.toString());
                    jsonObject.put("elevation", elevation1.toString());
                    build.setPositionCoor(jsonObject.toString());
                }
                jsonObject = new JSONObject();
                jsonObject.put("longitude", longitude.toString());
                jsonObject.put("latitude", latitude.toString());
                jsonObject.put("elevation", elevation.toString());
                build.setCenterCoor(jsonObject.toString());
//                for (int j = 0; j < CommonAttributes.BASETYPEE.length; j++) {
//                    if (CommonAttributes.BASETYPEE[j].equals(type.toString().trim())) {
//                        build.setName(CommonAttributes.BASETYPEC[j]);
//                        break;
//                    }
//                }
                List<Attribe> attribeList1 = new ArrayList<>();
                for (Map<String, Object> map : ((List<Map<String, Object>>) attribes)) {
                    attribe = new Attribe();
                    code = map.get("code");
                    if (map.get("alias") == null||map.get("value") == null) {
                        continue;
                    }
                    attribe.setAlias(map.get("alias").toString());
                    attribe.setValue(map.get("value").toString());
                    if (code != null) {
                        attribe.setCode(Integer.valueOf(code.toString()));
                    }
                    attribe.setBuild(build);
                    attribeList1.add(attribe);
                }
                List<Attribe> list;
                if (!flag) {
                    list = contrastEditAttribe(build.getAttribeList(), attribeList1);
                }else{
                    list = new ArrayList<>();
                    list.addAll(attribeList1);
                }
                build.setAttribeList(list);
                buildService.save(build);
            }
        }
        JSONArray jsonArray = new JSONArray();
        listToString(graphs, jsonArray);
        for (Coordinate coordinate1 : coordinates1) {
            if (coordinate1.getDeviceMac().equals(deviceMac.toString().trim())) {
                coordinate2 = coordinate1;
                break;
            }
        }
        if (coordinate2 == null) {
            coordinate2 = new Coordinate();
            coordinate2.setProject(project);
            coordinate2.setName(name.toString());
            coordinate2.setDeviceMac(deviceMac.toString());
            coordinate2.setUserId(Long.valueOf(userId.toString()));
        }
        coordinate2.setDescription(deviceMac.toString()+":"+project.getId());
        coordinate2.setCoordinateStr(jsonArray.toString());
        coordinate2.setSource(Build.Source.FIELD);
        coordinateService.save(coordinate2);
        return new Message(Message.Type.OK);
    }

    /**
     * 删除建筑物以及其下所有属性
     * @param build
     */
    private void deleteBuildAndAttribes(Build build) {
        for (Attribe attribe : build.getAttribeList()) {
            attribeService.remove(attribe);
        }
        buildService.remove(build);
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
     * @param builds
     * @param longitude1
     * @param latitude1
     * @param elevation1
     * @return
     */
    public Build allEqual(List<Build> builds, String longitude1, String latitude1,String elevation1){
        for (Build build : builds) {
            JSONObject centerCoor = JSONObject.fromObject(build.getCenterCoor());
            String longitude = centerCoor.get("longitude").toString();
            String latitude = centerCoor.get("latitude").toString();
            String elevation = centerCoor.get("elevation").toString();
            if (longitude1.equals(longitude)&&latitude1.equals(latitude)&&elevation1.equals(elevation)) {
                return build;
            }
        }
        return null;
    }

    /**
     * 点线面坐标文件转字符串
     * @param list
     * @return
     */
    private String listToString(List<Graph> list, JSONArray coordinates) {
        List<JSONObject> objects=new ArrayList<>();
        JSONObject jsonObject;
        JSONObject coordinate;
//        Coordinate.Type type = null;
        CommonEnum.CommonType baseType = null;
        String description = null;
        for (int i = 0; i < list.size(); i++) {
            jsonObject=new JSONObject();
            jsonObject.put("longitude",list.get(i).getCoordinates().get(0).getLongitude());
            jsonObject.put("latitude",list.get(i).getCoordinates().get(0).getLatitude());
            jsonObject.put("elevation",list.get(i).getCoordinates().get(0).getElevation());
//            type= Coordinate.Type.FIELD;
            baseType=list.get(i).getBaseType();
            if(list.get(i).getDescription()==null) {
                continue;
            }else{
                description=list.get(i).getDescription();
            }
            objects.add(jsonObject);
            coordinate=new JSONObject();
            coordinate.put("coordinate",objects);
            if(baseType!=null){
//                coordinate.put("type",type.toString());
                coordinate.put("baseType",baseType.toString());
                coordinate.put("alias", list.get(i).getAlias());
                if(description!=null){
                    coordinate.put("description",description);
                }
            }
            coordinates.add(coordinate);
            objects=new ArrayList<>();
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
        List<Coordinate> coordinates = coordinateService.findByProjectAndSource(project, source);
        List<Build> builds = buildService.findByProjectAndSource(project, source);
        JSONArray jsonArray = matchCoordinate(coordinates);
        JSONArray jsonArray1 = returnSimpleBuildJson(builds);
        JSONObject jsonObject1 = new JSONObject();
        if (!jsonArray.isEmpty()) {
            jsonObject1.put("coordinates", jsonArray);
        }
        if (!jsonArray1.isEmpty()) {
            jsonObject1.put("builds", jsonArray1);
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
     * @param build
     * @param build1
     * @param sign
     */
    public void setProperty(Build build, Build build1,boolean sign) {
        build.setCenterCoor(build1.getCenterCoor());
        build.setPositionCoor(build1.getPositionCoor());
        build.setId(build1.getId());
        build.setRemark(build1.getRemark());
        if (!sign) {
            return;
        }
        build.setAttribeList((List<Attribe>) SettingUtils.objectCopy(build1.getAttribeList()));
        attribeGroupNotNuLL(build.getMaterAttribeGroup(),build1.getAttribeList());
//        setAttribe(build.getMaterAttribeGroup().getAttribes(),build1.getAttribeList());
//        setChild(build.getMaterAttribeGroup().getChilds(),build1.getAttribeList());
        attribeGroupNotNuLL(build.getDimensionsAttribeGroup(),build1.getAttribeList());
//        setAttribe(build.getDimensionsAttribeGroup().getAttribes(),build1.getAttribeList());
//        setChild(build.getDimensionsAttribeGroup().getChilds(),build1.getAttribeList());

        attribeGroupNotNuLL(build.getHydraulicsAttribeGroup(),build1.getAttribeList());
//        setAttribe(build.getHydraulicsAttribeGroup().getAttribes(),build1.getAttribeList());
//        setChild(build.getHydraulicsAttribeGroup().getChilds(),build1.getAttribeList());

        attribeGroupNotNuLL(build.getGeologyAttribeGroup(),build1.getAttribeList());
//        setAttribe(build.getGeologyAttribeGroup().getAttribes(),build1.getAttribeList());
//        setChild(build.getGeologyAttribeGroup().getChilds(),build1.getAttribeList());

        attribeGroupNotNuLL(build.getStructureAttribeGroup(),build1.getAttribeList());
//        setAttribe(build.getStructureAttribeGroup().getAttribes(),build1.getAttribeList());
//        setChild(build.getStructureAttribeGroup().getChilds(),build1.getAttribeList());

    }

    private void attribeGroupNotNuLL(AttribeGroup attribeGroup, List<Attribe> attribeList) {
        if (attribeGroup == null) {
            return;
        }
        setAttribe(attribeGroup.getAttribes(),attribeList);
        setChild(attribeGroup.getChilds(),attribeList);
    }

    /**
     * 匹配属性
     * @param attribes  输出的
     * @param attribeList   数据库中的
     */
    private void setAttribe(List<Attribe> attribes, List<Attribe> attribeList) {
        if (attribes == null) {
            return;
        }
        for (Attribe attribe : attribes) {
            for (Attribe attribe1 : attribeList) {
                if (attribe.getAlias().equals(attribe1.getAlias())&&attribe.getCode()==attribe1.getCode()) {
                    attribe.setValue(attribe1.getValue());
                    attribe.setId(attribe1.getId());
                    attribe.setCreateDate(attribe1.getCreateDate());
                    attribeList.remove(attribe1);
                    break;
                }
            }
//            if(attribe.get)
        }
    }

    /**
     * 子节点匹配属性
     * @param attribeGroups
     * @param attribeList
     */
    private void setChild(List<AttribeGroup> attribeGroups,List<Attribe> attribeList){
        if (attribeGroups == null) {
            return;
        }
        for (AttribeGroup attribeGroup : attribeGroups) {
            setAttribe(attribeGroup.getAttribes(),attribeList);
            setChild(attribeGroup.getChilds(),attribeList);
        }
    }

    /**
     * 对比差异数据并挑选未改变的数据组成新的list
     * @param attribeList
     * @param attribes
     * @return
     */
    public List<Attribe> contrastEditAttribe(List<Attribe> attribeList, List<Attribe> attribes) {
        List<Attribe> list = new ArrayList<>();
        if (attribeList==null||attribeList.size() == 0) {
            list.addAll(attribes);
            return list;
        }
        if (attribes.size() == 0) {
            return null;
        }
        boolean flag;
        Attribe attribe1 = null;
        for (Attribe attribe : attribeList) {
            flag=false;
            for (int i = 0; i < attribes.size(); i++) {
                attribe1 = attribes.get(i);
                if (attribe.getAlias().equals(attribe1.getAlias())&&attribe.getCode()==attribe1.getCode()) {
                    attribe.setValue(attribe1.getValue());
                    list.add(attribe);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                list.add(attribe1);
            }
        }
        return list;
    }

    /**
     * 根据项目以及保存时的来源（设计、外业）输出数据到excel
     * @param project
     * @param source
     * @return
     */
    public Workbook writeExcel(Project project, Build.Source source, Coordinate.WGS84Type wgs84Type) {
        List<Coordinate> coordinates = coordinateService.findByProjectAndSource(project,source);
        JSONArray jsonArray=matchCoordinate(coordinates);
        List<Build> builds2 = buildService.findByProjectAndSource(project,source);
        List<Build> list=matchBuild(builds2,true);
        Map<CommonEnum.CommonType,List<Build>>map=groupBuild(list);
        putBuildOntheLine(jsonArray, list);
        Workbook wb = new HSSFWorkbook();
        String central = coordinateService.getCoordinateBasedatum(project);
        if (central == null || central.equals("null") || central.equals("")) {
            return null;
        }
        String code = transFromService.checkCode84(central);
        writeCoordinateToExcel(jsonArray,wb,source,code,wgs84Type);
        writeBuildToExcel(map,wb,code,wgs84Type);
        return wb;
    }

    /**
     * 将坐标数据输出到excel
     * @param code
     * @param jsonArray1
     * @param wb
     * @param source
     * @param wgs84Type
     */
    private void writeCoordinateToExcel(JSONArray jsonArray1, Workbook wb, Build.Source source, String code, Coordinate.WGS84Type wgs84Type) {
        if (source == Build.Source.DESIGN) {
            writeCoordinateToExcelDesign(jsonArray1, wb, code,wgs84Type);
        }else{
            writeCoordinateToExcelField(jsonArray1, wb, code,wgs84Type);
        }
    }

    /**
     * 将外业坐标数据输出到excel
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
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        Row row = null;
        Cell cell = null;
        WriteExecl we = new WriteExecl();
        writeToCell(sheet,row,cell,style,we,"经度","纬度","高程","类型","描述");
        String baseType = null,description = null,longitude,latitude;
        for (Object o : jsonArray2) {
            JSONObject jsonObject = JSONObject.fromObject(o);
            if (jsonObject.get("baseType") != null) {
                for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
                    if (CommonAttributes.BASETYPEE[i].equals(jsonObject.get("baseType").toString().trim())) {
                        baseType = CommonAttributes.BASETYPEC[i];
                    }
                }
            }
            if (jsonObject.get("description") != null) {
                description = jsonObject.get("description").toString();
            }
            longitude = jsonObject.get("longitude").toString();
            latitude = jsonObject.get("latitude").toString();
            JSONObject jsonObject1 = coordinateBLHToXYZ(longitude, latitude, code, wgs84Type);
            writeToCell(sheet,row,cell,style,we,jsonObject1.get("longitude").toString(),jsonObject1.get("latitude").toString(),jsonObject.get("elevation").toString(),baseType,description);
        }
    }

    /**
     * 将设计数据输出到excel
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
                map.put(CommonEnum.CommonType.valueOf(baseType),jsonArray2);
            }else{
                jsonArray2.add(jsonObject);
                map.put(CommonEnum.CommonType.valueOf(baseType),jsonArray2);
            }
        }
        writeDesignCoordinateToExcel(map, wb, code,wgs84Type);
    }

    /**
     * 设计数据写入excel
     * @param code
     * @param map
     * @param wb
     * @param wgs84Type
     */
    private void writeDesignCoordinateToExcel(Map<CommonEnum.CommonType, JSONArray> map, Workbook wb, String code, Coordinate.WGS84Type wgs84Type) {
        for (Map.Entry<CommonEnum.CommonType, JSONArray> entry : map.entrySet()) {
            Sheet sheet = null;
            Row row = null;
            Cell cell = null;
            JSONObject jsonObject1;
            Object longitude,latitude,elevation,baseType,description;
            WriteExecl we = new WriteExecl();
            for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
                if (CommonAttributes.BASETYPEE[i].equals(entry.getKey().toString())) {
                    sheet = wb.createSheet(CommonAttributes.BASETYPEC[i]);
                    break;
                }
            }
            JSONArray value = entry.getValue();
            for (Object o : value) {
                JSONObject jsonObject = JSONObject.fromObject(o);
                CellStyle style = wb.createCellStyle();
                style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                writeToCell(sheet,row,cell,style,we,"描述",jsonObject.get("description").toString(),null,null,null);
                writeToCell(sheet,row,cell,style,we,"经度","纬度","高程","类型","描述");
                Object coordinate = jsonObject.get("coordinate");
                JSONArray jsonArray = JSONArray.fromObject(coordinate);
                for (Object o1 : jsonArray) {
                    String baseType1 = null;
                    String description1 = null;
                    jsonObject1 = JSONObject.fromObject(o1);
                    longitude = jsonObject1.get("longitude");
                    latitude = jsonObject1.get("latitude");
                    JSONObject jsonObject2 = coordinateBLHToXYZ(longitude.toString(), latitude.toString(), code, wgs84Type);
                    longitude = jsonObject2.get("longitude");
                    latitude = jsonObject2.get("latitude");
                    elevation = jsonObject1.get("elevation");
                    baseType = jsonObject1.get("baseType");
                    description = jsonObject1.get("description");
                    if (description != null) {
                        description1 = description.toString();
                    }
                    if (baseType != null) {
                        for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
                            if (baseType.toString().equals(CommonAttributes.BASETYPEE[i])) {
                                baseType1=CommonAttributes.BASETYPEC[i];
                            }
                        }
                    }
                    writeToCell(sheet,row,cell,style,we,longitude.toString(),latitude.toString(),elevation.toString(),baseType1,description1);
                }
            }
        }

    }

    /**
     * 写入单元格
     * @param sheet
     * @param row
     * @param cell
     * @param style
     * @param we
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     */
    private void writeToCell(Sheet sheet, Row row, Cell cell, CellStyle style, WriteExecl we, String a, String b, String c, String d, String e) {
        row = sheet.createRow(we.getIndexAdd());
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
        if (e != null && !e.equals("") && !e.equals("null")) {
            cell.setCellValue(e);
        }
        cell.setCellStyle(style);
    }

    /**
     * 将建筑物匹配到线上用于输出
     * @param jsonArray
     * @param builds
     */
    private void putBuildOntheLine(JSONArray jsonArray, List<Build> builds) {
        JSONObject coordinates,jsonObject1;
        String longitude,latitude,elevation;
        Build build;
        for (Object o : jsonArray) {
            coordinates = (JSONObject) o;
            JSONArray coordinate1 = (JSONArray) coordinates.get("coordinate");
            for (Object o1 : coordinate1) {
                jsonObject1 = (JSONObject) o1;
                longitude = jsonObject1.get("longitude").toString();
                latitude = jsonObject1.get("latitude").toString();
                elevation = jsonObject1.get("elevation").toString();
                build = allEqual(builds, longitude, latitude, elevation);
                if (build != null) {
                    jsonObject1.put("baseType", build.getType().toString());
                    jsonObject1.put("description", build.getRemark());
                }
            }
        }
    }

    /**
     * 筛选坐标数据，并归类
     * @param coordinates
     * @return
     */
    public JSONArray matchCoordinate(List<Coordinate> coordinates ) {
        JSONArray jsonArray = new JSONArray();
        for (Coordinate coordinate : coordinates) {
            if (coordinate.getSource() == Build.Source.FIELD) {
                JSONArray jsonArray1 = JSONArray.fromObject(coordinate.getCoordinateStr());
                for (Object o : jsonArray1) {
                    putIdInJson(coordinate,jsonArray,o);
                }
            }
            if (coordinate.getSource() == Build.Source.DESIGN) {
                putIdInJson(coordinate, jsonArray, coordinate.getCoordinateStr());
            }
        }
        return jsonArray;
    }

    /**
     * 将id放入Json中
     * @param coordinate
     * @param jsonArray
     * @param str
     */
    private void putIdInJson(Coordinate coordinate, JSONArray jsonArray, Object str) {
        JSONObject jsonObject = JSONObject.fromObject(str);
        JSONObject jsonObject11 = jsonObject;
        jsonObject11.put("id", coordinate.getId());
//        jsonObject11.put("description", coordinate.getDescription());
        if (coordinate.getName() != null) {
            jsonObject11.put("name", coordinate.getName());
            jsonObject11.put("modifyDate", coordinate.getModifyDate());
            jsonObject11.put("deviceMac", coordinate.getDeviceMac());
        }
        jsonArray.add(jsonObject11);
    }

    /**
     * 匹配建筑
     * sign=true同时匹配属性
     * sign=false不匹配属性
     * @param builds2
     * @param sign
     * @return
     */
    private List<Build> matchBuild(List<Build> builds2,boolean sign) {
        Build build = null;
        List<Build> list = new ArrayList<>();
        List<Build> builds = buildGroupService.getBuildsDynamic();
        for (Build build2 : builds2) {
            for (Build build1 : builds) {
                if (build2.getType().equals(build1.getType())) {
                    build = (Build) SettingUtils.objectCopy(build1);
                    setProperty(build,build2,sign);
                    list.add(build);
                    break;
                }
            }
        }
        return list;
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
     * 将建筑物写入excel
     * @param map
     * @param wb
     * @param code
     * @param wgs84Type
     */
    void writeBuildToExcel(Map<CommonEnum.CommonType, List<Build>> map, Workbook wb, String code, Coordinate.WGS84Type wgs84Type) {
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
                if (builds.get(i).getAttribeList() == null || builds.get(i).getAttribeList().size() == 0) {
                    continue;
                }
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

                JSONObject jsonObject1 = JSONObject.fromObject(builds.get(i).getCenterCoor());
                String longitude = jsonObject1.get("longitude").toString();
                String latitude = jsonObject1.get("latitude").toString();
                JSONObject jsonObject2 = coordinateBLHToXYZ(longitude, latitude, code, wgs84Type);
                longitude = jsonObject2.get("longitude").toString();
                latitude = jsonObject2.get("latitude").toString();
                String elevation = jsonObject1.get("elevation").toString();
                writeToCell(sheet,row,cell,style,we,"1","中心点","经度,纬度,高程",longitude+ "," + latitude + "," + elevation,"coor1",true);

                if (builds.get(i).getPositionCoor() != null&&!builds.get(i).getPositionCoor().equals("")) {
                    jsonObject1 = JSONObject.fromObject(builds.get(i).getPositionCoor());
                    longitude = jsonObject1.get("longitude").toString();
                    latitude = jsonObject1.get("latitude").toString();
                    jsonObject2 = coordinateBLHToXYZ(longitude, latitude, code, wgs84Type);
                    longitude = jsonObject2.get("longitude").toString();
                    latitude = jsonObject2.get("latitude").toString();
                    elevation = jsonObject1.get("elevation").toString();
                    writeToCell(sheet,row,cell,style,we,"2","定位点","经度,纬度,高程",longitude + "," + latitude + "," + elevation,"coor2",true);
                }

                int n = 1;
                if (builds.get(i).getRemark() != null) {
                    writeToCell(sheet,row,cell,style,we,num[n++],"描述",null,builds.get(i).getRemark(),"remark",true);
                }
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

                writeToExcel(style, sheet, row, cell, we, builds.get(i).getGeologyAttribeGroup(),num[n],false,false);

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

    }

    /**
     * 将数据写入单元格
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param sheet
     * @param row
     * @param cell
     * @param style
     * @param we
     * @param e
     */
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
     * 分类别写入excel
     * @param style
     * @param sheet
     * @param row
     * @param cell
     * @param we
     * @param attribeGroup
     * @param sign
     * @param flag
     * @param isComment
     * @return
     */
    boolean writeToExcel(CellStyle style, Sheet sheet, Row row, Cell cell, WriteExecl we, AttribeGroup attribeGroup,String sign,boolean flag,boolean isComment) {
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
        int a =0;
        if (attribeGroup.getAttribes() != null) {
            for (int j = 0; j < attribeGroup.getAttribes().size(); j++) {
                if (attribeGroup.getAttribes().get(j).getValue() != null && !attribeGroup.getAttribes().get(j).getValue().equals("")) {
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
                        int e = a + 1;
                        a++;
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
                }
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

    /**
     * 对建筑物进行归类
     * @param list
     * @return
     */
    protected Map<CommonEnum.CommonType, List<Build>> groupBuild(List<Build> list) {
        Map<CommonEnum.CommonType, List<Build>> map = new LinkedHashMap<>();
        List<Build> builds;
        for (Build build : list) {
            builds = map.get(build.getType());
            if (builds == null) {
                builds = new ArrayList<>();
                builds.add(build);
            }else{
                builds.add(build);
            }
            map.put(build.getType(), builds);
        }
        return map;
    }


    /**
     * 新建建筑物
     * @param objectMap
     * @return
     */
    public Message newBuild(Map<String, Object> objectMap) {
        Object type = objectMap.get("type");
        Object centerCoor = objectMap.get("centerCoor");
        Object remark = objectMap.get("remark");
        Object projectId = objectMap.get("projectId");
        if (projectId == null || type == null || centerCoor == null || remark == null) {
            return new Message(Message.Type.FAIL);
        }
        Build build1 = null;
        List<Build> builds = buildGroupService.getBuilds();
        for (Build build : builds) {
            if (build.getType().toString().equals(type.toString())) {
                build1 = (Build) SettingUtils.objectCopy(build);
                break;
            }
        }
        build1.setSource(Build.Source.DESIGN);
        JSONObject jsonObject = JSONObject.fromObject(centerCoor);
        build1.setCenterCoor(String.valueOf(jsonObject));
        build1.setRemark(remark.toString());
        Project project = projectService.find(Long.valueOf(projectId.toString()));
        build1.setProject(project);
//        Object attribes = objectMap.get("attribes");
//        Attribe attribe;
//        List<Attribe> attribes1 = new ArrayList<>();
//        if (attribes != null) {
//            for (Map<String, String> map : (List<Map<String, String>>) attribes) {
//                attribe = new Attribe();
//                attribe.setAlias(map.get("alias"));
//                attribe.setValue(map.get("value"));
//                attribes1.add(attribe);
//            }
//        }
//        build1.setAttribeList(attribes1);
        buildService.save(build1);
        return new Message(Message.Type.OK);
    }


    /**
     * 编辑建筑物
     * @param objectMap
     * @return
     */
    public Message editBuild(Map<String, Object> objectMap) {
        Object id = objectMap.get("id");
        Object remark = objectMap.get("remark");
        Object type = objectMap.get("type");
        if (id == null) {
            return new Message(Message.Type.FAIL);
        }
        Build build = buildService.find(Long.valueOf(id.toString()));
        if (build == null) {
            return new Message(Message.Type.EXIST);
        }
        if ((build.getAttribeList() == null || build.getAttribeList().size() == 0) && type != null) {
            build.setType(CommonEnum.CommonType.valueOf(type.toString()));
        }
        Object attribes = objectMap.get("attribes");
        Attribe attribe;
        List<Attribe> attribes1 = new ArrayList<>();
        if (attribes != null) {
            for (Map<String, Object> map : (List<Map<String, Object>>) attribes) {
                if (map.get("alias") == null || map.get("value") == null) {
                    return new Message(Message.Type.FAIL);
                }
                attribe = new Attribe();
                attribe.setAlias(map.get("alias").toString());
                attribe.setValue(map.get("value").toString());
                attribes1.add(attribe);
            }
        }
        List<Attribe> list = contrastEditAttribe(build.getAttribeList(), attribes1);
        build.setAttribeList(list);
        if (remark != null) {
            build.setRemark(remark.toString());
        }
        buildService.save(build);
        return new Message(Message.Type.OK);
    }



    /**
     * 根据source类型传回相应的类型的坐标数据与简单的建筑物信息
     * @param builds2
     * @return
     */
    private JSONArray returnSimpleBuildJson(List<Build> builds2){
        JSONArray jsonArray1 = new JSONArray();
        JSONObject jsonObject;
        for (Build build : builds2) {
            jsonObject = new JSONObject();
            jsonObject.put("id", build.getId());
            jsonObject.put("centerCoor", build.getCenterCoor());
            if (build.getCoordinateId() != null) {
                jsonObject.put("coordinateId", build.getCoordinateId());
            }
            if (build.getPositionCoor() != null) {
                jsonObject.put("positionCoor", build.getPositionCoor());
            }
            jsonObject.put("type", build.getType());
            jsonObject.put("remark", build.getRemark());
            jsonArray1.add(jsonObject);
        }
        return jsonArray1;
    }

    public Message isAllowUploadCoordinateFile(Long id) {
        Project project = projectService.find(Long.valueOf(id));
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        List<Coordinate> coordinates = coordinateService.findByProject(project);
        if (coordinates.size()< CommonAttributes.COORDINATELIMIT) {
            return new Message(Message.Type.OK);
        }
//        超过限制数量，返回已达到最大限制数量
        return new Message(Message.Type.OTHER);
    }

    /**
     * 将大地坐标转换为各类坐标
     * @param longitude
     * @param latitude
     * @param code
     * @return
     */
    private JSONObject coordinateBLHToXYZ(String longitude, String latitude, String code,Coordinate.WGS84Type wgs84Type) {
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

    private JSONObject planeCoordinate(String longitude, String latitude, String code) {
        ProjCoordinate projCoordinate = transFromService.BLHToXYZ(code, Double.valueOf(longitude), Double.valueOf(latitude));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("longitude", projCoordinate.x);
        jsonObject.put("latitude", projCoordinate.y);
        return jsonObject;
    }

    private JSONObject degreeMinuteSecond2(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        String c = b.substring(0, b.indexOf("."));
        String d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        longitude = a + "°" + c + "'" + d + "\"";
        jsonObject.put("longitude", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        c = b.substring(0, b.indexOf("."));
        d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        latitude = a + "°" + c + "'" + d + "\"";
        jsonObject.put("latitude", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinuteSecond1(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        String c = b.substring(0, b.indexOf("."));
        String d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        longitude = a + ":" + c + ":" + d;
        jsonObject.put("longitude", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        c = b.substring(0, b.indexOf("."));
        d = String.valueOf(Double.valueOf("0" + b.substring(b.indexOf("."), b.length())) * 60);
        latitude = a + ":" + c + ":" + d;
        jsonObject.put("latitude", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinute2(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        longitude = a + "°" + b + "'";
        jsonObject.put("longitude", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        latitude = a + "°" + b + "'";
        jsonObject.put("latitude", latitude);
        return jsonObject;
    }

    private JSONObject degreeMinute1(String longitude,String latitude) {
        JSONObject jsonObject = new JSONObject();
        String a = longitude.substring(0, longitude.indexOf("."));
        String b = String.valueOf(Double.valueOf("0" + longitude.substring(longitude.indexOf("."), longitude.length())) * 60);
        longitude = a + ":" + b;
        jsonObject.put("longitude", longitude);
        a = latitude.substring(0, latitude.indexOf("."));
        b = String.valueOf(Double.valueOf("0" + latitude.substring(latitude.indexOf("."), latitude.length())) * 60);
        latitude = a + ":" + b;
        jsonObject.put("latitude", latitude);
        return jsonObject;
    }

    private JSONObject degree(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("longitude", longitude);
        jsonObject.put("latitude", latitude);
        return jsonObject;
    }
}
