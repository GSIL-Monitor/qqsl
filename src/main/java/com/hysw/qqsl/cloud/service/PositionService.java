package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.controller.Message;
import com.hysw.qqsl.cloud.entity.Setting;
import com.hysw.qqsl.cloud.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.entity.element.Position;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 千寻位置连接池
 * Created by chenl on 17-4-7.
 */
@Service("positionService")
public class PositionService {

    List<Position> unuseds = new ArrayList<>();
    List<Position> useds = new ArrayList<>();
    List<Position> timeout = new ArrayList<>();
    Setting setting = SettingUtils.getInstance().getSetting();

    @Autowired
    private DiffConnPollService diffConnPollService;

    /**
     * 格式化缓存
     */
    public void format(){
        unuseds = new ArrayList<>();
        useds = new ArrayList<>();
        timeout = new ArrayList<>();
    }
    /**
     * 初始化
     */
    public void init(){
        Position position;
//        if (setting.getStatus().equals("run")) {
        List<DiffConnPoll> diffConnPolls = diffConnPollService.findAll();
        for (DiffConnPoll diffConnPoll : diffConnPolls) {
            position = new Position(diffConnPoll.getUserName(), diffConnPoll.getPassword(), System.currentTimeMillis(),diffConnPoll.getTimeout());
            unuseds.add(position);
        }
//            return;
//        }
////        for (int i = 0; i < 100; i++) {
////            position = new Position("zhangyong"+i, "zy"+i, System.currentTimeMillis(),System.currentTimeMillis()+2*24*60*60*1000l);
////            unuseds.add(position);
////        }
//        position = new Position("xnqqsl", "qhhysw", System.currentTimeMillis(),System.currentTimeMillis()+2*24*60*60*1000l);
//        unuseds.add(position);
    }

    /**
     * 监测缓存内对象心跳时间与当前时间差超过10分钟过期
     */
    public void checkIsUseds(){
        if (useds.size() == 0) {
            return;
        }
        for (int i = 0; i < useds.size(); i++) {
//            System.out.println("xt:"+useds.get(i).getUserName()+":"+new Date(useds.get(i).getDate()));
//            System.out.println("us:"+useds.get(i).getUserName()+":"+new Date(useds.get(i).getTimeout()));
            if (Math.abs(useds.get(i).getDate() - System.currentTimeMillis()) > 10 * 60 * 1000l) {
                unuseds.add(useds.get(i));
                useds.remove(i);
            }
        }

    }

    /**
     * 检测千寻账号是否到期(还差一天过期就算过期，添加至过期缓存)
     *
     */
    public void accountTimeout(){
//        if (unuseds.size() == 0) {
//            return;
//        }
        for (int i = 0; i < unuseds.size(); i++) {
//            System.out.println("un:"+unuseds.get(i).getUserName()+":"+new Date(unuseds.get(i).getTimeout()));
            if (unuseds.get(i).getTimeout() < (System.currentTimeMillis()+1 * 24 * 60 * 60 * 1000l)) {
//                System.out.println("un:"+unuseds.get(i).getUserName()+":"+new Date(unuseds.get(i).getTimeout()));
                timeout.add(unuseds.get(i));
                unuseds.remove(i);
            }
        }
//        if (timeout.size() == 0) {
//            return;
//        }
        for (int i = 0; i < timeout.size(); i++) {
//            System.out.println("to:"+timeout.get(i).getUserName()+":"+new Date(timeout.get(i).getTimeout()));
            if (timeout.get(i).getTimeout()>(System.currentTimeMillis()+1 * 24 * 60 * 60 * 1000l)) {
//                System.out.println("to:"+timeout.get(i).getUserName()+":"+new Date(timeout.get(i).getTimeout()));
                unuseds.add(timeout.get(i));
                timeout.remove(i);
            }
        }
    }
    /**
     * 随机返回用户账户密码
     * @return
     * @param mac
     */
    public Message randomPosition(String mac){
        Position position=null;
        for (int i = 0; i < useds.size(); i++) {
            if (useds.get(i).getMac().equals(mac)) {
                position = useds.get(i);
                break;
            }
        }
        if (position == null) {
            if (unuseds.size() == 0) {
                return new Message(Message.Type.FAIL);
            }
            Random random = new Random();
            int i = random.nextInt(unuseds.size());
            position = unuseds.get(i);
            position.setMac(mac);
            unuseds.remove(i);
            useds.add(position);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", position.getUserName());
        jsonObject.put("passWord", position.getPassWord());
        String s = null;
        try {
            s = RSACoderUtil.encryptAES(jsonObject.toString(), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Message(Message.Type.OK,s);
    }

    /**
     * 根据心跳改变缓存内对象时间
     * @param username
     */
    public Message changeDate(String username){
        boolean flag = true;
        for (int i = 0; i < useds.size(); i++) {
            if (useds.get(i).getUserName().equals(username)) {
                flag = false;
                useds.get(i).setDate(System.currentTimeMillis());
                break;
            }
        }
        if (flag) {
            return new Message(Message.Type.FAIL);
        }
        return new Message(Message.Type.OK);
    }




    /**
     * 删除缓存中一条数据
     * @param diffConnPoll
     */
    public void deleteOneCache(DiffConnPoll diffConnPoll) {
        for (int i = 0; i < unuseds.size(); i++) {
            if (unuseds.get(i).getUserName().equals(diffConnPoll.getUserName())) {
                unuseds.remove(i);
                return;
            }
        }
        for (int i = 0; i < useds.size(); i++) {
            if (useds.get(i).getUserName().equals(diffConnPoll.getUserName())) {
                useds.remove(i);
                return;
            }
        }
        for (int i = 0; i < timeout.size(); i++) {
            if (timeout.get(i).getUserName().equals(diffConnPoll.getUserName())) {
                timeout.remove(i);
                return;
            }
        }
    }

    /**
     * 编辑缓存中账号的过期时间
     * @param diffConnPoll
     */
    public void editPosition(DiffConnPoll diffConnPoll) {
        for (int i = 0; i < unuseds.size(); i++) {
            if (unuseds.get(i).getUserName().equals(diffConnPoll.getUserName())) {
                unuseds.get(i).setTimeout(diffConnPoll.getTimeout());
                break;
            }
        }
        for (int i = 0; i < useds.size(); i++) {
            if (useds.get(i).getUserName().equals(diffConnPoll.getUserName())) {
                useds.get(i).setTimeout(diffConnPoll.getTimeout());
                break;
            }
        }
        for (int i = 0; i < timeout.size(); i++) {
            if (timeout.get(i).getUserName().equals(diffConnPoll.getUserName())) {
                timeout.get(i).setTimeout(diffConnPoll.getTimeout());
                break;
            }
        }
    }

    public Message changeTimeout(String userName, String timeout) {
        DiffConnPoll diffConnPoll = null;
//        if (setting.getStatus().equals("run")) {
        diffConnPoll = diffConnPollService.findByUserName(userName);
        if (diffConnPoll == null) {
            return new Message(Message.Type.EXIST);
        }
        try {
            diffConnPoll.setTimeout(Long.valueOf(timeout));
        }catch (Exception e){
            return new Message(Message.Type.FAIL);
        }
        diffConnPollService.save(diffConnPoll);
//        }else{
//            boolean flag = false;
//            diffConnPoll = new DiffConnPoll();
//            for (int i = 0; i < this.timeout.size(); i++) {
//                if (flag) {
//                    break;
//                }
//                if (this.timeout.get(i).getUserName().equals(userName)) {
//                    diffConnPoll.setUserName(userName);
//                    diffConnPoll.setTimeout(Long.valueOf(timeout));
//                    flag = true;
//                    break;
//                }
//            }
//            for (int i = 0; i < useds.size(); i++) {
//                if (flag) {
//                    break;
//                }
//                if (useds.get(i).getUserName().equals(userName)) {
//                    diffConnPoll.setUserName(userName);
//                    diffConnPoll.setTimeout(Long.valueOf(timeout));
//                    flag = true;
//                    break;
//                }
//            }
//            for (int i = 0; i < unuseds.size(); i++) {
//                if (flag) {
//                    break;
//                }
//                if (unuseds.get(i).getUserName().equals(userName)) {
//                    diffConnPoll.setUserName(userName);
//                    diffConnPoll.setTimeout(Long.valueOf(timeout));
////                    flag = true;
//                    break;
//                }
//            }
//
//        }
        editPosition(diffConnPoll);
        return new Message(Message.Type.OK);
    }

}
