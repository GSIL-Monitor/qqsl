package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.DiffConnPollDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.element.Position;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-4-8.
 */
@Service("diffConnPollService")
public class DiffConnPollService extends BaseService<DiffConnPoll,Long> {
    @Autowired
    private DiffConnPollDao diffConnPollDao;
    @Autowired
    private PackageService packageService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private DiffConnPollService diffConnPollService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    public void setBaseDao(DiffConnPollDao diffConnPollDao) {
        super.setBaseDao(diffConnPollDao);
    }

    public DiffConnPoll findByUserName(String userName) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userName", userName));
        List<DiffConnPoll> diffConnPolls = diffConnPollDao.findList(0, null, filters);
        if (diffConnPolls.size() == 1) {
            return diffConnPolls.get(0);
        }
        return null;
    }


    /**
     * 套餐是否含有千寻功能，套餐是否过期
     * @param id
     * @return
     */
    public Message isFindCM(String id) {
        Project project = projectService.find(Long.valueOf(id));
        Package aPackage = packageService.findByUser(project.getUser());
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.FINDCM) {
                if (aPackage.getExpireDate().getTime() > System.currentTimeMillis()) {
                    return new Message(Message.Type.OK);
                } else {
                    return new Message(Message.Type.EXPIRED);
                }
            }
        }
        return new Message(Message.Type.FAIL);
    }


    /**
     * 添加账户至差分表+缓存
     * @param object
     * @return
     */
    public Message addDiffConnPoll(Map<String, Object> object) {
        Object userName = object.get("userName");
        Object password = object.get("password");
        Object timeout = object.get("timeout");
        if (userName == null || password == null||timeout==null) {
            return new Message(Message.Type.FAIL);
        }
        try {
            Long.valueOf(timeout.toString());
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        DiffConnPoll diffConnPoll = new DiffConnPoll(userName.toString(),password.toString(),Long.valueOf(timeout.toString()));
        diffConnPollService.save(diffConnPoll);
        Position position = new Position(userName.toString(), password.toString(), System.currentTimeMillis(),Long.valueOf(timeout.toString()));
        positionService.setPosition(position);
        return new Message(Message.Type.OK);
    }

    /**
     * 删除差分账户及缓存数据
     * @param id
     * @return
     */
    public Message deteleDiffConnPoll(Long id) {
        DiffConnPoll diffConnPoll = diffConnPollService.find(id);
        positionService.deleteOneCache(diffConnPoll);
        diffConnPollService.remove(diffConnPoll);
        return new Message(Message.Type.OK);
    }

    /**
     * 显示差分账户列表
     * @return
     */
    public Message accountList() {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < positionService.getUnuseds().size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("userName", positionService.getUnuseds().get(i).getUserName());
            jsonObject.put("timeout", positionService.getUnuseds().get(i).getTimeout());
            jsonObject.put("using", false);
            jsonArray.add(jsonArray);
        }
        for (int i = 0; i < positionService.getUseds().size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("userName", positionService.getUseds().get(i).getUserName());
            jsonObject.put("timeout", positionService.getUseds().get(i).getTimeout());
            jsonObject.put("using", true);
            jsonArray.add(jsonObject);
        }
        for (int i = 0; i < positionService.getTimeout().size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("userName", positionService.getTimeout().get(i).getUserName());
            jsonObject.put("timeout", positionService.getTimeout().get(i).getTimeout());
            jsonObject.put("using", false);
            jsonArray.add(jsonArray);
        }
        return new Message(Message.Type.OK,jsonArray);
    }
}
