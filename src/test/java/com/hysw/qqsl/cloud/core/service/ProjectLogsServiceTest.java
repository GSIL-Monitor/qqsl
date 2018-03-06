package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectLogsServiceTest extends BaseTest {
    @Autowired
    private ProjectLogService projectLogService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;

    @Test
    public void testLog(){
        List<ProjectLog> projectLogs = projectLogService.findByProjectId(1060l);
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

    @Test
    public void testDeleteNotNearlyWeekLog(){
        List<ProjectLog> projectLogs = projectLogService.findByProjectIdAndWeek(1060l);
        boolean flag = false;
        for (ProjectLog projectLog : projectLogs) {
            if (projectLog.getCreateDate().getTime() < System.currentTimeMillis() - 7 * 24 * 3600 * 1000l) {
                flag = true;
                break;
            }
        }
        Assert.assertTrue(!flag);
        ProjectLog projectLog = new ProjectLog();
        projectLog.setProjectId(1060l);
        projectLog.setCooperateType(CooperateVisit.Type.VISIT_PREPARATION_ELEMENT);
        projectLog.setType(ProjectLog.Type.ELEMENT);
        projectLog.setContent("aaaaa");
        projectLogService.save(projectLog);
        projectLogService.flush();
        List<ProjectLog> projectLogs1 = projectLogService.findByProjectIdAndWeek(1060l);
        flag = false;
        for (ProjectLog projectLog1 : projectLogs1) {
            if (projectLog1.getContent().equals("aaaaa")) {
                projectLog1.setCreateDate(new Date(System.currentTimeMillis() - 7 * 24 * 3600 * 1000l - 30 * 1000l));
            }
            if (projectLog1.getCreateDate().getTime() < System.currentTimeMillis() - 7 * 24 * 3600 * 1000l) {
                flag = true;
            }
        }
        Assert.assertTrue(flag);
        projectLogService.deleteNotNearlyWeekLog();
        List<ProjectLog> projectLogs2 = projectLogService.findByProjectIdAndWeek(1060l);
        flag = false;
        for (ProjectLog projectLog2 : projectLogs2) {
            if (projectLog2.getCreateDate().getTime() < System.currentTimeMillis() - 7 * 24 * 3600 * 1000l) {
                flag = true;
                break;
            }
        }
        Assert.assertTrue(!flag);
    }

    @Test
    public void testCovert(){
        Project.Type type=Project.Type.FLOOD_DEFENCES;
        String alias="23A3";
        String object="aaaaa";
        String covert = projectLogService.covert(type, alias, object);
        Assert.assertTrue(covert.contains("项目前期--可研--基本情况--治理段以上流域面积(km²)：aaaaa"));
    }

    @Test
    public void testSaveLog(){
        ProjectLog.Type type= ProjectLog.Type.ELEMENT;
        Project project = projectService.find(848l);
        Object object=userService.find(1l);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("23A3", "aaaaa");
        projectLogService.saveLog(project,object,map,type);
        List<ProjectLog> projectLogs = projectLogService.findByProjectId(848l);
        Assert.assertTrue(projectLogs.size()==1);
    }
}
