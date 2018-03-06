package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Admin;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.listener.TestExecutionListener;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.apache.commons.codec.digest.DigestUtils;
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
@Rollback(value = true)
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

    /**
     * 添加管理员账号
     */
    @Test
    public void addAdmin(){
        Admin admin1 = adminService.findByUserName("zhugy");
        if (admin1 == null) {
            admin1 = new Admin();
            admin1.setDepartment("朱广云");
            admin1.setEmail("123456@qq.com");
            admin1.setEnabled(true);
            admin1.setLocked(false);
            admin1.setName("朱广云");
            admin1.setPhone("15009719246");
            admin1.setUserName("zhugy");
            admin1.setRoles("admin:simple");
            admin1.setPassword(DigestUtils.md5Hex("qqsl"));
            adminService.save(admin1);
        }
        admin1 = adminService.findByUserName("zhaod");
        if (admin1 == null) {
            admin1 = new Admin();
            admin1.setDepartment("赵东");
            admin1.setEmail("123456@qq.com");
            admin1.setEnabled(true);
            admin1.setLocked(false);
            admin1.setName("赵东");
            admin1.setPhone("15111718639");
            admin1.setUserName("zhaod");
            admin1.setRoles("admin:simple");
            admin1.setPassword(DigestUtils.md5Hex("qqsl"));
            adminService.save(admin1);
        }
        admin1 = adminService.findByUserName("wensq");
        if (admin1 == null) {
            admin1 = new Admin();
            admin1.setDepartment("温生麒");
            admin1.setEmail("123456@qq.com");
            admin1.setEnabled(true);
            admin1.setLocked(false);
            admin1.setName("温生麒");
            admin1.setPhone("13519710141");
            admin1.setUserName("wensq");
            admin1.setRoles("admin:simple");
            admin1.setPassword(DigestUtils.md5Hex("qqsl"));
            adminService.save(admin1);
        }
        admin1 = adminService.findByUserName("songfs");
        if (admin1 == null) {
            admin1 = new Admin();
            admin1.setDepartment("宋生发");
            admin1.setEmail("123456@qq.com");
            admin1.setEnabled(true);
            admin1.setLocked(false);
            admin1.setName("宋生发");
            admin1.setPhone("13639750192");
            admin1.setUserName("songsf");
            admin1.setRoles("admin:simple");
            admin1.setPassword(DigestUtils.md5Hex("qqsl"));
            adminService.save(admin1);
        }
    }
}
