package com.hysw.qqsl.cloud.core.entity.station;

import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 测站协同访问
 * 在企业与子账号间协同
 *
 * @since 2018年5月14日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class Cooperate {
    private Cooperate(){

    }
    public Cooperate(Station station) {
        this.station = station;
    }

    private Station station;
    /** 查看　*/
    private List<Account> visits =  new ArrayList<>();

    /**
     * 查看注册
     * @param account
     * @return
     */
    public void register(Account account) {
        if(visits.size()>0){
            for(int i = 0 ;i<visits.size();i++){
                if(visits.get(i).getId().equals(account.getId())){
                    return;
                }
            }
        }
        visits.add(account);
    }

    /**
     * 查看注销
     * @param account
     * @return
     */
    public boolean unRegister(Account account) {
        if(visits.size()==0){
            return true;
        };
        for(int i =0 ;i<visits.size();i++){
            if(visits.get(i).getId().equals(account.getId())){
                visits.remove(i);
                return true;
            }
        }
        return false;
    }

    public void addToStation(){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Account account : visits) {
            jsonObject = new JSONObject();
            jsonObject.put("id", account.getId());
            jsonObject.put("name", account.getName());
            jsonObject.put("phone", account.getPhone());
            jsonArray.add(jsonObject);
        }
        station.setCooperate(jsonArray.toString());
    }

    public void setVisits(List<Account> visits) {
        this.visits = visits;
    }

    public List<Account> getVisits() {
        return visits;
    }
}
