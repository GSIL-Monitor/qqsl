package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.buildModel.Elevation;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.listener.TestExecutionListener;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    /**
     * 1.删除attribute表
     * 2.修改build表名为build1表名
     * 3.运行测试用例
     * 4.删除build1Dao
     * 5.删除build1实体
     * 6.删除coordinate实体
     * 7.删除build1srvice
     *  删除对应数据库 build1，coordinate
     */

    @Test
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
            shapeCoordinate.setElevation(new Elevation(jsonObject1.get("elevation").toString(),"desgin-ele"));
            shapeCoordinate.setLon(jsonObject1.get("longitude").toString());
            shapeCoordinate.setLat(jsonObject1.get("latitude").toString());
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

}
