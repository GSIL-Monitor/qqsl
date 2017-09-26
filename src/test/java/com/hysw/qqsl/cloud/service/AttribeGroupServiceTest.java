package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.build.AttribeGroup;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by flysic on 17-4-11.
 */
public class AttribeGroupServiceTest extends BaseTest {

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AttribeGroupService attribeGroupService;

    @Test
    public void getMaterGroup() throws Exception {
        AttribeGroup attribeGroup = attribeGroupService.getMaterGroup("1");
        assertNotNull(attribeGroup);
    }

    @Test
    public void getDimensionsGroup() throws Exception {
        AttribeGroup attribeGroup = attribeGroupService.getDimensionsGroup("1");
        assertNotNull(attribeGroup);
    }

    @Test
    public void getHydraulicsGroup() throws Exception {
        AttribeGroup attribeGroup = attribeGroupService.getHydraulicsGroup("1");
        assertNotNull(attribeGroup);
    }

    @Test
    public void getGeologyGroup() throws Exception {
        AttribeGroup attribeGroup = attribeGroupService.getGeologyGroup("1");
        assertNotNull(attribeGroup);
    }

    @Test
    public void getGeologyDynamicGroups() throws Exception {
        AttribeGroup attribeGroup = attribeGroupService.getGeologyDynamicGroups("1");
        assertNotNull(attribeGroup);
    }

    @Test
    public void getStructureGroup() throws Exception {
        AttribeGroup attribeGroup = attribeGroupService.getStructureGroup("1");
        assertNotNull(attribeGroup);
    }


    @Test
    public void makeMaterGroups() throws Exception {
       // attribeGroupService.makeMaterGroups();
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        Element element = cache.get("mater");
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        assertNotNull(attribeGroups);
        assertEquals(9,attribeGroups.size());
    }

    @Test
    public void makeDimensionsGroups() throws Exception {
       // attribeGroupService.makeDimensionsGroups();
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        Element element = cache.get("dimension");
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        assertNotNull(attribeGroups);
        assertEquals(24,attribeGroups.size());
    }

    @Test
    public void makeHydraulicsGroups() throws Exception {
       // attribeGroupService.makeHydraulicsGroups();
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        Element element = cache.get("hydraulics");
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        assertNotNull(attribeGroups);
        assertEquals(10,attribeGroups.size());
    }

    @Test
    public void makeGeologyGroups() throws Exception {
       // attribeGroupService.makeHydraulicsGroups();
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        Element element = cache.get("geology");
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        assertNotNull(attribeGroups);
        assertEquals(1,attribeGroups.size());
    }

    @Test
    public void makeStructureGroups() throws Exception {
        Cache cache = cacheManager.getCache("attribeGroupsCache");
        Element element = cache.get("structure");
        List<AttribeGroup> attribeGroups = (List<AttribeGroup>) element.getValue();
        assertNotNull(attribeGroups);
        assertEquals(4,attribeGroups.size());
    }
}