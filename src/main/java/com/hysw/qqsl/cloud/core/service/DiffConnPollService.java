package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.DiffConnPollDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.DiffConnPoll;
import com.hysw.qqsl.cloud.core.entity.element.Position;
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
    private PositionService positionService;
    @Autowired
    private DiffConnPollService diffConnPollService;
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
     * 添加账户至差分表+缓存
     * @param object
     * @return
     */
    public boolean addDiffConnPoll(Map<String, Object> object) {
        Object userName = object.get("userName");
        Object password = object.get("password");
        Object timeout = object.get("timeout");
        if (userName == null || password == null||timeout==null) {
            return false;
        }
        try {
            Long.valueOf(timeout.toString());
        } catch (Exception e) {
            return false;
        }
        DiffConnPoll diffConnPoll = new DiffConnPoll(userName.toString(),password.toString(),Long.valueOf(timeout.toString()));
        diffConnPollService.save(diffConnPoll);
        Position position = new Position(diffConnPoll.getId(), diffConnPoll.getUserName(), diffConnPoll.getPassword(), System.currentTimeMillis(), diffConnPoll.getTimeout());
        positionService.setPosition(position);
        return true;
    }

    /**
     * 编辑过期时间
     * @param diffConnPoll
     * @param l
     * @param id
     */
    public void editDiffConnPoll(DiffConnPoll diffConnPoll,long l,Object id) {
        diffConnPoll.setTimeout(l);
        diffConnPollService.save(diffConnPoll);
        positionService.editTimeout(Long.valueOf(id.toString()),l);
    }

    /**
     * 删除差分账户及缓存数据
     * @param id
     * @return
     */
    public void deteleDiffConnPoll(Long id) {
        DiffConnPoll diffConnPoll = diffConnPollService.find(id);
        positionService.deleteOneCache(diffConnPoll);
        diffConnPollService.remove(diffConnPoll);
    }

    /**
     * 显示差分账户列表
     * @return
     */
    public JSONArray accountList() {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < positionService.getUnuseds().size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("id", positionService.getUnuseds().get(i).getId());
            jsonObject.put("userName", positionService.getUnuseds().get(i).getUserName());
            jsonObject.put("password", positionService.getUnuseds().get(i).getPassword());
            jsonObject.put("timeout", positionService.getUnuseds().get(i).getTimeout());
            jsonObject.put("using", false);
            jsonArray.add(jsonObject);
        }
        for (int i = 0; i < positionService.getUseds().size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("id", positionService.getUseds().get(i).getId());
            jsonObject.put("userName", positionService.getUseds().get(i).getUserName());
            jsonObject.put("password", positionService.getUseds().get(i).getPassword());
            jsonObject.put("timeout", positionService.getUseds().get(i).getTimeout());
            jsonObject.put("using", true);
            jsonArray.add(jsonObject);
        }
        for (int i = 0; i < positionService.getTimeout().size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("id", positionService.getTimeout().get(i).getId());
            jsonObject.put("userName", positionService.getTimeout().get(i).getUserName());
            jsonObject.put("password", positionService.getTimeout().get(i).getPassword());
            jsonObject.put("timeout", positionService.getTimeout().get(i).getTimeout());
            jsonObject.put("using", false);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
