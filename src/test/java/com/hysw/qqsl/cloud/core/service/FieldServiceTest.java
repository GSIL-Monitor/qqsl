package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

/**
 * Created by chenl on 17-4-21.
 */
public class FieldServiceTest extends BaseTest {
    @Autowired
    private FieldService fieldService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildBelongService buildBelongService;
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
        Message message = fieldService.saveField(map);
        Assert.assertTrue(message.getType()== Message.Type.OK);
    }

    @Test
    public void testWriteExcel(){
        Workbook workbook = fieldService.writeExcel(projectService.find(848l), Build.Source.FIELD, Coordinate.WGS84Type.DEGREE);
        Assert.assertTrue(workbook.getNumberOfSheets()!=0);
    }

}
