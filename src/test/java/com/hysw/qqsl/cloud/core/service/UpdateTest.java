package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
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
    //coordinate 删除username,type,treePath,baseType,device_mac,source,userId,name
    //buildModel 删除alias,name，coordinateId
    //删除attribe表 删除属性code,status,genre,
    //修改索引/field为fieldWork/
    //新建线面时传入的坐标格式为lon，lat，ele
    /**
     * 将坐标格式转换为新的格式
     */
    @Test
    public void test0001(){
        List<Coordinate> coordinates = coordinateService.findAll();
        for (Coordinate coordinate : coordinates) {
            if (coordinate.getSource() != Build.Source.DESIGN) {
                continue;
            }
            if (coordinate.getCommonType() != null) {
                continue;
            }
            JSONObject jsonObject = JSONObject.fromObject(coordinate.getCoordinateStr());
            if (jsonObject.get("baseType") == null) {
                coordinate.setCommonType(CommonEnum.CommonType.GONGGXM);
            } else {
                coordinate.setCommonType(CommonEnum.CommonType.valueOf(jsonObject.get("baseType").toString()));
            }
            JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("coordinate"));
            JSONArray jsonArray1 = new JSONArray();
            for (Object o : jsonArray) {
                JSONObject jsonObject1 = (JSONObject) o;
                Object longitude = jsonObject1.get("longitude");
                Object latitude = jsonObject1.get("latitude");
                Object elevation = jsonObject1.get("elevation");
                jsonObject1 = new JSONObject();
                jsonObject1.put("lon", longitude);
                jsonObject1.put("lat", latitude);
                jsonObject1.put("ele", elevation);
                jsonArray1.add(jsonObject1);
            }
            jsonObject = new JSONObject();
            jsonObject.put("coordinate", jsonArray1);
            coordinate.setCoordinateStr(jsonObject.toString());
            coordinateService.save(coordinate);
        }
    }

    /**
     * 将建筑物转换为新的格式
     */
    @Test
    public void test0002(){
        List<Build> builds = buildService.findAll();
        for (Build build : builds) {
            JSONObject jsonObject = JSONObject.fromObject(build.getCenterCoor());
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("lon", jsonObject.get("longitude"));
            jsonObject1.put("lat", jsonObject.get("latitude"));
            jsonObject1.put("ele", jsonObject.get("elevation"));
            build.setCenterCoor(jsonObject1.toString());
            if (build.getPositionCoor() != null) {
                jsonObject = JSONObject.fromObject(build.getPositionCoor());
                jsonObject1 = new JSONObject();
                jsonObject1.put("lon", jsonObject.get("longitude"));
                jsonObject1.put("lat", jsonObject.get("latitude"));
                jsonObject1.put("ele", jsonObject.get("elevation"));
                build.setPositionCoor(jsonObject1.toString());
            }
            buildService.save(build);
        }
    }

    /**
     * 将外业迁移至field
     */
    @Test
    public void test0003(){
        FieldWork fieldWork;
        List<Coordinate> coordinates = coordinateService.findAll();
        Iterator<Coordinate> it = coordinates.iterator();
        Coordinate coordinate;
        while (it.hasNext()) {
            coordinate = it.next();
            if (coordinate.getSource() == Build.Source.DESIGN) {
                continue;
            }
            fieldWork = new FieldWork();
            fieldWork.setProject(coordinate.getProject());
            fieldWork.setCoordinateStr(coordinate.getCoordinateStr());
            fieldWork.setDeviceMac(coordinate.getDeviceMac());
            fieldWork.setName(coordinate.getName());
            fieldWork.setAccountId(coordinate.getUserId());
            fieldWorkService.save(fieldWork);
            coordinateService.remove(coordinate);
            it.remove();
        }
    }


    /**
     * 调整外业格式
     */
    @Test
    public void test0004(){
        List<FieldWork> fieldWorks = fieldWorkService.findAll();
        for (FieldWork fieldWork : fieldWorks) {
            JSONArray jsonArray = JSONArray.fromObject(fieldWork.getCoordinateStr());
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                JSONArray jsonArray2 = (JSONArray) jsonObject.get("coordinate");
                for (Object o1 : jsonArray2) {
                    JSONObject jsonObject1 = (JSONObject) o1;
                    jsonObject1.put("lon", jsonObject1.get("longitude"));
                    jsonObject1.put("lat", jsonObject1.get("latitude"));
                    jsonObject1.put("ele", jsonObject1.get("elevation"));
                    jsonObject1.remove("longitude");
                    jsonObject1.remove("latitude");
                    jsonObject1.remove("elevation");
                }
            }
            fieldWork.setCoordinateStr(jsonArray.toString());
            fieldWorkService.save(fieldWork);
        }
    }

    /**
     * 将build中coordinateId转存到commonId
     */
    @Test
    public void test0005() {
        List<Build> builds = buildService.findAll();
        for (Build build : builds) {
            build.setCommonId(build.getCommonId());
            buildService.save(build);
        }
    }
}
