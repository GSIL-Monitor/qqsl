package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.listener.TestExecutionListener;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
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
@ContextConfiguration(locations = {"classpath*:/applicationContext-test.xml", "classpath*:/applicationContext-cache-test.xml"})
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

//    删除accountMessage,userMessage,log表

    /**
     * 为所有已注册用户增加测试版套餐
     */
    @Test
    public void testAddPackageTestToAllUser(){
        List<User> users = userService.findAll();
        for (User user : users) {
            Package aPackage = packageService.findByUser(user);
            if (aPackage == null) {
                packageService.activateTestPackage(user);
            }
        }
    }

    /**
     * 为所有用户增加认证信息
     */
    @Test
    public void testAddAllUserCertify(){
        List<User> all = userService.findAll();
        for (User user : all) {
            Certify certify = certifyService.findByUser(user);
            if (certify == null) {
                certify = new Certify(user);
            }
            certifyService.save(certify);
        }
    }

    /**
     * 重置权限
     */
    @Test
    public void testChangeUserRoles(){
        List<User> all = (List<User>) SettingUtils.objectCopy(userService.findAll());
        String roles="user:simple";
        Certify certify;
        for (User user : all) {
            certify = certifyService.findByUser(user);
            if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING) {
                roles = roles + ",user:identify";
            }
            if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING) {
                roles = roles + ",user:company";
            }
            user.setRoles(roles);
            userService.save(user);
        }
    }

    /**
     * 兼容原项目列表，为所有项目增加项目图标类型
     */
    @Test
    public void testAddIconType(){
        List<Project> all = (List<Project>) SettingUtils.objectCopy(projectService.findAll());
        for (int i = 0; i < all.size(); i++) {
            Project project = projectService.find(all.get(i).getId());
            project.setIconType(Project.IconType.STYLE_0);
            projectService.save(project);
        }
    }
}
