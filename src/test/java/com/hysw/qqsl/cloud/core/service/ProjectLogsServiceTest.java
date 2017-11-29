package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
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

    @Test
    public void testTime(){
        long l = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ProjectLog projectLog = projectLogService.findByCooperateType(CooperateVisit.Type.VISIT_PREPARATION_ELEMENT, 555l);
        }
        long l1 = System.currentTimeMillis();
        System.out.println(l1-l);
    }
}
