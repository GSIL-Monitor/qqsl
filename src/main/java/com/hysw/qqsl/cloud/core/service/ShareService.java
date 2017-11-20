package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.project.Share;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by leinuo on 17-5-17 下午2:41
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 * 企业间项目分享service层
 */
@Service("shareService")
public class ShareService {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private UserService userService;

    /**
     * 获取企业自己的项目和分享的项目
     *
     * @param user
     * @return
     */
    public List<Share> getShares(User user) {
        List<Share> shares = new ArrayList<>();
        Share share;
        List<Project> projects = projectService.findAll();
        for (int i = 0; i < projects.size(); i++) {
            share = makeShare(projects.get(i));
            if (projects.get(i).getUser().getId().equals(user.getId()) || isShare(projects.get(i), user)) {
                shares.add(share);
            }
        }
        return shares;
    }

    /**
     * 判断该企业是否有查看权限
     *
     * @param project
     * @param user
     * @return
     */
    public boolean isShare(Project project, User user) {
        if (project == null) {
            return false;
        }
        if (project.getUser().getId().equals(user.getId())) {
            return true;
        }
        Share share = makeShare(project);
        List<User> users = share.getShareUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 企业间的项目分享
     *
     * @param projectIds
     * @param userIds
     */
    public Message shares(List<Integer> projectIds, List<Integer> userIds, User own) {
        long projectId;
        Project project;
        User user = null;
        //判断项目归属
        if(!isOwn(projectIds,own)){
            return new Message(Message.Type.FAIL);
        }
        for (int i = 0; i < projectIds.size(); i++) {
            projectId = Long.valueOf(projectIds.get(i));
            project = projectService.find(projectId);
            for(int k = 0;k<userIds.size();k++){
                List<User> all = userService.findAll();
                for (User user1 : all) {
                    if (userIds.get(k).toString().equals(user1.getId().toString())) {
                        user = (User) SettingUtils.objectCopy(user1);
                        break;
                    }
                }
                if(user==null||user.getId().equals(own.getId())){
                    continue;
                }
                if(!isShare(project,user)){
                    share(project, user);
                }
            }
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 判断项目归属
     * @return
     */
    private boolean isOwn(List<Integer> projectIds,User own) {
        long projectId;
        Project project;
        for (int i = 0; i < projectIds.size(); i++) {
            projectId = Long.valueOf(projectIds.get(i));
            project = projectService.find(projectId);
            if (project == null || !project.getUser().getId().equals(own.getId())) {
                return false;
            }
        }
        return true;
    }
    /**
     * 单个项目的分享,记录分享消息
     *
     * @param project
     * @param user
     */
    private void share(Project project, User user) {
        Share share = makeShare(project);
        share.register(user);
        JSONArray shareJsons = share.toJson();
        project.setShares(shareJsons.isEmpty()?null:shareJsons.toString());
        projectService.save(project);
        //记录分享消息
        userMessageService.shareMessage(project, user, true);
    }

    /**
     * @param project
     * @return
     */
    public Share makeShare(Project project) {
        Share share = new Share(project);
        JSONArray jsonArray;
        JSONObject jsonObject;
        User user;
        if (StringUtils.hasText(project.getShares())) {
            jsonArray = JSONArray.fromObject(project.getShares());
        } else {
            return share;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            user = new User();
            user.setId(jsonObject.getLong("id"));
            user.setName(jsonObject.getString("name"));
            user.setPhone(jsonObject.getString("phone"));
            share.register(user);
        }
        return share;
    }

    /**
     * 取消分享
     *
     * @param userIds
     */
    public void unShares(Project project, List<Integer> userIds,User own) {
        Share share = makeShare(project);
        User user;
        boolean flag;
        for (int i = 0; i < userIds.size(); i++) {
            user = userService.find(Long.valueOf(userIds.get(i)));
            flag = share.unRegister(user);
            if (flag) {
                userMessageService.shareMessage(project, user, false);
            }
        }
        project.setShares(share.toJson().toString());
        projectService.save(project);
    }

}
