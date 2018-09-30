package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by flysic on 17-4-11.
 */
public class AttributeGroupServiceTest extends BaseTest {

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AttributeGroupService attributeGroupService;


    @Test
    public void makeMaterGroups() throws Exception {
       // attributeGroupService.makeMaterGroups();
        Cache cache = cacheManager.getCache("attributeGroupsCache");
        Element element = cache.get("mater");
        List<AttributeGroup> attributeGroups = (List<AttributeGroup>) element.getValue();
        assertNotNull(attributeGroups);
        assertEquals(9,attributeGroups.size());
    }

    @Test
    public void makeDimensionsGroups() throws Exception {
        Cache cache = cacheManager.getCache("attributeGroupsCache");
        Element element = cache.get("dimension");
        List<AttributeGroup> attributeGroups = (List<AttributeGroup>) element.getValue();
        assertNotNull(attributeGroups);
        assertEquals(24,attributeGroups.size());
    }

    @Test
    public void makeHydraulicsGroups() throws Exception {
        Cache cache = cacheManager.getCache("attributeGroupsCache");
        Element element = cache.get("hydraulics");
        List<AttributeGroup> attributeGroups = (List<AttributeGroup>) element.getValue();
        assertNotNull(attributeGroups);
        assertEquals(10,attributeGroups.size());
    }

    @Test
    public void makeGeologyGroups() throws Exception {
        Cache cache = cacheManager.getCache("attributeGroupsCache");
        Element element = cache.get("geology");
        List<AttributeGroup> attributeGroups = (List<AttributeGroup>) element.getValue();
        assertNotNull(attributeGroups);
        assertEquals(1,attributeGroups.size());
    }

    @Test
    public void makeStructureGroups() throws Exception {
        Cache cache = cacheManager.getCache("attributeGroupsCache");
        Element element = cache.get("structure");
        List<AttributeGroup> attributeGroups = (List<AttributeGroup>) element.getValue();
        assertNotNull(attributeGroups);
        assertEquals(4,attributeGroups.size());
    }

}