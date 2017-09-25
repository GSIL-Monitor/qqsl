package com.hysw.qqsl.cloud.core.entity.monitor;

import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by leinuo on 17-6-28 上午10:59
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 水利测点仪表的分享
 */
public class Share {

   private List<ShareVisit> visits = new ArrayList<>();
   private Station station;
   private Share(){}

   public Share(Station station ){
       this.station = station;
   }

   public final Station getStation(){
       return this.station;
   }

   public void register(User user){
       if(visits.size()>0){
           for(int i = 0;i<visits.size();i++){
               if(visits.get(i).getUser().getId().equals(user.getId())){
                   return;
               }
           }
       }
       visits.add(new ShareVisit(user));

   }

    public boolean unRegister(User user) {
        if(visits.size()==0){
            return true;
        }
        for(int i =0 ;i<visits.size();i++){
            if(visits.get(i).getUser().getId().equals(user.getId())){
                visits.remove(i);
                return true;
            }
        }
        return false;
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
