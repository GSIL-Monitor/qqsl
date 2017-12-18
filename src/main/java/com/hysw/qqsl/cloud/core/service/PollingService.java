package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.Polling;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @anthor Administrator
 * @since 10:21 2017/12/14
 */
@Service("pollingService")
public class PollingService {
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    private Map<Long, Polling> userPolling = new LinkedHashMap<>();
    private Map<Long, Polling> accountPolling = new LinkedHashMap<>();

    /**
     * 从缓存中获取user的轮询状态
     * @param userId
     * @return
     */
    public Polling findByUser(Long userId) {
        return userPolling.get(userId);
    }

    /**
     * 从缓存中获取account的轮询状态
     * @param accountId
     * @return
     */
    public Polling findByAccount(Long accountId) {
        return accountPolling.get(accountId);
    }

    /**
     * 对象转换为json
     * @param polling
     * @return
     */
    public JSONObject toJson(Polling polling) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cooperateStatus",polling.isCooperateStatus());
        jsonObject.put("messageStatus",polling.isMessageStatus());
        jsonObject.put("shareStatus",polling.isShareStatus());
        jsonObject.put("stationStatus",polling.isStationStatus());
        return jsonObject;
    }

    /**
     * 初始化轮询状态
     */
    public void init(){
        userPolling = new LinkedHashMap<>();
        accountPolling = new LinkedHashMap<>();
        List<User> users = userService.findAll();
        List<Account> accounts = accountService.findAll();
        for (User user : users) {
            userPolling.put(user.getId(), new Polling());
        }
        for (Account account : accounts) {
            accountPolling.put(account.getId(), new Polling());
        }
    }

    /**
     * 改变账户消息轮询状态
     * @param object
     * @param flag
     */
    public void changeMessageStatus(Object object,boolean flag) {
        if (object instanceof Account) {
            Polling polling = accountPolling.get(((Account)object).getId());
            polling.setMessageStatus(flag);
            accountPolling.put(((Account)object).getId(), polling);
        }else{
            Polling polling = userPolling.get(((User)object).getId());
            polling.setMessageStatus(flag);
            userPolling.put(((User)object).getId(), polling);
        }
    }

    /**
     * 改变账户测站轮询状态
     * @param object
     * @param flag
     */
    public void changeStationStatus(Object object,boolean flag) {
        if (object instanceof Account) {
            Polling polling = accountPolling.get(((Account)object).getId());
            polling.setStationStatus(flag);
            accountPolling.put(((Account)object).getId(), polling);
        }else{
            Polling polling = userPolling.get(((User)object).getId());
            polling.setStationStatus(flag);
            userPolling.put(((User)object).getId(), polling);
        }
    }

    /**
     * 改变账户分享轮询状态
     * @param object
     * @param flag
     */
    public void changeShareStatus(Object object,boolean flag) {
        if (object instanceof Account) {
            Polling polling = accountPolling.get(((Account)object).getId());
            polling.setShareStatus(flag);
            accountPolling.put(((Account)object).getId(), polling);
        }else{
            Polling polling = userPolling.get(((User)object).getId());
            polling.setShareStatus(flag);
            userPolling.put(((User)object).getId(), polling);
        }
    }

    /**
     * 改变账户协同轮询状态
     * @param object
     * @param flag
     */
    public void changeCooperateStatus(Object object,boolean flag) {
        if (object instanceof Account) {
            Polling polling = accountPolling.get(((Account)object).getId());
            polling.setCooperateStatus(flag);
            accountPolling.put(((Account)object).getId(), polling);
        }else{
            Polling polling = userPolling.get(((User)object).getId());
            polling.setCooperateStatus(flag);
            userPolling.put(((User)object).getId(), polling);
        }
    }

    public void addAccount(Account account) {
        accountPolling.put(account.getId(), new Polling());
    }

    public void addUser(User user) {
        userPolling.put(user.getId(), new Polling());
    }
}
