package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.build.BuildGroup;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.service.BuildGroupService;
import net.sf.json.JSONArray;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by leinuo on 17-4-6.
 */
public class BuildGroupServiceTest extends BaseTest {

    @Autowired
    private BuildGroupService buildGroupService;
    @Test
    public void makeBuildGroupsXml() throws Exception {
        List<BuildGroup> buildGroups = buildGroupService.getBuildGroupsXml();
        assertNotNull(buildGroups);
        assertEquals(8,buildGroups.size());
        BuildGroup buildGroup;
        buildGroup = buildGroups.get(0);
        assertEquals("1",buildGroup.getAlias());
        assertEquals(6,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(1);
        assertEquals("2",buildGroup.getAlias());
        assertEquals(13,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(2);
        assertEquals("3",buildGroup.getAlias());
        assertEquals(9,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(3);
        assertEquals("4",buildGroup.getAlias());
        assertEquals(5,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(4);
        assertEquals("5",buildGroup.getAlias());
        assertEquals(5,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(5);
        assertEquals("6",buildGroup.getAlias());
        assertEquals(4,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(6);
        assertEquals("7",buildGroup.getAlias());
        assertEquals(9,buildGroup.getBuilds().size());
        buildGroup = buildGroups.get(7);
        assertEquals("8",buildGroup.getAlias());
        assertEquals(4,buildGroup.getBuilds().size());
    }

    @Test
    public void makeCompleteBuildGroups(){
        List<BuildGroup> buildGroups = buildGroupService.getCompleteBuildGroups();
        assertNotNull(buildGroups);
    }

    @Test
    public void getBuildGroups(){
        List<BuildGroup> buildGroups = buildGroupService.getBuildGroups(true);
        assertEquals(6,buildGroups.size());
        buildGroups = buildGroupService.getBuildGroups(false);
        assertEquals(7,buildGroups.size());
    }

    @Test
    public void getBuildJson(){
        JSONArray jsonArray = buildGroupService.getBuildJson(false);
        assertNotNull(jsonArray);
        logger.info(jsonArray);
    }

    @Test
    public void getBuildsDynamic() throws Exception {
        List<Build> builds = buildGroupService.getBuildsDynamic();
        assertNotNull(builds);
    }

}