package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProjectLogsServiceTest extends BaseTest {
    @Autowired
    private ProjectLogService projectLogService;

    @Test
    public void testLog(){
        List<ProjectLog> projectLogs = projectLogService.findByProjectId(615l);
        System.out.println();
    }
}
