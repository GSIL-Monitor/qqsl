package com.hysw.qqsl.cloud.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import com.hysw.qqsl.cloud.CommonTest;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.ElementDataGroupService;
import com.hysw.qqsl.cloud.core.service.ProjectService;
import com.hysw.qqsl.cloud.core.service.UserService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.ElementDB;
import com.hysw.qqsl.cloud.core.entity.data.ElementDataGroup;

/**
 * Created by Administrator on 2016/6/23.
 */
public class ElementDataGroupServiceTest extends BaseTest {
    @Autowired
    private ElementDataGroupService elementDataGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private CacheManager cacheManager;
    @BeforeClass
    public static void testStart(){
    }
    @Before
    public void setUp(){
            super.setUp();
            long planningId =2;
            User user = userService.findByUserName(CommonTest.USER_NAME);
            Project project = new Project();
            project.setUser(user);
            project.setPlanning(planningId);
            project.setCode(CommonTest.PROJECT_CODE);
            project.setName(CommonTest.PROJECT_NAME);
            projectService.setType(project,1);
            try {
                projectService.createProject(project);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    /**
     * 测试数据清除
     * @throws Exception
     */
//    @After
//    public void tearDown() throws Exception {
//        List<Project> projects = projectService.findAll();
//        if(projects.size()>0){
//            projectService.removeAll(projects);
//        }
//        System.out.println("over");
//    }
    //@Test
    public void testDoSaveElementDataGroup(){
        Map<String,Object> elementDataGroupsMap=new HashMap<>();
        Map<String,Object> elementDataGroupMap=new HashMap<>();
        Map<String,Object> map=new HashMap<>();
        map.put("name","泉室(座)");
        map.put("value","10");
        map.put("alias","23B1/1");
        map.put("type","number");
        map.put("unit","座");
        map.put("description","泉室");
        Map<String,Object> map1=new HashMap<>();
        map1.put("name","容量(座)");
        map1.put("value","11");
        map1.put("alias","23B1/1");
        map1.put("type","number");
        map1.put("unit","吨");
        map1.put("description","容量");
        List<Object> objectList=new ArrayList<>();
        objectList.add(map);
        objectList.add(map1);
        elementDataGroupMap.put("name","泉室");
        elementDataGroupMap.put("alias","23B1");
        elementDataGroupMap.put("type","POINT");
        elementDataGroupMap.put("id","10");
        elementDataGroupMap.put("elementData",objectList);
        elementDataGroupsMap.put("elementDataGroups",elementDataGroupMap);
        ElementDB elementDB=new ElementDB();
        elementDB.setId(12157l);
        List<Object> elementDataGroups= new ArrayList<>();
        elementDataGroups.add(elementDataGroupMap);
        Unit unit=new Unit();
        elementDataGroupService.doSaveElementDataGroup(elementDB,elementDataGroups);
    }

    @Test
    public  void  testGetElementDataSimpleGroups(){
        List<ElementDataGroup> elementDataSimpleGroups = elementDataGroupService.getElementDataSimpleGroups();
        assertTrue(elementDataSimpleGroups.size()==109);
    }

    /**
     * 刷新要素数据的缓存
     */
    @Test
    public void testRefreshElementDataGroups(){
        //刷新缓存
        Cache cache = cacheManager.getCache("elementDataGroupsCache");
        cache.remove("simpleGroups");
        Element simpleGroups = cache.get("simpleGroups");
        assertTrue(simpleGroups == null);
        //验证是否读取成功
        List<ElementDataGroup> elementDataSimpleGroups = elementDataGroupService.getElementDataSimpleGroups();
        assertTrue(elementDataSimpleGroups.size()==109);

    }



  }
