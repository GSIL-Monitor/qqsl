package com.hysw.qqsl.cloud.entity.project;

import com.hysw.qqsl.cloud.entity.data.Project;
import com.hysw.qqsl.cloud.entity.data.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 项目分享
 * 在企业间分享项目
 *
 * @since 2017年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class Share {

    private Share(){}

    public Share(Project project) {
        this.project = project;
    }

    private Project project;
    // 分享情况
    private List<ShareVisit> visits = new ArrayList<>();

    public final Project getProject() {
        return project;
    }

    public List<User> getShareUsers(){
        List<User> users = new ArrayList<>();
        User user;
        for(int i=0;i<visits.size();i++){
            user = visits.get(i).getUser();
            users.add(user);
        }
        return users;
    }
    /**
     * 注册
     * @param user
     * @return
     */
    public void register(User user) {
        if(visits.size()>0){
            for(int i = 0 ;i<visits.size();i++){
                if(visits.get(i).getUser().getId().equals(user.getId())
                        ||this.getProject().getUser().getId().equals(user.getId())) {
                    return;
                }

            }
        }
        visits.add(new ShareVisit(user));
        return;
    }

    /**
     * 注销
     * @param user
     * @return
     */
    public boolean unRegister(User user) {
        if(visits.size()==0){
            return true;
        };
        for(int i =0 ;i<visits.size();i++){
            if(visits.get(i).getUser().getId().equals(user.getId())){
                visits.remove(i);
                return true;
            }
        }
        return false;
    }

    public JSONArray toJson() {
        JSONArray shareJsons = new JSONArray();;
        if(this.visits.size()==0){
            return shareJsons;
        }
        JSONObject jsonObject;
        for(int i=0;i<visits.size();i++){
            jsonObject = new JSONObject();
            jsonObject.put("id",visits.get(i).getUser().getId());
            jsonObject.put("name",visits.get(i).getUser().getName());
            jsonObject.put("phone",visits.get(i).getUser().getPhone());
            jsonObject.put("createTime",visits.get(i).getCreateTime().getTime());
            shareJsons.add(jsonObject);
        }
        return shareJsons;
    }

}
