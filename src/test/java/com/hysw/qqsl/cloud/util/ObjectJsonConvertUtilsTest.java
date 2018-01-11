package com.hysw.qqsl.cloud.util;
import static org.junit.Assert.*;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.element.JsonTree;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.service.ProjectService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Administrator on 2016/8/22.
 */
public class ObjectJsonConvertUtilsTest extends BaseTest{
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ObjectJsonConvertUtils objectJsonConvertUtils;
    /**
     * 获取各个项目类型的要素输出模版json
     */
    @Test
    public void testGetjsons(){
     Project project = new Project();
     project.setType(Project.Type.AGRICULTURAL_IRRIGATION);
     List<JsonTree> jsons = objectJsonConvertUtils.getAgrJsonTree();
     assertEquals(jsons.size(),32);
    }

    @Test
    public void testGetConJsons(){
        Project project = new Project();
        project.setType(Project.Type.CONSERVATION);
        List<JsonTree> jsons = objectJsonConvertUtils.getAgrJsonTree();
        assertEquals(jsons.size(),32);
    }

    @Test
    public void testGetDriJsons(){
        Project project = new Project();
        project.setType(Project.Type.DRINGING_WATER);
        List<JsonTree> jsons = objectJsonConvertUtils.getAgrJsonTree();
        assertEquals(jsons.size(),32);
    }

    @Test
    public void testGetHydJsons(){
        Project project = new Project();
        project.setType(Project.Type.HYDROPOWER_ENGINEERING);
        List<JsonTree> jsons = objectJsonConvertUtils.getAgrJsonTree();
        assertEquals(jsons.size(),32);
    }

    @Test
    public void testGetFloJsons(){
        Project project = new Project();
        project.setType(Project.Type.FLOOD_DEFENCES);
        List<JsonTree> jsons = objectJsonConvertUtils.getAgrJsonTree();
        assertEquals(jsons.size(),32);
    }

    @Test
    public void testGetWatJsons(){
        Project project = new Project();
        project.setType(Project.Type.WATER_SUPPLY);
        List<JsonTree> jsons = objectJsonConvertUtils.getAgrJsonTree();
        assertEquals(jsons.size(),32);
    }

    /**
     * 项目结构构建测试
     */
    @Test
    public void testGetUnitJson(){
        Project project = projectService.findAll().get(0);
        List<Unit> units = projectService.buildTemplate(project);
        List<JsonTree> unitJsons = objectJsonConvertUtils.getJsons(project,units);
        assertTrue(unitJsons.size()>0);
    }
}
