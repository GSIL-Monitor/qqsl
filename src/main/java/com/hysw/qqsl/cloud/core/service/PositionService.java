package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.element.Position;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
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
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private TradeService tradeService;

    private List<Position> unuseds = new ArrayList<>();
    private List<Position> useds = new ArrayList<>();
    private List<Position> timeout = new ArrayList<>();
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
        accountTimeout();
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
     *
     * @param mac
     * @return
     */
    public Message randomPosition(String mac, String id) {
        Project project = null;
        try {
            project = projectService.find(Long.valueOf(id));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        Position position = null;
        for (int i = 0; i < useds.size(); i++) {
            if (useds.get(i).getMac().equals(mac)) {
                position = useds.get(i);
                position.setUserId(project.getUser().getId());
                break;
            }
        }
        if (position == null) {
            if (unuseds.size() == 0) {
//                链接池已满
                return new Message(Message.Type.UNKNOWN);
            }
            Random random = new Random();
            int i = random.nextInt(unuseds.size());
            position = unuseds.get(i);
            position.setMac(mac);
            position.setUserId(project.getUser().getId());
            unuseds.remove(i);
            useds.add(position);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", position.getUserName());
        jsonObject.put("password", position.getPassword());
        String s = null;
        try {
            s = RSACoderUtil.encryptAES(jsonObject.toString(), CommonAttributes.tokenKey, CommonAttributes.tokenIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Message(Message.Type.OK, s);
    }

//    /**
//     * 根据type与phone查询用户
//     * @param phone
//     * @param type
//     * @return
//     */
//    private String getUser(String phone, String type) {
//        if (type.equalsIgnoreCase("user")) {
//            return phone;
//        } else if (type.equalsIgnoreCase("account")) {
//            Account account = accountService.findByPhone(phone);
//            account.get
//        }
//
//    }

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
    private void editPosition(DiffConnPoll diffConnPoll) {
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

    /**
     * 获取已使用千寻账号列表
     */
    public List<Position> getUnuseds(){
        return unuseds;
    }

    /**
     * 获取已使用千寻账号列表
     */
    public List<Position> getUseds(){
        return useds;
    }

    /**
     * 获取已使用千寻账号列表
     */
    public List<Position> getTimeout(){
        return timeout;
    }

    /**
     * 添加新的千寻账号
     */
    public void setPosition(Position position){
        unuseds.add(position);
    }

    /**
     * 根据用户查询已使用千寻账户中用户使用个数
     * @param user
     * @return
     */
    public int findByUserInUseds(User user){
        int i = 0;
        for (Position used : useds) {
            if (used.getUserId().equals(user.getId())) {
                i++;
            }
        }
        return i;
    }

    /**
     * 是否允许连接千寻位置(含限制数)
     * @param projectId
     * @return
     */
    public Message isAllowConnectQXWZ(Long projectId){
        Project project = projectService.find(Long.valueOf(projectId));
        int i = findByUserInUseds(project.getUser());
        Package aPackage = packageService.findByUser(project.getUser());
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.FINDCM && i < packageItem.getLimitNum()) {
                return new Message(Message.Type.OK);
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

}
