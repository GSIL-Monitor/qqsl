package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.ProjectService;
import com.hysw.qqsl.cloud.core.service.ShareService;
import com.hysw.qqsl.cloud.core.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Create by leinuo on 17-5-17 下午2:45
 *
 * qq:1321404703 https://github.com/leinuo2016
 */
public class ShareServiceTest extends BaseTest {

    @Autowired
    private ShareService shareService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Test
    public void share() throws Exception{
        User user = userService.findByUserName("qqsl");
        assertNotNull(user);
        List<Project> projects = projectService.findByUser(user);
        assertTrue(projects.size()>0);
        List<Integer> projectIds = new ArrayList<>();
        for(int i=0;i<projects.size();i++){
            projects.get(i).setShares(null);
            projectService.save(projects.get(i));
            projectIds.add(Integer.valueOf( projects.get(i).getId().toString()));
        }
        User user1 = userService.findByPhoneOrEmial("18661925010");
        List<Integer> userIds = new ArrayList<>();
        userIds.add(Integer.valueOf(user1.getId().toString()));
        assertNotNull(user1);
        shareService.shares(projectIds,userIds,user);
        projects = projectService.findByUser(user);
        for(int k=0;k<projects.size();k++){
           assertTrue(projects.get(k).getShares().contains("liujb"));
            }
    }
}