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
    @Autowired
    private CoordinateService coordinateService;

    @Test
    public void testWriteExcel(){
        Workbook workbook = fieldWorkService.writeExcelByFieldWork(projectService.find(848l), Coordinate.WGS84Type.DEGREE);
        Assert.assertTrue(workbook.getNumberOfSheets()!=0);
    }

    @Test
    public void testNewBuild(){
        List<Coordinate> all = coordinateService.findAll();
        String s = "{\"type\":\"QS\", \"centerCoor\":{\"longitude\":\"101.49382902737608\", \"latitude\":\"36.72807717821667\", \"elevation\":\"0\"}, \"projectId\":\"848\", \"remark\":\"qw\"}";
        JSONObject jsonObject = JSONObject.fromObject(s);
        Map<String, Object> map = (Map<String, Object>) jsonObject;
        boolean flag = fieldWorkService.newBuild(map.get("type"), map.get("centerCoor"), map.get("remark"), map.get("projectId"), all.get(0).getId());
        Assert.assertTrue(flag);
        buildService.flush();
        s = "{\"type\":\"QS\", \"centerCoor\":{\"longitude\":\"101.49382902737608\", \"latitude\":\"36.72807717821667\", \"elevation\":\"0\"}, \"id\":\"6106\", \"projectId\":\"848\", \"remark\":\"qw1\"}";
        jsonObject = JSONObject.fromObject(s);
        map = (Map<String, Object>) jsonObject;
        Build build = buildService.find(Long.valueOf(map.get("id").toString()));
        flag = fieldWorkService.editBuild(build,map.get("remark"),map.get("type"),map.get("attributes"));
        Assert.assertTrue(flag);
    }

}
