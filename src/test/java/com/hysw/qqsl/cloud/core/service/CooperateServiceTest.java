package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AccountService;
import com.hysw.qqsl.cloud.core.service.CooperateService;
import com.hysw.qqsl.cloud.core.service.ProjectService;
import com.hysw.qqsl.cloud.core.service.UserService;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Create by leinuo on 17-5-17 下午2:46
 *
 * qq:1321404703 https://github.com/leinuo2016
 * 子账号项目协同工作业务测试
 */
public class CooperateServiceTest extends BaseTest {

    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CooperateService cooperateService;
    @Test
    public void cooperate() throws Exception{

    }

    /**
     * 测试授予查看权限
     * @throws Exception
     */
    @Test
    public void registViews() throws Exception{
        User user = userService.findByPhoneOrUserName("qqsl");
        Account account = userService.getAccountsByUserId(user.getId()).get(0);
        assertNotNull(account);
        List<Project> projects = projectService.findByUser(user);
        assertTrue(projects.size()>0);
        String ids ="";
        for(int i=0;i<projects.size();i++){
            ids = ids+projects.get(i).getId()+",";
            if(StringUtils.hasText(projects.get(i).getViews())){
                projects.get(i).setViews(null);
                projectService.save(projects.get(i));
            }
        }
       // cooperateService.views(ids,account);
        projects = projectService.findByUser(user);
        for(int i=0;i<projects.size();i++){
            assertTrue(projects.get(i).getViews().contains("qqsl"));
        }
    }

    /**
     * 测试将多个项目的一个权限分享给一个子账号
     * @throws Exception
     */
    @Test
    public void registEdits() throws Exception{
        User user = userService.findByPhoneOrUserName("qqsl");
        Account account = userService.getAccountsByUserId(user.getId()).get(0);
        assertNotNull(account);
        List<Project> projects = projectService.findByUser(user);
        assertTrue(projects.size()>0);
        List<Integer> ids = new ArrayList<>();
        Long l;
        for(int i=0;i<projects.size();i++){
            l = new Long(projects.get(i).getId());
            ids.add(Integer.valueOf((l.intValue())));
            if(StringUtils.hasText(projects.get(i).getViews())){
                projects.get(i).setCooperate(null);
                projectService.save(projects.get(i));
            }
        }
        String type = "VISIT_PREPARATION_ELEMENT";
        cooperateService.cooperate(ids,type,account,user);
        projects = projectService.findByUser(user);
        for(int i=0;i<projects.size();i++){
            assertTrue(projects.get(i).getCooperate().contains("preparation"));
        }
    }

    /**
     * 测试将一个项目的多个权限分享给一个子账号
     * @throws Exception
     */
    @Test
    public void registEdit() throws Exception{
        User user = userService.findByPhoneOrUserName("qqsl");
        Account account = userService.getAccountsByUserId(user.getId()).get(0);
        assertNotNull(account);
        List<Project> projects = projectService.findByUser(user);
        assertTrue(projects.size()>0);
        projects.get(0).setCooperate(null);
        projectService.save(projects.get(0));
        String type = "VISIT_INVITE_FILE,VISIT_BUILDING_ELEMENT,VISIT_MAINTENANCE_FILE,VISIT_PREPARATION_ELEMENT";
        cooperateService.cooperateMult(projects.get(0),type,account,user);
        Project project= projectService.find(projects.get(0).getId());
        //{"invite":{"file":{"id":1,"name":"qqsl","phone":"18661925010","createTime":1495103128463}},"preparation":{"element":{"id":1,"name":"qqsl","phone":"18661925010","createTime":1495103128593}},"building":{"element":{"id":1,"name":"qqsl","phone":"18661925010","createTime":1495103128587}},"maintenance":{"file":{"id":1,"name":"qqsl","phone":"18661925010","createTime":1495103128591}}}
        JSONObject jsonObject = JSONObject.fromObject(project.getCooperate());
        assertTrue(!jsonObject.getJSONObject("invite").isEmpty()&&
                jsonObject.getJSONObject("invite").getJSONObject("file").getLong("id")==account.getId());

    }

}