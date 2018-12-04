package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Shape;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;
import com.hysw.qqsl.cloud.core.entity.data.ShapeCoordinate;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Administrator
 * @since 2018/9/25
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShapeServiceTest extends BaseTest {
    @Autowired
    private ShapeService shapeService;
    @Autowired
    private ShapeCoordinateService shapeCoordinateService;
    @Autowired
    private ShapeAttributeService shapeAttributeService;
    @Autowired
    private BuildService buildService;

    /**
     * 获取所有图形线面类型
     */
    @Test
    public void test0001(){
        JSONArray modelType = shapeService.getModelType();
        Assert.assertTrue(modelType.size() != 0);
    }

    /**
     * 上传图形线面
     */
    @Test
    public void test0002(){

    }

    /**
     * 获取图形线面详情
     */
    @Test
    public void test0003(){
        Shape shape = shapeService.find(1l);
        JSONObject jsonObject = shapeService.buildJson(shape);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    /**
     * 新建图形线面
     */
    @Test
    public void test0004(){
        Object coors="[{\"lon\":\"102\",\"lat\":\"35\"},{\"lon\":\"102\",\"lat\":\"35\"}]";
        Object type= "DANGQ";
        Object remark="test0001";
        Object projectId=1197;
        Shape shape = shapeService.newShape(coors, type, remark, projectId);
        Assert.assertTrue(shape != null && shape.getId() != null);
    }

    /**
     * 编辑图形线面
     */
    @Test
    public void test0005(){
        Object id = "29";
        Object remark="test";
        Object coors = "[{\"id\":\"165\",\"lon\":\"105\",\"lat\":\"39\"},{\"lon\":\"106\",\"lat\":\"38\"},{\"lon\":\"107\",\"lat\":\"40\"},{\"id\":\"166\",\"lon\":\"105\",\"lat\":\"39\"},{\"lon\":\"106\",\"lat\":\"38\"},{\"lon\":\"107\",\"lat\":\"40\"}]";
        Shape shape = shapeService.editShape(coors,id,remark);
        Assert.assertTrue(shape != null && shape.getId() != null);
    }

    /**
     * 删除图形线面
     */
    @Test
    public void test0006(){

    }

    /**
     * 删除图形线面某点
     */
    @Test
    public void test0007(){
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(173l);
        shapeCoordinateService.deleteShapeCoordinateById(shapeCoordinate);
        shapeCoordinateService.flush();
        ShapeCoordinate shapeCoordinate1 = shapeCoordinateService.find(173l);
        Assert.assertTrue(shapeCoordinate1 == null);
    }

    /**
     * 取得图形线面下某点详情
     */
    @Test
    public void test0008(){
        ShapeCoordinate shapeCoordinate = shapeCoordinateService.find(173l);
        JSONObject jsonObject = shapeCoordinateService.getCoordinateDetails(shapeCoordinate);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    /**
     * 编辑图形线面下某点高程组
     */

    @Test
    public void test0009(){

    }

    /**
     * 新建图形线面剖面属性
     */
    @Test
    public void test0010(){

    }

    /**
     * 删除图形剖面属性
     */
    @Test
    public void test0011(){

    }

    /**
     * 编辑图形线面剖面属性
     */
    @Test
    public void test0012(){
        Shape shape = shapeService.find(31l);
        Object attributes = "[{\"alias\":\"ct0004\",\"value\":\"11\"},{\"alias\":\"remark\",\"value\":\"11\"}]";
        shapeAttributeService.editShapeAttribute(shape,attributes);
        List<ShapeAttribute> shapeAttributeList = shapeAttributeService.findByShape(shape);
        Assert.assertTrue(shapeAttributeList.size() != 0);
    }

    /**
     * 获取建筑物类型
     */
    @Test
    public void test0013(){
        JSONArray modelType = buildService.getModelType();
        Assert.assertTrue(!modelType.isEmpty());
    }

    /**
     * 单建筑物上传
     */
    @Test
    public void test0014(){

    }

    /**
     * 多建筑物上传
     */
    @Test
    public void test0015(){

    }

    /**
     * 获取建筑物详情
     */
    @Test
    public void test0016(){
        Build build = buildService.find(58l);
        JSONObject jsonObject = buildService.buildJson(build);
        Assert.assertTrue(!jsonObject.isEmpty());
    }

    /**
     * 编辑建筑物
     */
    @Test
    public void test0017(){

    }

    /**
     * 地图上新建建筑物
     */
    @Test
    public void test0018(){

    }

    /**
     * 地图上编辑建筑物
     */
    @Test
    public void test0019(){

    }

    /**
     * BIM取得建筑物详情
     */
    @Test
    public void test0020(){

    }

    /**
     * BIM获取图形及其属性信息
     */
    @Test
    public void test0021(){

    }

}
