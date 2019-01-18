package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.buildModel.Elevation;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.listener.TestExecutionListener;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by chenl on 17-5-24.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(value = {TestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ContextConfiguration(locations = {"classpath*:/applicationContext-test.xml", "classpath*:/applicationContext-cache-test.xml","classpath*:/applicationContext-shiro-test.xml"})
@Transactional(transactionManager = "transactionManager")
@Rollback(value = false)
public class UpdateTest {

    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private ElementDBService elementDBService;
    @Autowired
    private ElementDataGroupService elementDataGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private Build1Service build1Service;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private FieldWorkService fieldWorkService;
    @Autowired
    private ShapeService shapeService;
    @Autowired
    private StationService stationService;
    @Autowired
    private CameraService cameraService;

    /**
     * 1.删除attribute表
     * 2.修改build表名为build1表名
     * 3.运行测试用例
     * 4.删除build1Dao
     * 5.删除build1实体
     * 6.删除coordinate实体
     * 7.删除build1srvice
     * 8.删除对应数据库 build1，coordinate
     * 9.模板补全时需对迁移数据进行多高程处理
     */

//    @Test
    public void test00001(){
        List<Coordinate> all = coordinateService.findAll();
        for (Coordinate coordinate : all) {
            if (coordinate.getSource() == Build.Source.DESIGN) {
                rosloveDesign(coordinate);
            } else {
                rosloveField(coordinate);
            }
        }
    }

    /**
     * 处理外业
     * @param coordinate
     */
    private void rosloveField(Coordinate coordinate) {
        FieldWork fieldWork = new FieldWork();
        fieldWork.setProject(coordinate.getProject());
        fieldWork.setName(coordinate.getName());
        fieldWork.setDeviceMac(coordinate.getDeviceMac());
        fieldWork.setAccountId(coordinate.getUserId());
        List<FieldWorkPoint> fieldWorkPoints = new ArrayList<>();
        FieldWorkPoint fieldWorkPoint;
        JSONArray jsonArray = JSONArray.fromObject(coordinate.getCoordinateStr());
        List<Build1> build1s = build1Service.findByCoordinateId(coordinate.getId());
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            JSONArray jsonArray1 = JSONArray.fromObject(jsonObject.get("coordinate"));
            JSONObject jsonObject1 = JSONObject.fromObject(jsonArray1.get(0));
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("lon", jsonObject1.get("longitude"));
            jsonObject2.put("lat", jsonObject1.get("latitude"));
            fieldWorkPoint = new FieldWorkPoint();
            fieldWorkPoint.setDescription(jsonObject.get("description").toString());
            fieldWorkPoint.setFieldWork(fieldWork);
            fieldWorkPoint.setCommonType(CommonEnum.CommonType.valueOf(jsonObject.get("baseType").toString()));
            fieldWorkPoint.setCenterCoor(jsonObject2.toString());
            fieldWorkPoint.setAlias(jsonObject.get("alias").toString());
            for (Build1 build1 : build1s) {
                if (build1.getCenterCoor() == null) {
                    continue;
                }
                JSONObject jsonObject3 = JSONObject.fromObject(build1.getCenterCoor());
                if (jsonObject3.get("longitude").toString().equals(jsonObject1.get("longitude").toString()) && jsonObject3.get("latitude").toString().equals(jsonObject1.get("latitude").toString())) {
                    if (build1.getPositionCoor() != null) {
                        JSONObject jsonObject4 = JSONObject.fromObject(build1.getPositionCoor());
                        jsonObject2 = new JSONObject();
                        jsonObject2.put("lon", jsonObject4.get("longitude"));
                        jsonObject2.put("lat", jsonObject4.get("latitude"));
                        fieldWorkPoint.setPositionCoor(jsonObject2.toString());
                        break;
                    }

                }
            }
            fieldWorkPoints.add(fieldWorkPoint);
        }
        fieldWork.setFieldWorkPoints(fieldWorkPoints);
        fieldWorkService.save(fieldWork);
    }

    /**
     * 处理内业
     * @param coordinate
     */
    private void rosloveDesign(Coordinate coordinate) {
        Shape shape = new Shape();
        shape.setProject(coordinate.getProject());
        JSONObject jsonObject = JSONObject.fromObject(coordinate.getCoordinateStr());
        Object baseType = jsonObject.get("baseType");
        if (baseType == null) {
            return;
        }
        JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("coordinate"));
        ShapeCoordinate shapeCoordinate;
        List<ShapeCoordinate> shapeCoordinates = new ArrayList<>();
        List<Build1> build1s = build1Service.findByCoordinateId(coordinate.getId());
        for (Object o : jsonArray) {
            JSONObject jsonObject1 = (JSONObject) o;
            shapeCoordinate = new ShapeCoordinate();
            shapeCoordinate.setShape(shape);
            if (jsonObject1.get("elevation") == null) {
                continue;
            }
            shapeCoordinate.setElevation(new Elevation(jsonObject1.get("elevation").toString(),"desgin-ele","设计高程"));
            shapeCoordinate.setLon(jsonObject1.get("longitude").toString());
            shapeCoordinate.setLat(jsonObject1.get("latitude").toString());
            if (shapeCoordinates.size() != 0) {
                shapeCoordinates.get(shapeCoordinates.size() - 1).setNext(shapeCoordinate);
                shapeCoordinate.setParent(shapeCoordinates.get(shapeCoordinates.size()-1));
            }
            shapeCoordinates.add(shapeCoordinate);
            for (Build1 build1 : build1s) {
                if (build1.getCenterCoor() == null) {
                    continue;
                }
                JSONObject jsonObject2 = JSONObject.fromObject(build1.getCenterCoor());
                if (jsonObject2.get("longitude").toString().equals(shapeCoordinate.getLon()) && jsonObject2.get("latitude").toString().equals(shapeCoordinate.getLat())) {
                    Build build = new Build();
                    build.setShapeCoordinate(shapeCoordinate);
                    build.setProjectId(coordinate.getProject().getId());
                    if (build1.getPositionCoor() != null) {
                        JSONObject jsonObject3 = JSONObject.fromObject(build1.getPositionCoor());
                        JSONObject jsonObject4 = new JSONObject();
                        jsonObject4.put("lon", jsonObject3.get("longitude"));
                        jsonObject4.put("lat", jsonObject3.get("latitude"));
                        build.setPositionCoor(jsonObject4.toString());
                    }
                    build.setRemark(build1.getRemark());
                    build.setType(build1.getType());
                    buildService.save(build);
                }
            }
        }
        shape.setShapeCoordinates(shapeCoordinates);
        shape.setRemark(coordinate.getDescription());
        shape.setCommonType(CommonEnum.CommonType.valueOf(baseType.toString()));
        shapeService.save(shape);
    }

    /**
     * 测站导入
     */
//    @Test
    public void test0001() throws DocumentException {
        File file = null;
        try {
            file = new ClassPathResource("/stationtest.xml").getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SAXReader reader = new SAXReader();
        try {
            reader.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
        } catch (SAXException e1) {
            e1.printStackTrace();
        }
        Document doc = reader.read(file);
        Element elem = doc.getRootElement();
        List<Element> elements = elem.elements();
        for (Element element : elements) {
            String instanceId = element.attributeValue("instanceId");
            Station station = stationService.findByInstanceId(instanceId);
            if (station == null) {
                station = new Station();
            }
            if (element.attributeValue("name") != null && !element.attributeValue("name").equals("")) {
                station.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("description") != null && !element.attributeValue("description").equals("")) {
                station.setDescription(element.attributeValue("description"));
            }
            station.setType(CommonEnum.StationType.NORMAL_STATION);
            if (element.attributeValue("coor") != null && !element.attributeValue("coor").equals("")) {
                station.setCoor(element.attributeValue("coor"));
            }
            if (element.attributeValue("address") != null && !element.attributeValue("address").equals("")) {
                station.setAddress(element.attributeValue("address"));
            }
            if (element.attributeValue("riverModel") != null && !element.attributeValue("riverModel").equals("")) {
                station.setRiverModel(element.attributeValue("riverModel"));
            }
            if (element.attributeValue("flowModel") != null && !element.attributeValue("flowModel").equals("")) {
                station.setFlowModel(element.attributeValue("flowModel"));
            }

            if (element.attributeValue("instanceId") != null && !element.attributeValue("instanceId").equals("")) {
                station.setInstanceId(element.attributeValue("instanceId"));
            }
            if (element.attributeValue("shares") != null && !element.attributeValue("shares").equals("")) {
                station.setShares(element.attributeValue("shares"));
            }
            if (element.attributeValue("expireDate") != null && !element.attributeValue("expireDate").equals("")) {
                station.setExpireDate(new Date(Long.valueOf(element.attributeValue("expireDate"))));
            }
            if (element.attributeValue("user") != null && !element.attributeValue("user").equals("")) {
                User user = userService.find(Long.valueOf(element.attributeValue("user")));
                station.setUser(user);
            }
            if (element.attributeValue("cooperate") != null && !element.attributeValue("cooperate").equals("")) {
                station.setCooperate(element.attributeValue("cooperate"));
            }
            if (element.attributeValue("bottomElevation") != null && !element.attributeValue("bottomElevation").equals("")) {
                station.setBottomElevation(Double.valueOf(element.attributeValue("bottomElevation")));
            }
            stationService.save(station);
            List<Element> elements1 = element.elements();
            for (Element child : elements1) {
                String code = child.attributeValue("code");
                if (code != null && !code.equals("")) {
                    Sensor sensor = sensorService.findByCode(code);
                    if (sensor == null) {
                        sensor = new Sensor();
                    }
                    if (child.attributeValue("code") != null && !child.attributeValue("code").equals("")) {
                        sensor.setCode(child.attributeValue("code"));
                    }
                    if (child.attributeValue("type") != null && !child.attributeValue("type").equals("")) {
                        sensor.setType(Sensor.Type.valueOf(child.attributeValue("type")));
                    }
                    if (child.attributeValue("activate") != null && !child.attributeValue("activate").equals("")) {
                        sensor.setActivate(Boolean.valueOf(child.attributeValue("activate")));
                    }

                    if (child.attributeValue("info") != null && !child.attributeValue("info").equals("")) {
                        String info = child.attributeValue("info");
                        JSONObject jsonObject = JSONObject.fromObject(info);
                        sensor.setFactory(jsonObject.get("factory").toString());
                        sensor.setContact(jsonObject.get("contact").toString());
                        sensor.setPhone(jsonObject.get("phone").toString());
                    }
//
                    if (child.attributeValue("settingHeight") != null && !child.attributeValue("settingHeight").equals("")) {
                        sensor.setSettingHeight(Double.valueOf(child.attributeValue("settingHeight")));
                    }
                    sensor.setStation(station);
                    sensorService.save(sensor);
                } else {
                    String cameraUrl = child.attributeValue("cameraUrl");
                    Camera camera = cameraService.findByCode(cameraUrl);
                    if (camera == null) {
                        camera = new Camera();
                    }
                    if (child.attributeValue("cameraUrl") != null && !child.attributeValue("cameraUrl").equals("")) {
                        camera.setCode(child.attributeValue("cameraUrl"));
                    }
                    if (child.attributeValue("info") != null && !child.attributeValue("info").equals("")) {
                        String info = child.attributeValue("info");
                        JSONObject jsonObject = JSONObject.fromObject(info);
                        camera.setFactory(jsonObject.get("factory").toString());
                        camera.setContact(jsonObject.get("contact").toString());
                        camera.setPhone(jsonObject.get("phone").toString());
                    }
                    camera.setStation(station);
                    cameraService.save(camera);
                }
            }
        }
    }

//    @Test
    public void test0002(){
        List<Station> all = stationService.findAll();
        for (Station station : all) {
            JSONObject jsonObject = JSONObject.fromObject(station.getCoor());
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("lat", jsonObject.get("latitude"));
            jsonObject1.put("lon", jsonObject.get("longitude"));
            station.setCoor(jsonObject1.toString());
            stationService.save(station);
        }
    }

}
