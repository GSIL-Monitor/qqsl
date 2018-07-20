package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.listener.TestExecutionListener;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Created by chenl on 17-5-24.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(value = {TestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ContextConfiguration(locations = {"classpath*:/applicationContext-test.xml", "classpath*:/applicationContext-cache-test.xml","classpath*:/applicationContext-shiro-test.xml"})
@Transactional(transactionManager = "transactionManager")
@Rollback(value = false)
public class UpdateTest {

    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private ElementDBService elementDBService;
    @Autowired
    private ElementDataGroupService elementDataGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private AdminService adminService;

    /** 删除全部account  accountMessage user_account表 删除build表中的cut属性   project表中的cooperate属性全部改为null
     * 修改mylistener 中的短信上行接口为run启动*/
    @Test
    public void projectCooperateIsNULL() {
        List<Project> projects = (List<Project>) SettingUtils.objectCopy(projectService.findAll());
        for (int i = 0; i < projects.size(); i++) {
            projects.get(i).setCooperate(null);
            projects.get(i).setViews(null);
            projectService.save(projects.get(i));
            projectService.flush();
        }
    }
}
