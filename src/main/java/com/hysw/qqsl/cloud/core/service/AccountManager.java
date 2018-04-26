package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.data.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @anthor Administrator
 * @since 17:29 2018/4/18
 */
@Service("accountManager")
public class AccountManager{
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserMessageService userMessageService;
    private Map<String, List<Account>> map = new LinkedHashMap<>();

    /**
     * 服务器启动时添加所有未确认子账户
     */
    public void init(){
        List<Account> accounts=accountService.findAllAwaiting();
        for (Account account : accounts) {
            add(account);
        }
    }

    public void add(Account account) {
        List<Account> accounts = map.get(account.getPhone());
        if (accounts == null || accounts.size() == 0) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        map.put(account.getPhone(), accounts);
    }

    /**
     * 检查超过24小时未确认的，并删除,同时通知用户
     */
    public void changeExpired(){
        if (map.size() == 0) {
            return;
        }
        for (Map.Entry<String, List<Account>> entry : map.entrySet()) {
            Iterator<Account> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                Account account = iterator.next();
                if (account.getCreateDate().getTime() + 5 * 60 * 1000l < System.currentTimeMillis()) {
                    accountService.remove(account);
                    userMessageService.accountMessageExpired(account);
                    iterator.remove();
                    delete(account);
                }
            }
        }
    }

    public void delete(Account account) {
        List<Account> accounts = map.get(account.getPhone());
        accounts.remove(account);
        map.put(account.getPhone(), accounts);
    }

}
