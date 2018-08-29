package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by chenl on 17-4-21.
 */
public class FieldWorkServiceTest extends BaseTest {
    @Autowired
    private FieldWorkService fieldWorkService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private BuildGroupService buildGroupService;

    @Test
    public void testSaveField(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("projectId", 848l);
        map.put("userId",1l);
        map.put("name","aaa");
        map.put("deviceMac","ascvb");
        Map<String, Object> coordinate = new LinkedHashMap<>();
        coordinate.put("type", CommonEnum.CommonType.DC.toString());
        JSONObject center = new JSONObject();
        center.put("longitude", "102.35");
        center.put("latitude","35.6" );
        center.put("elevation","0" );
        JSONObject position = new JSONObject();
        position.put("longitude", "102.35");
        position.put("latitude","35.6" );
        position.put("elevation","0" );
        coordinate.put("center", center);
        coordinate.put("position", position);
        coordinate.put("description", "ducao");
        coordinate.put("alias", "dc1");
        List<Map<String, Object>> attribes = new LinkedList<>();
        Map<String, Object> attribe = new LinkedHashMap<>();
        attribe.put("code", 0);
        attribe.put("alias", "M11");
        attribe.put("value", "混凝土");
        attribes.add(attribe);
        coordinate.put("attribes", attribes);
        List<Object> coordinates = new LinkedList<>();
        coordinates.add(coordinate);
        map.put("coordinates",coordinates);
        boolean flag = fieldWorkService.saveField(map);
        Assert.assertTrue(flag);
    }

    @Test
    public void testWriteExcel(){
        Workbook workbook = fieldWorkService.writeExcelByFieldWork(projectService.find(848l), Coordinate.WGS84Type.DEGREE);
        Assert.assertTrue(workbook.getNumberOfSheets()!=0);
    }

    @Test
    public void testNewBuild(){
        String s = "{\"type\":\"QS\", \"centerCoor\":{\"longitude\":\"101.49382902737608\", \"latitude\":\"36.72807717821667\", \"elevation\":\"0\"}, \"projectId\":\"848\", \"remark\":\"qw\"}";
        JSONObject jsonObject = JSONObject.fromObject(s);
        Map<String, Object> map = (Map<String, Object>) jsonObject;
        boolean flag = fieldWorkService.newBuild(map.get("type"), map.get("centerCoor"), map.get("remark"), map.get("projectId"));
        Assert.assertTrue(flag);
        buildService.flush();
        s = "{\"type\":\"QS\", \"centerCoor\":{\"longitude\":\"101.49382902737608\", \"latitude\":\"36.72807717821667\", \"elevation\":\"0\"}, \"id\":\"6106\", \"projectId\":\"848\", \"remark\":\"qw1\"}";
        jsonObject = JSONObject.fromObject(s);
        map = (Map<String, Object>) jsonObject;
        Build build = buildService.find(Long.valueOf(map.get("id").toString()));
        flag = fieldWorkService.editBuild(build,map.get("id"),map.get("remark"),map.get("type"),map.get("attribes"));
        Assert.assertTrue(flag);
    }

}
